package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.DisasterAlert
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.MittiViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DisasterAlertsScreen(
    viewModel: MittiViewModel,
    modifier: Modifier = Modifier
) {
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val alerts by viewModel.alerts.collectAsStateWithLifecycle()
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    var activeChecklistTitle by remember { mutableStateOf("") }
    var activeChecklistText by remember { mutableStateOf("") }

    val activeCriticalAlert = alerts.firstOrNull { it.severity == "CRITICAL" && !it.isAcknowledged }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Critical Disaster Warnings Panel ---
        item {
            val panelBg = if (isDark) Color(0xFF252538) else PureWhite
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = panelBg),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.ENGLISH -> "🚨 Emergency Early Warnings"
                            AppLanguage.HINDI -> "🚨 आपदा पूर्व चेतावनी"
                            AppLanguage.KANNADA -> "🚨 ತುರ್ತು ಮುನ್ನೆಚ್ಚರಿಕೆಗಳು"
                            AppLanguage.MARATHI -> "🚨 आपत्कालीन पूर्वसूचना"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DroughtRed
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.ENGLISH -> "Real-time alerts synced from Indian Meteorological Dept (IMD)."
                            AppLanguage.HINDI -> "भारतीय मौसम विज्ञान विभाग (IMD) से समन्वित वास्तविक समय के अलर्ट।"
                            AppLanguage.KANNADA -> "ಭಾರತೀಯ ಹವಾಮಾನ ಇಲಾಖೆಯಿಂದ (IMD) ಪಡೆದ ಹವಾಮಾನ ಎಚ್ಚರಿಕೆಗಳು."
                            AppLanguage.MARATHI -> "भारतीय हवामान विभागाकडून (IMD) रिअल-टाइम अलर्ट समक्रमित केले आहेत."
                        },
                        fontSize = 12.sp,
                        color = AshGrey
                    )
                }
            }
        }

        // --- Active Red Flood Alert Section ---
        if (activeCriticalAlert != null) {
            val alertCardBg = if (isDark) Color(0xFF3E1F1A) else DroughtRed.copy(alpha = 0.08f)
            val alertCardTextColor = if (isDark) PureWhite else RaatBlue

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = alertCardBg),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(2.dp, DroughtRed)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "Alert",
                                tint = DroughtRed,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = getLocalizedAlertType(activeCriticalAlert.alertType, currentLanguage),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DroughtRed
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = getLocalizedMessage(activeCriticalAlert.message, currentLanguage),
                            fontSize = 14.sp,
                            color = alertCardTextColor,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Actions for Farmers (Livestock & Crops)
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.ENGLISH -> "👇 What to protect immediately:"
                                AppLanguage.HINDI -> "👇 तुरंत सुरक्षा के उपाय:"
                                AppLanguage.KANNADA -> "👇 ತಕ್ಷಣದ ರಕ್ಷಣೆಗಾಗಿ ಕೈಗೊಳ್ಳಿ:"
                                AppLanguage.MARATHI -> "👇 त्वरित संरक्षणात्मक उपाय:"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = alertCardTextColor
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Protect Livestock
                            Button(
                                onClick = {
                                    activeChecklistTitle = when (currentLanguage) {
                                        AppLanguage.ENGLISH -> "🐮 Livestock Checklist"
                                        AppLanguage.HINDI -> "🐮 मवेशी सुरक्षा चेकलिस्ट"
                                        AppLanguage.KANNADA -> "🐮 ಜಾನುವಾರು ರಕ್ಷಣೆ"
                                        AppLanguage.MARATHI -> "🐮 पशुधन संरक्षण"
                                    }
                                    activeChecklistText = getLivestockChecklist(currentLanguage)
                                    viewModel.speakOut(activeChecklistText)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = HarvestGold),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(8.dp)
                            ) {
                                Icon(Icons.Default.Pets, contentDescription = null, tint = RaatBlue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.ENGLISH -> "Save Livestock"
                                        AppLanguage.HINDI -> "मवेशी बचाएं"
                                        AppLanguage.KANNADA -> "ಜಾನುವಾರು"
                                        AppLanguage.MARATHI -> "पशुधन वाचवा"
                                    },
                                    fontSize = 11.sp,
                                    color = RaatBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Protect Crops
                            Button(
                                onClick = {
                                    activeChecklistTitle = when (currentLanguage) {
                                        AppLanguage.ENGLISH -> "🌾 Crop Rescue Checklist"
                                        AppLanguage.HINDI -> "🌾 फसल सुरक्षा चेकलिस्ट"
                                        AppLanguage.KANNADA -> "🌾 ಬೆಳೆ ಸಂರಕ್ಷಣೆ"
                                        AppLanguage.MARATHI -> "🌾 पीक संरक्षण"
                                    }
                                    activeChecklistText = getCropChecklist(currentLanguage)
                                    viewModel.speakOut(activeChecklistText)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MittiGreen),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(8.dp)
                            ) {
                                Icon(Icons.Default.Eco, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.ENGLISH -> "Protect Crops"
                                        AppLanguage.HINDI -> "फसल बचाएं"
                                        AppLanguage.KANNADA -> "ಬೆಳೆ ರಕ್ಷಿಸಿ"
                                        AppLanguage.MARATHI -> "पीक वाचवा"
                                    },
                                    fontSize = 11.sp,
                                    color = PureWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Acknowledge/Report Safe Button
                        OutlinedButton(
                            onClick = {
                                viewModel.acknowledgeAlert(activeCriticalAlert.id)
                                activeChecklistTitle = ""
                                activeChecklistText = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, DroughtRed),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DroughtRed),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.ENGLISH -> "Mark Received & Inform Village"
                                    AppLanguage.HINDI -> "सूचना मिली, सुरक्षित हूँ"
                                    AppLanguage.KANNADA -> "ವರದಿ ತಲುಪಿದೆ"
                                    AppLanguage.MARATHI -> "माहिती मिळाली"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LeafTint),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CloudQueue, contentDescription = null, tint = MittiGreen, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.ENGLISH -> "All Clear! No critical alerts."
                                    AppLanguage.HINDI -> "सब सुरक्षित है! कोई आपातकालीन चेतावनी नहीं।"
                                    AppLanguage.KANNADA -> "ಎಲ್ಲವೂ ಸುರಕ್ಷಿತವಾಗಿದೆ! ಯಾವುದೇ ತುರ್ತು ಎಚ್ಚರಿಕೆಗಳಿಲ್ಲ."
                                    AppLanguage.MARATHI -> "सर्व काही सुरळीत! कोणतीही गंभीर चेतावणी नाही."
                                },
                                fontWeight = FontWeight.Bold,
                                color = MittiGreen,
                                fontSize = 15.sp
                            )
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.ENGLISH -> "Weather is normal for agricultural operations today."
                                    AppLanguage.HINDI -> "कृषि कार्यों के लिए आज मौसम सामान्य और अनुकूल है।"
                                    AppLanguage.KANNADA -> "ಇಂದು ಕೃಷಿ ಚಟುವಟಿಕೆಗಳಿಗೆ ಹವಾಮಾನ ಸಾಮಾನ್ಯವಾಗಿದೆ."
                                    AppLanguage.MARATHI -> "आज शेतीकामासाठी हवामान अनुकूल व सामान्य आहे."
                                },
                                color = AshGrey,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // --- Checklist Expansion Board ---
        item {
            AnimatedVisibility(
                visible = activeChecklistTitle.isNotEmpty(),
                enter = fadeIn() + expandVertically()
            ) {
                val checklistBg = if (isDark) Color(0xFF252538) else PureWhite
                val checklistTextColor = if (isDark) PureWhite else RaatBlue

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = checklistBg),
                    border = BorderStroke(1.dp, HarvestGold)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = activeChecklistTitle,
                                fontWeight = FontWeight.Bold,
                                color = checklistTextColor,
                                fontSize = 15.sp
                            )
                            IconButton(onClick = { viewModel.speakOut(activeChecklistText) }) {
                                Icon(Icons.Default.VolumeUp, contentDescription = "Read Checklist", tint = MittiGreen)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = activeChecklistText,
                            fontSize = 13.sp,
                            color = checklistTextColor,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // --- Historic Alerts Log ---
        item {
            Text(
                text = when (currentLanguage) {
                    AppLanguage.ENGLISH -> "📜 Disaster Alerts Log"
                    AppLanguage.HINDI -> "📜 पिछले अलर्ट की सूची"
                    AppLanguage.KANNADA -> "📜 ಹಿಂದಿನ ಎಚ್ಚರಿಕೆಗಳು"
                    AppLanguage.MARATHI -> "📜 मागील अलर्ट यादी"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = RaatBlue,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(alerts) { alert ->
            AlertItemRow(alert, currentLanguage)
        }
    }
}

@Composable
fun AlertItemRow(alert: DisasterAlert, currentLanguage: AppLanguage) {
    val dateString = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(alert.timestamp))
    val isCritical = alert.severity == "CRITICAL"

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val cardBg = if (isDark) Color(0xFF252538) else PureWhite
    val textColor = if (isDark) PureWhite else RaatBlue

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(if (isCritical) DroughtRed else HarvestGold, RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getLocalizedAlertType(alert.alertType, currentLanguage),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isCritical) DroughtRed else textColor
                )
                Text(
                    text = getLocalizedMessage(alert.message, currentLanguage),
                    fontSize = 12.sp,
                    color = AshGrey,
                    maxLines = 2
                )
                Text(
                    text = dateString,
                    fontSize = 10.sp,
                    color = AshGrey
                )
            }

            if (alert.isAcknowledged) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Acknowledged",
                    tint = MittiGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun getLivestockChecklist(lang: AppLanguage): String {
    return when (lang) {
        AppLanguage.ENGLISH -> """
            1. Move cows and goats immediately to the elevated school playground or Panchayat yard.
            2. Pack dry straw/fodder (bhusa) inside protective plastic sheets for at least 3 days.
            3. Do NOT tie ropes or chains around animals' necks tonight — let them swim freely if water levels surge.
            4. Add bleaching powder to stock water tanks to avoid dysentery disease.
        """.trimIndent()
        AppLanguage.HINDI -> """
            1. अपने गाय, भैंस और बकरियों को तुरंत पंचायत भवन या स्कूल के ऊंचे मैदान में ले जाएं।
            2. पशुओं के लिए कम से कम ३ दिनों का सूखा भूसा और चारा प्लास्टिक शीट में सुरक्षित रखें।
            3. पशुओं को आज रात खूंटे से बांधकर न रखें — ताकि बाढ़ आने पर वे तैरकर जान बचा सकें।
            4. पीने के पानी के कुंड में लाल दवा मिलाएं ताकि मवेशी बीमारियों से बच सकें।
        """.trimIndent()
        AppLanguage.KANNADA -> """
            1. ನಿಮ್ಮ ಜಾನುವಾರುಗಳನ್ನು ತಕ್ಷಣ ಶಾಲೆಯ ಆಟದ ಮೈದಾನ ಅಥವಾ ಎತ್ತರದ ಸ್ಥಳಕ್ಕೆ ಸ್ಥಳಾಂತರಿಸಿ.
            2. ಕನಿಷ್ಠ ೩ ದಿನಗಳಿಗೆ ಸಾಕಾಗುವಷ್ಟು ಒಣ ಹುಲ್ಲು ಮತ್ತು ಮೇವನ್ನು ಪ್ಲಾಸ್ಟಿಕ್ ಚೀಲದಲ್ಲಿ ಸಂಗ್ರಹಿಸಿಡಿ.
            3. ಇಂದು ರಾತ್ರಿ ಪ್ರಾಣಿಗಳನ್ನು ಹಗ್ಗದಿಂದ ಕಟ್ಟಬೇಡಿ — ಪ್ರವಾಹ ಬಂದರೆ ಅವು ಈಜಲು ಸಾಧ್ಯವಾಗಬೇಕು.
            4. ಕುಡಿಯುವ ನೀರಿಗೆ ಸೂಕ್ತ ಔಷಧ ಬೆರೆಸಿ ಸಾಂಕ್ರಾಮಿಕ ರೋಗಗಳಿಂದ ಕಾಪಾಡಿ.
        """.trimIndent()
        AppLanguage.MARATHI -> """
            1. आपल्या गायी, म्हशी आणि शेळ्यांना त्वरित पंचायत कार्यालय किंवा शाळेच्या उंच मैदानावर न्यावे.
            2. जनावरांसाठी किमान ३ दिवसांचा सुका चारा प्लास्टिक शीटमध्ये गुंडाळून सुरक्षित ठेवावा.
            3. आज रात्री जनावरांना गोठ्यात घट्ट बांधू नका — जेणेकरून पूर आल्यास ते पोहून स्वतःचा बचाव करू शकतील.
            4. पिण्याच्या पाण्यात जंतुनाशक पावडर वापरावी.
        """.trimIndent()
    }
}

