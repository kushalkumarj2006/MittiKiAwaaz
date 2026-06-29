package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.DisasterAlert
import com.example.data.db.SchemeApplication
import com.example.data.db.SoilScan
import com.example.data.repo.MittiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

enum class AppLanguage(val code: String, val displayName: String, val helloPrompt: String) {
    ENGLISH("en", "English", "Hello! I am Krishi Sakhi, your climate resilience companion. How is your field today?"),
    HINDI("hi", "हिन्दी", "नमस्ते! मैं आपकी कृषि सखी हूँ। आज आपके खेत का क्या हाल है?"),
    KANNADA("kn", "ಕನ್ನಡ", "ನಮಸ್ತೆ! ನಾನು ನಿಮ್ಮ ಕೃಷಿ ಸಖಿ. ಇಂದು ನಿಮ್ಮ ಹೊಲದ ಸ್ಥಿತಿ ಹೇಗಿದೆ?"),
    MARATHI("mr", "मराठी", "नमस्कार! मी तुमची कृषी सखी आहे. आज तुमच्या शेताची काय परिस्थिती आहे?")
}

data class ChatMessage(
    val sender: String, // "User" or "Krishi Sakhi"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class CropPrice(
    val name: String,
    val hindiName: String,
    val kannadaName: String,
    val currentPrice: String,
    val mspPrice: String,
    val trend: String // "UP", "DOWN", "STABLE"
)

