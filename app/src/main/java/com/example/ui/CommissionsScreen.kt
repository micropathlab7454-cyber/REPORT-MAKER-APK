package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DoctorCommission
import com.example.ui.theme.ClinicalNavy
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.SoftGrey

@Composable
fun CommissionsScreen(
    viewModel: LabViewModel,
    commissionsList: List<DoctorCommission>,
    lang: String,
    currency: String
) {
    val activeReportId by viewModel.activeReportId
    val reportsList by viewModel.reports.collectAsState()

    if (activeReportId != null) {
        // Render A4 PDF report in place!
        ReportPdfScreen(
            viewModel = viewModel,
            reportId = activeReportId!!,
            reportsList = reportsList,
            lang = lang,
            currency = currency,
            onBack = {
                viewModel.activeReportId.value = null
            }
        )
    } else {
        // Render Commissions dashboard lists
        val totalCommission = commissionsList.sumOf { it.commissionAmount }
        val paidCommission = commissionsList.filter { it.isPaid }.sumOf { it.commissionAmount }
        val pendingCommission = commissionsList.filter { !it.isPaid }.sumOf { it.commissionAmount }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                text = t("Doctor Referral Commissions", "डॉक्टर रेफरल कमीशन", lang),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = ClinicalNavy,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Mini analytics bar
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total Accrued", fontSize = 11.sp, color = SoftGrey)
                        Text("$currency$totalCommission", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ClinicalNavy)
                    }
                    Column {
                        Text("Paid Commission", fontSize = 11.sp, color = SoftGrey)
                        Text("$currency$paidCommission", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SafeEmerald)
                    }
                    Column {
                        Text("Pending Due", fontSize = 11.sp, color = SoftGrey)
                        Text("$currency$pendingCommission", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            if (commissionsList.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = SoftGrey, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No Commission Logs Found", color = SoftGrey, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(commissionsList) { comm ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(comm.doctorName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Patient: ${comm.patientName} (${comm.reportNo})", fontSize = 11.sp, color = SoftGrey)
                                    Text("Billing Amt: $currency${comm.billingAmount}", fontSize = 10.sp, color = SoftGrey)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "$currency${comm.commissionAmount}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = if (comm.isPaid) SafeEmerald else ClinicalNavy
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (comm.isPaid) {
                                        Box(
                                            modifier = Modifier
                                                .background(SafeEmerald.copy(alpha = 0.12f), MaterialTheme.shapes.small)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SafeEmerald, modifier = Modifier.size(10.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("Paid", color = SafeEmerald, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    } else {
                                        Button(
                                            onClick = { viewModel.markCommissionAsPaid(comm) },
                                            colors = ButtonDefaults.buttonColors(containerColor = ClinicalNavy),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("Mark Paid", fontSize = 9.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
