package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ClinicalNavy
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SoftGrey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppLayout(viewModel: LabViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val loggedInStaff by viewModel.loggedInStaff.collectAsState()

    val lang by viewModel.languageCode
    val currency by viewModel.currencySymbol
    val darkTheme by viewModel.isDarkTheme

    val config = LocalConfiguration.current
    val isTablet = config.screenWidthDp >= 600

    var showMoreSheet by remember { mutableStateOf(false) }

    MyApplicationTheme(darkTheme = darkTheme) {
        if (loggedInStaff == null) {
            // Force login page
            LoginScreen(viewModel = viewModel, lang = lang)
        } else {
            // Main app scaffolding
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Science, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = viewModel.labName.value,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${t("Staff Active", "सक्रिय कर्मचारी", lang)}: ${loggedInStaff!!.name} (${loggedInStaff!!.role})",
                                        fontSize = 10.sp,
                                        color = SoftGrey
                                    )
                                }
                            }
                        },
                        actions = {
                            // Logout button
                            IconButton(onClick = { viewModel.logout() }) {
                                Icon(Icons.Default.Logout, contentDescription = "Log Out", tint = MaterialTheme.colorScheme.error)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                },
                bottomBar = {
                    if (!isTablet) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            NavigationBarItem(
                                selected = currentScreen == Screen.Dashboard,
                                onClick = { viewModel.navigateTo(Screen.Dashboard) },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                                label = { Text(t("Home", "मुख्य", lang), fontSize = 10.sp) }
                            )
                            NavigationBarItem(
                                selected = currentScreen == Screen.Patients,
                                onClick = { viewModel.navigateTo(Screen.Patients) },
                                icon = { Icon(Icons.Default.Groups, contentDescription = "Patients") },
                                label = { Text(t("Patients", "मरीज", lang), fontSize = 10.sp) }
                            )
                            NavigationBarItem(
                                selected = currentScreen == Screen.Billing,
                                onClick = { viewModel.navigateTo(Screen.Billing) },
                                icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Billing") },
                                label = { Text(t("Billing", "बिलिंग", lang), fontSize = 10.sp) }
                            )
                            NavigationBarItem(
                                selected = currentScreen == Screen.Reports,
                                onClick = { viewModel.navigateTo(Screen.Reports) },
                                icon = { Icon(Icons.Default.Science, contentDescription = "Results") },
                                label = { Text(t("Reports", "रिपोर्ट", lang), fontSize = 10.sp) }
                            )
                            NavigationBarItem(
                                selected = showMoreSheet,
                                onClick = { showMoreSheet = true },
                                icon = { Icon(Icons.Default.Menu, contentDescription = "More options") },
                                label = { Text(t("More", "अधिक", lang), fontSize = 10.sp) }
                            )
                        }
                    }
                }
            ) { paddingValues ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // On wide devices, render left navigation rail instead of bottom bar
                    if (isTablet) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Spacer(modifier = Modifier.height(12.dp))
                            NavigationRailItem(
                                selected = currentScreen == Screen.Dashboard,
                                onClick = { viewModel.navigateTo(Screen.Dashboard) },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                                label = { Text(t("Home", "मुख्य", lang), fontSize = 10.sp) }
                            )
                            NavigationRailItem(
                                selected = currentScreen == Screen.Patients,
                                onClick = { viewModel.navigateTo(Screen.Patients) },
                                icon = { Icon(Icons.Default.Groups, contentDescription = "Patients") },
                                label = { Text(t("Patients", "मरीज", lang), fontSize = 10.sp) }
                            )
                            NavigationRailItem(
                                selected = currentScreen == Screen.Billing,
                                onClick = { viewModel.navigateTo(Screen.Billing) },
                                icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Billing") },
                                label = { Text(t("Billing", "बिलिंग", lang), fontSize = 10.sp) }
                            )
                            NavigationRailItem(
                                selected = currentScreen == Screen.Reports,
                                onClick = { viewModel.navigateTo(Screen.Reports) },
                                icon = { Icon(Icons.Default.Science, contentDescription = "Results") },
                                label = { Text(t("Reports", "रिपोर्ट", lang), fontSize = 10.sp) }
                            )
                            NavigationRailItem(
                                selected = currentScreen == Screen.Doctors,
                                onClick = { viewModel.navigateTo(Screen.Doctors) },
                                icon = { Icon(Icons.Default.Person, contentDescription = "Doctors") },
                                label = { Text(t("Doctors", "डॉक्टर", lang), fontSize = 10.sp) }
                            )
                            NavigationRailItem(
                                selected = currentScreen == Screen.Tests,
                                onClick = { viewModel.navigateTo(Screen.Tests) },
                                icon = { Icon(Icons.Default.ListAlt, contentDescription = "Tests") },
                                label = { Text(t("Tests", "टेस्ट", lang), fontSize = 10.sp) }
                            )
                            NavigationRailItem(
                                selected = currentScreen == Screen.Inventory,
                                onClick = { viewModel.navigateTo(Screen.Inventory) },
                                icon = { Icon(Icons.Default.Inventory, contentDescription = "Inventory") },
                                label = { Text(t("Reagents", "इन्वेंट्री", lang), fontSize = 10.sp) }
                            )
                            NavigationRailItem(
                                selected = currentScreen == Screen.Commissions,
                                onClick = { viewModel.navigateTo(Screen.Commissions) },
                                icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Commissions") },
                                label = { Text(t("Earnings", "कमीशन", lang), fontSize = 10.sp) }
                            )
                            NavigationRailItem(
                                selected = currentScreen == Screen.Settings,
                                onClick = { viewModel.navigateTo(Screen.Settings) },
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                label = { Text(t("Settings", "सेटिंग्स", lang), fontSize = 10.sp) }
                            )
                        }
                        Divider(modifier = Modifier.fillMaxHeight().width(1.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    }

                    // MAIN SCREEN VIEWER CONTAINER
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        val patientsList by viewModel.patients.collectAsState()
                        val doctorsList by viewModel.doctors.collectAsState()
                        val testsList by viewModel.tests.collectAsState()
                        val packagesList by viewModel.packages.collectAsState()
                        val reportsList by viewModel.reports.collectAsState()
                        val inventoryList by viewModel.inventoryItems.collectAsState()
                        val staffList by viewModel.staffList.collectAsState()
                        val commissionsList by viewModel.commissions.collectAsState()

                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "ScreenTransition"
                        ) { screen ->
                            when (screen) {
                                Screen.Dashboard -> DashboardScreen(
                                    viewModel = viewModel,
                                    patientsList = patientsList,
                                    reportsList = reportsList,
                                    inventoryList = inventoryList,
                                    lang = lang,
                                    currency = currency,
                                    onNavigate = { viewModel.navigateTo(it) }
                                )
                                Screen.Patients -> PatientScreen(
                                    viewModel = viewModel,
                                    patientsList = patientsList,
                                    doctorsList = doctorsList,
                                    lang = lang
                                )
                                Screen.Doctors -> DoctorScreen(
                                    viewModel = viewModel,
                                    doctorsList = doctorsList,
                                    commissionsList = commissionsList,
                                    lang = lang,
                                    currency = currency
                                )
                                Screen.Tests -> TestsScreen(
                                    viewModel = viewModel,
                                    testsList = testsList,
                                    lang = lang,
                                    currency = currency
                                )
                                Screen.Billing -> BillingScreen(
                                    viewModel = viewModel,
                                    patientsList = patientsList,
                                    testsList = testsList,
                                    packagesList = packagesList,
                                    lang = lang,
                                    currency = currency,
                                    onNavigate = { viewModel.navigateTo(it) }
                                )
                                Screen.Reports -> ResultScreen(
                                    viewModel = viewModel,
                                    reportsList = reportsList,
                                    lang = lang,
                                    onNavigate = { viewModel.navigateTo(it) }
                                )
                                Screen.Inventory -> InventoryScreen(
                                    viewModel = viewModel,
                                    inventoryList = inventoryList,
                                    lang = lang
                                )
                                Screen.Staff -> StaffScreen(
                                    viewModel = viewModel,
                                    staffList = staffList,
                                    lang = lang
                                )
                                Screen.Commissions -> CommissionsScreen(
                                    viewModel = viewModel,
                                    commissionsList = commissionsList,
                                    lang = lang,
                                    currency = currency
                                )
                                Screen.Settings -> SettingsScreen(
                                    viewModel = viewModel,
                                    lang = lang,
                                    currency = currency
                                )
                                else -> DashboardScreen(
                                    viewModel = viewModel,
                                    patientsList = patientsList,
                                    reportsList = reportsList,
                                    inventoryList = inventoryList,
                                    lang = lang,
                                    currency = currency,
                                    onNavigate = { viewModel.navigateTo(it) }
                                )
                            }
                        }
                    }
                }
            }

            // SLIDE UP MORE OPTIONS SHEET (For Compact Devices)
            if (showMoreSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showMoreSheet = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text = t("Pathology Administration", "पैथोलॉजी प्रशासन", lang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ClinicalNavy,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MenuGridItem(
                                modifier = Modifier.weight(1f),
                                label = t("Doctors", "डॉक्टर", lang),
                                icon = Icons.Default.Person,
                                color = MaterialTheme.colorScheme.primary,
                                onClick = { viewModel.navigateTo(Screen.Doctors); showMoreSheet = false }
                            )
                            MenuGridItem(
                                modifier = Modifier.weight(1f),
                                label = t("All Tests", "सभी टेस्ट", lang),
                                icon = Icons.Default.ListAlt,
                                color = ClinicalNavy,
                                onClick = { viewModel.navigateTo(Screen.Tests); showMoreSheet = false }
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MenuGridItem(
                                modifier = Modifier.weight(1f),
                                label = t("Reagents", "इन्वेंट्री", lang),
                                icon = Icons.Default.Inventory,
                                color = MaterialTheme.colorScheme.tertiary,
                                onClick = { viewModel.navigateTo(Screen.Inventory); showMoreSheet = false }
                            )
                            MenuGridItem(
                                modifier = Modifier.weight(1f),
                                label = t("Commissions", "कमीशन", lang),
                                icon = Icons.Default.AccountBalanceWallet,
                                color = MaterialTheme.colorScheme.secondary,
                                onClick = { viewModel.navigateTo(Screen.Commissions); showMoreSheet = false }
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MenuGridItem(
                                modifier = Modifier.weight(1f),
                                label = t("Staff Roles", "स्टाफ भूमिकाएं", lang),
                                icon = Icons.Default.Badge,
                                color = MaterialTheme.colorScheme.primary,
                                onClick = { viewModel.navigateTo(Screen.Staff); showMoreSheet = false }
                            )
                            MenuGridItem(
                                modifier = Modifier.weight(1f),
                                label = t("Settings", "सेटिंग्स", lang),
                                icon = Icons.Default.Settings,
                                color = SoftGrey,
                                onClick = { viewModel.navigateTo(Screen.Settings); showMoreSheet = false }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { showMoreSheet = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(t("Dismiss", "बंद करें", lang))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MenuGridItem(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(64.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.12f), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = label, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