private fun getCropChecklist(lang: AppLanguage): String {
    return when (lang) {
        AppLanguage.ENGLISH -> """
            1. Clear crop drains and silt canals immediately so floodwater does not pool in the roots.
            2. Harvest early pods of soybean/mustard now, even if 90% ripe, to prevent rot damage.
            3. Shift harvested sacks from open ground to dry storage sheds.
        """.trimIndent()
        AppLanguage.HINDI -> """
            1. खेतों के जल निकासी नालों को तुरंत साफ करें ताकि बाढ़ का पानी जड़ों में जमा न हो।
            2. अगर सोयाबीन या सरसों की फसल ९०% तक पक चुकी है, तो तुरंत कटाई कर लें ताकि सड़ने से बचाया जा सके।
            3. कटी हुई फसलों के बोरों को खुले मैदान से हटाकर सूखे गोदामों में रखें।
        """.trimIndent()
        AppLanguage.KANNADA -> """
            1. ಜಮೀನಿನ ನೀರು ಹರಿದುಹೋಗಲು ಚರಂಡಿಗಳನ್ನು ತಕ್ಷಣ ಸ್ವಚ್ಛಗೊಳಿಸಿ.
            2. ಶೇ. ೯೦ ರಷ್ಟು ಬೆಳೆದ ಬೆಳೆಗಳನ್ನು ತಕ್ಷಣ ಕೊಯ್ಲು ಮಾಡಿ ಕೊಳೆಯದಂತೆ ರಕ್ಷಿಸಿ.
            3. ಕೊಯ್ಲು ಮಾಡಿದ ಮೂಟೆಗಳನ್ನು ತೆರೆದ ಮೈದಾನದಿಂದ ಒಣ ಗೋದಾಮಿಗೆ ಸಾಗಿಸಿ.
        """.trimIndent()
        AppLanguage.MARATHI -> """
            1. शेतातील पाण्याचा निचरा होणारे नाले त्वरित मोकळे करा, जेणेकरून पाणी साचणार नाही.
            2. सोयाबीन किंवा इतर पिके ९०% पर्यंत पक्व झाली असल्यास त्वरित कापणी करून घ्या.
            3. कापणी केलेल्या पोत्यांना उघड्या मैदानावरून सुरक्षित व सुक्या गोदामात हलवा.
        """.trimIndent()
    }
}

