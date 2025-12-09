package com.mehmetmertmazici.domaincheckercompose

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.mehmetmertmazici.domaincheckercompose.navigation.DomainCheckerApp
import com.mehmetmertmazici.domaincheckercompose.ui.theme.DomainCheckerTheme
import com.mehmetmertmazici.domaincheckercompose.viewmodel.DomainPricesViewModel
import com.mehmetmertmazici.domaincheckercompose.viewmodel.DomainSearchViewModel
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DomainCheckerTheme {
                val searchViewModel = remember { DomainSearchViewModel() }
                val pricesViewModel = remember { DomainPricesViewModel() }

                // Dialog State'leri
                var showHelpDialog by remember { mutableStateOf(false) }
                var showLicenseDialog by remember { mutableStateOf(false) }

                DomainCheckerApp(
                    searchViewModel = searchViewModel,
                    pricesViewModel = pricesViewModel,
                    onHelpClick = { showHelpDialog = true }, // Dialogu aç
                    onIsimkayitClick = { openUrl("https://www.isimkayit.com") },
                    onRateClick = ::rateApp,
                    onShareClick = ::shareApp,
                    onEmailClick = ::sendEmail,
                    onContractsClick = { openUrl("https://www.isimkayit.com/sozlesmeler") },
                    onLicenseClick = { showLicenseDialog = true }, // Dialogu aç
                    getVersionInfo = ::getVersionInfo
                )

                // Declarative Help Dialog
                if (showHelpDialog) {
                    HelpDialog(
                        onDismiss = { showHelpDialog = false },
                        onGoToSite = {
                            openUrl("https://www.isimkayit.com")
                            showHelpDialog = false
                        }
                    )
                }

                // Declarative License Dialog
                if (showLicenseDialog) {
                    LicenseDialog(
                        onDismiss = { showLicenseDialog = false }
                    )
                }
            }
        }
    }

    private fun getVersionInfo(): Triple<String, Long, String> {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val versionName = packageInfo.versionName
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
            val installDate = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("tr-TR"))
                .format(Date(packageInfo.firstInstallTime))
            Triple(versionName ?: "Unknown", versionCode, installDate)
        } catch (e: Exception) {
            Triple("Unknown", 0, "Unknown")
        }
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }

    private fun rateApp() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri()))
        } catch (e: Exception) {
            openUrl("https://play.google.com/store/apps/details?id=$packageName")
        }
    }

    private fun shareApp() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Domain Checker ile domain sorgula! https://play.google.com/store/apps/details?id=$packageName")
        }
        startActivity(Intent.createChooser(intent, "Paylaş"))
    }

    private fun sendEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:mazicimert@gmail.com".toUri()
            putExtra(Intent.EXTRA_SUBJECT, "Domain Checker Hakkında")
        }
        try { startActivity(intent) } catch (e: Exception) { Toast.makeText(this, "E-posta uygulaması bulunamadı", Toast.LENGTH_SHORT).show() }
    }

    // Compose Native Dialog Components
    @Composable
    fun HelpDialog(onDismiss: () -> Unit, onGoToSite: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = "📖 Uygulama Yardımı", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Domain Checker Kullanım Kılavuzu\n\n" +
                            "📋 TEMEL KULLANIM\n• Arama kutusuna domain adını yazın\n" +
                            "• Minimum 2 karakter gereklidir\n\n" +
                            "🔍 ARAMA ÖRNEKLERİ\n• 'google' (tüm uzantılar)\n• 'google.com' (öncelikli)\n\n" +
                            "🛠 AKILLI DÜZELTME\n• Yazım hatalarını otomatik tespit eder.\n\n" +
                            "💰 FİYATLAR\n• Euro cinsinden güncel fiyatlar.")
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) { Text("Anladım") }
            },
            dismissButton = {
                TextButton(onClick = onGoToSite) { Text("İsimKayıt'a Git") }
            }
        )
    }

    @Composable
    fun LicenseDialog(onDismiss: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Açık Kaynak Lisansları") },
            text = {
                Column {
                    Text("Bu uygulama aşağıdaki kütüphaneleri kullanır:\n\n" +
                            "• Retrofit\n• OkHttp\n• Gson\n• Jetpack Compose\n• Kotlin Coroutines")
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) { Text("Tamam") }
            }
        )
    }
}