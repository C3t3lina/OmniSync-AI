package com.example

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.speech.RecognizerIntent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AgentViewModel
import com.example.ui.DashboardScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var modelRef: AgentViewModel

    // Speech recognition launcher for real STT
    private val speechLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenTextList = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val text = spokenTextList?.firstOrNull() ?: ""
            if (::modelRef.isInitialized && text.isNotEmpty()) {
                modelRef.onSpeechRecognized(text)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val viewModel: AgentViewModel = viewModel()
                modelRef = viewModel
                
                // Set up functional biometric hooks and speech hooks
                setupViewModelHooks(viewModel)
                
                DashboardScreen(viewModel = viewModel)
            }
        }

        // Handle text shared on startup/cold launch
        handleIncomingIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent != null && intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val textToImport = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!textToImport.isNullOrEmpty()) {
                // Wait for ViewModel init then dispatch
                lifecycleScope.launch {
                    while (!::modelRef.isInitialized) {
                        kotlinx.coroutines.delay(100)
                    }
                    modelRef.handleSharedTextArrival(textToImport)
                }
            }
        }
    }

    private fun setupViewModelHooks(viewModel: AgentViewModel) {
        // Biometric authentication trigger using built-in Android Pie+ hardware prompt
        viewModel.triggerBiometricPrompt = {
            runOnUiThread {
                executeNativeBiometricUnlock(viewModel)
            }
        }
        
        // Listen to isVaultLocked state to toggle WindowManager LayoutParams.FLAG_SECURE
        // Prevents screenshots, video recordings, and recents thumbnail from exposing confidential Vault data!
        lifecycleScope.launch {
            viewModel.isVaultLocked.collectLatest { locked ->
                runOnUiThread {
                    if (locked) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    }
                }
            }
        }
    }

    fun triggerSpeechRecordingUI() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Dicta notas o correos para Aura...")
            }
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            // Speech recognizer not installed or broken
        }
    }

    private fun executeNativeBiometricUnlock(viewModel: AgentViewModel) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                val executor = mainExecutor
                val prompt = android.hardware.biometrics.BiometricPrompt.Builder(this)
                    .setTitle("Desbloqueo Biométrico Aura")
                    .setSubtitle("Autenticación para descifrar la Bóveda AES-256")
                    .setDescription("Verifica tu Face o Huella para derivar las claves de descifrado local.")
                    .setNegativeButton("Cancelar / Usar PIN", executor) { _, _ -> }
                    .build()

                val cancellationSignal = CancellationSignal()
                prompt.authenticate(
                    cancellationSignal,
                    executor,
                    object : android.hardware.biometrics.BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: android.hardware.biometrics.BiometricPrompt.AuthenticationResult?) {
                            super.onAuthenticationSucceeded(result)
                            runOnUiThread {
                                viewModel.onBiometricUnlocked()
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                // Fallback to PIN
                com.example.ui.AuraBackgroundAuditor.triggerLocalAlert(
                    this,
                    "Hardware Sin Configurar",
                    "No se detectaron huellas o FaceID guardados. Usa el PIN por defecto '1234'."
                )
            }
        } else {
            // fallback
            com.example.ui.AuraBackgroundAuditor.triggerLocalAlert(
                this,
                "API de Biometría No Soportada",
                "Tu versión de Android no soporta biometría nativa. Usa el PIN '1234'."
            )
        }
    }
}
