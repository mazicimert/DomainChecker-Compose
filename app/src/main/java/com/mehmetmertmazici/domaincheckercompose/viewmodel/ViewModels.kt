package com.mehmetmertmazici.domaincheckercompose.viewmodel

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mehmetmertmazici.domaincheckercompose.model.Domain
import com.mehmetmertmazici.domaincheckercompose.network.ApiClient
import com.mehmetmertmazici.domaincheckercompose.utils.DomainCorrector
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


sealed interface SearchEffect {
    data class ShowError(val message: String) : SearchEffect
    data class ShowToast(val message: String) : SearchEffect
    data class OpenUrl(val url: String) : SearchEffect
    data class CopyToClipboard(val text: String) : SearchEffect
}

data class DomainSearchUiState(
    val searchQuery: String = "",
    val domains: List<Domain> = emptyList(),
    val isLoading: Boolean = false,
    val showEmptyState: Boolean = false,
    val suggestions: List<DomainCorrector.Suggestion>? = null,
    val originalSearchQuery: String? = null,
    val showWhoisDialog: Pair<String, String>? = null,
    val error: String? = null
)

class DomainSearchViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DomainSearchUiState())
    val uiState: StateFlow<DomainSearchUiState> = _uiState.asStateFlow()

    private val _effect = Channel<SearchEffect>()
    val effect = _effect.receiveAsFlow()

    private val domainCorrector = DomainCorrector()

    companion object {
        private const val TAG = "DomainSearchViewModel"
        private const val MIN_SEARCH_LENGTH = 2

        private val COMMON_TLDS = setOf(
            "com", "net", "org", "com.tr", "net.tr", "org.tr", "info", "biz",
            "xyz", "io", "co", "ai", "app", "dev", "me", "pro", "store", "online",
            "site", "tech", "web.tr", "gen.tr"
        )
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query, suggestions = null) }
        if (query.isEmpty()) {
            clearResults()
        }
    }

    fun performSearch() {
        val query = _uiState.value.searchQuery.trim().lowercase()

        if (query.length < MIN_SEARCH_LENGTH) {
            sendEffect(SearchEffect.ShowError("En az $MIN_SEARCH_LENGTH karakter girmelisiniz"))
            return
        }

        if (isExactDomainMatch(query)) {
            val (domainName, preferredExtension) = parseDomainQuery(query)
            searchDomainsWithExtensionPriority(domainName, preferredExtension)
            return
        }

        val suggestions = domainCorrector.getSuggestions(query)
        if (suggestions.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    suggestions = suggestions,
                    originalSearchQuery = query,
                    isLoading = false,
                    showEmptyState = false
                )
            }
            return
        }

        val (domainName, preferredExtension) = parseDomainQuery(query)
        searchDomainsWithExtensionPriority(domainName, preferredExtension)
    }

    private fun isExactDomainMatch(query: String): Boolean {
        // Nokta içermiyorsa direkt domain değildir (örn: "google")
        if (!query.contains('.')) return false

        // Uzantıyı al (örn: google.com -> com, google.com.tr -> com.tr)
        val parts = query.split('.')

        // Son parça (com) veya son iki parça (com.tr) kontrolü
        val lastPart = parts.lastOrNull() ?: return false
        val lastTwoParts = if (parts.size >= 2) "${parts[parts.size-2]}.${parts.last()}" else ""

        return COMMON_TLDS.contains(lastPart) || COMMON_TLDS.contains(lastTwoParts)
    }

    fun searchAnyway() {
        val originalQuery = _uiState.value.originalSearchQuery ?: return
        _uiState.update { it.copy(suggestions = null) }
        val (domainName, preferredExtension) = parseDomainQuery(originalQuery)
        searchDomainsWithExtensionPriority(domainName, preferredExtension)
    }

    fun applySuggestion(suggestion: String) {
        _uiState.update { it.copy(searchQuery = suggestion, suggestions = null) }
        val (domainName, preferredExtension) = parseDomainQuery(suggestion)
        searchDomainsWithExtensionPriority(domainName, preferredExtension)
    }

    fun closeSuggestions() {
        _uiState.update { it.copy(suggestions = null) }
    }

    fun clearSearch() {
        _uiState.update { DomainSearchUiState() }
    }

    fun showWhois(domain: String) {
        viewModelScope.launch {
            try {
                val whoisData = ApiClient.apiService.getWhoisInfo(domain)
                _uiState.update { it.copy(showWhoisDialog = Pair(domain, whoisData)) }
            } catch (e: Exception) {
                Log.e(TAG, "Whois error", e)
                sendEffect(SearchEffect.ShowError("Whois sorgulama hatası: ${e.message}"))
            }
        }
    }

    fun dismissWhois() {
        _uiState.update { it.copy(showWhoisDialog = null) }
    }

    fun copyWhoisData(data: String) {
        sendEffect(SearchEffect.CopyToClipboard(data))
        sendEffect(SearchEffect.ShowToast("Whois bilgisi kopyalandı"))
    }

    fun openRegistration(domain: String) {
        val url = "https://www.google.com"
        sendEffect(SearchEffect.OpenUrl(url))
    }

    private fun searchDomainsWithExtensionPriority(domainName: String, preferredExtension: String?) {
        _uiState.update { it.copy(isLoading = true, showEmptyState = false, suggestions = null) }

        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.searchDomains(domainName)

                if (response.code == 1 && response.status == "success") {
                    val domainList = response.message?.domains ?: emptyList()
                    val sortedDomains = if (preferredExtension != null) {
                        prioritizeByExtension(domainList, preferredExtension)
                    } else {
                        domainList
                    }

                    _uiState.update {
                        it.copy(domains = sortedDomains, isLoading = false, showEmptyState = sortedDomains.isEmpty())
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                    sendEffect(SearchEffect.ShowError("Arama sırasında bir hata oluştu"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Search error", e)
                _uiState.update { it.copy(isLoading = false, error = e.message) }
                sendEffect(SearchEffect.ShowError("Bağlantı hatası: ${e.message}"))
            }
        }
    }

    private fun prioritizeByExtension(domains: List<Domain>, preferredExtension: String): List<Domain> {
        return domains.sortedWith { domain1, domain2 ->
            val ext1 = domain1.domain.substringAfter('.', "")
            val ext2 = domain2.domain.substringAfter('.', "")
            when {
                ext1 == preferredExtension && ext2 != preferredExtension -> -1
                ext1 != preferredExtension && ext2 == preferredExtension -> 1
                else -> 0
            }
        }
    }

    private fun parseDomainQuery(query: String): Pair<String, String?> {
        if (!query.contains('.')) return Pair(query, null)
        val parts = query.split('.').filter { it.isNotBlank() }
        return when {
            parts.isEmpty() -> Pair("", null)
            parts.size == 1 -> Pair(parts[0], null)
            else -> {
                val domainName = parts[0]
                val extension = parts.drop(1).joinToString(".")
                Pair(domainName, extension)
            }
        }
    }

    private fun clearResults() {
        _uiState.update {
            it.copy(domains = emptyList(), showEmptyState = false, isLoading = false, suggestions = null)
        }
    }

    private fun sendEffect(effect: SearchEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}


// ------- DomainPricesViewModel --------

// 1. Yan Etkiler (Side Effects) için arayüz
sealed interface PricesEffect {
    data class ShowError(val message: String) : PricesEffect
}

// 2. UI Durumu (State)
data class DomainPricesUiState(
    val domains: List<Domain> = emptyList(),
    val originalDomains: List<Domain> = emptyList(),
    val searchQuery: String = "",
    val activeCategory: String = "all",
    val isLoading: Boolean = false,
    val showEmptyState: Boolean = false,
    val error: String? = null
)

// 3. ViewModel - Artık Context parametresi ALMIYOR
class DomainPricesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DomainPricesUiState())
    val uiState: StateFlow<DomainPricesUiState> = _uiState.asStateFlow()

    private val _effect = Channel<PricesEffect>()
    val effect = _effect.receiveAsFlow()

    companion object {
        private const val TAG = "DomainPricesViewModel"

        private val SAMPLE_DOMAINS = listOf(
            "google", "facebook", "amazon", "microsoft", "apple", "tesla", "spacex"
        )

        private val POPULAR_EXTS = setOf("com", "net", "org", "biz", "info")
        private val TURKEY_EXTS = setOf("com.tr", "net.tr", "org.tr", "gen.tr", "web.tr", "info.tr", "biz.tr", "tv.tr")
        private val INTERNATIONAL_EXTS = setOf("pro", "name", "xyz", "club", "site", "online", "tech", "store")
        private val COUNTRY_EXTS = setOf("us", "uk", "de", "fr", "ru", "cn", "jp", "co", "io", "me", "tv")
    }

    init {
        loadDomainPrices()
    }

    fun loadDomainPrices() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        val randomDomain = SAMPLE_DOMAINS.random()

        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.searchDomains(randomDomain)

                if (response.code == 1 && response.status == "success") {
                    val domainList = response.message?.domains ?: emptyList()

                    _uiState.update {
                        it.copy(
                            domains = domainList,
                            originalDomains = domainList,
                            isLoading = false,
                            showEmptyState = domainList.isEmpty()
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            showEmptyState = true
                        )
                    }
                    sendEffect(PricesEffect.ShowError("Fiyatlar alınamadı."))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading domain prices", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        showEmptyState = true,
                        error = e.message
                    )
                }
                sendEffect(PricesEffect.ShowError("Bağlantı hatası: ${e.message}"))
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun filterByCategory(category: String) {
        _uiState.update { it.copy(activeCategory = category) }
        applyFilters()
    }

    private fun applyFilters() {
        val currentState = _uiState.value
        val originalDomains = currentState.originalDomains
        val query = currentState.searchQuery.trim().lowercase()
        val category = currentState.activeCategory

        // 1. Filter by Category
        val categoryFiltered = if (category == "all") {
            originalDomains
        } else {
            originalDomains.filter { domainItem ->
                val extension = getExtensionFromDomain(domainItem.domain)
                when (category) {
                    "popular" -> POPULAR_EXTS.contains(extension)
                    "turkey" -> TURKEY_EXTS.contains(extension) || extension.endsWith(".tr")
                    "international" -> INTERNATIONAL_EXTS.contains(extension)
                    "country" -> COUNTRY_EXTS.contains(extension)
                    else -> true
                }
            }
        }

        // 2. Filter by Search Query
        val finalFiltered = if (query.isEmpty()) {
            categoryFiltered
        } else {
            categoryFiltered.filter { domain ->
                domain.domain.lowercase().contains(query)
            }
        }

        _uiState.update { 
            it.copy(
                domains = finalFiltered,
                showEmptyState = finalFiltered.isEmpty() && !currentState.isLoading
            ) 
        }
    }

    private fun getExtensionFromDomain(domainName: String): String {
        val parts = domainName.split(".")
        if (parts.size >= 3) {
            val lastTwo = "${parts[parts.size - 2]}.${parts[parts.size - 1]}"
            if (TURKEY_EXTS.contains(lastTwo) || lastTwo == "co.uk") {
                return lastTwo
            }
        }
        return parts.lastOrNull() ?: ""
    }

    fun getTLD(domain: Domain): String {
        return getExtensionFromDomain(domain.domain)
    }

    // Effect gönderme yardımcısı
    private fun sendEffect(effect: PricesEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}