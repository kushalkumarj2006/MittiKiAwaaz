package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.AppDatabase
import com.example.data.network.GeminiService
import com.example.data.repo.MittiRepository
import com.example.ui.screens.DisasterAlertsScreen
import com.example.ui.screens.SarpanchDashboardScreen
import com.example.ui.screens.SoilScanScreen
import com.example.ui.screens.VoiceHomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.MittiGreen
import com.example.ui.theme.PureWhite
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.MittiViewModel
import com.example.ui.viewmodel.MittiViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MittiViewModel
    private var speechRecognizer: SpeechRecognizer? = null

    private val requestAudioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startSpeechInput()
        } else {
            Toast.makeText(
                this,
                "Microphone permission is required for voice queries. You can still type!",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Setup local persistence & services
        val database = AppDatabase.getDatabase(this)
        val geminiService = GeminiService()
        val repository = MittiRepository(
            soilScanDao = database.soilScanDao(),
            disasterAlertDao = database.disasterAlertDao(),
            schemeApplicationDao = database.schemeApplicationDao(),
            geminiService = geminiService
        )

        // 2. Instantiate ViewModel with standard factory
        val factory = MittiViewModelFactory(application, repository)
        viewModel = ViewModelProvider(this, factory)[MittiViewModel::class.java]

        // 3. Initialize native SpeechRecognizer
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                setRecognitionListener(createSpeechListener())
            }
        }

        setContent {
            MyApplicationTheme {
                val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
                if (isLoggedIn) {
                    MainLayout(
                        viewModel = viewModel,
                        onSpeechTrigger = { checkAudioPermissionAndStart() }
                    )
                } else {
                    LoginScreen(viewModel = viewModel)
                }
            }
        }
    }

    private fun checkAudioPermissionAndStart() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                startSpeechInput()
            }
            else -> {
                requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun startSpeechInput() {
        if (speechRecognizer == null) {
            // Speech recognition not available on some headless virtual emulators
            // Provide a graceful fallback popup so the developer is aware
            Toast.makeText(this, "Native Speech Engine unavailable. Please tap Quick Shortcuts or Type!", Toast.LENGTH_SHORT).show()
            viewModel.startListening()
            // Wait 2.5 seconds and simulate speech input
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (viewModel.isListening.value) {
                    val fallbackQueries = when (viewModel.currentLanguage.value) {
                        AppLanguage.ENGLISH -> "Check my soil health"
                        AppLanguage.HINDI -> "मेरी मिट्टी जाँचो"
                        AppLanguage.KANNADA -> "ಮಣ್ಣಿನ ಪರೀಕ್ಷೆ ಮಾಡಿ"
                        AppLanguage.MARATHI -> "माती परीक्षण करा"
                    }
                    viewModel.submitQuery(fallbackQueries)
                }
            }, 2500)
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            val langTag = when (viewModel.currentLanguage.value) {
                AppLanguage.ENGLISH -> "en-IN"
                AppLanguage.HINDI -> "hi-IN"
                AppLanguage.KANNADA -> "kn-IN"
                AppLanguage.MARATHI -> "mr-IN"
            }
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, langTag)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, true)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }

        try {
            speechRecognizer?.startListening(intent)
            viewModel.startListening()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to start speech engine: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createSpeechListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {
            viewModel.cancelListening()
        }

        override fun onError(error: Int) {
            viewModel.cancelListening()
            val errorMsg = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Client speech error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer busy"
                SpeechRecognizer.ERROR_SERVER -> "Server error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input detected"
                else -> "Voice recognition failed"
            }
            Toast.makeText(this@MainActivity, "$errorMsg. Try quick actions or typing!", Toast.LENGTH_SHORT).show()
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val bestMatch = matches[0]
                viewModel.submitQuery(bestMatch)
            } else {
                Toast.makeText(this@MainActivity, "Couldn't hear clearly. Try again!", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        super.onDestroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(
    viewModel: MittiViewModel,
    onSpeechTrigger: () -> Unit
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.ENGLISH -> "Mitti Ki Awaaz"
                            AppLanguage.HINDI -> "मिट्टी की आवाज़"
                            AppLanguage.KANNADA -> "ಮಣ್ಣಿನ ಧ್ವನಿ"
                            AppLanguage.MARATHI -> "मातीचा आवाज"
                        },
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (currentScreen == "settings") {
                                viewModel.navigateTo("voice_home")
                            } else {
                                viewModel.navigateTo("settings")
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (currentScreen == "settings") Icons.Filled.Close else Icons.Filled.Settings,
                            contentDescription = "Settings",
                            tint = PureWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MittiGreen
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = PureWhite,
                tonalElevation = 8.dp
            ) {
                // Nav Item 1: Voice Home
                NavigationBarItem(
                    selected = currentScreen == "voice_home",
                    onClick = { viewModel.navigateTo("voice_home") },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == "voice_home") Icons.Filled.RecordVoiceOver else Icons.Outlined.RecordVoiceOver,
                            contentDescription = "Voice Assistant"
                        )
                    },
                    label = { Text("कृषि सखी", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MittiGreen,
                        selectedTextColor = MittiGreen,
                        indicatorColor = MittiGreen.copy(alpha = 0.1f)
                    )
                )

                // Nav Item 2: Soil Scan
                NavigationBarItem(
                    selected = currentScreen == "soil_scan",
                    onClick = { viewModel.navigateTo("soil_scan") },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == "soil_scan") Icons.Filled.Analytics else Icons.Outlined.Analytics,
                            contentDescription = "Soil Scan"
                        )
                    },
                    label = { Text("मिट्टी जाँच", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MittiGreen,
                        selectedTextColor = MittiGreen,
                        indicatorColor = MittiGreen.copy(alpha = 0.1f)
                    )
                )

                // Nav Item 3: Disaster Warnings
                NavigationBarItem(
                    selected = currentScreen == "disaster_alerts",
                    onClick = { viewModel.navigateTo("disaster_alerts") },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == "disaster_alerts") Icons.Filled.CrisisAlert else Icons.Outlined.CrisisAlert,
                            contentDescription = "Warnings"
                        )
                    },
                    label = { Text("चेतावनी", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MittiGreen,
                        selectedTextColor = MittiGreen,
                        indicatorColor = MittiGreen.copy(alpha = 0.1f)
                    )
                )

                // Nav Item 4: Sarpanch Dashboard
                NavigationBarItem(
                    selected = currentScreen == "sarpanch_dashboard",
                    onClick = { viewModel.navigateTo("sarpanch_dashboard") },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == "sarpanch_dashboard") Icons.Filled.SupervisorAccount else Icons.Outlined.SupervisorAccount,
                            contentDescription = "Sarpanch"
                        )
                    },
                    label = { Text("सरपंच", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MittiGreen,
                        selectedTextColor = MittiGreen,
                        indicatorColor = MittiGreen.copy(alpha = 0.1f)
                    )
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                "voice_home" -> VoiceHomeScreen(
                    viewModel = viewModel,
                    onSpeechTrigger = onSpeechTrigger
                )
                "soil_scan" -> SoilScanScreen(viewModel = viewModel)
                "disaster_alerts" -> DisasterAlertsScreen(viewModel = viewModel)
                "sarpanch_dashboard" -> SarpanchDashboardScreen(viewModel = viewModel)
                "settings" -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
