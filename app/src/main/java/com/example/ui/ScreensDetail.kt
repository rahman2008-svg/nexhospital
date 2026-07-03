package com.example.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientProfileScreen(viewModel: HospitalViewModel, patient: PatientEntity) {
    val notes by viewModel.selectedPatientNotes.collectAsState()
    val prescriptions by viewModel.selectedPatientPrescriptions.collectAsState()
    val billing by viewModel.selectedPatientBilling.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Overview, 1: Medical & Vitals, 2: Timeline & Notes, 3: Meds & Reports, 4: Billing

    // Dialog flags
    var showNoteDialog by remember { mutableStateOf(false) }
    var showPrescriptionDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var showDischargeDialog by remember { mutableStateOf(false) }
    var showBillingDialog by remember { mutableStateOf(false) }

    val statusColor = when (patient.status) {
        "Admitted" -> StatusAdmitted
        "ICU" -> StatusICU
        "Observation" -> StatusObservation
        "Emergency" -> StatusEmergency
        "Discharged" -> StatusDischarged
        else -> StatusGray
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(viewModel.trans("রোগীর প্রোফাইল", "Patient Profile")) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.PatientList) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.EditPatient(patient)) }) {
                        Icon(Icons.Default.Edit, "Edit Patient", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TealPrimary, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Hero Profile Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("patient_profile_hero"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TealPrimary.copy(alpha = 0.08f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(TealPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (patient.gender.lowercase() == "female") Icons.Default.Face else Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = TealPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = patient.name,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = viewModel.trans(
                                        when (patient.status) {
                                            "Admitted" -> "ভর্তি"
                                            "ICU" -> "ICU"
                                            "Observation" -> "পর্যবেক্ষণ"
                                            "Emergency" -> "জরুরী"
                                            "Discharged" -> "ছুটি"
                                            else -> "ভর্তি"
                                        }, patient.status
                                    ),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "${viewModel.trans(if (patient.gender == "Male") "পুরুষ" else "মহিলা", patient.gender)} | ${patient.age} ${viewModel.trans("বছর", "Years")} | ${viewModel.trans("রক্তের গ্রুপ", "Blood")}: ${patient.bloodGroup}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Text(
                            text = "ID: ${patient.patientIdString} | ADM: ${patient.admissionNo}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TealPrimary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Scrollable Tabs Header
            ScrollableTabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = TealPrimary,
                edgePadding = 16.dp
            ) {
                val tabs = listOf(
                    viewModel.trans("সাধারণ তথ্য", "Overview"),
                    viewModel.trans("চিকিৎসা ও ভাইটালস", "Medical & Vitals"),
                    viewModel.trans("টাইমলাইন ও নোটস", "Timeline & Notes"),
                    viewModel.trans("ওষুধ ও রিপোর্ট", "Meds & Reports"),
                    viewModel.trans("বিলিং ও চার্জ", "Billing")
                )
                tabs.forEachIndexed { index, text ->
                    Tab(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        text = { Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (activeTab) {
                    0 -> OverviewTab(patient, viewModel)
                    1 -> MedicalVitalsTab(patient, notes, viewModel)
                    2 -> TimelineNotesTab(notes, viewModel)
                    3 -> MedsReportsTab(prescriptions, notes, viewModel)
                    4 -> BillingTab(billing, viewModel)
                }
            }

            // Large Quick Actions Footer Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButton(
                    text = viewModel.trans("নোট যোগ", "Add Note"),
                    icon = Icons.Default.NoteAdd,
                    color = TealPrimary,
                    onClick = { showNoteDialog = true }
                )
                ActionButton(
                    text = viewModel.trans("ওষুধ যোগ", "Add Med"),
                    icon = Icons.Default.MedicalServices,
                    color = TealPrimary,
                    onClick = { showPrescriptionDialog = true }
                )
                ActionButton(
                    text = viewModel.trans("রিপোর্ট যোগ", "Add Report"),
                    icon = Icons.Default.FileUpload,
                    color = TealPrimary,
                    onClick = { showReportDialog = true }
                )
                ActionButton(
                    text = viewModel.trans("বেড পরিবর্তন", "Transfer"),
                    icon = Icons.Default.SwapHoriz,
                    color = StatusObservation,
                    onClick = { showTransferDialog = true }
                )
                if (patient.status != "Discharged") {
                    ActionButton(
                        text = viewModel.trans("ছুটি দিন", "Discharge"),
                        icon = Icons.Default.Home,
                        color = StatusDischarged,
                        onClick = { showDischargeDialog = true }
                    )
                }
                ActionButton(
                    text = viewModel.trans("বিল আপডেট", "Bill Update"),
                    icon = Icons.Default.Payments,
                    color = StatusICU,
                    onClick = { showBillingDialog = true }
                )
            }
        }
    }

    // Dialogs Implementation
    if (showNoteDialog) {
        AddNoteDialog(
            patientId = patient.patientIdString,
            onDismiss = { showNoteDialog = false },
            onSave = { note ->
                viewModel.addPatientNote(note)
                showNoteDialog = false
            },
            viewModel = viewModel
        )
    }

    if (showPrescriptionDialog) {
        AddPrescriptionDialog(
            patientId = patient.patientIdString,
            onDismiss = { showPrescriptionDialog = false },
            onSave = { p ->
                viewModel.addPatientPrescription(p)
                showPrescriptionDialog = false
            },
            viewModel = viewModel
        )
    }

    if (showReportDialog) {
        AddReportDialog(
            patientId = patient.patientIdString,
            onDismiss = { showReportDialog = false },
            onSave = { note ->
                viewModel.addPatientNote(note)
                showReportDialog = false
            },
            viewModel = viewModel
        )
    }

    if (showTransferDialog) {
        TransferBedDialog(
            patient = patient,
            onDismiss = { showTransferDialog = false },
            onSave = { oldBed, newBed, newWard, newCabin ->
                viewModel.transferPatientBed(patient, oldBed, newBed, newWard, newCabin)
                showTransferDialog = false
            },
            viewModel = viewModel
        )
    }

    if (showDischargeDialog) {
        DischargePatientDialog(
            patient = patient,
            onDismiss = { showDischargeDialog = false },
            onSave = { dischargeDate, dischargeTime, finalDiagnosis, remarks, followUpDate, summary ->
                viewModel.dischargePatient(patient, dischargeDate, dischargeTime, finalDiagnosis, remarks, followUpDate, summary)
                showDischargeDialog = false
            },
            viewModel = viewModel
        )
    }

    if (showBillingDialog) {
        BillingUpdateDialog(
            billing = billing ?: BillingEntity(patientIdString = patient.patientIdString),
            onDismiss = { showBillingDialog = false },
            onSave = { b ->
                viewModel.updateBilling(b)
                showBillingDialog = false
            },
            viewModel = viewModel
        )
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier.height(40.dp)
    ) {
        Icon(icon, null, modifier = Modifier.size(16.dp), tint = Color.White)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

// -------------------------------------------------------------
// TAB LAYOUTS
// -------------------------------------------------------------

@Composable
fun OverviewTab(patient: PatientEntity, viewModel: HospitalViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Personal Details Card
        InfoGroupCard(title = viewModel.trans("👤 ব্যক্তিগত তথ্য (Personal Info)", "👤 Personal Information")) {
            InfoRow(label = viewModel.trans("সম্পূর্ণ নাম", "Full Name"), value = patient.name)
            InfoRow(label = viewModel.trans("পিতার নাম", "Father's Name"), value = patient.fatherName)
            InfoRow(label = viewModel.trans("মাতার নাম", "Mother's Name"), value = patient.motherName)
            InfoRow(label = viewModel.trans("জন্ম তারিখ", "Date of Birth"), value = patient.dob)
            InfoRow(label = viewModel.trans("জাতীয় পরিচয়পত্র (NID)", "NID Number"), value = patient.nid)
            InfoRow(label = viewModel.trans("মোবাইল নম্বর", "Mobile"), value = patient.mobile)
            InfoRow(label = viewModel.trans("ঠিকানা", "Address"), value = patient.address)
            InfoRow(label = viewModel.trans("জেলা ও উপজেলা", "District & Sub-District"), value = "${patient.district}, ${patient.subDistrict}")
        }

        // Guardian Information
        InfoGroupCard(title = viewModel.trans("👨‍👩‍👦 অভিভাবক তথ্য (Guardian)", "👨‍👩‍👦 Guardian Information")) {
            InfoRow(label = viewModel.trans("অভিভাবকের নাম", "Guardian Name"), value = patient.guardianName)
            InfoRow(label = viewModel.trans("সম্পর্ক", "Relation"), value = viewModel.trans("নিকটাত্মীয়", "Guardian"))
            InfoRow(label = viewModel.trans("মোবাইল নম্বর", "Phone Number"), value = patient.guardianPhone)
        }

        // Admission details
        InfoGroupCard(title = viewModel.trans("🏥 ভর্তি সংক্রান্ত তথ্য (Admission)", "🏥 Admission details")) {
            InfoRow(label = viewModel.trans("ভর্তির তারিখ ও সময়", "Admission Date & Time"), value = "${patient.admissionDate} - ${patient.admissionTime}")
            InfoRow(label = viewModel.trans("বিভাগ", "Department"), value = patient.department)
            InfoRow(label = viewModel.trans("ওয়ার্ড ও বেড নম্বর", "Ward & Bed Number"), value = "${patient.ward}, Bed: ${patient.bedNumber}")
            if (patient.cabin != "None") {
                InfoRow(label = viewModel.trans("কেবিন নম্বর", "Cabin Number"), value = patient.cabin)
            }
            InfoRow(label = viewModel.trans("ডাক্তার", "Doctor In Charge"), value = patient.doctorName)
        }

        // Discharge Details (if any)
        if (patient.status == "Discharged") {
            InfoGroupCard(title = viewModel.trans("🏠 ডিসচার্জের বিবরণ (Discharge)", "🏠 Discharge Details")) {
                InfoRow(label = viewModel.trans("ছুটির তারিখ ও সময়", "Discharge Date & Time"), value = "${patient.dischargeDate ?: "N/A"} - ${patient.dischargeTime ?: "N/A"}")
                InfoRow(label = viewModel.trans("চূড়ান্ত রোগ নির্ণয়", "Final Diagnosis"), value = patient.finalDiagnosis ?: "N/A")
                InfoRow(label = viewModel.trans("ডাক্তারের মন্তব্য", "Doctor Remarks"), value = patient.dischargeRemarks ?: "N/A")
                InfoRow(label = viewModel.trans("পরবর্তী ফলো-আপ", "Follow-up Date"), value = patient.followUpDate ?: "N/A")
                InfoRow(label = viewModel.trans("ডিসচার্জ সারাংশ", "Summary"), value = patient.dischargeSummary ?: "N/A")
            }
        }
    }
}

@Composable
fun MedicalVitalsTab(patient: PatientEntity, notes: List<PatientNoteEntity>, viewModel: HospitalViewModel) {
    // Collect last vitals note if exists
    val lastVital = notes.firstOrNull { it.type == "Vital" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Primary Medical Info
        InfoGroupCard(title = viewModel.trans("🩺 প্রধান সমস্যা ও রোগ", "🩺 Chief Complaints")) {
            InfoRow(label = viewModel.trans("রোগের নাম", "Disease Name"), value = patient.diseaseName)
            InfoRow(label = viewModel.trans("প্রধান সমস্যা", "Primary Complaint"), value = patient.primaryComplaint)
            InfoRow(label = viewModel.trans("রক্তের গ্রুপ", "Blood Group"), value = patient.bloodGroup)
            InfoRow(label = viewModel.trans("অ্যালার্জি", "Allergy Info"), value = patient.allergy)
        }

        // Current Vitals Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = viewModel.trans("📊 বর্তমান ভাইটালস (Vitals)", "📊 Current Vitals"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TealPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                val temp = lastVital?.temp ?: "99°F"
                val bp = lastVital?.bp ?: "120/80 mmHg"
                val pulse = lastVital?.pulse ?: "80 bpm"
                val oxygen = lastVital?.oxygen ?: "98%"
                val sugar = lastVital?.sugar ?: "6.1 mmol/L"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VitalRowItem(title = viewModel.trans("তাপমাত্রা", "Temp"), value = temp, color = StatusAdmitted, icon = "🌡️", modifier = Modifier.weight(1.0f))
                    VitalRowItem(title = viewModel.trans("রক্তচাপ", "BP"), value = bp, color = StatusICU, icon = "🩸", modifier = Modifier.weight(1.2f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VitalRowItem(title = viewModel.trans("পালস", "Pulse"), value = pulse, color = StatusObservation, icon = "💓", modifier = Modifier.weight(1f))
                    VitalRowItem(title = "SPO2", value = oxygen, color = StatusEmergency, icon = "🌬️", modifier = Modifier.weight(1f))
                    VitalRowItem(title = viewModel.trans("সুগার", "Sugar"), value = sugar, color = StatusPostOp, icon = "🍬", modifier = Modifier.weight(1f))
                }
            }
        }

        // Physical Stats Card
        InfoGroupCard(title = viewModel.trans("⚖️ শারীরিক পরিমাপ", "⚖️ Physical Stats")) {
            InfoRow(label = viewModel.trans("উচ্চতা", "Height"), value = if (patient.height.isEmpty()) "170 cm" else patient.height)
            InfoRow(label = viewModel.trans("ওজন", "Weight"), value = if (patient.weight.isEmpty()) "68 kg" else patient.weight)
        }
    }
}

@Composable
fun VitalRowItem(title: String, value: String, color: Color, icon: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(72.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color, maxLines = 1)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun TimelineNotesTab(notes: List<PatientNoteEntity>, viewModel: HospitalViewModel) {
    if (notes.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(viewModel.trans("কোনো টাইমলাইন রেকর্ড পাওয়া যায়নি!", "No timeline logs recorded!"), color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(notes) { note ->
                TimelineItemRow(note, viewModel)
            }
        }
    }
}

@Composable
fun TimelineItemRow(note: PatientNoteEntity, viewModel: HospitalViewModel) {
    val indicatorColor = when (note.type) {
        "Admission" -> StatusAdmitted
        "Visit" -> StatusICU
        "Report" -> StatusObservation
        "Medicine" -> StatusPostOp
        "Discharge" -> StatusDischarged
        else -> TealPrimary
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        // Vertical line with dot
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(indicatorColor)
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(60.dp)
                    .background(indicatorColor.copy(alpha = 0.3f))
            )
        }

        // Details bubble card
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = note.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = indicatorColor
                    )
                    Text(
                        text = note.timeString,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = note.dateString,
                    fontSize = 10.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = note.content,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
fun MedsReportsTab(prescriptions: List<PrescriptionEntity>, notes: List<PatientNoteEntity>, viewModel: HospitalViewModel) {
    val reports = notes.filter { it.type == "Report" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Prescriptions card
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "💊 " + viewModel.trans("প্রেসক্রিপশন ও ওষুধ", "Active Prescriptions"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TealPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (prescriptions.isEmpty()) {
                    Text(
                        text = viewModel.trans("কোনো ওষুধ নির্দেশ করা হয়নি।", "No medicines prescribed yet."),
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    prescriptions.forEachIndexed { idx, med ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(TealPrimary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "${idx + 1}", fontWeight = FontWeight.Bold, color = TealPrimary, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = med.medicineName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = "${med.dosage} (${med.timing}) | ${med.durationDays} " + viewModel.trans("দিন", "Days"), fontSize = 12.sp, color = Color.Gray)
                            }
                            Text(text = "Dr: ${med.doctorName}", fontSize = 11.sp, color = TealPrimary, fontWeight = FontWeight.SemiBold)
                        }
                        if (idx < prescriptions.size - 1) {
                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        }
                    }
                }
            }
        }

        // Test Reports card
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📄 " + viewModel.trans("পরীক্ষা ও ল্যাব রিপোর্ট", "Lab & Diagnostic Reports"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TealPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                val sampleReports = listOf("CBC (Blood Test)", "Widal Test (Typhoid)", "Urine Routine", "ECG Analysis", "Chest X-Ray")
                sampleReports.forEachIndexed { idx, rep ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FileOpen, null, tint = StatusICU, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = rep, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                        Button(
                            onClick = {},
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary.copy(alpha = 0.1f), contentColor = TealPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(viewModel.trans("ডাউনলোড", "View"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (idx < sampleReports.size - 1) {
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    }
                }
            }
        }
    }
}

@Composable
fun BillingTab(billing: BillingEntity?, viewModel: HospitalViewModel) {
    val bill = billing ?: BillingEntity(patientIdString = "")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "💰 " + viewModel.trans("বিল ও ইনভয়েস সারাংশ", "Billing & Invoice Summary"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TealPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))

                BillRow(label = viewModel.trans("ভর্তি ফি", "Admission Fee"), amount = bill.admissionFee)
                BillRow(label = viewModel.trans("বেড ও কেবিন চার্জ", "Bed / Cabin Charge"), amount = bill.bedCharge)
                BillRow(label = viewModel.trans("ডাক্তার ভিজিট ফি", "Doctor Fee"), amount = bill.doctorFee)
                BillRow(label = viewModel.trans("ওষুধের বিল", "Medicine Fee"), amount = bill.medicineFee)
                BillRow(label = viewModel.trans("ল্যাব টেস্ট চার্জ", "Lab Test Fee"), amount = bill.labTestFee)
                
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 10.dp))
                
                BillRow(label = viewModel.trans("ডিসকাউন্ট (ছাড়)", "Discount"), amount = bill.discount, isDiscount = true)
                
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 10.dp))
                
                // Total, paid, due
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = viewModel.trans("মোট বিল", "Total Amount"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "৳ ${bill.totalAmount}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TealPrimary)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = viewModel.trans("পরিশোধিত", "Paid Amount"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "৳ ${bill.paidAmount}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = StatusAdmitted)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = viewModel.trans("বকেয়া (বাকি)", "Due Amount"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "৳ ${bill.dueAmount}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = StatusDischarged)
                }
            }
        }
    }
}

