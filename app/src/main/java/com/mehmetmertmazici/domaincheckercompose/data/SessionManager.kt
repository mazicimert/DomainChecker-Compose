package com.mehmetmertmazici.domaincheckercompose.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mehmetmertmazici.domaincheckercompose.model.LoginResponse
import com.mehmetmertmazici.domaincheckercompose.model.RegisterResponse
import com.mehmetmertmazici.domaincheckercompose.model.UserInfo
import com.mehmetmertmazici.domaincheckercompose.network.SessionHolder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// DataStore extension
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session_prefs")

class SessionManager(private val context: Context) {

    companion object {
        // Preference Keys
        private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val KEY_TOKEN = stringPreferencesKey("token")
        private val KEY_USER_ID = intPreferencesKey("user_id")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_SURNAME = stringPreferencesKey("user_surname")
        private val KEY_USER_PHONE = stringPreferencesKey("user_phone")
        private val KEY_USER_ADDRESS = stringPreferencesKey("user_address")
        private val KEY_USER_CITY = stringPreferencesKey("user_city")
        private val KEY_USER_COUNTRY = stringPreferencesKey("user_country")
    }

    // ============================================
    // LOGIN / REGISTER İŞLEMLERİ
    // ============================================

    suspend fun saveLoginSession(response: LoginResponse) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = true
            prefs[KEY_TOKEN] = response.token ?: ""
            response.user?.let { user ->
                prefs[KEY_USER_ID] = user.id ?: 0
                prefs[KEY_USER_EMAIL] = user.email ?: ""
                prefs[KEY_USER_NAME] = user.name ?: ""
                prefs[KEY_USER_SURNAME] = user.surname ?: ""
                prefs[KEY_USER_PHONE] = user.phone ?: ""
                prefs[KEY_USER_ADDRESS] = user.address ?: ""
                prefs[KEY_USER_CITY] = user.city ?: ""
                prefs[KEY_USER_COUNTRY] = user.country ?: ""
            }
        }
        // In-memory session'ı da güncelle
        SessionHolder.setFromLogin(response)
    }

    suspend fun saveRegisterSession(response: RegisterResponse) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = true
            prefs[KEY_TOKEN] = response.token ?: ""
            response.user?.let { user ->
                prefs[KEY_USER_ID] = user.id ?: 0
                prefs[KEY_USER_EMAIL] = user.email ?: ""
                prefs[KEY_USER_NAME] = user.name ?: ""
                prefs[KEY_USER_SURNAME] = user.surname ?: ""
                prefs[KEY_USER_PHONE] = user.phone ?: ""
                prefs[KEY_USER_ADDRESS] = user.address ?: ""
                prefs[KEY_USER_CITY] = user.city ?: ""
                prefs[KEY_USER_COUNTRY] = user.country ?: ""
            }
        }
        // In-memory session'ı da güncelle
        SessionHolder.setFromRegister(response)
    }

    // ============================================
    // LOGOUT İŞLEMİ
    // ============================================

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
        // In-memory session'ı da temizle
        SessionHolder.clear()
    }

    // ============================================
    // SESSION KONTROL
    // ============================================

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_IS_LOGGED_IN] ?: false
    }

    suspend fun isLoggedInSync(): Boolean {
        return context.dataStore.data.first()[KEY_IS_LOGGED_IN] ?: false
    }

    val token: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_TOKEN]
    }

    suspend fun getToken(): String? {
        return context.dataStore.data.first()[KEY_TOKEN]
    }

    // ============================================
    // USER BİLGİLERİ
    // ============================================

    val userId: Flow<Int?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_ID]
    }

    val userEmail: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_EMAIL]
    }

    val userName: Flow<String?> = context.dataStore.data.map { prefs ->
        val name = prefs[KEY_USER_NAME] ?: ""
        val surname = prefs[KEY_USER_SURNAME] ?: ""
        "$name $surname".trim().ifEmpty { null }
    }

    // Tüm kullanıcı bilgilerini tek seferde al
    val userInfo: Flow<UserInfo?> = context.dataStore.data.map { prefs ->
        val isLoggedIn = prefs[KEY_IS_LOGGED_IN] ?: false
        if (!isLoggedIn) {
            null
        } else {
            UserInfo(
                id = prefs[KEY_USER_ID],
                name = prefs[KEY_USER_NAME],
                surname = prefs[KEY_USER_SURNAME],
                email = prefs[KEY_USER_EMAIL],
                companyName = null,
                address = prefs[KEY_USER_ADDRESS],
                address2 = null,
                city = prefs[KEY_USER_CITY],
                district = null,
                zipCode = null,
                country = prefs[KEY_USER_COUNTRY],
                phone = prefs[KEY_USER_PHONE],
                gsm = null,
                taxNumber = null
            )
        }
    }

    // ============================================
    // SESSION RESTORE (Uygulama açılışında)
    // ============================================

    suspend fun restoreSession() {
        val prefs = context.dataStore.data.first()
        val isLoggedIn = prefs[KEY_IS_LOGGED_IN] ?: false

        if (isLoggedIn) {
            SessionHolder.token = prefs[KEY_TOKEN]
            SessionHolder.userId = prefs[KEY_USER_ID]
            SessionHolder.userEmail = prefs[KEY_USER_EMAIL]
            SessionHolder.userName = "${prefs[KEY_USER_NAME] ?: ""} ${prefs[KEY_USER_SURNAME] ?: ""}".trim()
        }
    }

    // ============================================
    // USER BİLGİLERİNİ GÜNCELLE
    // ============================================

    suspend fun updateUserInfo(user: UserInfo) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = user.id ?: 0
            prefs[KEY_USER_EMAIL] = user.email ?: ""
            prefs[KEY_USER_NAME] = user.name ?: ""
            prefs[KEY_USER_SURNAME] = user.surname ?: ""
            prefs[KEY_USER_PHONE] = user.phone ?: ""
            prefs[KEY_USER_ADDRESS] = user.address ?: ""
            prefs[KEY_USER_CITY] = user.city ?: ""
            prefs[KEY_USER_COUNTRY] = user.country ?: ""
        }
    }


    /**
     * E-posta doğrulaması sonrası session oluştur
     */
    suspend fun createSessionAfterVerification(
        userId: Int,
        email: String,
        name: String,
        surname: String,
        token: String?
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = true
            prefs[KEY_TOKEN] = token ?: ""
            prefs[KEY_USER_ID] = userId
            prefs[KEY_USER_EMAIL] = email
            prefs[KEY_USER_NAME] = name
            prefs[KEY_USER_SURNAME] = surname
        }

        // In-memory session'ı da güncelle
        SessionHolder.token = token
        SessionHolder.userId = userId
        SessionHolder.userEmail = email
        SessionHolder.userName = "$name $surname".trim()
    }
}