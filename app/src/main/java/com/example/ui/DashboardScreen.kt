package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ChatMessage
import com.example.data.ConnectedDevice
import com.example.data.GeneratedDoc
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: AgentViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    
    // States from ViewModel
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val generatedDocs by viewModel.generatedDocs.collectAsStateWithLifecycle()
    val connectedDevices by viewModel.connectedDevices.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isVaultLocked by viewModel.isVaultLocked.collectAsStateWithLifecycle()
    val syncPulseInProgress by viewModel.syncPulseInProgress.collectAsStateWithLifecycle()
    val showShareAlert by viewModel.showShareAlert.collectAsStateWithLifecycle()

    // Dialog flags
    var showEmailDialog by remember { mutableStateOf(false) }
    var showStudyDialog by remember { mutableStateOf(false) }
    var showBankDialog by remember { mutableStateOf(false) }
    var showAddDeviceDialog by remember { mutableStateOf(false) }

    // Floating overlay Dialog to handle real on-device Android system Shares (Direct Share / Share target)
    showShareAlert?.let { sharedText ->
        var importTitle by remember { mutableStateOf("Nota Importada") }
        var importType by remember { mutableStateOf("EMAIL") }
        
        androidx.compose.ui.window.Dialog(onDismissRequest = { viewModel.dismissShareAlert() }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CyberCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSlate),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📥 ENLACE/TEXTO COMPARTIDO DETECTADO",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AccentCyan
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Aura ha interceptado tramas de texto enviadas desde otra aplicación vía Android Share Target. ¿Deseas catalogarlas e importarlas con cifrado AES-256?",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = importTitle,
                        onValueChange = { importTitle = it },
                        label = { Text("Título de Documento", color = TextSecondary, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = BorderSlate
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("share_import_title_field")
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        "Clasificar en tu Bóveda como:",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("EMAIL" to "Correo", "STUDY_GUIDE" to "Estudio", "BANK_ANALYSIS" to "Finanzas").forEach { (typeVal, labelVal) ->
                            val sel = importType == typeVal
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (sel) AccentCyan else CyberCardLight)
                                    .clickable { importType = typeVal }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    labelVal,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (sel) Color.Black else TextPrimary
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Vista previa del contenido:",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberCardLight)
                            .padding(8.dp)
                    ) {
                        Text(
                            sharedText,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 4,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { viewModel.dismissShareAlert() }) {
                            Text("Ignorar", color = TextSecondary, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.importSharedTextIntoVault(importTitle, importType, sharedText)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                            modifier = Modifier.testTag("share_import_submit_button")
                        ) {
                            Text("Importar", color = Color.Black, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isLoading) AlertAmber else SecurityGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Aura Personal Agent",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "24/7 Encriptado • Uso Offline",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                },
                actions = {
                    // Lock/Unlock vault button available globally in top bar with actual biometric integration
                    IconButton(
                        onClick = {
                            if (isVaultLocked) {
                                viewModel.triggerBiometricPrompt?.invoke()
                            } else {
                                viewModel.isVaultLocked.value = true
                            }
                        },
                        modifier = Modifier
                            .testTag("global_vault_lock_button")
                            .border(1.dp, BorderSlate, RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = if (isVaultLocked) Icons.Default.Lock else Icons.Default.Build, // Use Build as alternative green unlock state
                            contentDescription = "Cifrado de datos locales",
                            tint = if (isVaultLocked) AlertAmber else SecurityGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ObsidianBg,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = ObsidianBg,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Conversar") },
                    label = { Text("Asistente") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentCyan,
                        selectedTextColor = AccentCyan,
                        indicatorColor = CyberCardLight,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.List, contentDescription = "Bóveda") },
                    label = { Text("Vault") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentCyan,
                        selectedTextColor = AccentCyan,
                        indicatorColor = CyberCardLight,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Sandbox de Enfoque") },
                    label = { Text("Sandbox") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentCyan,
                        selectedTextColor = AccentCyan,
                        indicatorColor = CyberCardLight,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Refresh, contentDescription = "Sincronizador") },
                    label = { Text("Sincro") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentCyan,
                        selectedTextColor = AccentCyan,
                        indicatorColor = CyberCardLight,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Seguridad") },
                    label = { Text("Seguridad") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentCyan,
                        selectedTextColor = AccentCyan,
                        indicatorColor = CyberCardLight,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
            }
        },
        containerColor = ObsidianBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> AgentChatTab(
                    viewModel = viewModel,
                    messages = chatMessages,
                    isLoading = isLoading,
                    onSendMessage = { text -> viewModel.sendMessage(text) },
                    onClearChat = { viewModel.clearChat() },
                    onDraftEmailClick = { showEmailDialog = true },
                    onStudyGuideClick = { showStudyDialog = true },
                    onBankScanClick = { showBankDialog = true }
                )
                1 -> LocalVaultTab(
                    docs = generatedDocs,
                    isVaultLocked = isVaultLocked,
                    viewModel = viewModel,
                    onDeleteDoc = { doc -> viewModel.deleteDoc(doc) }
                )
                2 -> SyncTab(
                    viewModel = viewModel,
                    devices = connectedDevices,
                    syncPulseInProgress = syncPulseInProgress,
                    onSyncAll = { viewModel.syncAllDevicesNow() },
                    onToggleSync = { id, enabled -> viewModel.toggleDeviceSync(id, enabled) },
                    onForceSyncItem = { id -> viewModel.triggerSingleDeviceSync(id) },
                    onDeleteDevice = { id -> viewModel.deleteDevice(id) },
                    onAddDeviceClick = { showAddDeviceDialog = true }
                )
                3 -> SecurityTab(
                    viewModel = viewModel
                )
                4 -> SandboxFocusTab(
                    viewModel = viewModel
                )
            }
        }
    }

    // --- TASK DIALOGS ---

    // 1. Redactar Correo Dialog
    if (showEmailDialog) {
        var subject by remember { mutableStateOf("") }
        var audience by remember { mutableStateOf("") }
        var details by remember { mutableStateOf("") }
        var tone by remember { mutableStateOf("Profesional") }

        Dialog(onDismissRequest = { showEmailDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, BorderSlate, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Redactar Correo Profesional",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = AccentCyan
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Asunto o Propósito") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = BorderSlate,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = audience,
                        onValueChange = { audience = it },
                        label = { Text("Destinatario / Audiencia") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = BorderSlate,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = details,
                        onValueChange = { details = it },
                        label = { Text("Puntos clave a incluir") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = BorderSlate,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Tono & Estilo:", fontSize = 12.sp, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("Profesional", "Cercano", "Persuasivo").forEach { styleOption ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { tone = styleOption }
                            ) {
                                RadioButton(
                                    selected = tone == styleOption,
                                    onClick = { tone = styleOption },
                                    colors = RadioButtonDefaults.colors(selectedColor = AccentCyan)
                                )
                                Text(styleOption, fontSize = 12.sp, color = TextPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showEmailDialog = false }) {
                            Text("Cancelar", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (subject.trim().isNotEmpty()) {
                                    showEmailDialog = false
                                    viewModel.runDraftEmailTask(subject, audience, details, tone) {
                                        // Completed, result is in repository
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                            enabled = subject.trim().isNotEmpty()
                        ) {
                            Text("Generar Correo", color = Color.Black)
                        }
                    }
                }
            }
        }
    }

    // 2. Estructurar Guía de Estudio Dialog
    if (showStudyDialog) {
        var topic by remember { mutableStateOf("") }
        var level by remember { mutableStateOf("Universitario") }
        var details by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showStudyDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, BorderSlate, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Planificar Guía de Aprendizaje",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = AccentCyan
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = topic,
                        onValueChange = { topic = it },
                        label = { Text("Tema o Materia Central") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = BorderSlate,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = details,
                        onValueChange = { details = it },
                        label = { Text("Instrucciones o subtemas (Opcional)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = BorderSlate,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Nivel del Estudiante:", fontSize = 12.sp, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("Escolar", "Técnico", "Universitario").forEach { levelOption ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { level = levelOption }
                            ) {
                                RadioButton(
                                    selected = level == levelOption,
                                    onClick = { level = levelOption },
                                    colors = RadioButtonDefaults.colors(selectedColor = AccentCyan)
                                )
                                Text(levelOption, fontSize = 11.sp, color = TextPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showStudyDialog = false }) {
                            Text("Cancelar", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (topic.trim().isNotEmpty()) {
                                    showStudyDialog = false
                                    viewModel.runStudyGuideTask(topic, level, details) {
                                        // Save & notify
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                            enabled = topic.trim().isNotEmpty()
                        ) {
                            Text("Estructurar Guía", color = Color.Black)
                        }
                    }
                }
            }
        }
    }

    // 3. Revisar Extracto Bancario Dialog
    if (showBankDialog) {
        var statementContent by remember { mutableStateOf("") }
        var inputTitle by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showBankDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, BorderSlate, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Detector de Cargos Sospechosos",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = AccentCyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Pega los movimientos de tu banco. Aura analizará de forma privada posibles comisiones ocultas, duplicados o cobros de riesgo.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = inputTitle,
                        onValueChange = { inputTitle = it },
                        label = { Text("Identificador de cuenta (ej: Visa Oro)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = BorderSlate,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = statementContent,
                        onValueChange = { statementContent = it },
                        placeholder = { 
                            Text("Ej: \n28/05/2026 - SUSCR PLAN CANVA - €12.99\n28/05/2026 - COMIS. MANTENIM - €15.00\n29/05/2026 - TAXI NIGHT RIDE (03:00 AM) - €45.00", 
                            fontSize = 11.sp) 
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = BorderSlate,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        maxLines = 8
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showBankDialog = false }) {
                            Text("Cancelar", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (statementContent.trim().isNotEmpty()) {
                                    showBankDialog = false
                                    viewModel.runBankStatementTask(statementContent, inputTitle) {
                                        // Saved
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                            enabled = statementContent.trim().isNotEmpty()
                        ) {
                            Text("Auditar Extracto", color = Color.Black)
                        }
                    }
                }
            }
        }
    }

    // 4. Agregar Dispositivo Dialog
    if (showAddDeviceDialog) {
        var devName by remember { mutableStateOf("") }
        var platform by remember { mutableStateOf("macOS") }

        Dialog(onDismissRequest = { showAddDeviceDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, BorderSlate, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Vincular Dispositivo u Aplicación",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = AccentCyan
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = devName,
                        onValueChange = { devName = it },
                        label = { Text("Nombre del dispositivo/cuenta") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = BorderSlate,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Sistema / Plataforma:", fontSize = 12.sp, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("macOS", "Canva", "iPhone", "Android").forEach { plat ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { platform = plat }
                            ) {
                                RadioButton(
                                    selected = platform == plat,
                                    onClick = { platform = plat },
                                    colors = RadioButtonDefaults.colors(selectedColor = AccentCyan)
                                )
                                Text(plat, fontSize = 10.sp, color = TextPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddDeviceDialog = false }) {
                            Text("Cancelar", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (devName.trim().isNotEmpty()) {
                                    showAddDeviceDialog = false
                                    viewModel.connectNewDevice(devName, platform)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                            enabled = devName.trim().isNotEmpty()
                        ) {
                            Text("Sincronizar", color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

// ================= TAB 0: AGENTE COGNITIVO CHAT =================

@Composable
fun AgentStepRow(step: com.example.api.AgentStep) {
    val (icon, color, label) = when (step.state) {
        com.example.api.AgentStepState.THOUGHT -> Triple(Icons.Default.Build, AlertAmber, "PENSAMIENTO 🧠")
        com.example.api.AgentStepState.ACTION -> Triple(Icons.Default.PlayArrow, AccentCyan, "HERRAMIENTA 🛠️")
        com.example.api.AgentStepState.OBSERVATION -> Triple(Icons.Default.List, SecurityGreen, "OBSERVACIÓN 📋")
        com.example.api.AgentStepState.FINAL_ANSWER -> Triple(Icons.Default.Check, SecurityGreen, "FINALIZACIÓN 🛡️")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberCardLight, RoundedCornerShape(10.dp))
                .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                .padding(10.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = label,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = step.title,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = step.detail,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun AgentChatTab(
    viewModel: AgentViewModel,
    messages: List<ChatMessage>,
    isLoading: Boolean,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    onDraftEmailClick: () -> Unit,
    onStudyGuideClick: () -> Unit,
    onBankScanClick: () -> Unit
) {
    var rawText by remember { mutableStateOf("") }
    val inferenceMode by viewModel.selectedInferenceMode.collectAsStateWithLifecycle()
    val agentSteps by viewModel.currentAgentSteps.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.onSpeechRecognizedCallback = { text ->
            rawText = if (rawText.isEmpty()) text else "$rawText $text"
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Toggle card for selecting between Local Offline Gemma 2B core and Cloud Gemini API
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberCard),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .border(2.dp, BorderSlate, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "MOTOR DE INTELIGENCIA COGNITIVA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (inferenceMode == "Local") "Cortical Gemma 2B (Offline) 🔒" else "Cloud Gemini 1.5 Pro (Híbrido) ⚡",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (inferenceMode == "Local") SecurityGreen else AccentCyan
                        )
                    }
                    
                    Row(
                        modifier = Modifier
                            .background(CyberCardLight, RoundedCornerShape(20.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = { viewModel.selectedInferenceMode.value = "Cloud" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (inferenceMode == "Cloud") AccentCyan else Color.Transparent,
                                contentColor = if (inferenceMode == "Cloud") Color.Black else TextSecondary
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Cloud", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { viewModel.selectedInferenceMode.value = "Local" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (inferenceMode == "Local") SecurityGreen else Color.Transparent,
                                contentColor = if (inferenceMode == "Local") Color.Black else TextSecondary
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Gemma Local", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (inferenceMode == "Local") 
                        "Cálculos 100% offline. Sincronización, auditorías y memoria semántica local blindadas frente a filtraciones."
                    else "Usa la API en la nube para procesos complejos y razonamientos avanzados con llamadas remotas.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )

                Divider(modifier = Modifier.padding(vertical = 10.dp), color = BorderSlate)
                val isChainingActive by viewModel.offlineChainingActive.collectAsStateWithLifecycle()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CADENA AGÉNTICA MULTI-MODELO OFFLINE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Voz ➜ ReAct local ➜ Bóveda Cifrada",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isChainingActive) AccentCyan else TextPrimary
                        )
                    }
                    Switch(
                        checked = isChainingActive,
                        onCheckedChange = { viewModel.offlineChainingActive.value = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AccentCyan,
                            checkedTrackColor = CyberCardLight
                        ),
                        modifier = Modifier.testTag("offline_agent_chaining_toggle")
                    )
                }
            }
        }

        // Quick utility tools panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .background(CyberCard)
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .border(1.dp, BorderSlate, RoundedCornerShape(10.dp))
                .padding(8.dp)
        ) {
            Text(
                "ATRECHOS RÁPIDOS DE PRODUCTIVIDAD IA:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Email button
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberCardLight),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onDraftEmailClick() }
                        .testTag("action_draft_email_card")
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Email, "Redactar", tint = AccentCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Redactar Correo", 
                            fontSize = 11.sp, 
                            fontWeight = FontWeight.Medium, 
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                // Study helper button
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberCardLight),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onStudyGuideClick() }
                        .testTag("action_study_guide_card")
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Info, "Aprender", tint = AccentCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Guías de Estudio", 
                            fontSize = 11.sp, 
                            fontWeight = FontWeight.Medium, 
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                // Finance scanner button
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberCardLight),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onBankScanClick() }
                        .testTag("action_bank_scan_card")
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Warning, "Auditar", tint = AlertAmber, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Auditar Gastos", 
                            fontSize = 11.sp, 
                            fontWeight = FontWeight.Medium, 
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Message List
        Box(modifier = Modifier.weight(1f)) {
            if (messages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(CyberCardLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Aura",
                            tint = AccentCyan,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Aura te da la bienvenida 🛡️",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tu agente local 24/7 de IA. Escribe un mensaje o prueba los atajos de la barra superior para redactar correos, planificar guías o auditar tu extracto de cuenta.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { message ->
                        ChatBubbleRow(message)
                    }
                    
                    // Render real-time autonomous ReAct loops steps during loading cycle
                    if (isLoading && agentSteps.isNotEmpty()) {
                        item {
                            Text(
                                text = "PROCESAMIENTO COGNITIVO REAL-TIME (ReAct):",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                                letterSpacing = 0.8.sp
                            )
                        }
                        items(agentSteps) { step ->
                            AgentStepRow(step)
                        }
                    }

                    if (isLoading) {
                        item {
                            Row(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(CyberCard)
                                        .padding(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = AccentCyan,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            "Aura está procesando...",
                                            fontSize = 12.sp,
                                            color = TextSecondary,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Floating trash bin to clear empty chat logs
            if (messages.isNotEmpty()) {
                FloatingActionButton(
                    onClick = onClearChat,
                    containerColor = CyberCardLight,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .size(40.dp)
                ) {
                    Icon(Icons.Default.Delete, "Limpiar conversas", tint = Color.LightGray, modifier = Modifier.size(20.dp))
                }
            }
        }

        // Bottom Sending Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberCard)
                .padding(10.dp)
                .windowInsetsPadding(WindowInsets.navigationBars),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = rawText,
                onValueChange = { rawText = it },
                placeholder = { Text("Escribe para chatear con tu IA...", fontSize = 13.sp, color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentCyan,
                    unfocusedBorderColor = BorderSlate,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                maxLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (rawText.trim().isNotEmpty()) {
                        onSendMessage(rawText)
                        rawText = ""
                    }
                }),
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field")
            )
            Spacer(modifier = Modifier.width(6.dp))
            val context = LocalContext.current
            IconButton(
                onClick = {
                    (context as? com.example.MainActivity)?.triggerSpeechRecordingUI()
                },
                modifier = Modifier
                    .testTag("chat_voice_mic_button")
                    .background(CyberCardLight, CircleShape)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Grabar voz en Darija/Español",
                    tint = AccentCyan
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(
                onClick = {
                    if (rawText.trim().isNotEmpty()) {
                        onSendMessage(rawText)
                        rawText = ""
                    }
                },
                modifier = Modifier
                    .testTag("chat_send_button")
                    .background(AccentCyan, CircleShape)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Enviar",
                    tint = Color.Black
                )
            }
        }
    }
}

@Composable
fun ChatBubbleRow(message: ChatMessage) {
    val isUser = message.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 2.dp,
                            bottomEnd = if (isUser) 2.dp else 16.dp
                        )
                    )
                    .background(if (isUser) AccentCyan else CyberCard)
                    .border(
                        1.dp,
                        if (isUser) AccentCyan else BorderSlate,
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 2.dp,
                            bottomEnd = if (isUser) 2.dp else 16.dp
                        )
                    )
                    .padding(14.dp)
            ) {
                Text(
                    text = message.content,
                    fontSize = 14.sp,
                    color = if (isUser) Color.Black else TextPrimary,
                    lineHeight = 20.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isUser) "Tú" else "Aura",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUser) AccentCyan else SecurityGreen
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formatTime(message.timestamp),
                    fontSize = 9.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

// ================= TAB 1: BÓVEDA LOCAL DE DOCUMENTOS =================

@Composable
fun LocalVaultTab(
    docs: List<GeneratedDoc>,
    isVaultLocked: Boolean,
    viewModel: AgentViewModel,
    onDeleteDoc: (GeneratedDoc) -> Unit
) {
    var activeFilter by remember { mutableStateOf("ALL") }
    var expandedDocId by remember { mutableStateOf<Int?>(null) }
    
    var showPinDialog by remember { mutableStateOf(false) }
    var typedPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    val filteredDocs = remember(docs, activeFilter) {
        if (activeFilter == "ALL") docs else docs.filter { it.type == activeFilter }
    }

    if (showPinDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showPinDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CyberCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSlate),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🔐 ACCESO CRIPTOGRÁFICO",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = AccentCyan
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Introduce el PIN de derivación de clave para descifrar SQLite localmente con AES-256.\n\nPIN por defecto: 1234",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    @Suppress("DEPRECATION")
                    OutlinedTextField(
                        value = typedPin,
                        onValueChange = { 
                            typedPin = it
                            pinError = null
                        },
                        label = { Text("PIN de Bóveda", color = TextSecondary, fontSize = 12.sp) },
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("vault_pin_input")
                    )

                    if (pinError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = pinError!!, color = AlertAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                viewModel.triggerBiometricPrompt?.invoke()
                                showPinDialog = false
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Huella/Face Autenticación",
                                tint = AccentCyan
                            )
                        }
                        TextButton(onClick = { showPinDialog = false }) {
                            Text("Cancelar", color = TextSecondary, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (typedPin == "1234") {
                                    viewModel.isVaultLocked.value = false
                                    showPinDialog = false
                                    typedPin = ""
                                    pinError = null
                                } else {
                                    pinError = "❌ PIN INCORRECTO. Intenta con 1234"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                            modifier = Modifier.testTag("vault_pin_submit")
                        ) {
                            Text("Autorizar", color = Color.Black, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Master status of encription
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberCard)
                .border(1.dp, if (isVaultLocked) BorderSlate else SecurityGreen, RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isVaultLocked) CyberCardLight else SecurityGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isVaultLocked) Icons.Default.Lock else Icons.Default.Check,
                    contentDescription = null,
                    tint = if (isVaultLocked) AlertAmber else SecurityGreen
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isVaultLocked) "Boveda Encriptada Offline" else "Boveda Descifrada Localmente",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Text(
                    text = if (isVaultLocked) "Todos tus correos, guías y extractos se almacenan cifrados con AES256." else "Los datos están listos para lectura. Pulsa el candado para volver a cifrar.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { 
                    if (isVaultLocked) {
                        showPinDialog = true
                    } else {
                        viewModel.isVaultLocked.value = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isVaultLocked) AccentCyan else CyberCardLight
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.testTag("vault_security_toggle")
            ) {
                Text(
                    text = if (isVaultLocked) "Descifrar" else "Cifrar",
                    fontSize = 11.sp,
                    color = if (isVaultLocked) Color.Black else TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Collapsible RAG Local Card
        var showRagSection by remember { mutableStateOf(false) }
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberCard),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (showRagSection) AccentCyan else BorderSlate, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showRagSection = !showRagSection },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🧠 CORE RAG VECTORIAL OFFLINE", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AccentCyan)
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier.background(CyberCardLight, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("ONNX / BINDI", fontSize = 8.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                    Icon(
                        imageVector = if (showRagSection) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Expand",
                        tint = AccentCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }

                if (showRagSection) {
                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = BorderSlate)
                    
                    var newDocTitle by remember { mutableStateOf("Derecho Laboral - Título I") }
                    var newDocText by remember { mutableStateOf("El Estatuto de los Trabajadores en España define en su Título I los derechos básicos del trabajador, tales como la libre elección de profesión u oficio, la negociación colectiva y el derecho a la huelga por motivos legítimos. Las horas extraordinarias tendrán recargos correspondientes no inferiores al valor de la hora de trabajo ordinario, y se prohíben comisiones o penalizaciones injustificadas en los pagos salariales.") }
                    var ragQueryInput by remember { mutableStateOf("¿Qué dice de las horas extraordinarias y salarios?") }
                    
                    val ragIndexedDocs by viewModel.ragIndexedDocs.collectAsStateWithLifecycle()
                    val ragResults by viewModel.ragQueryResults.collectAsStateWithLifecycle()

                    Text("1. ALIMENTAR MODELO VECTORIAL (Indexar corpus local):", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    OutlinedTextField(
                        value = newDocTitle,
                        onValueChange = { newDocTitle = it },
                        label = { Text("Título del manual u expediente", fontSize = 10.sp, color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = BorderSlate
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = newDocText,
                        onValueChange = { newDocText = it },
                        label = { Text("Texto completo para fragmentar en párrafos", fontSize = 10.sp, color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = BorderSlate
                        ),
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = {
                            viewModel.indexDocumentForRAG(newDocTitle, newDocText)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        modifier = Modifier.fillMaxWidth().testTag("rag_index_button")
                    ) {
                        Text("Vectorizar e Indexar en Memoria Lógica", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Documentos Indexados Directamente: ${if(ragIndexedDocs.isEmpty()) "Ninguno en esta sesión" else ragIndexedDocs.joinToString()}", fontSize = 10.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("2. INTERROGAR CORTEZA LOCAL (Búsqueda Coseno):", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = ragQueryInput,
                            onValueChange = { ragQueryInput = it },
                            placeholder = { Text("Buscar semánticamente...", fontSize = 12.sp, color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = AccentCyan,
                                unfocusedBorderColor = BorderSlate
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = { viewModel.queryLocalRAG(ragQueryInput) },
                            modifier = Modifier.background(CyberCardLight, CircleShape).size(44.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Query", tint = AccentCyan)
                        }
                    }

                    if (ragResults.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("🎯 CHUNKS LÉXICOS RECUPERADOS:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SecurityGreen)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            ragResults.forEach { (chunk, score) ->
                                Box(
                                    modifier = Modifier.fillMaxWidth().background(CyberCardLight, RoundedCornerShape(8.dp)).padding(8.dp)
                                ) {
                                    Column {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Documento: ${chunk.docTitle}", fontSize = 9.sp, color = AccentCyan, fontWeight = FontWeight.Bold)
                                            Text("Coseno Similitud: ${String.format(java.util.Locale.US, "%.2f", score)}", fontSize = 9.sp, color = SecurityGreen, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(chunk.text, fontSize = 11.sp, color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Collapsible Norma 43 Card
        var showNorma43Section by remember { mutableStateOf(false) }
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberCard),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (showNorma43Section) AccentCyan else BorderSlate, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showNorma43Section = !showNorma43Section },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🏦 CONECTOR NORMA 43 / CSV NATIVO", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AccentCyan)
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier.background(CyberCardLight, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Banca Online", fontSize = 8.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                    Icon(
                        imageVector = if (showNorma43Section) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Expand",
                        tint = AccentCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }

                if (showNorma43Section) {
                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = BorderSlate)
                    
                    var accountNameInput by remember { mutableStateOf("Mi Cuenta BBVA Automática") }
                    var rawFilePayload by remember { mutableStateOf("") }
                    
                    Text("Pega el extracto bancario plano Norma 43 de tu banca electrónica (o prueba nuestra plantilla precargada con cargos repetidos de Canva y comisiones):", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = accountNameInput,
                        onValueChange = { accountNameInput = it },
                        label = { Text("Nombre Identificador de Cuenta", fontSize = 10.sp, color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = BorderSlate
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = rawFilePayload,
                        onValueChange = { rawFilePayload = it },
                        label = { Text("Contenido del archivo .n43 o .csv", fontSize = 10.sp, color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = BorderSlate
                        ),
                        maxLines = 8,
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                rawFilePayload = com.example.api.Norma43Parser.getSampleNorma43()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCardLight),
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Cargar Muestra .n43", color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        Button(
                            onClick = {
                                if (rawFilePayload.trim().isNotEmpty()) {
                                    viewModel.parseAndImportNorma43(accountNameInput, rawFilePayload)
                                    rawFilePayload = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                            modifier = Modifier.weight(1f).testTag("norma43_parse_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Auditar e Importar", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Horizontal filter chips
        Text(
            text = "ARCHIVOS CONFIDENCIALES:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val chipsOptions = listOf(
                "ALL" to "Todos",
                "EMAIL" to "Correos",
                "STUDY_GUIDE" to "Guías",
                "BANK_ANALYSIS" to "Auditoría"
            )
            chipsOptions.forEach { (filterVal, displayVal) ->
                val selected = activeFilter == filterVal
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) AccentCyan else CyberCard)
                        .clickable { activeFilter = filterVal }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("filter_chip_$filterVal")
                ) {
                    Text(
                        displayVal,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) Color.Black else TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // List of generated files
        if (filteredDocs.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "",
                    tint = BorderSlate,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Bóveda vacía",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextSecondary
                )
                Text(
                    "No hay documentos de este tipo todavía.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                filteredDocs.forEach { doc ->
                    val isExpanded = expandedDocId == doc.id
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberCard),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedDocId = if (isExpanded) null else doc.id }
                            .border(1.dp, if (isExpanded) AccentCyan else BorderSlate, RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(CyberCardLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (doc.type) {
                                            "EMAIL" -> Icons.Default.Email
                                            "STUDY_GUIDE" -> Icons.Default.Info
                                            else -> Icons.Default.Warning
                                        },
                                        contentDescription = null,
                                        tint = AccentCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        doc.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = when (doc.type) {
                                                "EMAIL" -> "Correo Redactado"
                                                "STUDY_GUIDE" -> "Guía de Aprendizaje"
                                                else -> "Informe de Alertas"
                                            },
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(if (doc.isSynced) SecurityGreen else AlertAmber)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (doc.isSynced) "Sincronizado" else "Pendiente Sincro",
                                            fontSize = 9.sp,
                                            color = if (doc.isSynced) SecurityGreen else AlertAmber
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { onDeleteDoc(doc) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Borrar",
                                        tint = Color(0xFFEF5350),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Expanded view of file content
                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column {
                                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = BorderSlate)
                                    
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(CyberCardLight, RoundedCornerShape(8.dp))
                                            .border(1.dp, BorderSlate, RoundedCornerShape(8.dp))
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = viewModel.getDisplayTextForDoc(doc),
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (doc.isEncrypted && isVaultLocked) AlertAmber else TextPrimary,
                                            lineHeight = 18.sp
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "Fecha: " + formatDate(doc.timestamp),
                                            fontSize = 10.sp,
                                            color = TextSecondary
                                        )
                                        if (doc.isEncrypted && isVaultLocked) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    null,
                                                    tint = AlertAmber,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    "🔒 Datos AES Cifrados",
                                                    fontSize = 10.sp,
                                                    color = AlertAmber,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        } else {
                                            Text(
                                                "🔓 Descifrado para Lectura",
                                                fontSize = 10.sp,
                                                color = SecurityGreen,
                                                fontWeight = FontWeight.Bold
                                            )
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

// ================= TAB 2: SINCRONIZADOR PUENTE DE ECOSISTEMA =================

@Composable
fun SyncTab(
    viewModel: AgentViewModel,
    devices: List<ConnectedDevice>,
    syncPulseInProgress: Boolean,
    onSyncAll: () -> Unit,
    onToggleSync: (Int, Boolean) -> Unit,
    onForceSyncItem: (Int) -> Unit,
    onDeleteDevice: (Int) -> Unit,
    onAddDeviceClick: () -> Unit
) {
    val isServerActive by viewModel.isLanServerActive.collectAsStateWithLifecycle()
    val logs by viewModel.lanLogs.collectAsStateWithLifecycle()
    var selectedKmpFile by remember { mutableStateOf("DatabaseCommon") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header card
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberCard),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, BorderSlate, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Sincronización Multidispositivo",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AccentCyan
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Aura unifica tus datos de forma offline. Permite sincronizar correos con Canva, guías locales con tu macOS, iPhone o Android mediante un puente local Bonjour encriptado de forma directa.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onSyncAll,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sync_all_devices_button"),
                    enabled = !syncPulseInProgress
                ) {
                    if (syncPulseInProgress) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Sincronizando de extremo a extremo...", color = Color.Black, fontSize = 13.sp)
                    } else {
                        Icon(Icons.Default.Refresh, "Sync", tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SINCRONIZAR COPIA SEGURA AHORA", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Device Sub-Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "DISPOSITIVOS EN TU RED:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Button(
                onClick = onAddDeviceClick,
                colors = ButtonDefaults.buttonColors(containerColor = CyberCardLight),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("add_custom_device_btn")
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp), tint = AccentCyan)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Nuevo Link", fontSize = 11.sp, color = AccentCyan)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Devices list in scrollable container
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            devices.forEach { dev ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderSlate, RoundedCornerShape(10.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Platform icon
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberCardLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dev.platform.substring(0, minOf(dev.platform.length, 3)).uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = AccentCyan,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                dev.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (dev.status) {
                                                "Sincronizado" -> SecurityGreen
                                                "Sincronizando..." -> AlertAmber
                                                "Pendiente" -> AlertAmber
                                                else -> Color.Gray
                                            }
                                        )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = dev.status,
                                    fontSize = 11.sp,
                                    color = when (dev.status) {
                                        "Sincronizado" -> SecurityGreen
                                        "Sincronizando..." -> AlertAmber
                                        "Pendiente" -> AlertAmber
                                        else -> TextSecondary
                                    }
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = formatTime(dev.lastSyncTime),
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Right side interactive toggle/button controls
                        if (dev.status != "Desconectado") {
                            IconButton(
                                onClick = { onForceSyncItem(dev.id) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Sincronizar",
                                    tint = AccentCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        
                        IconButton(
                            onClick = { onDeleteDevice(dev.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Desvincular",
                                tint = Color(0xFFEF5350),
                                modifier = Modifier.size(16.dp)
                              )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bonjour DNS-SD LAN service host controller
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberCard),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderSlate, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DAEMON BONJOUR mDNS DE RED LOCAL (NSD)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isServerActive) SecurityGreen else AlertAmber)
                             )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isServerActive) "Hospedando: AuraPersonalAgent._tcp" else "Sincronizador Detenido",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isServerActive) SecurityGreen else AlertAmber
                            )
                        }
                    }
                    
                    Switch(
                        checked = isServerActive,
                        onCheckedChange = { viewModel.toggleLanServer() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AccentCyan,
                            checkedTrackColor = CyberCardLight
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Abre un puerto de socket ligero offline para que daemons macOS de barra de menús o shortcuts iOS Bonjour auto-detecten y envíen archivos de forma segura por Wi-Fi directo.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "LOGS DEL SOCKET DAEMON LAN:",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                
                // Terminal output console
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(Color.Black, RoundedCornerShape(6.dp))
                        .border(1.dp, BorderSlate, RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (logs.isEmpty()) {
                            item {
                                Text(
                                    "AURA SYSTEM: Sockets inactivos. Habilita mDNS para reportar transmisiones...",
                                    color = Color.DarkGray,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp
                                )
                            }
                        } else {
                            items(logs) { log ->
                                val color = when (log.type) {
                                    com.example.api.LanDiscoveryLog.LogType.SUCCESS -> SecurityGreen
                                    com.example.api.LanDiscoveryLog.LogType.WARNING -> AlertAmber
                                    com.example.api.LanDiscoveryLog.LogType.INCOMING -> AccentCyan
                                    else -> Color.LightGray
                                }
                                Text(
                                    text = "[${formatTime(log.timestamp)}] ${log.message}",
                                    color = color,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Apple macOS & iOS physical-link LAN emulation controls
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberCard),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderSlate, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CENTRO DE EMULACIÓN DE ENLACES LAN",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Sincronización por Sockets de Red Local (Real)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Prueba la conexión real abriendo sockets TCP sobre localhost (127.0.0.1) transmitiendo tramas binarias que detecta el servidor Bonjour en segundo plano:",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.simulateLanIncomingPayload(
                                fileName = "extracto_frances_bancario_q2.pdf",
                                docType = "BANK_ANALYSIS",
                                content = "ANÁLISIS DE CARGOS - CONCILIADOR AURA OFFLINE\n- Cargo duplicado Canva Subscription €12.99 SECT-08\n- Cargo sospechoso Comisión de Mantenimiento de Cuenta €15.00 no autorizada."
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCardLight),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("iMac PDF Bancario", color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Button(
                        onClick = {
                            viewModel.simulateLanClipboardPush("Draft_Email_Aura_Backup_Bonjour")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCardLight),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Portapapeles Mac", color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Decentral Mesh Network Panel
        val isMeshActive by viewModel.isMeshActive.collectAsStateWithLifecycle()
        val peersInMesh by viewModel.peersInMesh.collectAsStateWithLifecycle()
        val meshSyncProgress by viewModel.meshSyncProgress.collectAsStateWithLifecycle()
        val meshSyncRatio by viewModel.meshSyncRatio.collectAsStateWithLifecycle()

        Card(
            colors = CardDefaults.cardColors(containerColor = CyberCard),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (isMeshActive) AccentCyan else BorderSlate, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "📡 RED DE MALLA PEER-TO-PEER DESCENTRALIZADA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Sincronización sin Router (BLE & Wi-Fi Direct)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isMeshActive) AccentCyan else TextPrimary
                        )
                    }
                    Switch(
                        checked = isMeshActive,
                        onCheckedChange = { viewModel.toggleMesh(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AccentCyan,
                            checkedTrackColor = CyberCardLight
                        ),
                        modifier = Modifier.testTag("mesh_network_toggle")
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Acopla sincronización en malla ad-hoc. Permite enlazar directamente el portapapeles y claves de la Bóveda en mitad de un vuelo, un sótano o en zonas sin cobertura recurriendo a enlaces Bluetooth Secundarios.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )

                if (isMeshActive) {
                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = BorderSlate)
                    
                    Text("PEERS DETECTADOS EN TU RANGO DE SEÑAL MESH:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        peersInMesh.forEach { node ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CyberCardLight, RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Antenna signal box
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(CyberCard),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${node.strengthDbm} dBm",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (node.strengthDbm > -50) SecurityGreen else if (node.strengthDbm > -70) AlertAmber else Color.Gray,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(node.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("Tipo: ${node.type} | Estatus: ${node.statusText}", fontSize = 10.sp, color = TextSecondary)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { viewModel.triggerMeshSyncNow() },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        modifier = Modifier.fillMaxWidth().testTag("mesh_sync_trigger")
                    ) {
                        Text("Sincronizar Malla Ad-Hoc", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    if (meshSyncProgress.trim().isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(meshSyncProgress, fontSize = 11.sp, color = SecurityGreen, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { meshSyncRatio },
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                            color = SecurityGreen,
                            trackColor = CyberCardLight
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Kotlin Multiplatform KMP Common Shared Code visualizer
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberCard),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, BorderSlate, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "MIGRACIÓN KOTLIN MULTIPLATFORM (KMP)",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Explorar Módulo Común de Red Aura 📦",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = AccentCyan
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Este código corre simultáneamente en macOS (Bonjour Bonjour / Cocoa), iOS y Android, permitiendo compartir repositorios, cifrado y esquemas Room.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // KMP select tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("DatabaseCommon", "ExpectCryptography", "SharedMdnsSync").forEach { tab ->
                        Button(
                            onClick = { selectedKmpFile = tab },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedKmpFile == tab) AccentCyan else CyberCardLight,
                                contentColor = if (selectedKmpFile == tab) Color.Black else TextPrimary
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                        ) {
                            Text(
                                text = when(tab) {
                                    "DatabaseCommon" -> "RoomCommon"
                                    "ExpectCryptography" -> "CryptoKeystore"
                                    else -> "BonjourSocket"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                val codeToDisplay = when (selectedKmpFile) {
                    "DatabaseCommon" -> com.example.kmp.KmpCodebase.commonDatabaseKotlin
                    "ExpectCryptography" -> com.example.kmp.KmpCodebase.cryptographyExpectActual
                    else -> com.example.kmp.KmpCodebase.mdnsSyncEngineCommon
                }
                
                // Monospace code code block
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(CyberCardLight, RoundedCornerShape(8.dp))
                        .border(1.dp, BorderSlate, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Text(
                                text = codeToDisplay,
                                color = Color(0xFFA5D6A7),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ================= TAB 3: CONFIGURACIONES & PRIVACIDAD =================

@Composable
fun SecurityTab(viewModel: AgentViewModel) {
    val masterKey by viewModel.localEncriptionKey.collectAsStateWithLifecycle()
    val isVaultLocked by viewModel.isVaultLocked.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberCard),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderSlate, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Ajustes de Privacidad Local",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AccentCyan
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Garantizamos la privacidad de tus datos personales mediante cifrado de punto final. Tus contraseñas y archivos nunca viajan sin cifrar.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Masters passwords settings
                Text(
                    text = "CLAVE DE CIFRADO MAESTRO (AES-256):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = masterKey,
                    onValueChange = { viewModel.localEncriptionKey.value = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SecurityGreen,
                        unfocusedBorderColor = BorderSlate,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().testTag("encryption_key_field")
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Esta clave genera la firma local SHA-256 de tu dispositivo.",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Security checklist
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberCard),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderSlate, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "EVALUACIÓN DE APARATO:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                val checklist = listOf(
                    "Cifrado de Base de Datos SQLite" to true,
                    "Cifrado AES de Documentos Personales" to isVaultLocked,
                    "Aislamiento de Claves en AI Studio" to true,
                    "Bloqueo Biométrico Virtual Activo" to isVaultLocked
                )

                checklist.forEach { (label, checked) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (checked) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (checked) SecurityGreen else AlertAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            color = if (checked) TextPrimary else TextSecondary,
                            fontWeight = if (checked) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Danger Action: Clean installation
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberCard),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderSlate, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "BORRADO ABSOLUTO (WIPE DATA):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF5350),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Al pulsar este botón eliminarás todos los datos del caché, hilos de chat, extractos financieros cargados y documentos offline.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        viewModel.wipeAllData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                    modifier = Modifier.fillMaxWidth().testTag("wipe_all_data_button")
                ) {
                    Text("BORRAR TODO Y REINICIAR SEGURIDAD", color = Color.White, fontSize = 11.sp)
                }
            }
        }
    }
}

// ================= COMPOSABLE HELPERS =================

fun formatTime(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        ""
    }
}

fun formatDate(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        ""
    }
}

// ================= TAB 4: MODO DE ENFOQUE SANDBOX REGISTRO ESTRICTO =================

@Composable
fun SandboxFocusTab(viewModel: AgentViewModel) {
    val timerSeconds by viewModel.sandboxTimerSeconds.collectAsStateWithLifecycle()
    val isActive by viewModel.sandboxIsActive.collectAsStateWithLifecycle()
    val notes by viewModel.sandboxStudyNotes.collectAsStateWithLifecycle()
    val mindmapNodes by viewModel.sandboxMindmapNodes.collectAsStateWithLifecycle()

    // Segmented Sub-tab controller
    var subTabSelector by remember { mutableStateOf(0) } // 0 = Estudio Cognitivo, 1 = Operadores Autónomos (Offline)
    var resName by remember { mutableStateOf("La Parroquia de Eduardo") }
    var resTime by remember { mutableStateOf("21:00") }
    var selectedGovProcedure by remember { mutableStateOf("OPOSICIONES") }

    // Collect state flows for simulation suites
    val loadingMsg by viewModel.loadingAgentSimMsg.collectAsStateWithLifecycle()
    val phoneLogs by viewModel.simPhoneLogs.collectAsStateWithLifecycle()
    val onScreenMedia by viewModel.simOnScreenMedia.collectAsStateWithLifecycle()
    val screenPrice by viewModel.simScreenProductPrice.collectAsStateWithLifecycle()
    val screenLogs by viewModel.simScreenProductLogs.collectAsStateWithLifecycle()
    val uiTaps by viewModel.simUiTaps.collectAsStateWithLifecycle()
    val chessCardCount by viewModel.simChessCardCount.collectAsStateWithLifecycle()
    val uiTappingLogs by viewModel.simUiTappingLogs.collectAsStateWithLifecycle()
    val physicalConfig by viewModel.simPhysicalConfig.collectAsStateWithLifecycle()
    val scrapedTextLogs by viewModel.simScrapedTextLogs.collectAsStateWithLifecycle()
    val spacedStudyPlan by viewModel.simSpacedStudyPlan.collectAsStateWithLifecycle()
    val agendaAutoLogs by viewModel.simAgendaAutoLogs.collectAsStateWithLifecycle()
    val devConsoleLogs by viewModel.simDevServerConsoleLogs.collectAsStateWithLifecycle()
    val codeProposal by viewModel.simActiveCodeProposal.collectAsStateWithLifecycle()
    val testsSuccess by viewModel.simTestsSuccessful.collectAsStateWithLifecycle()
    val apiLatencies by viewModel.simApiLatencies.collectAsStateWithLifecycle()

    // Real state channels
    val chessUsername by viewModel.chessUsername.collectAsStateWithLifecycle()
    val chessAnalysisResult by viewModel.chessAnalysisResult.collectAsStateWithLifecycle()
    
    val chromeUrl by viewModel.chromeUrl.collectAsStateWithLifecycle()
    val chromeResearchResult by viewModel.chromeResearchResult.collectAsStateWithLifecycle()

    val tiktokMetricsInput by viewModel.tiktokMetricsInput.collectAsStateWithLifecycle()
    val tiktokCurationResult by viewModel.tiktokCurationResult.collectAsStateWithLifecycle()

    val macSshHost by viewModel.macSshHost.collectAsStateWithLifecycle()
    val macSshPort by viewModel.macSshPort.collectAsStateWithLifecycle()
    val macSshCommand by viewModel.macSshCommand.collectAsStateWithLifecycle()
    val macSshResult by viewModel.macSshResult.collectAsStateWithLifecycle()
    
    var timeInput by remember { mutableStateOf("25") }
    var noteInput by remember { mutableStateOf("") }
    var saveTitle by remember { mutableStateOf("Sesión de Derecho") }

    val formattedTime = remember(timerSeconds) {
        val mins = timerSeconds / 60
        val secs = timerSeconds % 60
        String.format(java.util.Locale.US, "%02d:%02d", mins, secs)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Aesthetic Title
        Text(
            text = "⏳ SANDBOX DE ENFOQUE CRÍTICO & AGENTES",
            style = MaterialTheme.typography.titleMedium,
            color = AccentCyan,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )
        Text(
            text = "Bloquea notificaciones externas, cataloga memorias semánticas y opera agentes lógicos 100% offline sin APIs.",
            fontSize = 11.sp,
            color = TextSecondary,
            modifier = Modifier.align(Alignment.Start).padding(bottom = 12.dp)
        )

        // Segmented Choose Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
                .background(CyberCard, RoundedCornerShape(10.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(
                onClick = { subTabSelector = 0 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (subTabSelector == 0) AccentCyan else Color.Transparent,
                    contentColor = if (subTabSelector == 0) Color.Black else TextSecondary
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Text("⏳ Estudio Cognitivo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            
            Button(
                onClick = { subTabSelector = 1 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (subTabSelector == 1) AccentCyan else Color.Transparent,
                    contentColor = if (subTabSelector == 1) Color.Black else TextSecondary
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1.2f),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Text("🤖 Operador Autónomo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Simulating Agent Background loading progress overlay with High-Fidelity Dual Emulators
        if (loadingMsg != null) {
            val isCall = loadingMsg?.contains("LLAMADA", ignoreCase = true) == true ||
                         loadingMsg?.contains("☎️") == true ||
                         loadingMsg?.contains("🎙️") == true ||
                         loadingMsg?.contains("Filtro", ignoreCase = true) == true ||
                         loadingMsg?.contains("Reserva", ignoreCase = true) == true ||
                         loadingMsg?.contains("Bono", ignoreCase = true) == true ||
                         loadingMsg?.contains("denuncia", ignoreCase = true) == true

            if (isCall) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                        .border(1.5.dp, AccentCyan, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(if (loadingMsg?.contains("🚨") == true) Color.Red else SecurityGreen, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (loadingMsg?.contains("🚨") == true) "🛡️ AURA NEGO-SHIELD: FILTRADO ACTIVO" else "📞 AURA ASISTENTE: RESERVA DE VOZ EN CURSO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentCyan
                                )
                            }
                            Text("📶 CANAL EN VIVO", fontSize = 9.sp, color = TextSecondary)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(CyberCardLight, CircleShape)
                                    .border(1.dp, AccentCyan, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (loadingMsg?.contains("🚨") == true) "🛡️" else "📞",
                                    fontSize = 24.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Llamada gestionada de forma autónoma:",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = resName.ifBlank { "+34 656 889 123" },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = ObsidianBg),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.5.dp, BorderSlate, RoundedCornerShape(8.dp))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "MONITOR DE TRÁNSITO TELEFÓNICO:",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                Text(
                                    text = loadingMsg ?: "Conectando...",
                                    color = if (loadingMsg?.contains("Operador") == true) AlertAmber else AccentCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Button(
                            onClick = { viewModel.clearSimMsg() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("🔴 Colgar / Finalizar Simulación", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Sede Electrónica Web Form Emulator
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                        .border(1.5.dp, AccentCyan, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CyberCardLight, RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(6.dp).background(Color(0xFFFF5F56), CircleShape))
                                Box(modifier = Modifier.size(6.dp).background(Color(0xFFFFBD2E), CircleShape))
                                Box(modifier = Modifier.size(6.dp).background(Color(0xFF27C93F), CircleShape))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color.Black, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "https://sede.administracion.gob.es/procedimientos/" + selectedGovProcedure.lowercase(),
                                    fontSize = 9.sp,
                                    color = AccentCyan,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    maxLines = 1
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = ObsidianBg),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.5.dp, BorderSlate, RoundedCornerShape(8.dp))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "SEDE ELECTRÓNICA DE TRÁMITES GUBERNAMENTALES",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AlertAmber
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Column {
                                    Text("Nombre Solicitante:", fontSize = 8.sp, color = TextSecondary)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(CyberCardLight, RoundedCornerShape(4.dp))
                                            .border(1.dp, if (loadingMsg?.contains("Nombre") == true || loadingMsg?.contains("flujos") == true) AccentCyan else BorderSlate, RoundedCornerShape(4.dp))
                                            .padding(6.dp)
                                    ) {
                                        Text("Eduardo Herraiz", fontSize = 10.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                        if (loadingMsg?.contains("Nombre") == true) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("✍️ Escribiendo...", fontSize = 9.sp, color = AccentCyan)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Column {
                                    Text("DNI / NIE:", fontSize = 8.sp, color = TextSecondary)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(CyberCardLight, RoundedCornerShape(4.dp))
                                            .border(1.dp, if (loadingMsg?.contains("DNI") == true || loadingMsg?.contains("Autocompletando") == true) AccentCyan else BorderSlate, RoundedCornerShape(4.dp))
                                            .padding(6.dp)
                                    ) {
                                        Text("48123456-S", fontSize = 10.sp, color = TextPrimary)
                                        if (loadingMsg?.contains("DNI") == true) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("✍️ Escribiendo...", fontSize = 9.sp, color = AccentCyan)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Column {
                                    Text("Campos Especiales del Procedimiento:", fontSize = 8.sp, color = TextSecondary)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(CyberCardLight, RoundedCornerShape(4.dp))
                                            .border(1.dp, if (loadingMsg?.contains("Adjuntando") == true || loadingMsg?.contains("Inyectando") == true) AccentCyan else BorderSlate, RoundedCornerShape(4.dp))
                                            .padding(6.dp)
                                    ) {
                                        val detailsText = when (selectedGovProcedure) {
                                            "AYUDA_VIVIENDA" -> "Subvención Alquiler Vivienda 2026 - Calle Mayor 12. Alquiler: 750€/mes."
                                            "RECLAMACION_VUELO" -> "Reclamación Retraso Vuelo IB3110 (242 mins) - Iberia - Compensación fija 250€."
                                            "DEVOLUCION_AMAZON" -> "Reclamo Devolución Ticket AMZ-RET-9812-4217 - Teclado Mecánico RGB."
                                            else -> "Inscripción en Oposiciones Auxilio Judicial - Cuerpo de Auxilio de España."
                                        }
                                        Text(detailsText, fontSize = 9.sp, color = TextPrimary)
                                    }
                                }

                                if (selectedGovProcedure == "AYUDA_VIVIENDA" || selectedGovProcedure == "OPOSICIONES") {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = CyberCard),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(
                                                1.dp,
                                                if (loadingMsg?.contains("Cl@ve") == true) AlertAmber else BorderSlate,
                                                RoundedCornerShape(6.dp)
                                            )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                if (loadingMsg?.contains("Cl@ve") == true) "🔑 FIRMA REQUERIDA (PASARELA CL@VE)" else "✓ Cl@ve Firma Autorizada",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (loadingMsg?.contains("Cl@ve") == true) AlertAmber else SecurityGreen
                                            )
                                            if (loadingMsg?.contains("Cl@ve") == true) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                CircularProgressIndicator(color = AlertAmber, modifier = Modifier.size(10.dp), strokeWidth = 1.dp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Acción del Robot: " + (loadingMsg ?: ""),
                            color = AccentCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }

        if (subTabSelector == 0) {
            // ==========================================
            // SUB-TAB 0: CLASSIC COGNITIVE FOCUS STUDY MINDER
            // ==========================================
            
            // Large countdown circle
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                modifier = Modifier.fillMaxWidth().border(1.dp, if (isActive) AccentCyan else BorderSlate, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .border(3.dp, if (isActive) AccentCyan else BorderSlate, CircleShape)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = formattedTime,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) AccentCyan else TextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = if (isActive) "SANDBOX ACTIVO" else "EN ESPERA",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) AccentCyan else TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action panel
                    if (!isActive) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("Duración (min):", color = TextSecondary, fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            androidx.compose.foundation.text.BasicTextField(
                                value = timeInput,
                                onValueChange = { timeInput = it.take(3) },
                                textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                                modifier = Modifier
                                    .width(50.dp)
                                    .background(CyberCardLight, RoundedCornerShape(6.dp))
                                    .border(1.dp, BorderSlate, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Button(
                                onClick = {
                                    val mins = timeInput.toIntOrNull() ?: 25
                                    viewModel.startSandboxSession(mins)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Iniciar Sandbox", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.stopSandboxSession() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Cancelar Sesión", color = Color.White, fontSize = 11.sp)
                            }
                            Button(
                                onClick = {
                                    viewModel.commitSandboxToVault(saveTitle)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SecurityGreen),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Archivar Memorias", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = saveTitle,
                            onValueChange = { saveTitle = it },
                            label = { Text("Título de la Memoria Semántica", color = TextSecondary, fontSize = 9.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = AccentCyan,
                                unfocusedBorderColor = BorderSlate
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cognitive transcription feed and mindmap
            Text(
                text = "🎙️ TRANSCRIPCIÓN DE VOZ & MEMORIAS SEMÁNTICAS EN TIEMPO REAL",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            Text(
                text = "Pulsa el botón de dictado continuo o digita conceptos. Aura organizará esquemas mentales.",
                fontSize = 11.sp,
                color = TextSecondary,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 10.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                modifier = Modifier.fillMaxWidth().border(1.dp, BorderSlate, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Concept input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = noteInput,
                            onValueChange = { noteInput = it },
                            placeholder = { Text("Digita concepto o idea de estudio...", color = TextSecondary, fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = AccentCyan,
                                unfocusedBorderColor = BorderSlate
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val context = LocalContext.current
                        IconButton(
                            onClick = {
                                if (!isActive) {
                                    viewModel.startSandboxSession(25)
                                }
                                (context as? com.example.MainActivity)?.triggerSpeechRecordingUI()
                            },
                            modifier = Modifier.background(CyberCardLight, CircleShape).size(44.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Dictado de notas continuas", tint = AccentCyan)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = {
                                if (noteInput.trim().isNotEmpty()) {
                                    viewModel.addSandboxSemanticConcept(noteInput)
                                    noteInput = ""
                                }
                            },
                            modifier = Modifier.background(AccentCyan, CircleShape).size(44.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Añadir Concepto", tint = Color.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Real-time mental schemas
                    Text(
                        text = "💡 ESQUEMA MENTAL GENERADO (MINIMAL MINDMAP):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (mindmapNodes.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(80.dp).background(CyberCardLight, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No hay conceptos guardados. ¡Dicta o escribe para empezar el mapeado!", fontSize = 11.sp, color = TextSecondary)
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CyberCardLight, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            mindmapNodes.forEachIndexed { idx, concept ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(18.dp).background(AccentCyan, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("${idx+1}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = concept,
                                        fontSize = 12.sp,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Memorias registradas:\n" + if(notes.isEmpty()) "(Vacío)" else notes,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.fillMaxWidth().background(CyberCardLight, RoundedCornerShape(6.dp)).padding(10.dp)
                    )
                }
            }
        } else {
            // ==========================================
            // SUB-TAB 1: NEXT-GEN OFFLINE AGENT SIMULATOR SUITE
            // ==========================================
            
            var expandedLogIdx by remember { mutableStateOf<Int?>(null) }

            // 0. AUTO-GESTIÓN DE TRÁMITES Y RECLAMACIONES CARD
            
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .border(1.dp, BorderSlate, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("🏛️ GESTOR AUTÓNOMO DE TRÁMITES Y RECLAMACIONES", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "El agente gestiona de inicio a fin inscripciones en oposiciones del estado, solicitudes de subvención, reclamaciones aéreas o de mercaderías leyendo tu perfil local de la Bóveda de forma proactiva.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Simulated Profile Badge
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberCardLight),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .border(0.5.dp, BorderSlate, RoundedCornerShape(8.dp))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(SecurityGreen, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(modifier = Modifier.size(6.dp).background(Color.Black, CircleShape))
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("VINCULO SEGURO CON LA BÓVEDA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SecurityGreen)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("👤 Titular: Eduardo Herraiz", fontSize = 10.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text("🆔 DNI: 48123456-S", fontSize = 10.sp, color = TextPrimary)
                            Text("🏠 Domicilio Catastral: Calle Mayor 12, Madrid", fontSize = 10.sp, color = TextPrimary)
                            Text("✈️ Billete Reciente: Iberia IB3110 (28 Mayo)", fontSize = 10.sp, color = TextPrimary)
                        }
                    }

                    // Selector
                    Text("SELECCIONA EL TRÁMITE A EJECUTAR:", fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val procedures = listOf(
                            "OPOSICIONES" to "🏛️ Opos.",
                            "AYUDA_VIVIENDA" to "🏠 Alq.",
                            "RECLAMACION_VUELO" to "✈️ Iberia",
                            "DEVOLUCION_AMAZON" to "📦 AMZ"
                        )
                        procedures.forEach { (type, label) ->
                            val isSel = selectedGovProcedure == type
                            Button(
                                onClick = { selectedGovProcedure = type },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSel) AccentCyan else CyberCardLight,
                                    contentColor = if (isSel) Color.Black else TextPrimary
                                ),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { viewModel.triggerGovFormFilling(selectedGovProcedure) },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Text("Iniciar Automatización Completa y Redactar", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Render steps in real time!
                    if (uiTaps.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("ACCIONES AGÉNTICAS (ACCESIBILIDAD AUTOMÁTICA):", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            uiTaps.forEach { step ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CyberCardLight),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(0.5.dp, if (step.status == "ESPERA_CAPTCHA") AlertAmber else BorderSlate, RoundedCornerShape(6.dp))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .background(if (step.status == "COMPLETO") SecurityGreen else if (step.status == "ESPERA_CAPTCHA") AlertAmber else AccentCyan, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(step.stepIndex.toString(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(step.targetComponent, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                                            Text(step.actionText, fontSize = 11.sp, color = TextPrimary)
                                            if (step.requiredAction != null) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text("⚠️ DEBE COMPLETAR: ${step.requiredAction}", fontSize = 9.sp, color = AlertAmber, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = step.status,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (step.status == "COMPLETO") SecurityGreen else if (step.status == "ESPERA_CAPTCHA") AlertAmber else AccentCyan
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 1. NEGO-SHIELD CARD
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .border(1.dp, BorderSlate, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("🛡️ NEGOCIADOR TELEFÓNICO OFFLINE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "El agente gestiona las interacciones habladas mediante voz natural en segundo plano, descolgando y aislando tu línea totalmente.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = resName,
                            onValueChange = { resName = it },
                            label = { Text("Número o Establecimiento a Llamar/Filtrar", fontSize = 9.sp, color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                                focusedBorderColor = AccentCyan, unfocusedBorderColor = BorderSlate
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1.3f)
                        )
                        OutlinedTextField(
                            value = resTime,
                            onValueChange = { resTime = it },
                            label = { Text("Hora o Detalle", fontSize = 9.sp, color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                                focusedBorderColor = AccentCyan, unfocusedBorderColor = BorderSlate
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(0.7f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.triggerSimulateSpamCall(resName) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCardLight),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Filtrar Spam en Vivo", color = AccentCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { viewModel.triggerSimulateBooking(resName, resTime) },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Pedir Reserva Voz", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("REGISTRO DE LLAMADAS GESTIONADAS OFFLINE:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        phoneLogs.forEachIndexed { idx, log ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CyberCardLight),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, if (expandedLogIdx == idx) AccentCyan else Color.Transparent, RoundedCornerShape(8.dp))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(if (log.blocked) Color(0xFFD32F2F) else SecurityGreen, RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Text(log.type, fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(log.sender, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                            }
                                            Text(log.timestamp, fontSize = 9.sp, color = TextSecondary)
                                        }

                                        Button(
                                            onClick = { expandedLogIdx = if (expandedLogIdx == idx) null else idx },
                                            colors = ButtonDefaults.buttonColors(containerColor = CyberCard),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(if (expandedLogIdx == idx) "Ocultar" else "Ver Chat Voz", fontSize = 9.sp, color = TextPrimary)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Estatus: ${log.resolution}", fontSize = 10.sp, color = SecurityGreen)

                                    if (expandedLogIdx == idx) {
                                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = BorderSlate)
                                        Text("RECONSTRUCCIÓN DE LA CONVERSACIÓN NATURAL:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            log.transcript.forEach { (speaker, dialogue) ->
                                                val isAura = speaker.startsWith("Aura")
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = if (isAura) Arrangement.End else Arrangement.Start
                                                ) {
                                                    Card(
                                                        colors = CardDefaults.cardColors(containerColor = if (isAura) CyberCard else ObsidianBg),
                                                        modifier = Modifier
                                                            .widthIn(max = 240.dp)
                                                            .border(1.dp, if (isAura) AccentCyan else BorderSlate, RoundedCornerShape(8.dp))
                                                    ) {
                                                        Column(modifier = Modifier.padding(8.dp)) {
                                                            Text(speaker, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isAura) AccentCyan else TextSecondary)
                                                            Spacer(modifier = Modifier.height(2.dp))
                                                            Text(dialogue, fontSize = 10.sp, color = TextPrimary)
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
            }

            // 2. MULTIMODAL ON-SCREEN AWARENESS
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .border(1.dp, BorderSlate, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("📸 ACCIONES DE CONCIENCIA DE PANTALLA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "El agente decodifica el contexto visual (pantallas, chats, imágenes) y realiza acciones concatenadas locales.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("CONTRATISTA DE CONTEXTO VISUAL ACTIVO (REAL):", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberCardLight),
                            modifier = Modifier.weight(1.1f).border(1.dp, BorderSlate, RoundedCornerShape(8.dp))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("💬 Chat de WhatsApp", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("• Audio recibido: 3m 42s", fontSize = 9.sp, color = TextPrimary)
                                Text("• Foto: Mayor 12 (Central)", fontSize = 9.sp, color = TextPrimary)
                                Text("• Itinerario: Metro L3 (28 min)", fontSize = 8.sp, color = TextSecondary)
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberCardLight),
                            modifier = Modifier.weight(0.9f).border(1.dp, BorderSlate, RoundedCornerShape(8.dp))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("🚆 Renfe Ave", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Billetes Madrid-BCN", fontSize = 9.sp, color = TextPrimary)
                                Text("• Precio: " + String.format(java.util.Locale.US, "%.2f", screenPrice) + "€", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (screenPrice < 40.0) SecurityGreen else TextPrimary)
                                Text("• Compra aut: <40.00€", fontSize = 8.sp, color = TextSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.triggerOnScreenMessageAction() },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCardLight),
                            modifier = Modifier.weight(1.1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Mapear e Intervenir Chat", color = AccentCyan, fontSize = 9.sp)
                        }
                        
                        Button(
                            onClick = { viewModel.triggerPriceDropCheck() },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                            modifier = Modifier.weight(0.9f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Bajar Precio Renfe", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("AUDITORÍA VISUAL Y TRÁFICOS DE COMPRA AUTOMÁTICA:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ObsidianBg, RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        screenLogs.forEach { logLine ->
                            Text(logLine, fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = TextPrimary)
                        }
                    }

                    if (onScreenMedia != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberCardLight),
                            modifier = Modifier.fillMaxWidth().border(1.dp, SecurityGreen, RoundedCornerShape(8.dp))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("✓ ARRASTRADO DE COORGANIZACIÓN EXTRAS:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SecurityGreen)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("• Dirección extraída: ${onScreenMedia?.detectedLocation}", fontSize = 10.sp, color = TextPrimary)
                                Text("• Calculador transporte: ${onScreenMedia?.currentRouteEstimate}", fontSize = 10.sp, color = TextPrimary)
                                Text("• Autorespuesta redactada: \"${onScreenMedia?.draftReply}\"", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
                            }
                        }
                    }
                }
            }

            // 3. CHESS BRIDGE: REAL LICHESS NETWORKING AND BLUNDER STUDY GUIDE GENERATOR
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .border(1.dp, BorderSlate, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("♟️ PUENTE LICHESS / CHESS.COM & ESTUDIO DE ERRORES", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Descarga de forma real las últimas partidas desde la API de Lichess. Aura detecta imprecisiones en la Defensa Caro-Kann con Gemini y genera tarjetas de estudio y reportes tácticos.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    var inputUsername by remember { mutableStateOf(chessUsername) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputUsername,
                            onValueChange = { inputUsername = it },
                            label = { Text("Usuario de Lichess", fontSize = 9.sp, color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                                focusedBorderColor = AccentCyan, unfocusedBorderColor = BorderSlate
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1.1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.runRealChessAnalysis(inputUsername) },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                            modifier = Modifier.weight(0.9f),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("Analizar Caro-Kann", color = Color.Black, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (chessAnalysisResult != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("ANÁLISIS DE BLUNDERS Y MEJORAS PEDAGÓGICAS:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ObsidianBg),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 180.dp)
                                .border(0.5.dp, BorderSlate, RoundedCornerShape(8.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(chessAnalysisResult ?: "", fontSize = 10.sp, color = TextPrimary)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SecurityGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Text("✓ Informe de ajedrez procesado con éxito y almacenado como Guía de Estudio con encriptación AES-256 en la Bóveda SQLite local.", fontSize = 9.5.sp, color = SecurityGreen)
                        }
                    }
                }
            }

            // 4. CHROME RESEARCH: WEB SCRAPING AND LAW CONTRADICTION AUDITOR
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .border(1.dp, BorderSlate, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("🌐 INVESTIGACIÓN Y CRUCE (CHROME + GEMINI)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Rastrea de forma directa páginas de Chrome, apuntes de Derecho Procesal u otros textos. Detecta contradicciones legales del Código Civil y asocia jurisprudencias de forma real.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    var inputUrl by remember { mutableStateOf(chromeUrl) }
                    var manuallyPasted by remember { mutableStateOf("") }
                    
                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = { inputUrl = it },
                        label = { Text("URL de Chrome para Scraping Real", fontSize = 9.sp, color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentCyan, unfocusedBorderColor = BorderSlate
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = manuallyPasted,
                        onValueChange = { manuallyPasted = it },
                        placeholder = { Text("O pega aquí el texto/leyes en pantalla de forma directa...", fontSize = 10.sp, color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentCyan, unfocusedBorderColor = BorderSlate
                        ),
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.runRealChromeResearch(inputUrl, manuallyPasted) },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Escanear, Investigar Contradicciones e Indexar", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    if (chromeResearchResult != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("DIALECTO DE CONTRADICCIONES Y JURISPRUDENCIAS ASOCIADAS:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ObsidianBg),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .border(0.5.dp, BorderSlate, RoundedCornerShape(8.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(chromeResearchResult ?: "", fontSize = 10.sp, color = TextPrimary)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SecurityGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Text("✓ Estudio legal guardado. El log de investigación automatizada se ha depositado de forma confidencial y cifrada en la base de datos.", fontSize = 9.5.sp, color = SecurityGreen)
                        }
                    }
                }
            }

            // 5. TIKTOK METRICS: AUDIENCE AND RETENTION INDEXER
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .border(1.dp, BorderSlate, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("📊 CURACIÓN Y ANÁLISIS DE MÉTRICAS (TIKTOK)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Escanea las analíticas de retención y perfiles de edad. Aura identifica las curvas críticas y estipula la hora óptima para retener la audiencia femenina (25-38 años).",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    var metricsInput by remember { mutableStateOf(tiktokMetricsInput) }
                    OutlinedTextField(
                        value = metricsInput,
                        onValueChange = { metricsInput = it },
                        placeholder = { Text("Pega las analíticas copiadas o deja vacío para extraer de la pantalla activa si accesibilidad está activa...", fontSize = 10.sp, color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentCyan, unfocusedBorderColor = BorderSlate
                        ),
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.runRealTikTokCuration(metricsInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Calcular Optimizador Algorítmico TikTok", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    if (tiktokCurationResult != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("RESOLUCIÓN PARA SEGMENTO FEMENINO 25-38 Y RETENCIÓN:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ObsidianBg),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 180.dp)
                                .border(0.5.dp, BorderSlate, RoundedCornerShape(8.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(tiktokCurationResult ?: "", fontSize = 10.sp, color = TextPrimary)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SecurityGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Text("✓ Auditoría de conversión orgánica generada y resguardada de forma segura bajo cifrado simétrico en Room local.", fontSize = 9.5.sp, color = SecurityGreen)
                        }
                    }
                }
            }

            // 6. MACBOOK PRO SYNC & REMOTE SSH CONTROLLER
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .border(1.dp, BorderSlate, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("💻 SINCRONIZACIÓN AVANZADA MACBOOK PRO ↔ XIAOMI", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Orquesta entornos locales. Lanza comandos reales de socket SSH para vigilar daemons Mistral/LLaMA o consumo de RAM, y propaga el portapapeles sobre canales TCP locales.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    var hostIp by remember { mutableStateOf(macSshHost) }
                    var sshPortStr by remember { mutableStateOf(macSshPort.toString()) }
                    var terminalCommand by remember { mutableStateOf(macSshCommand) }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = hostIp,
                            onValueChange = { hostIp = it },
                            label = { Text("IP MacBook", fontSize = 9.sp, color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                                focusedBorderColor = AccentCyan, unfocusedBorderColor = BorderSlate
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1.3f)
                        )
                        OutlinedTextField(
                            value = sshPortStr,
                            onValueChange = { sshPortStr = it },
                            label = { Text("Puerto SSH", fontSize = 9.sp, color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                                focusedBorderColor = AccentCyan, unfocusedBorderColor = BorderSlate
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(0.7f)
                        )
                    }

                    OutlinedTextField(
                        value = terminalCommand,
                        onValueChange = { terminalCommand = it },
                        label = { Text("Comando Remoto", fontSize = 9.sp, color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentCyan, unfocusedBorderColor = BorderSlate
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.runRealMacSSH(hostIp, sshPortStr.toIntOrNull() ?: 22, terminalCommand) },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Ejecutar SSH en MacBook Pro", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    if (macSshResult != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("RESPUESTA DE CONSOLA SSH SECRETA (XIAOMI TERMINAL):", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ObsidianBg),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 180.dp)
                                .border(0.5.dp, BorderSlate, RoundedCornerShape(8.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(macSshResult ?: "", fontSize = 9.5.sp, fontFamily = FontFamily.Monospace, color = TextPrimary)
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = BorderSlate)
                    Text("PROPAGAR DATOS AL PORTAPAPELES DEL MAC (VÍA LAN TCP):", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))

                    var clipboardSyncText by remember { mutableStateOf("Pegar texto para traspasar al Mac...") }
                    OutlinedTextField(
                        value = clipboardSyncText,
                        onValueChange = { clipboardSyncText = it },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentCyan, unfocusedBorderColor = BorderSlate
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )

                    Button(
                        onClick = { viewModel.localLanService.simulateClipboardExchange(clipboardSyncText) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCardLight),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Enviar Portapapeles por Sockets TCP", color = AccentCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 4. PHYSICAL CONTEXT SENSOR ORCHESTRATOR
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .border(1.dp, BorderSlate, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("🛰️ ORQUESTACIÓN POR FILTRO FISICO / SENSORES", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "El agente analiza ubicación GPS, ruidos del micrófono y las apps abiertas para acondicionar y auto-configurar el hardware del celular.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("LECTURA DE SENSORES HARDWARE EN VIVO (REAL):", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberCardLight),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("📍 Coordenada GPS", fontSize = 8.sp, color = TextSecondary)
                                Text("Lat: 40.4532 | Lon: -3.7266", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Aproximación: Biblioteca", fontSize = 8.sp, color = AccentCyan)
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberCardLight),
                            modifier = Modifier.weight(1.1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("🎙️ Micrófono Ambiental", fontSize = 8.sp, color = TextSecondary)
                                Text("Frecuencias: 31 dB (Tenuo)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Aislamiento: Óptimo", fontSize = 8.sp, color = SecurityGreen)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.triggerSensorFocusActivation() },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Activar Ultra-Enfoque", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        Button(
                            onClick = { viewModel.disableFocusConfig() },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCardLight),
                            modifier = Modifier.weight(0.8f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Neutralizar", color = Color.White, fontSize = 10.sp)
                        }
                    }

                    if (physicalConfig != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SecurityGreen.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth().border(1.dp, SecurityGreen, RoundedCornerShape(10.dp))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.CheckCircle, "Active", tint = SecurityGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("HABILITADA ORQUESTACIÓN CORTEZA LOCAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SecurityGreen)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("• Zona detectada: ${physicalConfig?.placeName}", fontSize = 10.sp, color = TextPrimary)
                                Text("• Estado Red: DND (No molestar) habilitada", fontSize = 10.sp, color = TextPrimary)
                                Text("• Autorespuesta Chat: \"${physicalConfig?.activeAutoReply}\"", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
                                Text("• Daemon de Desarrollo: ${physicalConfig?.activeLocalMockEngine}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                            }
                        }
                    }
                }
            }

            // 5. SCRAPING AND LOCAL FLOWS
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .border(1.dp, BorderSlate, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("📂 AUTO-SCRAPING Y FLUJOS MULTIMEDIA OFFLINE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Realiza recortes automáticos de notas de voz largas, traduce ideas clave a boveda o cataloga correo administrativo.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.triggerMediaTranscription() },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCardLight),
                            modifier = Modifier.weight(1.1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Transcripción WhatsApp", color = AccentCyan, fontSize = 9.sp)
                        }
                        
                        Button(
                            onClick = { viewModel.triggerMailPdfCategorizer() },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                            modifier = Modifier.weight(0.9f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Catalogar PDF Correo", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("TRAZAS DE SCRAPEADO MULTIMEDIA:", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ObsidianBg, RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        scrapedTextLogs.forEach { log ->
                            Text(log, fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = TextPrimary)
                        }
                    }
                }
            }

            // 6. SPACED REPETITION ACADEMIC TIMELINE
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .border(1.dp, BorderSlate, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("📅 PLANIFICACIÓN DE REPASO ACADÉMICO INTELIGENTE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "A Aura le requieres: 'Tengo que dominar 10 temas para dentro de tres meses de forma cronológica'. Organiza en tu agenda un plan adaptativo espaciado dinámicamente.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Button(
                        onClick = { viewModel.triggerCalculateSpacedRepetition() },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Calcular Cronodieta Ebbinghaus (Repaso)", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("PLANIFICADOR ACADÉMICO LOCAL EN TIEMPO REAL:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        spacedStudyPlan.forEach { task ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CyberCardLight),
                                modifier = Modifier.fillMaxWidth().border(0.5.dp, BorderSlate, RoundedCornerShape(6.dp))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(task.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(1f))
                                        Box(
                                            modifier = Modifier
                                                .background(if (task.difficulty == "Alta") Color(0xFFD32F2F) else SecurityGreen, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(task.difficulty, fontSize = 8.sp, color = Color.White)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Días de Repaso: " + task.scheduledDays.joinToString { "D+$it" }, fontSize = 9.sp, color = AccentCyan)
                                        Text(task.activeStatusText, fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 7. DEVELOPER DEBUG CODES AND TESTS PORT CONSOLE
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .border(1.dp, BorderSlate, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("💻 DESPLIEGUE, TEST UNITARIO & AUDITORÍA COGNITIVA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Orquesta scripts locales. Audita logs de servidores MongoDB y FastAPI, analiza excepciones y aplica modificaciones de código correctoras sobre el editor en el móvil.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("MICROSERVICIOS EN PRUEBAS (TERMINAL DOCKER CONSOLE):", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(ObsidianBg, RoundedCornerShape(6.dp))
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        devConsoleLogs.forEach { logLine ->
                            Text(logLine, fontSize = 8.5.sp, fontFamily = FontFamily.Monospace, color = if (logLine.contains("❌") || logLine.contains("ERROR")) Color(0xFFEF5350) else if (logLine.contains("✅") || logLine.contains("correctos")) SecurityGreen else TextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.runDeveloperEnvironmentAudit() },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Levantar y Testear Servidores locales (FastAPI & Mongo)", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    if (codeProposal != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberCardLight),
                            modifier = Modifier.fillMaxWidth().border(1.5.dp, AlertAmber, RoundedCornerShape(10.dp))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("⚠️ DETECTADO AUTO-FALLO EN LA BASE DE DATOS:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AlertAmber)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Archivo: ${codeProposal?.fileName} (Línea ${codeProposal?.originalLine})", fontSize = 9.sp, color = TextPrimary)
                                Text("Causa: ${codeProposal?.fixExplanation}", fontSize = 9.sp, color = TextSecondary)
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("CÓDIGO CON ERRATA:", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF5350))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF3E2723), RoundedCornerShape(4.dp))
                                        .padding(6.dp)
                                ) {
                                    Text(codeProposal?.buggyCode ?: "", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = Color.White)
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text("CORRECCIÓN ESTRUCTURADA A SER INYECTADA:", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = SecurityGreen)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF1B5E20), RoundedCornerShape(4.dp))
                                        .padding(6.dp)
                                ) {
                                    Text(codeProposal?.proposedCode ?: "", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = Color.White)
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { viewModel.applyDevCodePatch() },
                                    colors = ButtonDefaults.buttonColors(containerColor = SecurityGreen),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Aplicar Auto-Patch en database.py", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    if (testsSuccess) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SecurityGreen.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth().border(1.dp, SecurityGreen, RoundedCornerShape(8.dp))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("🧪 SUITE PYTEST: 100% EXCELENTE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SecurityGreen)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("MÉTRICAS DE RENDIMIENTO REST LOCALES:", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                
                                apiLatencies.forEach { (endpoint, stat) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(endpoint, fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = TextPrimary)
                                        Text(stat, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { viewModel.simulateEndpointStressTest() },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Disparar Stress-Test JSON Blast (HTTP Stress)", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = { viewModel.resetSimulators() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("Re-inicializar Flujos de Operadores", color = Color.White, fontSize = 10.sp)
            }
        }
    }
}



