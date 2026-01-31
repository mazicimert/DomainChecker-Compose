package com.mehmetmertmazici.domaincheckercompose.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mehmetmertmazici.domaincheckercompose.ui.components.AnimatedListItem
import com.mehmetmertmazici.domaincheckercompose.ui.components.GradientBackground
import com.mehmetmertmazici.domaincheckercompose.ui.components.IconSurface
import com.mehmetmertmazici.domaincheckercompose.ui.components.scaleClick
import com.mehmetmertmazici.domaincheckercompose.ui.theme.DarkColors
import com.mehmetmertmazici.domaincheckercompose.ui.theme.LightColors
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun AppInfoScreen(
    versionName: String,
    versionCode: Long,
    installDate: String,
    onBackClick: () -> Unit,
    onEmailClick: () -> Unit,
    onIsimkayitClick: () -> Unit,
    onContractsClick: () -> Unit,
    onLicenseClick: () -> Unit,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    val colors = if (isDarkTheme) DarkColors else LightColors

    GradientBackground(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top App Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)

                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Text(
                            text = "Uygulama Hakkında",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }

            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // App Info Card
                item {
                    AppInfoCard(
                        versionName = versionName,
                        versionCode = versionCode,
                        installDate = installDate,
                        isDarkTheme = isDarkTheme
                    )
                }

                // Developer Info Card
                item {
                    DeveloperInfoCard(
                        onEmailClick = onEmailClick,
                        isDarkTheme = isDarkTheme
                    )
                }

                // Links Card
                item {
                    LinksCard(
                        onIsimkayitClick = onIsimkayitClick,
                        onContractsClick = onContractsClick,
                        onLicenseClick = onLicenseClick,
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        }
    }
}