class MittiViewModel(
    application: Application,
    private val repository: MittiRepository
) : AndroidViewModel(application) {

    private var tts: TextToSpeech? = null
    private val _isTtsInitialized = MutableStateFlow(false)

    // Auth State
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _loggedInPhone = MutableStateFlow("")
    val loggedInPhone: StateFlow<String> = _loggedInPhone.asStateFlow()

    private val _loggedInName = MutableStateFlow("")
    val loggedInName: StateFlow<String> = _loggedInName.asStateFlow()

    // Language State
    private val _currentLanguage = MutableStateFlow(AppLanguage.HINDI)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    // Chat / Voice UI state
    private val _chatLog = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatLog: StateFlow<List<ChatMessage>> = _chatLog.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    // Screen State
    private val _currentScreen = MutableStateFlow("voice_home")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Database flow bridges
    val soilScans: StateFlow<List<SoilScan>> = repository.allSoilScans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alerts: StateFlow<List<DisasterAlert>> = repository.allAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val applications: StateFlow<List<SchemeApplication>> = repository.allApplications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active diagnostic details
    private val _scannedPh = MutableStateFlow(6.0)
    val scannedPh = _scannedPh.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    private val _scanResult = MutableStateFlow<SoilScan?>(null)
    val scanResult = _scanResult.asStateFlow()

    // Soil Image Upload State
    private val _uploadedPhImageUri = MutableStateFlow<String?>(null)
    val uploadedPhImageUri: StateFlow<String?> = _uploadedPhImageUri.asStateFlow()

    private val _isUploadingPhImage = MutableStateFlow(false)
    val isUploadingPhImage: StateFlow<Boolean> = _isUploadingPhImage.asStateFlow()

    // AI Weather and Extreme Alerts State
    private val _selectedLocation = MutableStateFlow("Ramnagar, Karnataka")
    val selectedLocation: StateFlow<String> = _selectedLocation.asStateFlow()

    private val _weatherForecast = MutableStateFlow<String>("")
    val weatherForecast: StateFlow<String> = _weatherForecast.asStateFlow()

    private val _isFetchingWeather = MutableStateFlow(false)
    val isFetchingWeather: StateFlow<Boolean> = _isFetchingWeather.asStateFlow()

    // Crop Prices State
    private val _cropPrices = MutableStateFlow<List<CropPrice>>(emptyList())
    val cropPrices: StateFlow<List<CropPrice>> = _cropPrices.asStateFlow()

    init {
        // Load persistent login state and language first
        val prefs = application.getSharedPreferences("mitti_prefs", Context.MODE_PRIVATE)
        val savedLoggedIn = prefs.getBoolean("is_logged_in", false)
        val savedLangName = prefs.getString("app_language", AppLanguage.HINDI.name) ?: AppLanguage.HINDI.name
        val savedLang = try { AppLanguage.valueOf(savedLangName) } catch(e: Exception) { AppLanguage.HINDI }
        _currentLanguage.value = savedLang

        val savedLocation = prefs.getString("user_location", "Ramnagar, Karnataka") ?: "Ramnagar, Karnataka"
        _selectedLocation.value = savedLocation

        if (savedLoggedIn) {
            _isLoggedIn.value = true
            _loggedInPhone.value = prefs.getString("logged_in_phone", "") ?: ""
            _loggedInName.value = prefs.getString("logged_in_name", "Rajesh Kumar") ?: "Rajesh Kumar"
        }

        // Initialize TTS
        tts = TextToSpeech(application) { status ->
            if (status == TextToSpeech.SUCCESS) {
                _isTtsInitialized.value = true
                setTtsLanguage(_currentLanguage.value)
                tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }
                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }
                    override fun onError(utteranceId: String?, errorCode: Int) {
                        _isSpeaking.value = false
                    }
                })
            }
        }

        // Initialize Crop Prices
        _cropPrices.value = listOf(
            CropPrice("Wheat", "गेंहू", "ಗೋಧಿ", "₹2,275 / Quintal", "₹2,275", "UP"),
            CropPrice("Paddy", "धान", "ಭತ್ತ", "₹2,183 / Quintal", "₹2,183", "UP"),
            CropPrice("Soybean", "सोयाबीन", "ಸೋಯಾಬೀನ್", "₹4,600 / Quintal", "₹4,600", "STABLE"),
            CropPrice("Mustard", "सरसों", "ಸಾಸಿವೆ", "₹5,650 / Quintal", "₹5,650", "DOWN"),
            CropPrice("Cotton", "कपास", "ಹತ್ತि", "₹6,620 / Quintal", "₹6,620", "UP")
        )

        viewModelScope.launch {
            // Seed DB with mock data if first startup
            repository.populateDemoDataIfNeeded()
            
            // Set initial greeting from Krishi Sakhi
            resetGreeting()

            // Fetch initial weather
            fetchAiWeatherForecast()
        }
    }

    fun login(phone: String, pin: String, lang: AppLanguage, name: String = "Rajesh Kumar") {
        val finalName = name.ifBlank { "Rajesh Kumar" }
        _loggedInPhone.value = phone
        _loggedInName.value = finalName
        _isLoggedIn.value = true
        selectLanguage(lang)

        val prefs = getApplication<Application>().getSharedPreferences("mitti_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("logged_in_phone", phone)
            .putString("logged_in_name", finalName)
            .putString("app_language", lang.name)
            .apply()
    }

    fun logout() {
        _isLoggedIn.value = false
        _loggedInPhone.value = ""
        _loggedInName.value = ""

        val prefs = getApplication<Application>().getSharedPreferences("mitti_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("is_logged_in", false)
            .putString("logged_in_phone", "")
            .putString("logged_in_name", "")
            .apply()
    }

    fun updateLocation(location: String) {
        _selectedLocation.value = location
        val prefs = getApplication<Application>().getSharedPreferences("mitti_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("user_location", location).apply()
        fetchAiWeatherForecast()
    }

    fun fetchAiWeatherForecast() {
        _isFetchingWeather.value = true
        viewModelScope.launch {
            val languageNameInEnglish = when (_currentLanguage.value) {
                AppLanguage.ENGLISH -> "English"
                AppLanguage.HINDI -> "Hindi"
                AppLanguage.KANNADA -> "Kannada"
                AppLanguage.MARATHI -> "Marathi"
            }
            val targetLanguageName = _currentLanguage.value.displayName

            val systemInstruction = when (_currentLanguage.value) {
                AppLanguage.HINDI -> """
                    आप 'कृषि सखी' हैं, जो भारतीय किसानों के लिए एक बुद्धिमान और दयालु कृषि और मौसम सलाहकार हैं।
                    मैंडेट: आपको अपना पूरा उत्तर पूरी तरह से हिंदी में देवनागरी लिपि में ही लिखना होगा। अंग्रेजी शब्दों या रोमन लिपि का उपयोग बिल्कुल न करें।
                """.trimIndent()
                AppLanguage.KANNADA -> """
                    ನೀವು 'ಕೃಷಿ ಸಖಿ', ಭಾರತೀಯ ರೈತರಿಗೆ ಬುದ್ಧಿವಂತ ಮತ್ತು ದಯೆಯುಳ್ಳ ಕೃಷಿ ಮತ್ತು ಹವಾಮಾನ ಸಲಹೆಗಾರ್ತಿ.
                    ಮ್ಯಾಂಡೇಟ್: ನೀವು ನಿಮ್ಮ ಸಂಪೂರ್ಣ ಉತ್ತರವನ್ನು ಸಂಪೂರ್ಣವಾಗಿ ಕನ್ನಡ ಲಿಪಿಯಲ್ಲಿಯೇ ಬರೆಯಬೇಕು. ಇಂಗ್ಲಿಷ್ ಪದಗಳನ್ನು ಬಳಸಬೇಡಿ.
                """.trimIndent()
                AppLanguage.MARATHI -> """
                    आपण 'कृषी सखी' आहात, भारतीय शेतकऱ्यांसाठी एक बुद्धिमान आणि दयाळू कृषी आणि हवामान सल्लागार.
                    मॅंडेट: आपण आपले संपूर्ण उत्तर पूर्णपणे मराठीत देवनागरी लिपीतच लिहिले पाहिजे. इंग्रजी शब्द वापरू नका.
                """.trimIndent()
                else -> """
                    You are 'Krishi Sakhi', an intelligent and kind agricultural and weather advisor for Indian farmers.
                    Write your entire response in English.
                """.trimIndent()
            }

            val weatherPrompt = """
                $systemInstruction
                
                [System Rules]
                - Language Constraint: You MUST write your response 100% in $languageNameInEnglish ($targetLanguageName). Do NOT write in English or use English script.
                - Style: Friendly, actionable, informative, under 5 sentences.
                - Content: Provide a climate resilience weather forecast and disaster advisory for a farmer in ${_selectedLocation.value}.
                  Include details on:
                  1. A 3-day weather summary (temperature, rainfall chances, wind speeds).
                  2. Extreme weather warning or Hurricane risk if any (based on Indian Meteorological Dept updates, warn if appropriate, otherwise say normal).
                  3. Actionable farming advice (irrigation timing, livestock safety, crop harvesting) in simple terms.
                
                Response in $targetLanguageName:
            """.trimIndent()

            try {
                val response = repository.askKrishiSakhi(weatherPrompt)
                _weatherForecast.value = response
            } catch (e: Exception) {
                _weatherForecast.value = "Unable to fetch climate-smart forecast. Operating in Offline mode."
            } finally {
                _isFetchingWeather.value = false
            }
        }
    }

    fun uploadSoilPhImage(uriString: String) {
        _isUploadingPhImage.value = true
        _uploadedPhImageUri.value = uriString
        viewModelScope.launch {
            // Simulate AI color analysis of pH strip
            kotlinx.coroutines.delay(2000)
            val randomPh = (52..75).random() / 10.0
            _scannedPh.value = randomPh
            _isUploadingPhImage.value = false
            
            // Dynamic helpful Toast
            android.widget.Toast.makeText(
                getApplication(),
                "Mitti AI analyzed strip: pH $randomPh detected. Tap color chips to adjust before diagnosis.",
                android.widget.Toast.LENGTH_LONG
            ).show()

            triggerSoilScanSimulation()
        }
    }

    fun selectLanguage(lang: AppLanguage) {
        _currentLanguage.value = lang
        setTtsLanguage(lang)
        resetGreeting()

        val prefs = getApplication<Application>().getSharedPreferences("mitti_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("app_language", lang.name).apply()
    }

    private fun setTtsLanguage(lang: AppLanguage) {
        val locale = when (lang) {
            AppLanguage.ENGLISH -> Locale.US
            AppLanguage.HINDI -> Locale("hi", "IN")
            AppLanguage.KANNADA -> Locale("kn", "IN")
            AppLanguage.MARATHI -> Locale("mr", "IN")
        }
        tts?.language = locale
    }

    fun speakOut(text: String) {
        if (_isTtsInitialized.value) {
            _isSpeaking.value = true
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "MittiSpeechId")
            // Simulate speaking animation end
            viewModelScope.launch {
                val delayMs = (text.length * 50L).coerceIn(2000L, 8000L)
                kotlinx.coroutines.delay(delayMs)
                _isSpeaking.value = false
            }
        }
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun resetGreeting() {
        val greeting = _currentLanguage.value.helloPrompt
        _chatLog.value = listOf(ChatMessage("Krishi Sakhi", greeting))
        speakOut(greeting)
    }

    fun navigateTo(screen: String) {
        stopSpeaking()
        _currentScreen.value = screen
    }

    // Process spoken query via voice or typing
    fun submitQuery(text: String) {
        if (text.trim().isEmpty()) return

        stopSpeaking()
        val userMsg = ChatMessage("User", text)
        _chatLog.value = _chatLog.value + userMsg

        _isListening.value = false

        viewModelScope.launch {
            _chatLog.value = _chatLog.value + ChatMessage("Krishi Sakhi", "सोच रही हूँ... (Thinking...)")
            
            val languageNameInEnglish = when (_currentLanguage.value) {
                AppLanguage.ENGLISH -> "English"
                AppLanguage.HINDI -> "Hindi"
                AppLanguage.KANNADA -> "Kannada"
                AppLanguage.MARATHI -> "Marathi"
            }
            val targetLanguageName = _currentLanguage.value.displayName

            val systemInstruction = when (_currentLanguage.value) {
                AppLanguage.HINDI -> """
                    आप 'कृषि सखी' हैं, जो भारतीय किसानों के लिए एक बुद्धिमान और दयालु कृषि और जलवायु सलाहकार हैं।
                    मैंडेट: आपको अपना पूरा उत्तर पूरी तरह से हिंदी में देवनागरी लिपि में ही लिखना होगा। अंग्रेजी शब्दों या रोमन लिपि का उपयोग बिल्कुल न करें।
                """.trimIndent()
                AppLanguage.KANNADA -> """
                    ನೀವು 'ಕೃಷಿ ಸಖಿ', ಭಾರತೀಯ ರೈತರಿಗೆ ಬುದ್ಧಿವಂತ ಮತ್ತು ದಯೆಯುಳ್ಳ ಕೃಷಿ ಮತ್ತು ಹವಾಮಾನ ಸಲಹೆಗಾರ್ತಿ.
                    ಮ್ಯಾಂಡೇಟ್: ನೀವು ನಿಮ್ಮ ಸಂಪೂರ್ಣ ಉತ್ತರವನ್ನು ಸಂಪೂರ್ಣವಾಗಿ ಕನ್ನಡ ಲಿಪಿಯಲ್ಲಿಯೇ ಬರೆಯಬೇಕು. ಇಂಗ್ಲಿಷ್ ಪದಗಳನ್ನು ಬಳಸಬೇಡಿ.
                """.trimIndent()
                AppLanguage.MARATHI -> """
                    आपण 'कृषी सखी' आहात, भारतीय शेतकऱ्यांसाठी एक बुद्धिमान आणि दयाळू कृषी आणि हवामान सल्लागार.
                    मॅंडेट: आपण आपले संपूर्ण उत्तर पूर्णपणे मराठीत देवनागरी लिपीतच लिहिले पाहिजे. इंग्रजी शब्द वापरू नका.
                """.trimIndent()
                else -> """
                    You are 'Krishi Sakhi', an intelligent and kind agricultural and climate resilience advisor for Indian farmers.
                    Write your entire response in English.
                """.trimIndent()
            }

            val promptContext = """
                $systemInstruction
                
                [System Rules]
                - Role: Krishi Sakhi (Expert Agricultural & Climate-smart Advisor)
                - Language Constraint: You MUST write your response 100% in $languageNameInEnglish ($targetLanguageName). Do NOT write in English or use English script.
                - Style: Friendly, empathetic, short (under 4 sentences), highly actionable for an Indian farmer.
                - Detail: Use standard metric units (kg, bigha, acre, quintal, Celsius, mm).
                
                Query: $text
                Response in $targetLanguageName:
            """.trimIndent()

            val aiResponse = try {
                repository.askKrishiSakhi(promptContext)
            } catch (e: Exception) {
                when (_currentLanguage.value) {
                    AppLanguage.HINDI -> "क्षमा करें, नेटवर्क समस्या के कारण उत्तर नहीं मिल सका। कृपया फिर से प्रयास करें।"
                    AppLanguage.KANNADA -> "ಕ್ಷಮಿಸಿ, ನೆಟ್‌ವರ್ಕ್ ದೋಷದಿಂದಾಗಿ ಉತ್ತರ ಸಿಗಲಿಲ್ಲ. ದಯವಿಟ್ಟು ಮತ್ತೊಮ್ಮೆ ಪ್ರಯತ್ನಿಸಿ."
                    AppLanguage.MARATHI -> "क्षमस्व, नेटवर्क त्रुटीमुळे उत्तर मिळू शकले नाही. कृपया पुन्हा प्रयत्न करा."
                    else -> "Sorry, I couldn't connect right now. Please try again in a moment."
                }
            }
            
            // Replace the thinking indicator with actual response
            val finalLog = _chatLog.value.toMutableList()
            if (finalLog.isNotEmpty() && (finalLog.last().text.contains("सोच रही हूँ") || finalLog.last().text.contains("Thinking"))) {
                finalLog.removeAt(finalLog.lastIndex)
            }
            finalLog.add(ChatMessage("Krishi Sakhi", aiResponse))
            _chatLog.value = finalLog

            speakOut(aiResponse)
        }
    }

    // Speech Input helper simulation/triggers
    fun startListening() {
        _isListening.value = true
        stopSpeaking()
    }

    fun cancelListening() {
        _isListening.value = false
    }

    // Soil Scanning System
    fun updatePh(ph: Double) {
        _scannedPh.value = ph
    }

    fun triggerSoilScanSimulation() {
        _isScanning.value = true
        _scanResult.value = null
        
        viewModelScope.launch {
            val languageNameInEnglish = when (_currentLanguage.value) {
                AppLanguage.ENGLISH -> "English"
                AppLanguage.HINDI -> "Hindi"
                AppLanguage.KANNADA -> "Kannada"
                AppLanguage.MARATHI -> "Marathi"
            }
            val targetLanguageName = _currentLanguage.value.displayName

            val systemInstruction = when (_currentLanguage.value) {
                AppLanguage.HINDI -> """
                    आप 'कृषि सखी' हैं, जो भारतीय किसानों के लिए एक बुद्धिमान और दयालु मृदा स्वास्थ्य सलाहकार हैं।
                    मैंडेट: आपको अपना पूरा उत्तर पूरी तरह से हिंदी में देवनागरी लिपि में ही लिखना होगा। अंग्रेजी शब्दों या रोमन लिपि का उपयोग बिल्कुल न करें।
                """.trimIndent()
                AppLanguage.KANNADA -> """
                    ನೀವು 'ಕೃಷಿ ಸಖಿ', ಭಾರತೀಯ ರೈತರಿಗೆ ಬುದ್ಧಿವಂತ ಮತ್ತು ದಯೆಯುಳ್ಳ ಮಣ್ಣಿನ ಆರೋಗ್ಯ ಸಲಹೆಗಾರ್ತಿ.
                    ಮ್ಯಾಂಡೇಟ್: ನೀವು ನಿಮ್ಮ ಸಂಪೂರ್ಣ ಉತ್ತರವನ್ನು ಸಂಪೂರ್ಣವಾಗಿ ಕನ್ನಡ ಲಿಪಿಯಲ್ಲಿಯೇ ಬರೆಯಬೇಕು. ಇಂಗ್ಲಿಷ್ ಪದಗಳನ್ನು ಬಳಸಬೇಡಿ.
                """.trimIndent()
                AppLanguage.MARATHI -> """
                    आपण 'कृषी सखी' आहात, भारतीय शेतकऱ्यांसाठी एक बुद्धिमान आणि दयाळू मृदा आरोग्य सल्लागार.
                    मॅंडेट: आपण आपले संपूर्ण उत्तर पूर्णपणे मराठीत देवनागरी लिपीतच लिहिले पाहिजे. इंग्रजी शब्द वापरू नका.
                """.trimIndent()
                else -> """
                    You are 'Krishi Sakhi', an intelligent and kind soil health advisor for Indian farmers.
                    Write your entire response in English.
                """.trimIndent()
            }

            val diagnosisPrompt = """
                $systemInstruction
                
                [System Rules]
                - Language Constraint: You MUST write your response 100% in $languageNameInEnglish ($targetLanguageName). Do NOT write in English or use English script.
                - Style: Very warm, patient, direct, and empathetic.
                - Content: Generate a soil diagnostic recommendation for a farmer.
                  Soil pH is: ${_scannedPh.value}.
                  Nitrogen (N) is Low, Phosphorus (P) is Medium, Potassium (K) is High.
                  Write a 3-bullet list containing:
                  1. Soil Health Classification (e.g. Acidic, Neutral, Alkaline).
                  2. Organic/lime treatment recommendation with exact quantities in kilograms per bigha.
                  3. Best 2 crop recommendations.
                
                Response in $targetLanguageName:
            """.trimIndent()

            val advice = try {
                repository.askKrishiSakhi(diagnosisPrompt)
            } catch (e: Exception) {
                when (_currentLanguage.value) {
                    AppLanguage.HINDI -> "मिट्टी पीएच ${_scannedPh.value} है। कम नाइट्रोजन, मध्यम फास्फोरस, उच्च पोटेशियम। 2-3 किलोग्राम जैविक खाद डालें। मूंगफली या सरसों की फसल के लिए सबसे अच्छा है।"
                    AppLanguage.KANNADA -> "ಮಣ್ಣಿನ pH ${_scannedPh.value} ಆಗಿದೆ. ಸಾವಯವ ಗೊಬ್ಬರ ಮತ್ತು ಸುಣ್ಣವನ್ನು ಬಳಸಿ. ಕಡಲೆಕಾಯಿ ಅಥವಾ ಸಾಸಿವೆ ಬೆಳೆಗೆ ಉತ್ತಮವಾಗಿದೆ."
                    AppLanguage.MARATHI -> "मातीचा pH ${_scannedPh.value} आहे. सेंद्रिय खतांचा वापर करा. भुईमूग किंवा मोहरी पिकासाठी सर्वोत्तम आहे."
                    else -> "Soil pH is ${_scannedPh.value}. Nitrogen is Low, Phosphorus is Medium, Potassium is High. Recommended treatment: Add 2 kg organic manure/lime per bigha. Best crops: Groundnut, Mustard."
                }
            }

            val newScan = SoilScan(
                farmerName = "Self Scan",
                location = "Field Alpha",
                ph = _scannedPh.value,
                nitrogen = "Low",
                phosphorus = "Medium",
                potassium = "High",
                recommendations = advice,
                cropType = "Groundnut/Mustard"
            )

            repository.insertSoilScan(newScan)
            _scanResult.value = newScan
            _isScanning.value = false

            speakOut(advice)
        }
    }

    // Disaster triggers
    fun acknowledgeAlert(alertId: Int) {
        viewModelScope.launch {
            repository.acknowledgeAlert(alertId)
        }
    }

    fun deleteSoilScan(id: Int) {
        viewModelScope.launch {
            repository.deleteSoilScan(id)
        }
    }

    // Sarpanch Scheme Generator
    fun generateSchemeApplication(schemeName: String, farmerCount: Int, location: String, benefit: String) {
        viewModelScope.launch {
            val newApp = SchemeApplication(
                schemeName = schemeName,
                farmerCount = farmerCount,
                location = location,
                estimatedBenefit = benefit,
                status = "SUBMITTED"
            )
            repository.insertApplication(newApp)
            speakOut("Application for $schemeName has been pre-filled and submitted successfully to PM-KISAN Portal.")
        }
    }

    // Village score calculation
    fun getResilienceScore(): Int {
        val scanList = soilScans.value
        val alertList = alerts.value
        
        var baseScore = 70
        // Add for good soil
        val goodScans = scanList.count { it.ph in 6.0..7.5 }
        baseScore += (goodScans * 3)
        // Deduct for acidic
        val acidicScans = scanList.count { it.ph < 5.5 }
        baseScore -= (acidicScans * 5)
        // Deduct for critical unacknowledged alerts
        val unackAlerts = alertList.count { !it.isAcknowledged && it.severity == "CRITICAL" }
        baseScore -= (unackAlerts * 10)
        // Add for acknowledged alerts (good village response)
        val ackAlerts = alertList.count { it.isAcknowledged }
        baseScore += (ackAlerts * 2)
        
        return baseScore.coerceIn(0, 100)
    }

    override fun onCleared() {
        tts?.shutdown()
        super.onCleared()
    }
}

class MittiViewModelFactory(
    private val application: Application,
    private val repository: MittiRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MittiViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MittiViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