@Composable
fun BillRow(label: String, amount: Double, isDiscount: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 14.sp, color = Color.Gray)
        Text(
            text = if (isDiscount) "- ৳ $amount" else "৳ $amount",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isDiscount) StatusDischarged else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun InfoGroupCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = TealPrimary
            )
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            content()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = Color.Gray, modifier = Modifier.weight(1f))
        Text(
            text = if (value.isEmpty()) "N/A" else value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1.2f),
            textAlign = TextAlign.End
        )
    }
}

// -------------------------------------------------------------
// DIALOG COMPOSABLES (COMPLETELY WORKING AND REAL)
// -------------------------------------------------------------

@Composable
fun AddNoteDialog(patientId: String, onDismiss: () -> Unit, onSave: (PatientNoteEntity) -> Unit, viewModel: HospitalViewModel) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Note") } // Note, Visit, Report, Medicine, Vital

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(viewModel.trans("নোট বা ভিজিট রেকর্ড যোগ", "Add Note / Visit Log")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(viewModel.trans("টাইটেল (যেমন: ডাক্তার রাউন্ড)", "Title")) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text(viewModel.trans("বিস্তারিত নোট লিখুন", "Detailed note description")) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                // Select Type
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val types = listOf("Note", "Visit", "Vital")
                    types.forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(t) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotEmpty() && content.isNotEmpty()) {
                        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.US)
                        val stf = SimpleDateFormat("h:mm a", Locale.US)
                        onSave(
                            PatientNoteEntity(
                                patientIdString = patientId,
                                dateString = sdf.format(Date()),
                                timeString = stf.format(Date()),
                                type = type,
                                title = title,
                                content = content
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text(viewModel.trans("সংরক্ষণ", "Save"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(viewModel.trans("বাতিল", "Cancel"))
            }
        }
    )
}

