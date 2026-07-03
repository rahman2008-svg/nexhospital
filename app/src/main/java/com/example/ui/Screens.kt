package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent(viewModel: HospitalViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val language by viewModel.languageMode.collectAsState()

    MyApplicationTheme(darkTheme = themeMode == "Dark") {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                when (screen) {
                    is Screen.Welcome -> WelcomeScreen(viewModel)
                    is Screen.SetupHospital -> SetupHospitalScreen(viewModel)
                    is Screen.SetupAdmin -> SetupAdminScreen(viewModel)
                    is Screen.Login -> LoginScreen(viewModel)
                    is Screen.Dashboard -> MainDashboardScreen(viewModel)
                    is Screen.PatientList -> PatientListScreen(viewModel)
                    is Screen.AddPatient -> AddEditPatientScreen(viewModel, null)
                    is Screen.EditPatient -> AddEditPatientScreen(viewModel, screen.patient)
                    is Screen.PatientProfile -> PatientProfileScreen(viewModel, screen.patient)
                    is Screen.Reports -> ReportsScreen(viewModel)
                    is Screen.StaffManagement -> StaffManagementScreen(viewModel)
                    is Screen.Settings -> SettingsScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun WelcomeScreen(viewModel: HospitalViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        Text(
            text = "NexHospital",
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold,
            color = TealPrimary,
            modifier = Modifier.testTag("welcome_title")
        )
        
        Text(
            text = viewModel.trans("হাসপাতাল পেশেন্ট ম্যানেজমেন্ট সিস্টেম", "Hospital Patient Management System"),
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 8.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Image container with rounded corners and shadow
        Card(
            modifier = Modifier
                .size(240.dp)
                .testTag("welcome_image_card"),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_hospital_onboarding),
                contentDescription = "Hospital Illustration",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { viewModel.navigateTo(Screen.SetupHospital) },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp)
                .testTag("get_started_button"),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = viewModel.trans("শুরু করুন", "Get Started"),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Next",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { viewModel.skipAndLoadDemoData() },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp)
                .testTag("demo_data_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TealPrimary),
            border = BorderStroke(1.5.dp, TealPrimary)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = "Demo",
                    tint = TealPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = viewModel.trans("ডেমো ডেটা লোড করুন (দ্রুত পরীক্ষা)", "Load Demo Data (Quick Test)"),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupHospitalScreen(viewModel: HospitalViewModel) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("General Hospital") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    var license by remember { mutableStateOf("") }

    var expandedType by remember { mutableStateOf(false) }
    val types = listOf("General Hospital", "Clinic", "Specialized Hospital", "Diagnostic Center", "Nursing Home")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(viewModel.trans("হাসপাতালের তথ্য সেটআপ", "Hospital Setup")) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.Welcome) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = viewModel.trans("আপনার হাসপাতালের তথ্য প্রদান করুন", "Enter your hospital details"),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TealPrimary
            )
            Text(
                text = viewModel.trans("এই তথ্যগুলো প্রেসক্রিপশন ও রিপোর্টে প্রিন্ট হবে।", "This information will be printed on reports & prescriptions."),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(viewModel.trans("হাসপাতালের নাম *", "Hospital Name *")) },
                modifier = Modifier.fillMaxWidth().testTag("setup_hospital_name"),
                leadingIcon = { Icon(Icons.Default.Home, null) }
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = type,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(viewModel.trans("হাসপাতালের ধরন", "Hospital Type")) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.LocalHospital, null) },
                    trailingIcon = {
                        IconButton(onClick = { expandedType = true }) {
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                    }
                )
                DropdownMenu(
                    expanded = expandedType,
                    onDismissRequest = { expandedType = false },
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    types.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                type = item
                                expandedType = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text(viewModel.trans("ঠিকানা *", "Address *")) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Place, null) }
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text(viewModel.trans("মোবাইল নম্বর *", "Phone Number *")) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Phone, null) }
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Email, null) }
            )

            OutlinedTextField(
                value = website,
                onValueChange = { website = it },
                label = { Text("Website") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Public, null) }
            )

            OutlinedTextField(
                value = license,
                onValueChange = { license = it },
                label = { Text(viewModel.trans("লাইসেন্স নম্বর", "License Number")) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Description, null) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (name.isNotEmpty() && address.isNotEmpty() && phone.isNotEmpty()) {
                        viewModel.saveHospitalSetup(
                            HospitalEntity(
                                name = name,
                                type = type,
                                address = address,
                                phone = phone,
                                email = email,
                                website = website,
                                licenseNumber = license
                            )
                        )
                    }
                },
                enabled = name.isNotEmpty() && address.isNotEmpty() && phone.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_hospital_button"),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = viewModel.trans("সংরক্ষণ করুন ও পরবর্তী ধাপ", "Save & Continue"),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupAdminScreen(viewModel: HospitalViewModel) {
    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(viewModel.trans("অ্যাডমিন অ্যাকাউন্ট তৈরি", "Create Admin Account")) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TealPrimary, titleContentColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = viewModel.trans("মাস্টার অ্যাডমিন তথ্য দিন", "Enter Master Admin Credentials"),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TealPrimary
            )
            Text(
                text = viewModel.trans("এই অ্যাকাউন্টটি দিয়ে হাসপাতালের সকল তথ্য নিয়ন্ত্রণ করা যাবে।", "This account has full control over the system."),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(viewModel.trans("সম্পূর্ণ নাম *", "Full Name *")) },
                modifier = Modifier.fillMaxWidth().testTag("setup_admin_name"),
                leadingIcon = { Icon(Icons.Default.Person, null) }
            )

            OutlinedTextField(
                value = mobile,
                onValueChange = { mobile = it },
                label = { Text(viewModel.trans("মোবাইল নম্বর *", "Mobile Number *")) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Phone, null) }
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Email, null) }
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(viewModel.trans("পাসওয়ার্ড *", "Password *")) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Lock, null) }
            )

            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 4) pin = it },
                label = { Text(viewModel.trans("৪ ডিজিট পিন *", "4-Digit PIN *")) },
                placeholder = { Text("e.g. 1234") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Dialpad, null) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (name.isNotEmpty() && mobile.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && pin.length == 4) {
                        viewModel.saveAdminSetup(
                            AdminEntity(
                                name = name,
                                mobile = mobile,
                                email = email,
                                password = password,
                                pin = pin,
                                fingerprintEnabled = true
                            )
                        )
                    }
                },
                enabled = name.isNotEmpty() && mobile.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && pin.length == 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_admin_button"),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = viewModel.trans("অ্যাকাউন্ট তৈরি করুন", "Create Account"),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: HospitalViewModel) {
    val admin by viewModel.adminAccount.collectAsState()
    val hospital by viewModel.hospitalInfo.collectAsState()
    var pinInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var usePasswordLogin by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = hospital?.name ?: "NexHospital",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TealPrimary,
            textAlign = TextAlign.Center
        )
        Text(
            text = viewModel.trans("নিরাপদ প্যানেল লগইন", "Secure Portal Login"),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Profile avatar
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(TealPrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Lock, contentDescription = "Security", modifier = Modifier.size(48.dp), tint = TealPrimary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = admin?.name ?: "Administrator",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = admin?.email ?: "admin@hospital.com",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(30.dp))

        if (loginError) {
            Text(
                text = viewModel.trans("ভুল পিন বা পাসওয়ার্ড! আবার চেষ্টা করুন।", "Invalid PIN or Password! Try again."),
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        if (!usePasswordLogin) {
            // PIN Login Flow
            Text(
                text = viewModel.trans("৪ ডিজিট পিন দিন", "Enter 4-Digit PIN"),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Draw Dots representing entered PIN length
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                for (i in 1..4) {
                    val active = pinInput.length >= i
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(if (active) TealPrimary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Elegant Custom Number Pad
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val numGrid = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("Fingerprint", "0", "Delete")
                )
                for (row in numGrid) {
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        for (cell in row) {
                            if (cell == "Fingerprint") {
                                // Fingerprint Button
                                IconButton(
                                    onClick = {
                                        // Mock successful Fingerprint
                                        pinInput = admin?.pin ?: "1234"
                                        if (pinInput == admin?.pin) {
                                            viewModel.navigateTo(Screen.Dashboard)
                                        }
                                    },
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(TealPrimary.copy(alpha = 0.1f))
                                ) {
                                    Icon(Icons.Default.Fingerprint, "Fingerprint", tint = TealPrimary)
                                }
                            } else if (cell == "Delete") {
                                // Delete Button
                                IconButton(
                                    onClick = { if (pinInput.isNotEmpty()) pinInput = pinInput.dropLast(1) },
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
                                ) {
                                    Icon(Icons.Default.Backspace, "Delete")
                                }
                            } else {
                                // Standard digit button
                                Button(
                                    onClick = {
                                        if (pinInput.length < 4) {
                                            pinInput += cell
                                            if (pinInput.length == 4) {
                                                if (pinInput == admin?.pin) {
                                                    loginError = false
                                                    viewModel.navigateTo(Screen.Dashboard)
                                                } else {
                                                    loginError = true
                                                    pinInput = ""
                                                }
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    shape = CircleShape,
                                    modifier = Modifier.size(64.dp)
                                ) {
                                    Text(text = cell, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            TextButton(
                onClick = { usePasswordLogin = true },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(viewModel.trans("পাসওয়ার্ড দিয়ে লগইন করুন", "Login with Password"), color = TealPrimary)
            }
        } else {
            // Password login
            OutlinedTextField(
                value = passwordInput,
                onValueChange = { passwordInput = it },
                label = { Text(viewModel.trans("অ্যাডমিন পাসওয়ার্ড", "Admin Password")) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().testTag("login_password_input"),
                leadingIcon = { Icon(Icons.Default.Lock, null) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (passwordInput == admin?.password) {
                        loginError = false
                        viewModel.navigateTo(Screen.Dashboard)
                    } else {
                        loginError = true
                        passwordInput = ""
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("login_submit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(viewModel.trans("লগইন", "Log In"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            TextButton(
                onClick = { usePasswordLogin = false },
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Text(viewModel.trans("পিন দিয়ে লগইন করুন", "Login with PIN"), color = TealPrimary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(viewModel: HospitalViewModel) {
    val patients by viewModel.allPatients.collectAsState()
    val hospital by viewModel.hospitalInfo.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    // Computations based on current date (e.g. 15 July 2026)
    val dayPatients = patients.filter { it.admissionDate == selectedDate }
    val admittedCount = dayPatients.count { it.status != "Discharged" }
    val dischargedCount = dayPatients.count { it.status == "Discharged" }
    
    val totalActivePatients = patients.count { it.status != "Discharged" }
    val icuPatients = patients.count { it.status == "ICU" }
    
    val totalBeds = 300
    val occupiedBeds = totalActivePatients
    val freeBeds = totalBeds - occupiedBeds

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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = hospital?.name ?: "NexHospital",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = viewModel.trans("পেশেন্ট ট্র্যাকিং সিস্টেম", "Patient Tracking System"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B)
                        )
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.navigateTo(Screen.Reports) }) {
                            Icon(Icons.Default.Assessment, "Reports", tint = TealPrimary)
                        }
                        IconButton(onClick = { viewModel.navigateTo(Screen.StaffManagement) }) {
                            Icon(Icons.Default.Group, "Staff", tint = TealPrimary)
                        }
                        IconButton(onClick = { viewModel.navigateTo(Screen.Settings) }) {
                            Icon(Icons.Default.Settings, "Settings", tint = TealPrimary)
                        }
                        IconButton(onClick = { viewModel.navigateTo(Screen.Login) }) {
                            Icon(Icons.Default.ExitToApp, "Logout", tint = Color(0xFFEF4444))
                        }
                    }
                }
            }
        },
        bottomBar = {
            BentoBottomNavigation(
                currentScreen = Screen.Dashboard,
                onNavigate = { viewModel.navigateTo(it) },
                viewModel = viewModel
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F9FC)) // Bento Soft background
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Bento Search Bar (Interactive entry to patient list)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(Screen.PatientList) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = viewModel.trans("রোগী বা বেড খুঁজুন...", "Search patients or beds..."),
                        fontSize = 14.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // Large Status Card (Currently Admitted)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = TealPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = viewModel.trans("বর্তমানে ভর্তি", "Currently Admitted"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Monitoring",
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = totalActivePatients.toString(),
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = viewModel.trans("জন রোগী", "Patients"),
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }
            }

            // 2x2 Bento Stats Grid
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BentoStatCard(
                        title = viewModel.trans("আজ ভর্তি", "Admitted Today"),
                        value = admittedCount.toString(),
                        bg = BentoGreenBg,
                        border = BentoGreenBorder,
                        textColor = BentoGreenText,
                        icon = Icons.Default.PersonAdd,
                        modifier = Modifier.weight(1f)
                    )
                    BentoStatCard(
                        title = viewModel.trans("আজ ছুটি", "Discharged Today"),
                        value = dischargedCount.toString(),
                        bg = BentoRoseBg,
                        border = BentoRoseBorder,
                        textColor = BentoRoseText,
                        icon = Icons.Default.ExitToApp,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BentoStatCard(
                        title = viewModel.trans("খালি বেড", "Beds Available"),
                        value = freeBeds.toString(),
                        bg = BentoOrangeBg,
                        border = BentoOrangeBorder,
                        textColor = BentoOrangeText,
                        icon = Icons.Default.Bed,
                        modifier = Modifier.weight(1f)
                    )
                    BentoStatCard(
                        title = viewModel.trans("ICU বুকড", "ICU Booked"),
                        value = icuPatients.toString(),
                        bg = BentoPurpleBg,
                        border = BentoPurpleBorder,
                        textColor = BentoPurpleText,
                        icon = Icons.Default.LocalHospital,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Bento July 2026 Calendar Grid Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = viewModel.trans("📅 জুলাই ২০২৬ ক্যালেন্ডার", "📅 July 2026 Calendar"),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    // Week Days
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        val days = listOf("S", "M", "T", "W", "T", "F", "S")
                        days.forEach { d ->
                            Text(
                                text = d,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Calendar Grid July 2026 (Starts on Wednesday July 1st)
                    val emptySlots = 3
                    val totalDays = 31

                    var dayCounter = 1
                    for (week in 0..5) {
                        if (dayCounter > totalDays) break
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            for (dayOfWeek in 0..6) {
                                val index = week * 7 + dayOfWeek
                                if (index < emptySlots || dayCounter > totalDays) {
                                    Spacer(modifier = Modifier.weight(1f))
                                } else {
                                    val currentDayVal = dayCounter
                                    val dateStr = "$currentDayVal July 2026"
                                    val isSelected = dateStr == selectedDate
                                    
                                    // Count metrics for indicators
                                    val countAdmitted = patients.count { it.admissionDate == dateStr && it.status != "Discharged" }
                                    val countDischarged = patients.count { it.admissionDate == dateStr && it.status == "Discharged" }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) TealPrimary else if (countAdmitted > 0 || countDischarged > 0) TealPrimary.copy(alpha = 0.08f) else Color.Transparent
                                            )
                                            .clickable { viewModel.selectedDate.value = dateStr }
                                            .testTag("calendar_day_$currentDayVal"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = currentDayVal.toString(),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else Color(0xFF334155)
                                            )
                                            // Tiny indicators for admissions and discharges
                                            if (countAdmitted > 0 || countDischarged > 0) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                    modifier = Modifier.padding(top = 1.dp)
                                                ) {
                                                    if (countAdmitted > 0) {
                                                        Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) Color.White else StatusAdmitted))
                                                    }
                                                    if (countDischarged > 0) {
                                                        Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) Color.White else StatusDischarged))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    dayCounter++
                                }
                            }
                        }
                    }
                }
            }

            // Bento Recent Patient List Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = viewModel.trans("আজকের নতুন রোগী ($selectedDate)", "New Patients ($selectedDate)"),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        TextButton(
                            onClick = { viewModel.navigateTo(Screen.PatientList) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = viewModel.trans("সব দেখুন", "View All"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    if (dayPatients.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.HourglassEmpty,
                                    contentDescription = "Empty",
                                    modifier = Modifier.size(40.dp),
                                    tint = Color(0xFF94A3B8)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = viewModel.trans("এই তারিখে কোনো রোগীর কার্যক্রম নেই।", "No patient activities on this date."),
                                    color = Color(0xFF94A3B8),
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            dayPatients.take(3).forEach { patient ->
                                PatientSmallRowCard(patient, viewModel)
                            }
                            if (dayPatients.size > 3) {
                                Text(
                                    text = viewModel.trans("এবং আরও ${dayPatients.size - 3} জন...", "and ${dayPatients.size - 3} more..."),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BentoBottomNavigation(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    viewModel: HospitalViewModel
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        color = Color.White,
        shadowElevation = 16.dp,
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                label = viewModel.trans("হোম", "Home"),
                icon = Icons.Default.GridView,
                isSelected = currentScreen is Screen.Dashboard,
                onClick = { onNavigate(Screen.Dashboard) }
            )
            
            BottomNavItem(
                label = viewModel.trans("রোগী", "Patients"),
                icon = Icons.Default.Group,
                isSelected = currentScreen is Screen.PatientList,
                onClick = { onNavigate(Screen.PatientList) }
            )

            Box(
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                FloatingActionButton(
                    onClick = { onNavigate(Screen.AddPatient) },
                    containerColor = TealPrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(54.dp).testTag("add_patient_fab")
                ) {
                    Icon(Icons.Default.Add, "Add Patient", modifier = Modifier.size(28.dp))
                }
            }

            BottomNavItem(
                label = viewModel.trans("রিপোর্ট", "Reports"),
                icon = Icons.Default.BarChart,
                isSelected = currentScreen is Screen.Reports,
                onClick = { onNavigate(Screen.Reports) }
            )

            BottomNavItem(
                label = viewModel.trans("সেটিংস", "Settings"),
                icon = Icons.Default.Settings,
                isSelected = currentScreen is Screen.Settings,
                onClick = { onNavigate(Screen.Settings) }
            )
        }
    }
}

@Composable
fun RowScope.BottomNavItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(
                onClick = onClick,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) TealPrimary else Color(0xFF94A3B8),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) TealPrimary else Color(0xFF94A3B8)
        )
    }
}

@Composable
fun BentoStatCard(
    title: String,
    value: String,
    bg: Color,
    border: Color,
    textColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, border),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(border),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = value,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
        }
    }
}

@Composable
fun PatientSmallRowCard(patient: PatientEntity, viewModel: HospitalViewModel) {
    val statusColor = when (patient.status) {
        "Admitted" -> StatusAdmitted
        "ICU" -> StatusICU
        "Observation" -> StatusObservation
        "Emergency" -> StatusEmergency
        "Discharged" -> StatusDischarged
        else -> StatusGray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.selectPatient(patient) }
            .testTag("patient_row_card_${patient.id}"),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Status Stripe Indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(50.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(statusColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = patient.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = patient.patientIdString,
                        fontSize = 11.sp,
                        color = TealPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "🏥 ${patient.ward} ${if (patient.bedNumber.isNotEmpty()) "(${patient.bedNumber})" else ""}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "🩺 ${patient.diseaseName}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = { viewModel.selectPatient(patient) }) {
                Icon(Icons.Default.ChevronRight, "View Details", tint = TealPrimary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientListScreen(viewModel: HospitalViewModel) {
    val patients by viewModel.allPatients.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val selectedSort by viewModel.selectedSort.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }

    // Filtering & Sorting
    val filteredPatients = patients.filter { p ->
        // Search query
        val matchesSearch = p.name.contains(searchQuery, ignoreCase = true) ||
                p.mobile.contains(searchQuery, ignoreCase = true) ||
                p.patientIdString.contains(searchQuery, ignoreCase = true) ||
                p.doctorName.contains(searchQuery, ignoreCase = true) ||
                p.bedNumber.contains(searchQuery, ignoreCase = true) ||
                p.diseaseName.contains(searchQuery, ignoreCase = true)

        // Status / Ward filters
        val matchesFilter = when (selectedFilter) {
            "All" -> true
            "Admitted" -> p.status == "Admitted"
            "Discharged" -> p.status == "Discharged"
            "ICU" -> p.status == "ICU"
            "Cabin" -> p.ward.contains("Cabin", ignoreCase = true) || p.cabin != "None"
            "General Ward" -> p.ward.contains("Ward", ignoreCase = true)
            "Emergency" -> p.status == "Emergency"
            "Male" -> p.gender.equals("Male", ignoreCase = true)
            "Female" -> p.gender.equals("Female", ignoreCase = true)
            "Child" -> p.age <= 12
            else -> true
        }

        matchesSearch && matchesFilter
    }.sortedWith { a, b ->
        when (selectedSort) {
            "Name" -> a.name.compareTo(b.name, ignoreCase = true)
            "Patient ID" -> a.patientIdString.compareTo(b.patientIdString, ignoreCase = true)
            "Bed Number" -> a.bedNumber.compareTo(b.bedNumber, ignoreCase = true)
            "Doctor" -> a.doctorName.compareTo(b.doctorName, ignoreCase = true)
            "Disease" -> a.diseaseName.compareTo(b.diseaseName, ignoreCase = true)
            else -> b.id.compareTo(a.id) // Default: Admission/Registration ID Descending
        }
    }

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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.navigateTo(Screen.Dashboard) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TealPrimary)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = viewModel.trans("রোগীদের তালিকা", "Patients Directory"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF1E293B)
                        )
                    }
                    
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.Sort, "Sort", tint = TealPrimary)
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            val sorts = listOf("Admission Time", "Name", "Patient ID", "Bed Number", "Doctor", "Disease")
                            sorts.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item) },
                                    onClick = {
                                        viewModel.selectedSort.value = item
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            BentoBottomNavigation(
                currentScreen = Screen.PatientList,
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Bento styled Search Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchQuery.value = it },
                        placeholder = { Text(viewModel.trans("নাম, মোবাইল, আইডি, ডাক্তার, রোগ বা বেড...", "Search by name, mobile, ID, doctor, disease..."), fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_patient_box"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                }
            }

            // Horizontal Filters Strip
            val filterOptions = listOf(
                Pair("All", viewModel.trans("সব রোগী", "All Patients")),
                Pair("Admitted", viewModel.trans("ভর্তি রোগী", "Admitted")),
                Pair("Discharged", viewModel.trans("ছুটি হয়েছে", "Discharged")),
                Pair("ICU", "ICU"),
                Pair("Cabin", "Cabin"),
                Pair("General Ward", viewModel.trans("জেনারেল ওয়ার্ড", "General Ward")),
                Pair("Emergency", viewModel.trans("জরুরী", "Emergency")),
                Pair("Male", viewModel.trans("পুরুষ", "Male")),
                Pair("Female", viewModel.trans("মহিলা", "Female")),
                Pair("Child", viewModel.trans("শিশু", "Child"))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterOptions.forEach { opt ->
                    val isSelected = opt.first == selectedFilter
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectedFilter.value = opt.first },
                        label = { Text(opt.second, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TealPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Results count
            Text(
                text = viewModel.trans("মোট রোগী পাওয়া গেছে: ${filteredPatients.size} জন", "Total patients found: ${filteredPatients.size}"),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TealPrimary,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            if (filteredPatients.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.FolderOpen, "Empty", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = viewModel.trans("কোনো রোগী পাওয়া যায়নি!", "No patients found!"),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredPatients) { p ->
                        PatientDetailedCard(p, viewModel)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PatientDetailedCard(patient: PatientEntity, viewModel: HospitalViewModel) {
    val statusColor = when (patient.status) {
        "Admitted" -> StatusAdmitted
        "ICU" -> StatusICU
        "Observation" -> StatusObservation
        "Emergency" -> StatusEmergency
        "Discharged" -> StatusDischarged
        else -> StatusGray
    }

    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { viewModel.selectPatient(patient) },
                onLongClick = { showMenu = true }
            )
            .testTag("patient_card_${patient.id}"),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Colored Status Strip (right aligned)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(8.dp)
                    .fillMaxHeight()
                    .background(statusColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header (Status & ID)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(end = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = viewModel.trans(
                                when (patient.status) {
                                    "Admitted" -> "বর্তমানে ভর্তি"
                                    "ICU" -> "আইসিইউ ভর্তি"
                                    "Observation" -> "পর্যবেক্ষণ"
                                    "Emergency" -> "জরুরী ভর্তি"
                                    "Discharged" -> "ছুটি হয়েছে"
                                    else -> "ভর্তি"
                                },
                                patient.status
                            ),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = patient.patientIdString,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Name and demographics
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(TealPrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (patient.gender.lowercase() == "female") Icons.Default.Face else Icons.Default.Person,
                            contentDescription = "Gender",
                            tint = TealPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = patient.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${viewModel.trans(if (patient.gender == "Male") "পুরুষ" else "মহিলা", patient.gender)} | ${patient.age} ${viewModel.trans("বছর", "Years")}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                Spacer(modifier = Modifier.height(12.dp))

                // Hospital and Bed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = viewModel.trans("🏥 বেড ও ওয়ার্ড", "🏥 Ward & Bed"), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TealPrimary)
                        Text(text = "${patient.ward} ${if (patient.bedNumber.isNotEmpty()) "- ${patient.bedNumber}" else ""}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = viewModel.trans("👨‍⚕️ ডাক্তার", "👨‍⚕️ Doctor"), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TealPrimary)
                        Text(text = patient.doctorName, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Disease and Admission Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = viewModel.trans("🩺 রোগ", "🩺 Disease"), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TealPrimary)
                        Text(text = patient.diseaseName, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        val isDischarged = patient.status == "Discharged"
                        Text(
                            text = if (isDischarged) viewModel.trans("🏠 ছুটি সময়", "🏠 Discharge Time") else viewModel.trans("🕘 ভর্তি সময়", "🕘 Admission Time"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TealPrimary
                        )
                        Text(
                            text = if (isDischarged) "${patient.dischargeTime ?: "N/A"}" else "${patient.admissionTime}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action buttons footer
                Row(
                    modifier = Modifier.fillMaxWidth().padding(end = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { viewModel.selectPatient(patient) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Visibility, "View", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(viewModel.trans("বিশদ দেখুন", "View"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = { viewModel.navigateTo(Screen.EditPatient(patient)) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(viewModel.trans("সম্পাদনা", "Edit"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                    }
                }
            }
        }
    }

    // Long Press Actions Dropdown Menu
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false }
    ) {
        DropdownMenuItem(
            text = { Text(viewModel.trans("সম্পাদনা (Edit)", "Edit Profile")) },
            leadingIcon = { Icon(Icons.Default.Edit, null) },
            onClick = {
                showMenu = false
                viewModel.navigateTo(Screen.EditPatient(patient))
            }
        )
        DropdownMenuItem(
            text = { Text(viewModel.trans("বেড পরিবর্তন (Transfer Bed)", "Transfer Bed")) },
            leadingIcon = { Icon(Icons.Default.SwapHoriz, null) },
            onClick = {
                showMenu = false
                viewModel.selectPatient(patient)
            }
        )
        if (patient.status != "Discharged") {
            DropdownMenuItem(
                text = { Text(viewModel.trans("ছুটি দিন (Discharge)", "Discharge Patient")) },
                leadingIcon = { Icon(Icons.Default.ExitToApp, null) },
                onClick = {
                    showMenu = false
                    viewModel.selectPatient(patient)
                }
            )
        }
        DropdownMenuItem(
            text = { Text(viewModel.trans("মুছে ফেলুন (Delete)", "Delete Record")) },
            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
            onClick = {
                showMenu = false
                viewModel.deletePatient(patient)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPatientScreen(viewModel: HospitalViewModel, existingPatient: PatientEntity?) {
    val isEdit = existingPatient != null

    // Personal Info
    var name by remember { mutableStateOf(existingPatient?.name ?: "") }
    var fatherName by remember { mutableStateOf(existingPatient?.fatherName ?: "") }
    var motherName by remember { mutableStateOf(existingPatient?.motherName ?: "") }
    var dob by remember { mutableStateOf(existingPatient?.dob ?: "15-07-1992") }
    var age by remember { mutableStateOf(existingPatient?.age?.toString() ?: "34") }
    var gender by remember { mutableStateOf(existingPatient?.gender ?: "Male") }
    var mobile by remember { mutableStateOf(existingPatient?.mobile ?: "") }
    var address by remember { mutableStateOf(existingPatient?.address ?: "") }
    var district by remember { mutableStateOf(existingPatient?.district ?: "Dhaka") }
    var subDistrict by remember { mutableStateOf(existingPatient?.subDistrict ?: "") }
    var nid by remember { mutableStateOf(existingPatient?.nid ?: "") }
    var guardianName by remember { mutableStateOf(existingPatient?.guardianName ?: "") }
    var guardianPhone by remember { mutableStateOf(existingPatient?.guardianPhone ?: "") }

    // Medical Info
    var status by remember { mutableStateOf(existingPatient?.status ?: "Admitted") }
    var diseaseName by remember { mutableStateOf(existingPatient?.diseaseName ?: "") }
    var primaryComplaint by remember { mutableStateOf(existingPatient?.primaryComplaint ?: "") }
    var doctorName by remember { mutableStateOf(existingPatient?.doctorName ?: "") }
    var department by remember { mutableStateOf(existingPatient?.department ?: "") }
    var ward by remember { mutableStateOf(existingPatient?.ward ?: "Male Medicine") }
    var cabin by remember { mutableStateOf(existingPatient?.cabin ?: "None") }
    var bedNumber by remember { mutableStateOf(existingPatient?.bedNumber ?: "") }
    var bloodGroup by remember { mutableStateOf(existingPatient?.bloodGroup ?: "B+") }
    var allergy by remember { mutableStateOf(existingPatient?.allergy ?: "None") }
    var height by remember { mutableStateOf(existingPatient?.height ?: "") }
    var weight by remember { mutableStateOf(existingPatient?.weight ?: "") }

    // Admission Details
    var admissionDate by remember { mutableStateOf(existingPatient?.admissionDate ?: "15 July 2026") }
    var admissionTime by remember { mutableStateOf(existingPatient?.admissionTime ?: "8:30 AM") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) viewModel.trans("রোগীর তথ্য সংশোধন", "Edit Patient") else viewModel.trans("নতুন রোগী ভর্তি", "New Patient Admission")) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.Dashboard) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = viewModel.trans("ব্যক্তিগত তথ্য (Personal Information)", "Personal Information"), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TealPrimary)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(viewModel.trans("রোগীর নাম *", "Patient Name *")) },
                modifier = Modifier.fillMaxWidth().testTag("patient_form_name")
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text(viewModel.trans("বয়স *", "Age *")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                // Gender Selection
                var expandedGender by remember { mutableStateOf(false) }
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = gender,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(viewModel.trans("লিঙ্গ", "Gender")) },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { expandedGender = true }) {
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        }
                    )
                    DropdownMenu(expanded = expandedGender, onDismissRequest = { expandedGender = false }) {
                        listOf("Male", "Female", "Child", "Other").forEach { g ->
                            DropdownMenuItem(text = { Text(g) }, onClick = {
                                gender = g
                                expandedGender = false
                            })
                        }
                    }
                }
            }

            OutlinedTextField(
                value = mobile,
                onValueChange = { mobile = it },
                label = { Text(viewModel.trans("মোবাইল নম্বর *", "Mobile *")) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = fatherName,
                onValueChange = { fatherName = it },
                label = { Text(viewModel.trans("পিতার নাম", "Father's Name")) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = motherName,
                onValueChange = { motherName = it },
                label = { Text(viewModel.trans("মাতার নাম", "Mother's Name")) },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = dob,
                    onValueChange = { dob = it },
                    label = { Text(viewModel.trans("জন্ম তারিখ", "Date of Birth")) },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = nid,
                    onValueChange = { nid = it },
                    label = { Text(viewModel.trans("জাতীয় পরিচয়পত্র (NID)", "NID Number")) },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text(viewModel.trans("ঠিকানা", "Address")) },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = district,
                    onValueChange = { district = it },
                    label = { Text(viewModel.trans("জেলা", "District")) },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = subDistrict,
                    onValueChange = { subDistrict = it },
                    label = { Text(viewModel.trans("উপজেলা", "Sub-District")) },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = guardianName,
                    onValueChange = { guardianName = it },
                    label = { Text(viewModel.trans("অভিভাবকের নাম", "Guardian Name")) },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = guardianPhone,
                    onValueChange = { guardianPhone = it },
                    label = { Text(viewModel.trans("অভিভাবকের মোবাইল", "Guardian Phone")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = viewModel.trans("চিকিৎসা সংক্রান্ত তথ্য (Medical Information)", "Medical Information"), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TealPrimary)

            OutlinedTextField(
                value = diseaseName,
                onValueChange = { diseaseName = it },
                label = { Text(viewModel.trans("রোগের নাম *", "Disease Name *")) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = primaryComplaint,
                onValueChange = { primaryComplaint = it },
                label = { Text(viewModel.trans("প্রধান সমস্যা (Chief Complaint) *", "Chief Complaint *")) },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = doctorName,
                    onValueChange = { doctorName = it },
                    label = { Text(viewModel.trans("ডাক্তার *", "Doctor Name *")) },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text(viewModel.trans("বিভাগ", "Department")) },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = ward,
                    onValueChange = { ward = it },
                    label = { Text(viewModel.trans("ওয়ার্ড *", "Ward *")) },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = cabin,
                    onValueChange = { cabin = it },
                    label = { Text(viewModel.trans("কেবিন (যদি থাকে)", "Cabin (If any)")) },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = bedNumber,
                    onValueChange = { bedNumber = it },
                    label = { Text(viewModel.trans("বেড নম্বর *", "Bed Number *")) },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = bloodGroup,
                    onValueChange = { bloodGroup = it },
                    label = { Text(viewModel.trans("রক্তের গ্রুপ", "Blood Group")) },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = height,
                    onValueChange = { height = it },
                    label = { Text(viewModel.trans("উচ্চতা", "Height")) },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text(viewModel.trans("ওজন", "Weight")) },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = allergy,
                onValueChange = { allergy = it },
                label = { Text(viewModel.trans("অ্যালার্জি", "Allergy / Sensitivities")) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = viewModel.trans("ভর্তি সংক্রান্ত তথ্য (Admission Details)", "Admission Details"), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TealPrimary)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = admissionDate,
                    onValueChange = { admissionDate = it },
                    label = { Text(viewModel.trans("ভর্তির তারিখ", "Admission Date")) },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = admissionTime,
                    onValueChange = { admissionTime = it },
                    label = { Text(viewModel.trans("ভর্তির সময়", "Admission Time")) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (name.isNotEmpty() && age.isNotEmpty() && mobile.isNotEmpty() && diseaseName.isNotEmpty() && primaryComplaint.isNotEmpty() && doctorName.isNotEmpty() && bedNumber.isNotEmpty()) {
                        val parsedAge = age.toIntOrNull() ?: 0
                        val p = PatientEntity(
                            id = existingPatient?.id ?: 0,
                            patientIdString = existingPatient?.patientIdString ?: "",
                            admissionNo = existingPatient?.admissionNo ?: "",
                            status = status,
                            name = name,
                            fatherName = fatherName,
                            motherName = motherName,
                            dob = dob,
                            age = parsedAge,
                            gender = gender,
                            mobile = mobile,
                            address = address,
                            district = district,
                            subDistrict = subDistrict,
                            nid = nid,
                            guardianName = guardianName,
                            guardianPhone = guardianPhone,
                            diseaseName = diseaseName,
                            primaryComplaint = primaryComplaint,
                            doctorName = doctorName,
                            department = department,
                            ward = ward,
                            cabin = cabin,
                            bedNumber = bedNumber,
                            bloodGroup = bloodGroup,
                            allergy = allergy,
                            height = height,
                            weight = weight,
                            admissionDate = admissionDate,
                            admissionTime = admissionTime,
                            dischargeDate = existingPatient?.dischargeDate,
                            dischargeTime = existingPatient?.dischargeTime,
                            finalDiagnosis = existingPatient?.finalDiagnosis,
                            dischargeRemarks = existingPatient?.dischargeRemarks,
                            followUpDate = existingPatient?.followUpDate,
                            dischargeSummary = existingPatient?.dischargeSummary
                        )
                        if (isEdit) {
                            viewModel.updatePatient(p)
                        } else {
                            viewModel.addNewPatient(p)
                        }
                    }
                },
                enabled = name.isNotEmpty() && age.isNotEmpty() && mobile.isNotEmpty() && diseaseName.isNotEmpty() && primaryComplaint.isNotEmpty() && doctorName.isNotEmpty() && bedNumber.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_patient_form_button"),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isEdit) viewModel.trans("তথ্য সংশোধন করুন", "Save Updates") else viewModel.trans("ভর্তি সম্পন্ন করুন", "Admit Patient"),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
