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
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
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
                        text = "Real-time alerts synced from Indian Meteorological Dept (IMD).",
                        fontSize = 12.sp,
                        color = AshGrey
                    )
                }
            }
        }

        // --- Active Red Flood Alert Section ---
        if (activeCriticalAlert != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DroughtRed.copy(alpha = 0.08f)),
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
                                text = activeCriticalAlert.alertType,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DroughtRed
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = activeCriticalAlert.message,
                            fontSize = 14.sp,
                            color = RaatBlue,
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
                            color = RaatBlue
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
                                text = "All Clear! No critical alerts.",
                                fontWeight = FontWeight.Bold,
                                color = MittiGreen,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Weather is normal for agricultural operations today.",
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
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
                                color = RaatBlue,
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
                            color = RaatBlue,
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
            AlertItemRow(alert)
        }
    }
}

@Composable
fun AlertItemRow(alert: DisasterAlert) {
    val dateString = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(alert.timestamp))
    val isCritical = alert.severity == "CRITICAL"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite)
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
                    text = alert.alertType,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isCritical) DroughtRed else RaatBlue
                )
                Text(
                    text = alert.message,
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