fun getLocalizedAlertType(alertType: String, lang: AppLanguage): String {
    return when (alertType) {
        "Flood Alert" -> when (lang) {
            AppLanguage.ENGLISH -> "Flood Alert"
            AppLanguage.HINDI -> "बाढ़ की चेतावनी"
            AppLanguage.KANNADA -> "ಪ್ರವಾಹದ ಮುನ್ನೆಚ್ಚರಿಕೆ"
            AppLanguage.MARATHI -> "पुराचा इशारा"
        }
        "Heat Wave Warning" -> when (lang) {
            AppLanguage.ENGLISH -> "Heat Wave Warning"
            AppLanguage.HINDI -> "लू की चेतावनी"
            AppLanguage.KANNADA -> "ಬಿಸಿಗಾಳಿ ಮುನ್ನೆಚ್ಚರಿಕೆ"
            AppLanguage.MARATHI -> "उष्णतेच्या लाटेचा इशारा"
        }
        else -> alertType
    }
}

fun getLocalizedMessage(message: String, lang: AppLanguage): String {
    if (message.contains("120mm") || message.contains("heavy rainfall", ignoreCase = true)) {
        return when (lang) {
            AppLanguage.ENGLISH -> "IMD alerts heavy rainfall exceeding 120mm in next 48 hours. Flood warning issued for low-lying areas of River Gandak. Secure livestock and relocate pumps."
            AppLanguage.HINDI -> "मौसम विभाग ने अगले 48 घंटों में 120 मिमी से अधिक भारी बारिश की चेतावनी दी है। गंडक नदी के निचले इलाकों के लिए बाढ़ की चेतावनी जारी। मवेशियों को सुरक्षित रखें और पंपों को स्थानांतरित करें।"
            AppLanguage.KANNADA -> "ಮುಂದಿನ 48 ಗಂಟೆಗಳಲ್ಲಿ 120 ಮಿಮೀ ಮೀರಿ ಭಾರಿ ಮಳೆಯಾಗುವ ಮುನ್ಸೂಚನೆ ಇದೆ. ಗಂಡಕ್ ನದಿಯ ತಗ್ಗು ಪ್ರದೇಶಗಳಿಗೆ ಪ್ರವಾಹದ ಎಚ್ಚರಿಕೆ ನೀಡಲಾಗಿದೆ. ಜಾನುವಾರುಗಳನ್ನು ಸುರಕ್ಷಿತವಾಗಿರಿಸಿ ಮತ್ತು ಪಂಪ್‌ಗಳನ್ನು ಸ್ಥಳಾಂತರಿಸಿ."
            AppLanguage.MARATHI -> "हवामान विभागाने पुढील ४८ तासांत १२० मिमी पेक्षा जास्त मुसळधार पावसाचा इशारा दिला आहे. गंडक नदीच्या सखल भागात पुराचा इशारा जारी. गुरेढोरे सुरक्षित करा आणि पंप हलवा."
        }
    } else if (message.contains("45") || message.contains("Heat", ignoreCase = true) || message.contains("temperature", ignoreCase = true)) {
        return when (lang) {
            AppLanguage.ENGLISH -> "Temperatures likely to reach 45°C this week. Ensure frequent light irrigation for standing vegetable crops in evening hours."
            AppLanguage.HINDI -> "इस सप्ताह तापमान 45 डिग्री सेल्सियस तक पहुंचने की संभावना है। शाम के समय खड़ी सब्जी फसलों के लिए बार-बार हल्की सिंचाई सुनिश्चित करें।"
            AppLanguage.KANNADA -> "ಈ ವಾರ ತಾಪಮಾನವು 45°C ತಲುಪುವ ಸಾಧ್ಯತೆಯಿದೆ. ಸಂಜೆಯ ಸಮಯದಲ್ಲಿ ತರಕಾರಿ ಬೆಳೆಗಳಿಗೆ ಆಗಾಗ್ಗೆ ಲಘು ನೀರಾವರಿ ಒದಗಿಸಿ."
            AppLanguage.MARATHI -> "या आठवड्यात तापमान ४५ अंश सेल्सिअसपर्यंत पोहोचण्याची शक्यता आहे. संध्याकाळच्या वेळी उभ्या असलेल्या भाजीपाल्याला वारंवार हलके पाणी द्यावे."
        }
    }
    return message
}
