package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

// Local language translation helper
fun t(en: String, hi: String, lang: String): String {
    return if (lang == "hi") hi else en
}

@Composable
fun DashboardScreen(
    viewModel: LabViewModel,
    patientsList: List<com.example.data.Patient>,
    reportsList: List<com.example.data.Report>,
    inventoryList: List<com.example.data.InventoryItem>,
    lang: String,
    currency: String,
    onNavigate: (Screen) -> Unit
) {
    val config = LocalConfiguration.current
    val isTablet = config.screenWidthDp >= 600

    // Calculate Analytics Metrics
    val totalPatientsCount = patientsList.size
    val todayDateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    
    val todayPatientsCount = patientsList.count { p ->
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(p.visitDateTime)) == todayDateStr
    }

    val pendingReportsCount = reportsList.count { it.sampleStatus != "Completed" }
    val completedReportsCount = reportsList.count { it.sampleStatus == "Completed" }

    // Today's Income
    val todayIncome = reportsList.filter { r ->
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(r.dateCreated)) == todayDateStr
    }.sumOf { it.paidAmount }

    // Monthly Income
    val currentMonthStr = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault()).format(java.util.Date())
    val monthlyIncome = reportsList.filter { r ->
        java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault()).format(java.util.Date(r.dateCreated)) == currentMonthStr
    }.sumOf { it.paidAmount }

    val pendingPayments = reportsList.sumOf { it.pendingAmount }

    val activeNotifications = viewModel.notifications.value

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Hero Graphic Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isTablet) 180.dp else 120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(bottom = 12.dp)
            ) {
                // Try to load img_dashboard_hero, fallback to clean gradients
                Image(
                    painter = painterResource(id = R.drawable.img_dashboard_hero),
                    contentDescription = "Lab Hero Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = viewModel.labName.value,
                        color = Color.White,
                        fontSize = if (isTablet) 24.sp else 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = t("Offline-First Pathology Suite", "ऑफ़लाइन-फर्स्ट पैथोलॉजी सूट", lang),
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Low Stock / High Alerts warning banner
        if (activeNotifications.any { it.type == NotificationType.LowStock }) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = AlertCrimson.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp),
                    border = CardStroke(1.dp, AlertCrimson.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = AlertCrimson,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = t("Low Inventory Reagent Warning!", "कम इन्वेंटरी रीएजेंट चेतावनी!", lang),
                                color = AlertCrimson,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = t("Some reagent items have fallen below critical safety margins. Restock immediately.", "कुछ रीएजेंट आइटम सुरक्षा मार्जिन से नीचे आ गए हैं। तुरंत स्टॉक करें।", lang),
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 11.sp
                            )
                        }
                        Button(
                            onClick = { onNavigate(Screen.Inventory) },
                            colors = ButtonDefaults.buttonColors(containerColor = AlertCrimson),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(t("Restock", "स्टॉक करें", lang), fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // Metrics Grid (Responsive Layout)
        item {
            // Primary Hero Stat: Bold Typography Emphasis (from design)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)), // Indigo 900
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = t("TODAY'S REVENUE", "आज का राजस्व", lang),
                                color = Color(0xFFC7D2FE), // Indigo 200
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$currency$todayIncome",
                                color = Color.White,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF6366F1).copy(alpha = 0.3f), CircleShape)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = t("+12% vs yest.", "+12% कल से", lang),
                                color = Color(0xFFA5B4FC), // Indigo 300
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    Divider(color = Color(0xFF4F46E5).copy(alpha = 0.3f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = t("PENDING DUES", "बकाया शुल्क", lang),
                                color = Color(0xFFA5B4FC), // Indigo 300
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$currency$pendingPayments",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(36.dp)
                                .background(Color(0xFF4F46E5).copy(alpha = 0.3f))
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = t("MONTHLY COLLECTED", "मासिक एकत्रित", lang),
                                color = Color(0xFFA5B4FC), // Indigo 300
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$currency$monthlyIncome",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            Text(
                text = t("Diagnostic Lab Overview", "नैदानिक प्रयोगशाला अवलोकन", lang).uppercase(),
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                color = Slate900,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
            )
            
            val todayPatientsLabel = if (todayPatientsCount > 0) t("↑ $todayPatientsCount New", "↑ $todayPatientsCount नए", lang) else t("0 Today", "0 आज", lang)
            val todayPatientsColor = if (todayPatientsCount > 0) SafeEmerald else SoftGrey
            
            val pendingReportsLabel = if (pendingReportsCount > 0) t("● Pending", "● लंबित", lang) else t("✓ Clear", "✓ सुरक्षित", lang)
            val pendingReportsColor = if (pendingReportsCount > 0) PendingAmber else SafeEmerald

            if (isTablet) {
                // Large tablet layout: 4 columns
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MetricCard(modifier = Modifier.weight(1f), title = t("Patients", "कुल मरीज", lang), value = "$totalPatientsCount", icon = Icons.Default.Person, iconColor = MaterialTheme.colorScheme.primary, subtitle = t("Overall registered", "पंजीकृत मरीज", lang))
                        MetricCard(modifier = Modifier.weight(1f), title = t("Today", "आज के मरीज", lang), value = "$todayPatientsCount", icon = Icons.Default.Groups, iconColor = ScienceTeal, subtitle = todayPatientsLabel, subtitleColor = todayPatientsColor)
                        MetricCard(modifier = Modifier.weight(1f), title = t("Pending", "लंबित रिपोर्ट", lang), value = "$pendingReportsCount", icon = Icons.Default.Pending, iconColor = PendingAmber, subtitle = pendingReportsLabel, subtitleColor = pendingReportsColor)
                        MetricCard(modifier = Modifier.weight(1f), title = t("Completed", "पूर्ण रिपोर्ट", lang), value = "$completedReportsCount", icon = Icons.Default.CheckCircle, iconColor = SafeEmerald, subtitle = t("✓ All set", "✓ सब ठीक", lang), subtitleColor = SafeEmerald)
                    }
                }
            } else {
                // Mobile layout: 2 columns
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricCard(modifier = Modifier.weight(1f), title = t("Patients", "मरीज", lang), value = "$totalPatientsCount", icon = Icons.Default.Person, iconColor = MaterialTheme.colorScheme.primary)
                        MetricCard(modifier = Modifier.weight(1f), title = t("Today", "आज", lang), value = "$todayPatientsCount", icon = Icons.Default.Groups, iconColor = ScienceTeal, subtitle = todayPatientsLabel, subtitleColor = todayPatientsColor)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricCard(modifier = Modifier.weight(1f), title = t("Pending", "लंबित", lang), value = "$pendingReportsCount", icon = Icons.Default.Pending, iconColor = PendingAmber, subtitle = pendingReportsLabel, subtitleColor = pendingReportsColor)
                        MetricCard(modifier = Modifier.weight(1f), title = t("Completed", "पूर्ण", lang), value = "$completedReportsCount", icon = Icons.Default.CheckCircle, iconColor = SafeEmerald)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Active Sample Tracking (as in Design HTML)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = t("Active Tracking", "सक्रिय ट्रैकिंग", lang).uppercase(),
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = Slate900,
                    letterSpacing = 1.sp
                )
                TextButton(onClick = { onNavigate(Screen.Reports) }) {
                    Text(
                        text = t("VIEW ALL", "सभी देखें", lang),
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        color = ClinicalNavy,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        if (reportsList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = t("No active tracking samples today", "आज कोई सक्रिय सैंपल नहीं हैं", lang),
                            color = SoftGrey,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            items(reportsList.take(3)) { report ->
                ActiveTrackingItem(report = report, lang = lang)
                Spacer(modifier = Modifier.height(10.dp))
            }
            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Quick Action Buttons
        item {
            Text(
                text = t("Quick Action Controls", "त्वरित नियंत्रण", lang).uppercase(),
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                color = Slate900,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionButton(
                    modifier = Modifier.weight(1f),
                    title = t("Register Patient", "मरीज पंजीकरण", lang),
                    subtitle = t("Add & check history", "नया मरीज जोडें", lang),
                    icon = Icons.Default.PersonAdd,
                    color = MaterialTheme.colorScheme.primary,
                    onClick = { onNavigate(Screen.Patients) }
                )
                QuickActionButton(
                    modifier = Modifier.weight(1f),
                    title = t("Billing Cart", "बिलिंग / रसीद", lang),
                    subtitle = t("Create Invoices", "रसीद बनाएं", lang),
                    icon = Icons.Default.ReceiptLong,
                    color = ScienceTeal,
                    onClick = { onNavigate(Screen.Billing) }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionButton(
                    modifier = Modifier.weight(1f),
                    title = t("Report Entry", "रिपोर्ट दर्ज करें", lang),
                    subtitle = t("Enter results & values", "परिणाम दर्ज करें", lang),
                    icon = Icons.Default.Science,
                    color = PendingAmber,
                    onClick = { onNavigate(Screen.Reports) }
                )
                QuickActionButton(
                    modifier = Modifier.weight(1f),
                    title = t("Inventory Status", "इन्वेंट्री / किट", lang),
                    subtitle = t("Manage Stocks", "स्टॉक देखें", lang),
                    icon = Icons.Default.Inventory,
                    color = SafeEmerald,
                    onClick = { onNavigate(Screen.Inventory) }
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Live Notifications / Alerts Drawer
        item {
            Text(
                text = t("System Log & Status Alerts", "सिस्टम लॉग और अलर्ट", lang),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (activeNotifications.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.Verified, contentDescription = "Clear", tint = SafeEmerald, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = t("All Systems Functional", "सभी प्रणालियाँ ठीक से काम कर रही हैं", lang),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = t("No critical low stock or overdue billing items found.", "कोई महत्वपूर्ण कम स्टॉक या बिलिंग बकाया नहीं मिला।", lang),
                            fontSize = 11.sp,
                            color = SoftGrey,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(activeNotifications.take(4)) { notification ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val backgroundTint = when (notification.type) {
                            NotificationType.LowStock -> AlertCrimson
                            NotificationType.ExpiryReminder -> PendingAmber
                            NotificationType.PaymentDue -> AlertCrimson
                            NotificationType.ReportReady -> SafeEmerald
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(backgroundTint.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val icon = when (notification.type) {
                                NotificationType.LowStock -> Icons.Default.TrendingDown
                                NotificationType.ExpiryReminder -> Icons.Default.EventBusy
                                NotificationType.PaymentDue -> Icons.Default.AccountBalanceWallet
                                NotificationType.ReportReady -> Icons.Default.Check
                            }
                            Icon(imageVector = icon, contentDescription = "Alert", tint = backgroundTint, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = notification.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = notification.description,
                                fontSize = 11.sp,
                                color = SoftGrey,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveTrackingItem(report: com.example.data.Report, lang: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = CardStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val iconBgColor = when (report.sampleStatus) {
                "Completed" -> Color(0xFFD1FAE5) // Emerald 100
                "In Process" -> Color(0xFFFEF3C7) // Amber 100
                else -> Color(0xFFDBEAFE) // Blue 100
            }
            val iconColor = when (report.sampleStatus) {
                "Completed" -> Color(0xFF059669) // Emerald 600
                "In Process" -> Color(0xFFD97706) // Amber 600
                else -> Color(0xFF3B82F6) // Blue 500
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBgColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (report.sampleStatus == "Completed") Icons.Default.CheckCircle else Icons.Default.Science,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = report.patientName,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = Slate900
                )
                Text(
                    text = "${report.sampleType.uppercase()} • ${report.reportNo}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftGrey,
                    letterSpacing = 0.5.sp
                )
            }
            
            // Status Badge
            val badgeBg = when (report.sampleStatus) {
                "Completed" -> Color(0xFFD1FAE5) // light emerald
                "In Process" -> Color(0xFFFEF3C7) // light amber
                else -> Color(0xFFE0F2FE) // light blue
            }
            val badgeText = when (report.sampleStatus) {
                "Completed" -> Color(0xFF065F46)
                "In Process" -> Color(0xFFB45309)
                else -> Color(0xFF0369A1)
            }
            val statusLabel = when (report.sampleStatus) {
                "Completed" -> t("READY", "तैयार", lang)
                "In Process" -> t("PROCESSING", "प्रक्रिया में", lang)
                else -> t("COLLECTED", "एकत्रित", lang)
            }
            
            Box(
                modifier = Modifier
                    .background(badgeBg, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = statusLabel,
                    color = badgeText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    subtitle: String? = null,
    subtitleColor: Color = SoftGrey
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = CardStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    color = SoftGrey,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(iconColor.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Black,
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = (-0.5).sp
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = subtitleColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(76.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = subtitle,
                    color = SoftGrey,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CardStroke(width: androidx.compose.ui.unit.Dp, color: Color) = 
    androidx.compose.foundation.BorderStroke(width, color)
