package com.example.data.network

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>
)

@JsonClass(generateAdapter = true)
data class PartResponse(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class ContentResponse(
    @Json(name = "parts") val parts: List<PartResponse>? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: ContentResponse? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>? = null
)

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val geminiApi: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GeminiApi::class.java)
    }
}

class GeminiService {
    suspend fun generateContent(prompt: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY.trim().removeSurrounding("\"").removeSurrounding("'")
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "null") {
            return "[Offline Demo Mode]\n\n" + getFallbackResponse(prompt)
        }

        var lastError: Exception? = null
        repeat(3) { attempt ->
            try {
                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(parts = listOf(Part(text = prompt)))
                    )
                )
                val response = RetrofitClient.geminiApi.generateContent(apiKey, request)
                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!text.isNullOrBlank()) {
                    return text
                }
            } catch (e: Exception) {
                lastError = e
                e.printStackTrace()
                if (attempt < 2) {
                    kotlinx.coroutines.delay(1000L * (attempt + 1))
                }
            }
        }

        val errorDetails = lastError?.localizedMessage ?: "Unknown API response format"
        return "⚠️ [API Error: $errorDetails]\n\n[Offline Fallback Mode]\n\n" + getFallbackResponse(prompt)
    }

    private fun getFallbackResponse(prompt: String): String {
        val isHindi = prompt.contains("HINDI", ignoreCase = true) || prompt.contains("हिन्दी", ignoreCase = true)
        val isKannada = prompt.contains("KANNADA", ignoreCase = true) || prompt.contains("ಕನ್ನಡ", ignoreCase = true)
        val isMarathi = prompt.contains("MARATHI", ignoreCase = true) || prompt.contains("मराठी", ignoreCase = true)

        val isSoil = prompt.contains("soil", ignoreCase = true) ||
                prompt.contains("pH", ignoreCase = true) ||
                prompt.contains("acid", ignoreCase = true) ||
                prompt.contains("alkaline", ignoreCase = true) ||
                prompt.contains("lime", ignoreCase = true) ||
                prompt.contains("fertilizer", ignoreCase = true) ||
                prompt.contains("manure", ignoreCase = true) ||
                prompt.contains("mitti", ignoreCase = true) ||
                prompt.contains("मिट्टी", ignoreCase = true) ||
                prompt.contains("खाद", ignoreCase = true) ||
                prompt.contains("पीएच", ignoreCase = true) ||
                prompt.contains("ಮಣ್ಣು", ignoreCase = true) ||
                prompt.contains("ಗೊಬ್ಬರ", ignoreCase = true) ||
                prompt.contains("ಮಾती", ignoreCase = true) ||
                prompt.contains("खत", ignoreCase = true)

        val isWeather = prompt.contains("weather", ignoreCase = true) ||
                prompt.contains("rain", ignoreCase = true) ||
                prompt.contains("water", ignoreCase = true) ||
                prompt.contains("irrigation", ignoreCase = true) ||
                prompt.contains("drought", ignoreCase = true) ||
                prompt.contains("heat", ignoreCase = true) ||
                prompt.contains("storm", ignoreCase = true) ||
                prompt.contains("flood", ignoreCase = true) ||
                prompt.contains("monsoon", ignoreCase = true) ||
                prompt.contains("मौसम", ignoreCase = true) ||
                prompt.contains("बारिश", ignoreCase = true) ||
                prompt.contains("पानी", ignoreCase = true) ||
                prompt.contains("सिंचाई", ignoreCase = true) ||
                prompt.contains("बाढ़", ignoreCase = true) ||
                prompt.contains("ಮಳೆ", ignoreCase = true) ||
                prompt.contains("ನೀರು", ignoreCase = true) ||
                prompt.contains("ನೀರಾವರಿ", ignoreCase = true) ||
                prompt.contains("ಹವಾಮಾನ", ignoreCase = true) ||
                prompt.contains("पाऊस", ignoreCase = true) ||
                prompt.contains("पाणी", ignoreCase = true) ||
                prompt.contains("हवामान", ignoreCase = true)

        val isScheme = prompt.contains("scheme", ignoreCase = true) ||
                prompt.contains("yojana", ignoreCase = true) ||
                prompt.contains("PM-KISAN", ignoreCase = true) ||
                prompt.contains("pm kisan", ignoreCase = true) ||
                prompt.contains("sarpanch", ignoreCase = true) ||
                prompt.contains("subsidy", ignoreCase = true) ||
                prompt.contains("registration", ignoreCase = true) ||
                prompt.contains("eligible", ignoreCase = true) ||
                prompt.contains("योजना", ignoreCase = true) ||
                prompt.contains("पीएम-किसान", ignoreCase = true) ||
                prompt.contains("पात्र", ignoreCase = true) ||
                prompt.contains("नोंदणी", ignoreCase = true) ||
                prompt.contains("ಅರ್ಹತೆ", ignoreCase = true) ||
                prompt.contains("ಸಹಾಯಧನ", ignoreCase = true) ||
                prompt.contains("ಖಾತೆ", ignoreCase = true) ||
                prompt.contains("अनुदान", ignoreCase = true) ||
                prompt.contains("पात्रता", ignoreCase = true)

        val isCrop = prompt.contains("crop", ignoreCase = true) ||
                prompt.contains("seed", ignoreCase = true) ||
                prompt.contains("sow", ignoreCase = true) ||
                prompt.contains("groundnut", ignoreCase = true) ||
                prompt.contains("mustard", ignoreCase = true) ||
                prompt.contains("soybean", ignoreCase = true) ||
                prompt.contains("wheat", ignoreCase = true) ||
                prompt.contains("paddy", ignoreCase = true) ||
                prompt.contains("rice", ignoreCase = true) ||
                prompt.contains("maize", ignoreCase = true) ||
                prompt.contains("फसल", ignoreCase = true) ||
                prompt.contains("बीज", ignoreCase = true) ||
                prompt.contains("बुवाई", ignoreCase = true) ||
                prompt.contains("मूंगफली", ignoreCase = true) ||
                prompt.contains("सरसों", ignoreCase = true) ||
                prompt.contains("सोयाबीन", ignoreCase = true) ||
                prompt.contains("धान", ignoreCase = true) ||
                prompt.contains("ಬೆಳೆ", ignoreCase = true) ||
                prompt.contains("ಬಿತ್ತನೆ", ignoreCase = true) ||
                prompt.contains("ಶೇಂಗಾ", ignoreCase = true) ||
                prompt.contains("ಸಾಸಿವೆ", ignoreCase = true) ||
                prompt.contains("ಪೀಕ್", ignoreCase = true) ||
                prompt.contains("पेरणी", ignoreCase = true) ||
                prompt.contains("भुईमूग", ignoreCase = true) ||
                prompt.contains("मोहरी", ignoreCase = true)

        val isPrice = prompt.contains("price", ignoreCase = true) ||
                prompt.contains("market", ignoreCase = true) ||
                prompt.contains("mandi", ignoreCase = true) ||
                prompt.contains("cost", ignoreCase = true) ||
                prompt.contains("earning", ignoreCase = true) ||
                prompt.contains("msp", ignoreCase = true) ||
                prompt.contains("दर", ignoreCase = true) ||
                prompt.contains("भाव", ignoreCase = true) ||
                prompt.contains("मंडी", ignoreCase = true) ||
                prompt.contains("मूल्य", ignoreCase = true) ||
                prompt.contains("ಕಾರ್ಖಾನೆ", ignoreCase = true) ||
                prompt.contains("ಧಾರಣೆ", ignoreCase = true) ||
                prompt.contains("ಮಾರುಕಟ್ಟೆ", ignoreCase = true) ||
                prompt.contains("बाजार", ignoreCase = true) ||
                prompt.contains("किंमत", ignoreCase = true) ||
                prompt.contains("एमएसपी", ignoreCase = true)

        return when {
            isSoil -> {
                when {
                    isHindi -> {
                        """
                        🌿 मिट्टी विश्लेषण और उर्वरता निदान (कृषि सखी सलाहकार)
                        1. मिट्टी की स्थिति: पीएच 5.8 (मध्यम अम्लीय) है, नाइट्रोजन कम है और पोटेशियम उच्च है।
                        2. सुधार उपाय: अम्लता को संतुलित करने के लिए प्रति बीघा 2.5 किलोग्राम कृषि चूना (Agricultural Lime) मिलाएं। रासायनिक यूरिया से बचें और 150 किलोग्राम जैविक केंचुआ खाद (वर्मिकम्पोस्ट) का उपयोग करें।
                        3. सर्वोत्तम फसलें: मूंगफली, सरसों और सोयाबीन इस मिट्टी में अच्छी उपज देंगे और प्राकृतिक उर्वरता बढ़ाएंगे।
                        """.trimIndent()
                    }
                    isKannada -> {
                        """
                        🌿 ಮಣ್ಣಿನ ವಿಶ್ಲೇಷಣೆ ಮತ್ತು ಫಲವತ್ತತೆಯ ರೋಗನಿರ್ಣಯ (ಕೃಷಿ ಸಖಿ ಸಲಹೆಗಾರ್ತಿ)
                        1. ಮಣ್ಣಿನ ಸ್ಥಿತಿ: pH ಮಟ್ಟ 5.8 (ಮಧ್ಯಮ ಆಮ್ಲೀಯ), ಸಾರಜನಕ ಕಡಿಮೆ ಮತ್ತು ಪೊಟ್ಯಾಸಿಯಮ್ ಹೆಚ್ಚಾಗಿದೆ.
                        2. ಸುಧಾರಣಾ ಕ್ರಮ: ಆಮ್ಲೀಯತೆಯನ್ನು ಸರಿದೂಗಿಸಲು ಪ್ರತಿ ಬಿಘಾಕ್ಕೆ 2.5 ಕೆಜಿ ಕೃಷಿ ಸುಣ್ಣವನ್ನು (ಚುನಾ) ಬಳಸಿ. ರಾಸಾಯನಿಕ ಗೊಬ್ಬರ ಬಳಸಬೇಡಿ, ಬದಲಿಗೆ 150 ಕೆಜಿ ಸಾವಯವ ಎರೆಗೊಬ್ಬರವನ್ನು ಬಳಸಿ.
                        3. ಸೂಕ್ತ ಬೆಳೆಗಳು: ಶೇಂಗಾ (ನೆಲಗಡಲೆ), ಸಾಸಿವೆ ಮತ್ತು ಸೋಯಾಬೀನ್ ಈ ಮಣ್ಣಿನಲ್ಲಿ ಉತ್ತಮ ಇಳುವರಿ ನೀಡುತ್ತವೆ.
                        """.trimIndent()
                    }
                    isMarathi -> {
                        """
                        🌿 माती विश्लेषण आणि सुपीकता निदान (कृषी सखी सल्लागार)
                        1. मातीची स्थिती: पीएच पातळी 5.8 (मध्यम आम्लीय) आहे, नायट्रोजन कमी आहे आणि पोटॅशियम जास्त आहे।
                        2. सुधारणा उपाय: आम्लता संतुलित करण्यासाठी पेरणीपूर्वी प्रति बिघा 2.5 किलो कृषी चुना शेतात वापरा। युरियाऐवजी 150 किलो सेंद्रिय गांडूळ खत वापरा।
                        3. सर्वोत्तम पिके: भुईमूग, मोहरी आणि सोयाबीन या आम्लीय मातीत उत्तम वाढतात।
                        """.trimIndent()
                    }
                    else -> {
                        """
                        🌿 Soil & Fertility Diagnostics (Krishi Sakhi Assistant)
                        1. Soil Health: pH is 5.8 (moderately acidic), nitrogen is low, and potassium is high.
                        2. Recommendation: Apply 2.5 kg of agricultural lime (chuna) per bigha to balance acidity. Avoid chemical nitrogen; use organic vermicompost (150 kg per bigha) instead.
                        3. Best Crops: Groundnut, mustard, and soybean will thrive and help restore natural soil balance.
                        """.trimIndent()
                    }
                }
            }
            isWeather -> {
                when {
                    isHindi -> {
                        """
                        🌦️ जलवायु और सिंचाई सलाह (कृषि सखी पूर्वानुमान)
                        1. मौसम का हाल: अगले 5 दिनों में गर्म और उमस भरा मौसम रहेगा, शाम को हल्की बारिश की संभावना है।
                        2. सिंचाई सलाह: वाष्पीकरण को रोकने के लिए केवल शाम को पानी दें। यदि 10 मिमी से अधिक बारिश होती है, तो सिंचाई टाल दें।
                        3. फसल सुरक्षा: मक्के के खेतों में जलभराव रोकने के लिए नालियों को साफ रखें और धान में 2-3 सेमी पानी बनाए रखें।
                        """.trimIndent()
                    }
                    isKannada -> {
                        """
                        🌦️ ಹವಾಮಾನ ಮತ್ತು ನೀರಾವರಿ ಸಲಹೆ (ಕೃಷಿ ಸಖಿ ಮುನ್ಸೂಚನೆ)
                        1. ಹವಾಮಾನ ವಿವರ: ಮುಂದಿನ 5 ದಿನಗಳು ಬಿಸಿ ಮತ್ತು ಆರ್ದ್ರತೆಯಿಂದ ಕೂಡಿದ್ದು, ಸಂಜೆ ಸಣ್ಣ ಮಳೆಯಾಗುವ ಸಾಧ್ಯತೆಯಿದೆ.
                        2. ನೀರಾವರಿ ಸಲಹೆ: ಆವಿಯಾಗುವಿಕೆ ತಡೆಯಲು ಸಂಜೆ ನೀರುಣಿಸಿ. ಮಳೆ 10 ಮಿಮೀ ದಾಟಿದರೆ ನೀರಾವರಿಯನ್ನು ಸಂಪೂರ್ಣವಾಗಿ ನಿಲ್ಲಿಸಿ.
                        3. ಬೆಳೆ ರಕ್ಷಣೆ: ಜೋಳದ ಹೊಲಗಳಲ್ಲಿ ನೀರು ನಿಲ್ಲದಂತೆ ನೋಡಿಕೊಳ್ಳಿ ಮತ್ತು ಭತ್ತದ ಬೆಳೆಗೆ 2-3 ಸೆಂ.ಮೀ ನೀರನ್ನು ಕಾಯ್ದುಕೊಳ್ಳಿ.
                        """.trimIndent()
                    }
                    isMarathi -> {
                        """
                        🌦️ हवामान आणि सिंचन सल्ला (कृषी सखी अंदाज)
                        1. हवामान स्थिती: पुढील 5 दिवसांत उष्ण आणि दमट हवामान राहील, संध्याकाळी हलक्या सरी पडण्याची शक्यता आहे।
                        2. सिंचन सल्ला: बाष्पीभवन टाळण्यासाठी संध्याकाळी पाणी द्या। जर 10 मिमी पेक्षा जास्त पाऊस पडला तर सिंचन थांबवा।
                        3. पीक संरक्षण: मक्याच्या शेतात पाणी साचू नये म्हणून पाण्याचा निचरा करणारी गटारे मोकळी ठेवा आणि भात पिकात 2-3 सेमी पाणी ठेवा।
                        """.trimIndent()
                    }
                    else -> {
                        """
                        🌦️ Climate & Irrigation Advisory (Krishi Sakhi Forecast)
                        1. Outlook: Warm and humid conditions with light evening showers expected over the next 5 days.
                        2. Irrigation: Schedule watering for late evenings to minimize evaporation loss. Skip irrigation if cumulative rain exceeds 10mm.
                        3. Standing Crops: Ensure proper drainage channels in maize fields to prevent waterlogging, and maintain 2-3 cm shallow water in paddy.
                        """.trimIndent()
                    }
                }
            }
            isScheme -> {
                when {
                    isHindi -> {
                        """
                        📋 सरकारी योजनाएं और सब्सिडी सलाह (कृषि सखी पोर्टल)
                        1. पीएम-किसान निधि: यदि आपके पास 2 हेक्टेयर तक कृषि भूमि है, तो आप सालाना ₹6,000 पाने के पात्र हैं। पीएम-किसान पोर्टल पर पंजीकरण करें या सरपंच से संपर्क करें।
                        2. चूना सब्सिडी: अम्लीय भूमि सुधार योजना के तहत, कृषि चूने की खरीद पर 50% की सब्सिडी मिलती है। आवश्यक दस्तावेजों के साथ पंचायत कार्यालय में आवेदन करें।
                        """.trimIndent()
                    }
                    isKannada -> {
                        """
                        📋 ಸರ್ಕಾರಿ ಯೋಜನೆಗಳು ಮತ್ತು ಸಹಾಯಧನ ಸಲಹೆ (ಕೃಷಿ ಸಖಿ ಮಾಹಿತಿ)
                        1. ಪಿಎಂ-ಕಿಸಾನ್ ಯೋಜನೆ: ನಿಮ್ಮ ಹೆಸರಿನಲ್ಲಿ 2 ಹೆಕ್ಟೇರ್‌ವರೆಗಿನ ಕೃಷಿ ಭೂಮಿ ಇದ್ದರೆ ವರ್ಷಕ್ಕೆ ₹6,000 ಪಡೆಯಲು ನೀವು ಅರ್ಹರು. ಪಿಎಂ-ಕಿಸಾನ್ ವೆಬ್‌ಸೈಟ್‌ನಲ್ಲಿ ಅಥವಾ ಸರಪಂಚರ ಬಳಿ ನೋಂದಣಿ ಮಾಡಿ.
                        2. ಸುಣ್ಣದ ಸಹಾಯಧನ: ಆಮ್ಲೀಯ ಮಣ್ಣು ಸುಧಾರಣೆ ಯೋಜನೆಯಡಿ, ಕೃಷಿ ಸುಣ್ಣದ ಖರೀದಿಗೆ 50% ರಿಯಾಯಿತಿ ಸಿಗುತ್ತದೆ. ನಿಮ್ಮ ದಾಖಲೆಗಳೊಂದಿಗೆ ಪಂಚಾಯಿತಿಗೆ ಅರ್ಜಿ ಸಲ್ಲಿಸಿ.
                        """.trimIndent()
                    }
                    isMarathi -> {
                        """
                        📋 सरकारी योजना आणि अनुदान सल्ला (कृषी सखी माहिती)
                        1. पीएम-किसान निधी: आपल्याकडे 2 हेक्टरपर्यंत शेतजमीन असल्यास, आपण वार्षिक ₹6,000 मिळण्यास पात्र आहात। पीएम-किसान पोर्टलवर किंवा स्थानिक सरपंचांशी संपर्क साधून नोंदणी करा।
                        2. चुना अनुदान: आम्लीय जमीन सुधारणा योजनेअंतर्गत, कृषी चुन्याच्या खरेदीवर 50% अनुदान उपलब्ध आहे। आवश्यक कागदपत्रांसह पंचायत कार्यालयात अर्ज करा।
                        """.trimIndent()
                    }
                    else -> {
                        """
                        📋 Government Schemes & Subsidy Advisory (Krishi Sakhi Portal)
                        1. PM-KISAN Nidhi: If you own agricultural land up to 2 hectares, you are eligible for ₹6,000 yearly in 3 installments. Register via the PM-KISAN portal or contact your local Sarpanch.
                        2. Lime Subsidy: Under the acidic soil reclamation scheme, a 50% subsidy is available for agricultural lime purchase. Register with the Panchayat office.
                        """.trimIndent()
                    }
                }
            }
            isCrop -> {
                when {
                    isHindi -> {
                        """
                        🌱 फसल चयन और कृषि विज्ञान सलाह (कृषि सखी विशेषज्ञ)
                        1. अनुशंसित किस्में: अम्लीय मिट्टी के लिए, मूंगफली (K-6, कदिरी-9) या सरसों (पूसा बोल्ड, वरुणा) सर्वोत्तम विकल्प हैं।
                        2. बीज उपचार: जड़ सड़न से बचाव के लिए बुवाई से पहले मूंगफली के बीजों को ट्राइकोडरमा (5 ग्राम प्रति किलोग्राम बीज) से उपचारित करें।
                        3. दूरी और गहराई: अधिकतम उपज के लिए बीजों को 5 सेमी की गहराई पर और कतारों के बीच 30 सेमी की दूरी पर बोएं।
                        """.trimIndent()
                    }
                    isKannada -> {
                        """
                        🌱 ಬೆಳೆ ಆಯ್ಕೆ ಮತ್ತು ಕೃಷಿ ಸಲಹೆ (ಕೃಷಿ ಸಖಿ ಕೃಷಿ ತಜ್ಞರು)
                        1. ಶಿಫಾರಸು ಮಾಡಿದ ತಳಿಗಳು: ಆಮ್ಲೀಯ ಮಣ್ಣಿಗೆ ಶೇಂಗಾ (ಕೆ-6, ಕದಿರಿ-9) ಅಥವಾ ಸಾಸಿವೆ (ಪೂಸಾ ಬೋಲ್ಡ್, ವರುಣಾ) ಅತ್ಯುತ್ತಮ ಆಯ್ಕೆಯಾಗಿದೆ.
                        2. ಬೀಜೋಪಚಾರ: ಬೇರು ಕೊಳೆತು ಹೋಗುವುದನ್ನು ತಡೆಯಲು ಬಿತ್ತನೆಗೆ ಮುನ್ನ ಶೇಂಗಾ ಬೀಜಗಳನ್ನು ಟ್ರೈಕೋಡರ್ಮಾ (ಪ್ರತಿ ಕೆಜಿ ಬೀಜಕ್ಕೆ 5 ಗ್ರಾಂ) ದೊಂದಿಗೆ ಉಪಚರಿಸಿ.
                        3. ಬಿತ್ತನೆ ಅಂತರ: ಉತ್ತಮ ಇಳುವರಿಗಾಗಿ ಬೀಜಗಳನ್ನು 5 ಸೆಂ.ಮೀ ಆಳದಲ್ಲಿ ಮತ್ತು ಸಾಲುಗಳ ನಡುವೆ 30 ಸೆಂ.ಮೀ ಅಂತರದಲ್ಲಿ ಬಿತ್ತನೆ ಮಾಡಿ.
                        """.trimIndent()
                    }
                    isMarathi -> {
                        """
                        🌱 पीक निवड आणि कृषी सल्ला (कृषी सखी तज्ञ)
                        1. शिफारस केलेल्या जाती: आम्लीय मातीसाठी, भुईमूग (K-6, कादिरी-9) या मोहरी (पुसा बोल्ड, वरुणा) हे उत्तम पर्याय आहेत।
                        2. बीजप्रक्रिया: मूळ सडणे रोखण्यासाठी पेरणीपूर्वी भुईमुगाच्या बियांना ट्रायकोडर्मा (5 ग्रॅम प्रति किलो बियाणे) लावून बीजप्रक्रिया करा।
                        3. पेरणीचे अंतर: जास्तीत जास्त उत्पादनासाठी बियाणे 5 सेमी खोलीवर आणि दोन ओळींमध्ये 30 सेमी अंतर ठेवून पेरा।
                        """.trimIndent()
                    }
                    else -> {
                        """
                        🌱 Crop Selection & Agronomy Advisory (Krishi Sakhi Agronomist)
                        1. Recommended Varieties: For acidic soils, Groundnut (K-6, Kadiri-9) or Mustard (Pusa Bold, Varuna) are excellent choices.
                        2. Seed Treatment: Always treat groundnut seeds with Trichoderma (5g per kg seed) before sowing to protect against root-rot disease.
                        3. Spacing & Depth: Sow seeds at 5 cm depth with 30 cm row spacing for optimal yield and growth.
                        """.trimIndent()
                    }
                }
            }
            isPrice -> {
                when {
                    isHindi -> {
                        """
                        💰 बाजार भाव और मंडी खुफिया जानकारी (कृषि सखी मंडी सेवा)
                        1. मूंगफली: मंडी भाव ₹6,800 से ₹7,200 प्रति क्विंटल पर स्थिर हैं। न्यूनतम समर्थन मूल्य (MSP) ₹6,780 है।
                        2. सरसों: कीमतें बढ़ रही हैं, बाजार भाव ₹5,900 से ₹6,300 प्रति क्विंटल चल रहा है। एमएसपी ₹5,650 है।
                        3. व्यापार सलाह: यदि आपके पास भंडारण की सुविधा है, तो सरसों के स्टॉक को 3-4 सप्ताह तक रोककर रखें; अधिक लाभ मिल सकता है।
                        """.trimIndent()
                    }
                    isKannada -> {
                        """
                        💰 ಮಾರುಕಟ್ಟೆ ದರ ಮತ್ತು ಮಂಡಿ ಮಾಹಿತಿ (ಕೃಷಿ ಸಖಿ ಮಂಡಿ ಸೇವೆ)
                        1. ಶೇಂಗಾ: ಮಾರುಕಟ್ಟೆ ಬೆಲೆ ಪ್ರತಿ ಕ್ವಿಂಟಾಲ್‌ಗೆ ₹6,800 ರಿಂದ ₹7,200 ರಷ್ಟಿದ್ದು ಸ್ಥಿರವಾಗಿದೆ. ಎಂಎಸ್ಪಿ ₹6,780 ಆಗಿದೆ.
                        2. ಸಾಸಿವೆ: ಬೆಲೆ ಹೆಚ್ಚಾಗುತ್ತಿದ್ದು, ಪ್ರತಿ ಕ್ವಿಂಟಾಲ್‌ಗೆ ₹5,900 ರಿಂದ ₹6,300 ವರೆಗೆ ವ್ಯಾಪಾರವಾಗುತ್ತಿದೆ. ಎಂಎಸ್ಪಿ ₹5,650 ಆಗಿದೆ.
                        3. ಮಾರಾಟ ಸಲಹೆ: ನಿಮ್ಮ ಬಳಿ ದಾಸ್ತಾನು ಸೌಲಭ್ಯವಿದ್ದರೆ, ಬೆಲೆ ಹೆಚ್ಚಾಗುವ ನಿರೀಕ್ಷೆಯಿರುವುದರಿಂದ ಸಾಸಿವೆಯನ್ನು ಇನ್ನೂ 3-4 ವಾರಗಳ ಕಾಲ ಸಂಗ್ರಹಿಸಿಟ್ಟು ಮಾರಾಟ ಮಾಡಿ.
                        """.trimIndent()
                    }
                    isMarathi -> {
                        """
                        💰 बाजार भाव आणि मंडी माहिती (कृषी सखी बाजार सेवा)
                        1. भुईमूग: सध्या मंडी दर प्रति क्विंटल ₹6,800 ते ₹7,200 वर स्थिर आहेत। किमान आधारभूत किंमत (MSP) ₹6,780 आहे।
                        2. मोहरी: मोहरीच्या दरात वाढ होत असून, सध्या बाजार भाव प्रति क्विंटल ₹5,900 ते ₹6,300 च्या दरम्यान आहेत।
                        3. व्यापार सल्ला: आपल्याकडे साठवणूक क्षमता असल्यास मोहरी आणखी 3-4 आठवडे साठवून ठेवा, भाव वाढण्याची दाट शक्यता आहे।
                        """.trimIndent()
                    }
                    else -> {
                        """
                        💰 Market Rates & Mandi Intelligence (Krishi Sakhi Mandi Info)
                        1. Groundnut: Current Mandi prices are stable at ₹6,800 to ₹7,200 per quintal. MSP is ₹6,780.
                        2. Mustard: Current prices are trending upward, trading around ₹5,900 to ₹6,300 per quintal. MSP is ₹5,650.
                        3. Advice: If storage is available, hold mustard stock for another 3-4 weeks as demand is projected to rise, leading to higher prices.
                        """.trimIndent()
                    }
                }
            }
            else -> {
                when {
                    isHindi -> {
                        """
                        👋 नमस्ते! मैं आपकी कृषि सखी हूँ, आपकी व्यक्तिगत जलवायु-अनुकूल कृषि सलाहकार।
                        मैं आपकी निम्न विषयों में मदद कर सकती हूँ:
                        1. मिट्टी परीक्षण, पीएच विश्लेषण और जैविक खाद की सलाह में।
                        2. मौसम अनुकूल फसल योजना और सिंचाई कार्यक्रम तय करने में।
                        3. सरकारी योजनाओं, पीएम-किसान और चूना सब्सिडी की पात्रता में।
                        4. नवीनतम मंडी भाव और उपज बेचने की सही सलाह में।
                        अपने खेत के बारे में कुछ भी पूछें या नीचे दिए गए शॉर्टकट पर टैप करें!
                        """.trimIndent()
                    }
                    isKannada -> {
                        """
                        👋 ನಮಸ್ತೆ! ನಾನು ನಿಮ್ಮ ಕೃಷಿ ಸಖಿ, ನಿಮ್ಮ ವೈಯಕ್ತಿಕ ಹವಾಮಾನ-ಸ್ನೇಹಿ ಕೃಷಿ ಸಲಹೆಗಾರ್ತಿ.
                        ನಾನು ನಿಮಗೆ ಇವುಗಳಲ್ಲಿ ಸಹಾಯ ಮಾಡಬಲ್ಲೆ:
                        1. ಮಣ್ಣಿನ ಪರೀಕ್ಷೆ, pH ವಿಶ್ಲೇಷಣೆ ಮತ್ತು ಗೊಬ್ಬರದ ಶಿಫಾರಸುಗಳು.
                        2. ಹವಾಮಾನ ಆಧಾರಿತ ಬೆಳೆ ಯೋಜನೆ ಮತ್ತು ನೀರಾವರಿ ವೇಳಾಪಟ್ಟಿ.
                        3. ಸರ್ಕಾರಿ ಯೋಜನೆಗಳು, ಪಿಎಂ-ಕಿಸಾನ್ ಮತ್ತು ಸುಣ್ಣದ ಸಹಾಯಧನ.
                        4. ಇತ್ತೀಚಿನ ಮಂಡಿ ದರಗಳು ಮತ್ತು ಬೆಳೆ ಮಾರಾಟದ ಸಲಹೆಗಳು.
                        ನಿಮ್ಮ ಜಮೀನಿನ ಬಗ್ಗೆ ಯಾವುದೇ ಪ್ರಶ್ನೆ ಕೇಳಿ ಅಥವಾ ಕೆಳಗಿನ ಶಾರ್ಟ್‌ಕಟ್ ಬಳಸಿ!
                        """.trimIndent()
                    }
                    isMarathi -> {
                        """
                        👋 नमस्कार! मी तुमची कृषी सखी आहे, तुमची वैयक्तिक हवामान-अनुकूल कृषी सल्लागार।
                        मी तुम्हाला खालील गोष्टींमध्ये मदत करू शकते:
                        1. माती परीक्षण, पीएच विश्लेषण आणि खतांच्या शिफारसी।
                        2. हवामान-अनुकूल पीक नियोजन आणि सिंचन वेळापत्रक।
                        3. सरकारी योजना, पीएम-किसान आणि चुना अनुदान माहिती।
                        4. ताजे बाजार भाव आणि पीक विक्रीबाबत योग्य सल्ला।
                        तुमच्या शेतीबद्दल कोणताही प्रश्न विचारा किंवा खालील शॉर्टकट वर टॅप करा!
                        """.trimIndent()
                    }
                    else -> {
                        """
                        👋 Namaste! I am Krishi Sakhi, your personal climate-resilience agricultural advisor. 
                        I can help you with:
                        1. Soil testing, pH analysis, and fertilizer recipes.
                        2. Climate-smart crop planning and irrigation scheduling.
                        3. Government schemes, PM-KISAN, and lime subsidies.
                        4. Mandi prices and crop sales recommendations.
                        Ask me anything about your farm or tap a Quick Shortcut below!
                        """.trimIndent()
                    }
                }
            }
        }
    }
}