@Composable
private fun AppInfoCard(
    versionName: String,
    versionCode: Long,
    installDate: String,
    isDarkTheme: Boolean
) {
    val colors = if (isDarkTheme) DarkColors else LightColors

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.CardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // App Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = colors.PrimaryLight
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Domain,
                            contentDescription = null,
                            tint = colors.Primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Domain Checker",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.TextPrimary
                    )

                    Text(
                        text = "Versiyon: $versionName",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.TextSecondary
                    )

                    Text(
                        text = "Build: $versionCode",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.TextTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Text(
                text = "Domain Checker, İsimKayıt altyapısını kullanarak domain adı arama ve müsaitlik kontrolü yapmanıza olanak sağlar.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Install Date
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = colors.Primary,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Kurulum: $installDate",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun DeveloperInfoCard(
    onEmailClick: () -> Unit,
    isDarkTheme: Boolean
) {
    val colors = if (isDarkTheme) DarkColors else LightColors

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.CardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Geliştirici Bilgileri",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Developer Name
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = colors.Primary,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Mehmet Mert Mazıcı",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Developer Email
            Card(
                onClick = onEmailClick,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = null,
                        tint = colors.Primary,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "mazicimert@gmail.com",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.Primary,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = Icons.Filled.OpenInNew,
                        contentDescription = null,
                        tint = colors.TextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LinksCard(
    onIsimkayitClick: () -> Unit,
    onContractsClick: () -> Unit,
    onLicenseClick: () -> Unit,
    isDarkTheme: Boolean
) {
    val colors = if (isDarkTheme) DarkColors else LightColors

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.CardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Bağlantılar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Links
            val links = listOf(
                LinkItem("İsimKayıt.com", Icons.Filled.Public, onIsimkayitClick),
                LinkItem("Sözleşmeler", Icons.Filled.Description, onContractsClick),
                LinkItem("Açık Kaynak Lisansları", Icons.Filled.Policy, onLicenseClick)
            )

            links.forEachIndexed { index, link ->
                Card(
                    onClick = link.onClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = link.icon,
                            contentDescription = null,
                            tint = colors.Primary,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = link.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.Primary,
                            modifier = Modifier.weight(1f)
                        )

                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = colors.TextTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (index < links.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

private data class LinkItem(
    val text: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

// ============================================
// WHOIS SCREEN & PARSING LOGIC
// ============================================

data class ParsedWhoisData(
    val domainName: String? = null,
    val registrar: String? = null,
    val registrarUrl: String? = null,
    val creationDate: String? = null,
    val updatedDate: String? = null,
    val expiryDate: String? = null,
    val nameServers: List<String> = emptyList(),
    val status: List<String> = emptyList(),
    val raw: String
)

@Composable
fun WhoisDialog(
    domain: String,
    whoisData: String,
    onDismiss: () -> Unit,
    onCopy: () -> Unit,
    isDarkTheme: Boolean = false
) {
    val colors = if (isDarkTheme) DarkColors else LightColors
    val parsedData = remember(whoisData) { parseWhoisData(whoisData, domain) }
    var showRawData by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    fun copyText(label: String, text: String) {
        clipboardManager.setText(AnnotatedString(text))
        Toast.makeText(context, "$label kopyalandı", Toast.LENGTH_SHORT).show()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.9f) // Use most of the height for data
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colors.Surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(colors.Primary, colors.PrimaryDark)
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconSurface(
                                backgroundColor = Color.White.copy(alpha = 0.2f),
                                iconColor = Color.White
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Whois Bilgileri",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = domain.uppercase(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            IconButton(
                                onClick = onDismiss,
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Default.Close, "Kapat")
                            }
                        }
                    }

                    // Content
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Registration Info
                        if (parsedData.registrar != null || parsedData.domainName != null) {
                            item {
                                AnimatedListItem {
                                    InfoCard(
                                        title = "Kayıt Bilgileri",
                                        icon = Icons.Default.Domain,
                                        isDarkTheme = isDarkTheme
                                    ) {
                                        if (parsedData.domainName != null) {
                                            InfoRow("Domain Adı", parsedData.domainName) {
                                                copyText("Domain", parsedData.domainName)
                                            }
                                        }
                                        if (parsedData.registrar != null) {
                                            InfoRow("Registrar", parsedData.registrar) {
                                                copyText("Registrar", parsedData.registrar)
                                            }
                                        }
                                        if (parsedData.registrarUrl != null) {
                                            InfoRow("Web Sitesi", parsedData.registrarUrl) {
                                                copyText("URL", parsedData.registrarUrl)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Dates
                        if (parsedData.expiryDate != null || parsedData.creationDate != null) {
                            item {
                                AnimatedListItem(delayMillis = 100) {
                                    InfoCard(
                                        title = "Önemli Tarihler",
                                        icon = Icons.Default.DateRange,
                                        isDarkTheme = isDarkTheme
                                    ) {
                                        if (parsedData.expiryDate != null) {
                                            // Calculate expiry status for badge color could be done here
                                            InfoRow("Bitiş Tarihi", parsedData.expiryDate, isImportant = true) {
                                                copyText("Bitiş Tarihi", parsedData.expiryDate)
                                            }
                                        }
                                        if (parsedData.creationDate != null) {
                                            InfoRow("Oluşturulma", parsedData.creationDate) {
                                                copyText("Oluşturulma", parsedData.creationDate)
                                            }
                                        }
                                        if (parsedData.updatedDate != null) {
                                            InfoRow("Son Güncelleme", parsedData.updatedDate) {
                                                copyText("Güncelleme", parsedData.updatedDate)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Name Servers
                        if (parsedData.nameServers.isNotEmpty()) {
                            item {
                                AnimatedListItem(delayMillis = 200) {
                                    InfoCard(
                                        title = "Name Serverlar",
                                        icon = Icons.Default.Dns,
                                        isDarkTheme = isDarkTheme
                                    ) {
                                        parsedData.nameServers.forEach { ns ->
                                            InfoRow("NS", ns) {
                                                copyText("NS", ns)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Raw Data Expander
                        item {
                            AnimatedListItem(delayMillis = 300) {
                                Column {
                                    Surface(
                                        onClick = { showRawData = !showRawData },
                                        shape = RoundedCornerShape(12.dp),
                                        color = colors.SurfaceVariant.copy(alpha = 0.5f)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Ham Veriyi Göster",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.Primary
                                            )
                                            Icon(
                                                imageVector = if (showRawData) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = null,
                                                tint = colors.Primary
                                            )
                                        }
                                    }

                                    AnimatedVisibility(
                                        visible = showRawData,
                                        enter = expandVertically() + fadeIn(),
                                        exit = shrinkVertically() + fadeOut()
                                    ) {
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            color = colors.SurfaceVariant,
                                            border = BorderStroke(1.dp, colors.Outline.copy(alpha = 0.2f))
                                        ) {
                                            Text(
                                                text = parsedData.raw,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Normal
                                                ),
                                                color = colors.TextPrimary,
                                                modifier = Modifier.padding(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Bar
                    Surface(
                        color = colors.Surface,
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = onCopy,
                                modifier = Modifier.scaleClick(onClick = onCopy),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.Primary
                                )
                            ) {
                                Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Tümünü Kopyala")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    icon: ImageVector,
    isDarkTheme: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = if (isDarkTheme) DarkColors else LightColors

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.Surface.copy(alpha = 0.8f) // Glass-like
        ),
        border = BorderStroke(1.dp, colors.Outline.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconSurface(
                    backgroundColor = colors.Primary.copy(alpha = 0.1f),
                    iconColor = colors.Primary
                ) {
                    Icon(icon, null, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    isImportant: Boolean = false,
    onClick: () -> Unit
) {
    val isDarkTheme = false // Simplified specifically for this helper or pass it down
    // Just using standard themed text mostly
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if(isImportant) FontWeight.Bold else FontWeight.Normal,
            color = if(isImportant) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            modifier = Modifier.weight(1.5f)
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
}


// --- PARSING LOGIC ---

fun parseWhoisData(raw: String, domain: String): ParsedWhoisData {
    // 1. Clean the input if it's a JSON response
    var actualRaw = raw
    try {
        if (raw.trim().startsWith("{")) {
            val jsonObject = org.json.JSONObject(raw)
            if (jsonObject.has("message")) {
                actualRaw = jsonObject.getString("message")
            }
        }
    } catch (e: Exception) {
        // Fallback to original raw if JSON parsing fails
        e.printStackTrace()
    }

    // 2. Process the actual Whois text (actualRaw)
    val lines = actualRaw.lines()
    var registrar: String? = null
    var startUrl: String? = null
    var creationDate: String? = null
    var updatedDate: String? = null
    var expiryDate: String? = null
    val ns = mutableListOf<String>()

    // Regex patterns (Key: Value)
    // Supports: "Registrar: Name", "Registry Expiry Date: 2024-..."
    val keyValPattern = Regex("""^\s*(.*?):\s+(.*)$""")

    for (line in lines) {
        val match = keyValPattern.find(line)
        if (match != null) {
            val key = match.groupValues[1].trim().lowercase()
            val value = match.groupValues[2].trim()

            when {
                key == "registrar" -> registrar = value
                key == "registrar url" -> startUrl = value
                key.contains("creation date") -> creationDate = formatDate(value)
                key.contains("updated date") -> updatedDate = formatDate(value)
                key.contains("expiry date") || key.contains("expiration date") -> expiryDate = formatDate(value)
                key.contains("name server") -> ns.add(value.lowercase())
            }
        }
    }

    return ParsedWhoisData(
        domainName = domain,
        registrar = registrar,
        registrarUrl = startUrl,
        creationDate = creationDate,
        updatedDate = updatedDate,
        expiryDate = expiryDate,
        nameServers = ns.distinct(),
        raw = actualRaw // Return the cleaned raw text, not the JSON
    )
}

private fun formatDate(dateStr: String): String {
    // Try to parse ISO 8601 like 2024-09-14T04:00:00Z
    return try {
        // Just extract the YYYY-MM-DD part if it exists at the start
        if (dateStr.length >= 10) dateStr.substring(0, 10) else dateStr
    } catch (e: Exception) {
        dateStr
    }
}