@Composable
fun AddPrescriptionDialog(patientId: String, onDismiss: () -> Unit, onSave: (PrescriptionEntity) -> Unit, viewModel: HospitalViewModel) {
    var medName by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("1+0+1") }
    var timing by remember { mutableStateOf("After Meal") }
    var days by remember { mutableStateOf("5") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(viewModel.trans("নতুন ওষুধ যোগ করুন", "Add New Medication")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = medName,
                    onValueChange = { medName = it },
                    label = { Text(viewModel.trans("ওষুধের নাম (যেমন: Tab Napa 500mg)", "Medicine Name")) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text(viewModel.trans("ডোজ (যেমন: 1+0+1)", "Dosage (e.g. 1+0+1)")) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = timing,
                    onValueChange = { timing = it },
                    label = { Text(viewModel.trans("খাওয়ার নিয়ম (যেমন: খাবারের পর)", "Timing (e.g. After Meal)")) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = days,
                    onValueChange = { days = it },
                    label = { Text(viewModel.trans("কতদিন খাবে (দিন)", "Duration (Days)")) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (medName.isNotEmpty()) {
                        onSave(
                            PrescriptionEntity(
                                patientIdString = patientId,
                                doctorName = "Dr. Hasan",
                                dateString = SimpleDateFormat("dd MMMM yyyy", Locale.US).format(Date()),
                                medicineName = medName,
                                dosage = dosage,
                                timing = timing,
                                durationDays = days.toIntOrNull() ?: 5
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text(viewModel.trans("ওষুধ দিন", "Prescribe"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(viewModel.trans("বাতিল", "Cancel"))
            }
        }
    )
}

@Composable
fun AddReportDialog(patientId: String, onDismiss: () -> Unit, onSave: (PatientNoteEntity) -> Unit, viewModel: HospitalViewModel) {
    var reportName by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(viewModel.trans("ল্যাব টেস্ট রিপোর্ট রেকর্ড করুন", "Record Lab Test Report")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = reportName,
                    onValueChange = { reportName = it },
                    label = { Text(viewModel.trans("রিপোর্টের নাম (যেমন: CBC Blood)", "Report Name")) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text(viewModel.trans("ফলাফল / মন্তব্য", "Results / Remarks")) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reportName.isNotEmpty() && summary.isNotEmpty()) {
                        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.US)
                        val stf = SimpleDateFormat("h:mm a", Locale.US)
                        onSave(
                            PatientNoteEntity(
                                patientIdString = patientId,
                                dateString = sdf.format(Date()),
                                timeString = stf.format(Date()),
                                type = "Report",
                                title = "Report Received: $reportName",
                                content = summary
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text(viewModel.trans("সংরক্ষণ", "Save Report"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(viewModel.trans("বাতিল", "Cancel"))
            }
        }
    )
}

@Composable
fun TransferBedDialog(patient: PatientEntity, onDismiss: () -> Unit, onSave: (String, String, String, String) -> Unit, viewModel: HospitalViewModel) {
    var newBed by remember { mutableStateOf("") }
    var newWard by remember { mutableStateOf("Male Ward") }
    var newCabin by remember { mutableStateOf("None") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(viewModel.trans("বেড ও কেবিন স্থানান্তর (Transfer Bed)", "Transfer Patient Bed")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "${viewModel.trans("বর্তমান বেড নম্বর", "Current Bed")}: ${patient.bedNumber} (${patient.ward})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary
                )

                OutlinedTextField(
                    value = newWard,
                    onValueChange = { newWard = it },
                    label = { Text(viewModel.trans("নতুন ওয়ার্ডের নাম", "New Ward Name")) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = newCabin,
                    onValueChange = { newCabin = it },
                    label = { Text(viewModel.trans("নতুন কেবিন নম্বর (যদি থাকে)", "New Cabin (Optional)")) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = newBed,
                    onValueChange = { newBed = it },
                    label = { Text(viewModel.trans("নতুন বেড নম্বর *", "New Bed Number *")) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newBed.isNotEmpty() && newWard.isNotEmpty()) {
                        onSave(patient.bedNumber, newBed, newWard, newCabin)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text(viewModel.trans("স্থানান্তর", "Transfer Bed"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(viewModel.trans("বাতিল", "Cancel"))
            }
        }
    )
}

@Composable
fun DischargePatientDialog(patient: PatientEntity, onDismiss: () -> Unit, onSave: (String, String, String, String, String, String) -> Unit, viewModel: HospitalViewModel) {
    var diagnosis by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var followUp by remember { mutableStateOf("22 July 2026") }
    var summary by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(viewModel.trans("রোগীকে ডিসচার্জ (ছুটি) দিন", "Discharge Patient")) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "${viewModel.trans("ছুটি হচ্ছে", "Discharging")}: ${patient.name}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary
                )

                OutlinedTextField(
                    value = diagnosis,
                    onValueChange = { diagnosis = it },
                    label = { Text(viewModel.trans("চূড়ান্ত রোগ নির্ণয় (Final Diagnosis) *", "Final Diagnosis *")) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text(viewModel.trans("চিকিৎসকের মন্তব্য *", "Doctor's Advice/Remarks *")) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = followUp,
                    onValueChange = { followUp = it },
                    label = { Text(viewModel.trans("ফলো-আপ তারিখ", "Follow-Up Date")) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text(viewModel.trans("ডিসচার্জ সারসংক্ষেপ (Summary)", "Discharge Summary")) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (diagnosis.isNotEmpty() && remarks.isNotEmpty()) {
                        val curDate = SimpleDateFormat("dd MMMM yyyy", Locale.US).format(Date())
                        val curTime = SimpleDateFormat("h:mm a", Locale.US).format(Date())
                        onSave(curDate, curTime, diagnosis, remarks, followUp, summary)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = StatusDischarged)
            ) {
                Text(viewModel.trans("ছুটি দিন ও বেড খালি করুন", "Discharge & Release Bed"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(viewModel.trans("বাতিল", "Cancel"))
            }
        }
    )
}

@Composable
fun BillingUpdateDialog(billing: BillingEntity, onDismiss: () -> Unit, onSave: (BillingEntity) -> Unit, viewModel: HospitalViewModel) {
    var admissionFee by remember { mutableStateOf(billing.admissionFee.toString()) }
    var bedCharge by remember { mutableStateOf(billing.bedCharge.toString()) }
    var doctorFee by remember { mutableStateOf(billing.doctorFee.toString()) }
    var medicineFee by remember { mutableStateOf(billing.medicineFee.toString()) }
    var labTestFee by remember { mutableStateOf(billing.labTestFee.toString()) }
    var discount by remember { mutableStateOf(billing.discount.toString()) }
    var paidAmount by remember { mutableStateOf(billing.paidAmount.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(viewModel.trans("রোগীর বিল বিবরণী সংশোধন", "Modify Billing / Invoice")) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(value = admissionFee, onValueChange = { admissionFee = it }, label = { Text(viewModel.trans("ভর্তি ফি", "Admission Fee")) })
                OutlinedTextField(value = bedCharge, onValueChange = { bedCharge = it }, label = { Text(viewModel.trans("বেড ও কেবিন চার্জ", "Bed / Cabin Charge")) })
                OutlinedTextField(value = doctorFee, onValueChange = { doctorFee = it }, label = { Text(viewModel.trans("ডাক্তার ফি", "Doctor Fee")) })
                OutlinedTextField(value = medicineFee, onValueChange = { medicineFee = it }, label = { Text(viewModel.trans("ওষুধের বিল", "Medicine Fee")) })
                OutlinedTextField(value = labTestFee, onValueChange = { labTestFee = it }, label = { Text(viewModel.trans("ল্যাব পরীক্ষা ফি", "Lab Test Fee")) })
                OutlinedTextField(value = discount, onValueChange = { discount = it }, label = { Text(viewModel.trans("ডিসকাউন্ট (ছাড়)", "Discount")) })
                OutlinedTextField(value = paidAmount, onValueChange = { paidAmount = it }, label = { Text(viewModel.trans("পরিশোধিত টাকা (Paid)", "Amount Paid")) })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val addF = admissionFee.toDoubleOrNull() ?: 0.0
                    val bedC = bedCharge.toDoubleOrNull() ?: 0.0
                    val docF = doctorFee.toDoubleOrNull() ?: 0.0
                    val medF = medicineFee.toDoubleOrNull() ?: 0.0
                    val labF = labTestFee.toDoubleOrNull() ?: 0.0
                    val disc = discount.toDoubleOrNull() ?: 0.0
                    val paid = paidAmount.toDoubleOrNull() ?: 0.0
                    
                    val total = addF + bedC + docF + medF + labF - disc
                    val due = total - paid

                    onSave(
                        billing.copy(
                            admissionFee = addF,
                            bedCharge = bedC,
                            doctorFee = docF,
                            medicineFee = medF,
                            labTestFee = labF,
                            discount = disc,
                            totalAmount = total,
                            paidAmount = paid,
                            dueAmount = due
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text(viewModel.trans("সংরক্ষণ", "Save Billing"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(viewModel.trans("বাতিল", "Cancel"))
            }
        }
    )
}

// -------------------------------------------------------------
// REPORTS SCREEN
// -------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: HospitalViewModel) {
    val patients by viewModel.allPatients.collectAsState()
    var activeReportTab by remember { mutableStateOf(0) } // 0: Daily, 1: Disease, 2: Billing

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.navigateTo(Screen.Dashboard) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TealPrimary)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = viewModel.trans("হাসপাতাল রিপোর্টস", "Reports & Analytics"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF1E293B)
                    )
                }
            }
        },
        bottomBar = {
            BentoBottomNavigation(
                currentScreen = Screen.Reports,
                onNavigate = { viewModel.navigateTo(it) },
                viewModel = viewModel
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F9FC)) // Soft Bento background
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Report Tabs
            TabRow(selectedTabIndex = activeReportTab, containerColor = MaterialTheme.colorScheme.surface, contentColor = TealPrimary) {
                Tab(selected = activeReportTab == 0, onClick = { activeReportTab = 0 }, text = { Text(viewModel.trans("দৈনিক রিপোর্ট", "Daily"), fontWeight = FontWeight.Bold) })
                Tab(selected = activeReportTab == 1, onClick = { activeReportTab = 1 }, text = { Text(viewModel.trans("রোগভিত্তিক", "Disease-wise"), fontWeight = FontWeight.Bold) })
                Tab(selected = activeReportTab == 2, onClick = { activeReportTab = 2 }, text = { Text(viewModel.trans("আর্থিক বিলিং", "Financial"), fontWeight = FontWeight.Bold) })
            }

            when (activeReportTab) {
                0 -> {
                    // Daily metrics
                    val sdfDate = viewModel.selectedDate.value
                    val dayPatients = patients.filter { it.admissionDate == sdfDate }
                    val adm = dayPatients.count { it.status != "Discharged" }
                    val dis = dayPatients.count { it.status == "Discharged" }
                    val activeTotal = patients.count { it.status != "Discharged" }

                    Text(text = "📅 $sdfDate - " + viewModel.trans("আজকের পরিসংখ্যান", "Daily Statistics"), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReportMetricCard(title = viewModel.trans("আজ ভর্তি", "Admitted"), value = "$adm জন", color = StatusAdmitted, modifier = Modifier.weight(1f))
                        ReportMetricCard(title = viewModel.trans("আজ ছুটি", "Discharged"), value = "$dis জন", color = StatusDischarged, modifier = Modifier.weight(1f))
                        ReportMetricCard(title = viewModel.trans("হাসপাতালে ভর্তি", "Total Active"), value = "$activeTotal জন", color = TealPrimary, modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = viewModel.trans("ভর্তি রোগীর ট্রেন্ড (Line Progress)", "Daily Admission Load"), fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    // Display custom mock chart progress indicators
                    MockChartProgressBar(label = "8:00 AM - 12:00 PM", value = 0.65f, valueText = "12 admissions", color = StatusAdmitted)
                    MockChartProgressBar(label = "12:00 PM - 4:00 PM", value = 0.85f, valueText = "18 admissions", color = StatusICU)
                    MockChartProgressBar(label = "4:00 PM - 8:00 PM", value = 0.35f, valueText = "6 admissions", color = StatusObservation)
                }

                1 -> {
                    // Disease-wise metrics
                    val diseases = patients.groupBy { it.diseaseName }.mapValues { it.value.size }.toList().sortedByDescending { it.second }
                    Text(text = viewModel.trans("🩺 শীর্ষ রোগের পরিসংখ্যান", "🩺 Top Diseases Trend"), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TealPrimary)

                    if (diseases.isEmpty()) {
                        Text("No diseases recorded yet.")
                    } else {
                        diseases.take(5).forEach { item ->
                            val percent = if (patients.isNotEmpty()) item.second.toFloat() / patients.size else 0f
                            MockChartProgressBar(
                                label = item.first.ifEmpty { "General Symptoms" },
                                value = percent,
                                valueText = "${item.second} cases",
                                color = StatusAdmitted
                            )
                        }
                    }
                }

                2 -> {
                    // Billing and due amounts
                    val activeCount = patients.count { it.status != "Discharged" }
                    val totalReceivables = activeCount * 4500.0
                    val totalCollected = activeCount * 3000.0
                    val totalDue = totalReceivables - totalCollected

                    Text(text = viewModel.trans("💰 আর্থিক ইনভয়েস ট্র্যাকিং", "💰 Financial Revenue Summary"), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TealPrimary)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = TealPrimary.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            BillRow(label = viewModel.trans("প্রাক্কলিত মোট আয়", "Estimated Revenue"), amount = totalReceivables)
                            BillRow(label = viewModel.trans("আদায়কৃত বিল", "Collected Cash"), amount = totalCollected)
                            BillRow(label = viewModel.trans("বকেয়া বিল পরিমাণ", "Total Outstanding Due"), amount = totalDue)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Export Actions
            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("pdf_export_button"),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PictureAsPdf, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(viewModel.trans("পিডিএফ রিপোর্ট এক্সপোর্ট করুন (PDF Export)", "Export PDF Report"), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ReportMetricCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun MockChartProgressBar(label: String, value: Float, valueText: String, color: Color) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(text = valueText, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { value },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
    }
}

// -------------------------------------------------------------
// STAFF MANAGEMENT SCREEN
// -------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffManagementScreen(viewModel: HospitalViewModel) {
    val staffList by viewModel.allStaff.collectAsState()
    var showAddStaffDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(viewModel.trans("👥 স্টাফ ও কর্মী ব্যবস্থাপনা", "👥 Roster & Staff")) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.Dashboard) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddStaffDialog = true }) {
                        Icon(Icons.Default.Add, "Add Staff", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TealPrimary, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = viewModel.trans("ডাক্তার ও নার্স তালিকা", "Active Roster Staff"),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TealPrimary
            )

            if (staffList.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(viewModel.trans("কোনো স্টাফ মেম্বার যোগ করা হয়নি।", "No staff members registered."))
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(staffList) { staff ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(TealPrimary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (staff.role == "Doctor") Icons.Default.LocalHospital else Icons.Default.Person,
                                            contentDescription = null,
                                            tint = TealPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = staff.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(text = "${staff.role} ${if (staff.specialty != null) "- ${staff.specialty}" else ""}", fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                                IconButton(onClick = { viewModel.removeStaff(staff) }) {
                                    Icon(Icons.Default.Delete, "Remove", tint = StatusDischarged)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddStaffDialog) {
        var name by remember { mutableStateOf("") }
        var role by remember { mutableStateOf("Doctor") }
        var spec by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddStaffDialog = false },
            title = { Text(viewModel.trans("নতুন কর্মী যোগ করুন", "Add Staff Member")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(viewModel.trans("নাম", "Name")) })
                    Row {
                        FilterChip(selected = role == "Doctor", onClick = { role = "Doctor" }, label = { Text("Doctor") })
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(selected = role == "Nurse", onClick = { role = "Nurse" }, label = { Text("Nurse") })
                    }
                    if (role == "Doctor") {
                        OutlinedTextField(value = spec, onValueChange = { spec = it }, label = { Text(viewModel.trans("স্পেশালিটি (যেমন: মেডিসিন)", "Specialty")) })
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotEmpty()) {
                            viewModel.addStaff(StaffEntity(name = name, role = role, specialty = if (role == "Doctor") spec else null))
                            showAddStaffDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text(viewModel.trans("সংরক্ষণ", "Save"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStaffDialog = false }) {
                    Text(viewModel.trans("বাতিল", "Cancel"))
                }
            }
        )
    }
}

// -------------------------------------------------------------
// SETTINGS SCREEN
// -------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: HospitalViewModel) {
    val themeMode by viewModel.themeMode.collectAsState()
    val language by viewModel.languageMode.collectAsState()
    val hospital by viewModel.hospitalInfo.collectAsState()

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.navigateTo(Screen.Dashboard) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TealPrimary)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = viewModel.trans("সিস্টেম সেটিংস", "Control Panel Settings"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF1E293B)
                    )
                }
            }
        },
        bottomBar = {
            BentoBottomNavigation(
                currentScreen = Screen.Settings,
                onNavigate = { viewModel.navigateTo(it) },
                viewModel = viewModel
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F9FC)) // Soft Bento background
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(text = viewModel.trans("⚙️ সাধারণ সেটিংস (Settings)", "⚙️ App Customization"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TealPrimary)

            // Theme Switcher
            Card(shape = RoundedCornerShape(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = viewModel.trans("অ্যাপ থিম", "Theme"), fontWeight = FontWeight.Bold)
                        Text(text = viewModel.trans("ডার্ক/লাইট মোড পরিবর্তন", "Toggle Light & Dark view mode"), fontSize = 12.sp, color = Color.Gray)
                    }
                    Row {
                        FilterChip(selected = themeMode == "Light", onClick = { viewModel.themeMode.value = "Light" }, label = { Text("Light") })
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(selected = themeMode == "Dark", onClick = { viewModel.themeMode.value = "Dark" }, label = { Text("Dark") })
                    }
                }
            }

            // Language Switcher
            Card(shape = RoundedCornerShape(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = viewModel.trans("ভাষা পরিবর্তন", "Language"), fontWeight = FontWeight.Bold)
                        Text(text = viewModel.trans("বাংলা বা ইংরেজি নির্বাচন", "Toggle Bangla & English layout"), fontSize = 12.sp, color = Color.Gray)
                    }
                    Row {
                        FilterChip(selected = language == "Bangla", onClick = { viewModel.languageMode.value = "Bangla" }, label = { Text("বাংলা") })
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(selected = language == "English", onClick = { viewModel.languageMode.value = "English" }, label = { Text("English") })
                    }
                }
            }

            // Backup & Restore Block
            Card(shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = viewModel.trans("💾 ডেটা ব্যাকআপ ও রিস্টোর", "💾 Database Sync"), fontWeight = FontWeight.Bold)
                    Text(text = viewModel.trans("প্রতিদিন স্বয়ংক্রিয়ভাবে ব্যাকআপ সম্পন্ন হবে অথবা লোকাল ব্যাকআপ নিতে পারেন।", "Local file backup saves database instantly in JSON structure."), fontSize = 12.sp, color = Color.Gray)
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {},
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                        ) {
                            Icon(Icons.Default.CloudUpload, null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(viewModel.trans("ব্যাকআপ", "Backup"), fontSize = 12.sp)
                        }
                        Button(
                            onClick = {},
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusObservation)
                        ) {
                            Icon(Icons.Default.CloudDownload, null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(viewModel.trans("রিস্টোর", "Restore"), fontSize = 12.sp)
                        }
                    }
                }
            }

            // About Hospital Details Read-Only Summary
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = TealPrimary.copy(alpha = 0.05f))) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "🏥 " + (hospital?.name ?: "NexHospital"), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TealPrimary)
                    Text(text = "${viewModel.trans("লাইসেন্স", "Lic")}: ${hospital?.licenseNumber ?: "N/A"}", fontSize = 13.sp)
                    Text(text = "${viewModel.trans("ঠিকানা", "Addr")}: ${hospital?.address ?: "N/A"}", fontSize = 13.sp)
                    Text(text = "${viewModel.trans("হেল্পলাইন", "Call")}: ${hospital?.phone ?: "N/A"}", fontSize = 13.sp)
                }
            }

            // ---------------------------------------------------------
            // About Developer & Company Section
            // ---------------------------------------------------------
            val uriHandler = LocalUriHandler.current

            Text(
                text = viewModel.trans("ℹ️ ডেভেলপার ও কোম্পানি সম্পর্কে", "ℹ️ About Developer & Company"),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TealPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Developer Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(TealPrimary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = "Developer Logo",
                                tint = TealPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Prince AR Abdur Rahman",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = viewModel.trans("স্বাধীন অ্যাপ ডেভেলপার", "Independent App Developer"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TealPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Independent App Developer passionate about building modern Android applications, productivity tools, AI-powered experiences, media players, educational apps, and next-generation digital products.",
                        fontSize = 13.sp,
                        color = Color(0xFF475569),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Divider(color = Color(0xFFF1F5F9))

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = viewModel.trans("যোগাযোগ ও সোশ্যাল মিডিয়া:", "Contact & Social Media:"),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Contact Row with modern chip-like buttons
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // WhatsApp 1
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFECFDF5))
                                    .clickable {
                                        try { uriHandler.openUri("https://wa.me/8801707424006") } catch (e: Exception) {}
                                    }
                                    .padding(vertical = 10.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Phone, null, tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("WhatsApp 1", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                            }

                            // WhatsApp 2
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFECFDF5))
                                    .clickable {
                                        try { uriHandler.openUri("https://wa.me/8801796951709") } catch (e: Exception) {}
                                    }
                                    .padding(vertical = 10.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Phone, null, tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("WhatsApp 2", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Facebook
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFEFF6FF))
                                    .clickable {
                                        try { uriHandler.openUri("https://www.facebook.com/share/1BNn32qoJo/") } catch (e: Exception) {}
                                    }
                                    .padding(vertical = 10.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Link, null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Facebook", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E40AF))
                            }

                            // Instagram
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFFF1F2))
                                    .clickable {
                                        try { uriHandler.openUri("https://www.instagram.com/ur___abdur____rahman__2008") } catch (e: Exception) {}
                                    }
                                    .padding(vertical = 10.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.CameraAlt, null, tint = Color(0xFFE11D48), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Instagram", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9F1239))
                            }
                        }
                    }
                }
            }

            // Company Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(StatusAdmitted.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = "Company Logo",
                                tint = StatusAdmitted,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "NexVora Lab's Ofc",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = viewModel.trans("উদ্ভাবনী অ্যাপস ল্যাব", "Innovative Apps Lab"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StatusAdmitted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "NexVora Lab's Ofc focuses on creating innovative Android applications designed to improve productivity, entertainment, learning, and digital experiences.",
                        fontSize = 13.sp,
                        color = Color(0xFF475569),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Flag, null, tint = StatusAdmitted, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = viewModel.trans("আমাদের লক্ষ্য (Mission):", "Our Mission:"),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF334155)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Build fast, beautiful, privacy-friendly, and user-focused applications accessible to everyone.",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Tech Info & Credits Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = viewModel.trans("কারিগরি তথ্য (Technical Specs)", "Technical Specs & Credits"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF1E293B)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = viewModel.trans("সংস্করণ", "Version"), fontSize = 13.sp, color = Color(0xFF64748B))
                        Text(text = "1.0.0", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = viewModel.trans("ডেভেলপার", "Developed By"), fontSize = 13.sp, color = Color(0xFF64748B))
                        Text(text = "Prince AR Abdur Rahman", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = viewModel.trans("প্রকাশক", "Publisher"), fontSize = 13.sp, color = Color(0xFF64748B))
                        Text(text = "NexVora Lab's Ofc", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Divider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "© 2026 NexVora Lab's Ofc. All Rights Reserved.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
