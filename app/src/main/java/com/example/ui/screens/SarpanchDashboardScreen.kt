package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.SchemeApplication
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.MittiViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SarpanchDashboardScreen(
    viewModel: MittiViewModel,
    modifier: Modifier = Modifier
) {
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val applications by viewModel.applications.collectAsStateWithLifecycle()
    val resilienceScore = viewModel.getResilienceScore()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header Title Card ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.ENGLISH -> "👑 Sarpanch Leadership Dashboard"
                            AppLanguage.HINDI -> "👑 सरपंच ग्राम विकास डैशबोर्ड"
                            AppLanguage.KANNADA -> "👑 ಗ್ರಾಮೀಣ ನಾಯಕತ್ವದ ಡ್ಯಾಶ್‌ಬೋರ್ಡ್"
                            AppLanguage.MARATHI -> "👑 सरपंच ग्राम विकास डॅशबोर्ड"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = RaatBlue
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.ENGLISH -> "Ramnagar Village • Climate Resilience & Scheme Integrator"
                            AppLanguage.HINDI -> "रामनगर गांव • जलवायु अनुकूलता और योजना समन्वयक"
                            AppLanguage.KANNADA -> "ರಾಮನಗರ ಗ್ರಾಮ • ಹವಾಮಾನ ಸ್ಥಿತಿಸ್ಥಾಪಕತ್ವ ಮತ್ತು ಯೋಜನೆಗಳ ಸಂಯೋಜನೆ"
                            AppLanguage.MARATHI -> "रामनगर गाव • हवामान लवचिकता आणि योजना समन्वयक"
                        },
                        fontSize = 12.sp,
                        color = AshGrey
                    )
                }
            }
        }

        // --- Custom Canvas Circular Gauge for Village Resilience Score ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1.2f)) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.ENGLISH -> "Village Resilience Score"
                                AppLanguage.HINDI -> "ग्राम अनुकूलता स्कोर"
                                AppLanguage.KANNADA -> "ಗ್ರಾಮದ ಸ್ಥಿತಿಸ್ಥಾಪಕತ್ವ ಸ್ಕೋರ್"
                                AppLanguage.MARATHI -> "गाव लवचिकता धावसंख्या"
                            },
                            fontWeight = FontWeight.Bold,
                            color = RaatBlue,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = getResilienceLabel(resilienceScore, currentLanguage),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = getResilienceColor(resilienceScore)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.ENGLISH -> "Based on aggregate soil pH, water scans, and community active hazard reports."
                                AppLanguage.HINDI -> "एकत्रित मिट्टी पीएच, जल स्तर और सामुदायिक सक्रिय खतरों की रिपोर्ट के आधार पर।"
                                AppLanguage.KANNADA -> "ಮಣ್ಣಿನ ರಸಸಾರ (pH), ಜಲಮಟ್ಟ ಮತ್ತು ಸಕ್ರಿಯ ಎಚ್ಚರಿಕೆಗಳ ಆಧಾರದ ಮೇಲೆ."
                                AppLanguage.MARATHI -> "एकत्रित माती पीएच, पाण्याचे स्कॅन आणि सामूहिक सक्रिय धोक्यांच्या अहवालावर आधारित."
                            },
                            fontSize = 11.sp,
                            color = AshGrey,
                            lineHeight = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Gauge Drawing
                    Box(
                        modifier = Modifier.size(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(90.dp)) {
                            // Backing Track
                            drawArc(
                                color = Color.LightGray.copy(alpha = 0.3f),
                                startAngle = 135f,
                                sweepAngle = 270f,
                                useCenter = false,
                                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                            )
                            // Filled Score Progress Arc
                            drawArc(
                                color = getResilienceColor(resilienceScore),
                                startAngle = 135f,
                                sweepAngle = 270f * (resilienceScore / 100f),
                                useCenter = false,
                                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$resilienceScore",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = getResilienceColor(resilienceScore)
                            )
                            Text(
                                text = "/100",
                                fontSize = 11.sp,
                                color = AshGrey
                            )
                        }
                    }
                }
            }
        }

        // --- Community Soil & Water Concerns Heatmap Tile ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.ENGLISH -> "📍 Community Environmental Hotspots"
                            AppLanguage.HINDI -> "📍 सामुदायिक पर्यावरण हॉटस्पॉट"
                            AppLanguage.KANNADA -> "📍 ಸಮುದಾಯ ಪರಿಸರ ಹಾಟ್‌ಸ್ಪಾಟ್‌ಗಳು"
                            AppLanguage.MARATHI -> "📍 सामुदायिक पर्यावरण हॉटस्पॉट"
                        },
                        fontWeight = FontWeight.Bold,
                        color = RaatBlue,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Soil pH Warning
                    HotspotRowItem(
                        icon = Icons.Default.Terrain,
                        title = when (currentLanguage) {
                            AppLanguage.ENGLISH -> "Soil Acidity (Ward 4 Hotspot)"
                            AppLanguage.HINDI -> "मिट्टी की अम्लता (वार्ड 4 हॉटस्पॉट)"
                            AppLanguage.KANNADA -> "ಮಣ್ಣಿನ ಆಮ್ಲೀಯತೆ (ವಾರ್ಡ್ 4 ಹಾಟ್‌ಸ್ಪಾಟ್)"
                            AppLanguage.MARATHI -> "मातीची आम्लता (वॉर्ड 4 हॉटस्पॉट)"
                        },
                        description = when (currentLanguage) {
                            AppLanguage.ENGLISH -> "12 farms registered pH below 5.5. Immediate lime distribution advised."
                            AppLanguage.HINDI -> "12 खेतों का पीएच 5.5 से कम दर्ज किया गया। तुरंत चूना वितरण की सलाह दी जाती है।"
                            AppLanguage.KANNADA -> "12 ಜಮೀನುಗಳ ರಸಸಾರ 5.5 ಕ್ಕಿಂತ ಕಡಿಮೆ ಇದೆ. ತಕ್ಷಣ ಸುಣ್ಣ ವಿತರಣೆಗೆ ಶಿಫಾರಸು ಮಾಡಲಾಗಿದೆ."
                            AppLanguage.MARATHI -> "12 शेतांमध्ये पीएच 5.5 पेक्षा कमी आढळला. त्वरित चुना वाटपाचा सल्ला।"
                        },
                        badgeText = when (currentLanguage) {
                            AppLanguage.ENGLISH -> "CRITICAL"
                            AppLanguage.HINDI -> "गंभीर"
                            AppLanguage.KANNADA -> "ಅಪಾಯಕಾರಿ"
                            AppLanguage.MARATHI -> "गंभीर"
                        },
                        badgeBg = DroughtRed.copy(alpha = 0.1f),
                        badgeColor = DroughtRed
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Water Level
                    HotspotRowItem(
                        icon = Icons.Default.WaterDrop,
                        title = when (currentLanguage) {
                            AppLanguage.ENGLISH -> "Groundwater Aquifer Depth"
                            AppLanguage.HINDI -> "भूजल स्तर की गहराई"
                            AppLanguage.KANNADA -> "ಅಂತರ್ಜಲ ಮಟ್ಟ"
                            AppLanguage.MARATHI -> "भूजल पातळी खोली"
                        },
                        description = when (currentLanguage) {
                            AppLanguage.ENGLISH -> "Village well average aquifer down by 1.8 meters since last monsoon."
                            AppLanguage.HINDI -> "पिछले मानसून से गांव के कुएं का औसत भूजल स्तर 1.8 मीटर नीचे चला गया है।"
                            AppLanguage.KANNADA -> "ಕಳೆದ ಮಳೆಗಾಲದಿಂದ ಗ್ರಾಮದ ಬಾವಿಯ ಸರಾಸರಿ ಅಂತರ್ಜಲ ಮಟ್ಟ 1.8 ಮೀಟರ್ ಕುಸಿದಿದೆ."
                            AppLanguage.MARATHI -> "मागील पावसाळ्यापासून गावातील विहिरीची सरासरी पातळी 1.8 मीटर खाली गेली आहे."
                        },
                        badgeText = when (currentLanguage) {
                            AppLanguage.ENGLISH -> "WARNING"
                            AppLanguage.HINDI -> "चेतावनी"
                            AppLanguage.KANNADA -> "ಎಚ್ಚರಿಕೆ"
                            AppLanguage.MARATHI -> "इशारा"
                        },
                        badgeBg = HarvestGold.copy(alpha = 0.15f),
                        badgeColor = Color(0xFFC07000)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Sanitation Hotspot
                    HotspotRowItem(
                        icon = Icons.Default.DeleteOutline,
                        title = when (currentLanguage) {
                            AppLanguage.ENGLISH -> "Compost & Organic Conversion Pits"
                            AppLanguage.HINDI -> "कम्पोस्ट और जैविक खाद गड्ढे"
                            AppLanguage.KANNADA -> "ಕಂಪೋಸ್ಟ್ ಮತ್ತು ಜೈವಿಕ ಗೊಬ್ಬರ ಗುಂಡಿಗಳು"
                            AppLanguage.MARATHI -> "कंपोस्ट आणि सेंद्रिय खत खड्डे"
                        },
                        description = when (currentLanguage) {
                            AppLanguage.ENGLISH -> "8 village compost sites fully utilized. Swachh Bharat grant eligibility confirmed."
                            AppLanguage.HINDI -> "8 ग्राम कम्पोस्ट साइट्स का पूर्ण उपयोग। स्वच्छ भारत अनुदान पात्रता की पुष्टि।"
                            AppLanguage.KANNADA -> "8 ಜೈವಿಕ ಗೊಬ್ಬರ ಗುಂಡಿಗಳು ಪೂರ್ಣಗೊಂಡಿವೆ. ಸ್ವಚ್ಛ ಭಾರತ ಅನುದಾನದ ಅರ್ಹತೆ ದೃಢಪಟ್ಟಿದೆ."
                            AppLanguage.MARATHI -> "8 सेंद्रिय खत केंद्रे पूर्ण वापरात. स्वच्छ भारत अनुदान पात्रता निश्चित."
                        },
                        badgeText = when (currentLanguage) {
                            AppLanguage.ENGLISH -> "OPTIMAL"
                            AppLanguage.HINDI -> "सर्वोत्तम"
                            AppLanguage.KANNADA -> "ಉತ್ತಮ"
                            AppLanguage.MARATHI -> "सर्वोत्तम"
                        },
                        badgeBg = MittiGreen.copy(alpha = 0.1f),
                        badgeColor = MittiGreen
                    )
                }
            }
        }

        // --- Scheme Subsidy Quick-Generator ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LeafTint),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MittiGreen)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.ENGLISH -> "💡 Auto-Matched Subsidy Scheme Recommendation"
                            AppLanguage.HINDI -> "💡 ऑटो-मैच की गई सब्सिडी योजना अनुशंसा"
                            AppLanguage.KANNADA -> "💡 ಶಿಫಾರಸು ಮಾಡಲಾದ ಸಬ್ಸಿಡಿ ಯೋಜನೆ"
                            AppLanguage.MARATHI -> "💡 ऑटो-मॅच केलेली सवलत योजना शिफारस"
                        },
                        fontWeight = FontWeight.Bold,
                        color = MittiGreen,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.ENGLISH -> "Agricultural Lime Subsidy Scheme"
                            AppLanguage.HINDI -> "कृषि चूना सब्सिडी योजना"
                            AppLanguage.KANNADA -> "ಕೃಷಿ ಸುಣ್ಣ ಸಬ್ಸಿಡಿ ಯೋಜನೆ"
                            AppLanguage.MARATHI -> "कृषी चुना अनुदान योजना"
                        },
                        fontWeight = FontWeight.ExtraBold,
                        color = RaatBlue,
                        fontSize = 16.sp
                    )
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.ENGLISH -> "Target Audience: 12 Farmers with Acidic Soil (Ward 4)\nTotal Benefit: ₹2,40,000 Gov Subsidy Support"
                            AppLanguage.HINDI -> "लक्षित लाभार्थी: अम्लीय मिट्टी वाले 12 किसान (वार्ड 4)\nकुल लाभ: ₹2,40,000 सरकारी सब्सिडी सहायता"
                            AppLanguage.KANNADA -> "ಫಲಾನುಭವಿಗಳು: ಆಮ್ಲೀಯ ಮಣ್ಣು ಹೊಂದಿರುವ 12 ರೈತರು (ವಾರ್ಡ್ 4)\nಒಟ್ಟು ಲಾಭ: ₹2,40,000 ಸರ್ಕಾರಿ ಸಬ್ಸಿಡಿ ನೆರವು"
                            AppLanguage.MARATHI -> "लक्षित शेतकरी: आम्लयुक्त माती असलेले 12 शेतकरी (वॉर्ड 4)\nएकूण लाभ: ₹2,40,000 सरकारी अनुदान मदत"
                        },
                        fontSize = 12.sp,
                        color = AshGrey,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val nameToSave = when (currentLanguage) {
                                AppLanguage.ENGLISH -> "Agricultural Lime Subsidy Scheme"
                                AppLanguage.HINDI -> "कृषि चूना सब्सिडी योजना"
                                AppLanguage.KANNADA -> "ಕೃಷಿ ಸುಣ್ಣ ಸಬ್ಸಿಡಿ ಯೋಜನೆ"
                                AppLanguage.MARATHI -> "कृषी चुना अनुदान योजना"
                            }
                            val locToSave = when (currentLanguage) {
                                AppLanguage.ENGLISH -> "Ward 4 Hotspot"
                                AppLanguage.HINDI -> "वार्ड 4 हॉटस्पॉट"
                                AppLanguage.KANNADA -> "ವಾರ್ಡ್ 4 ಹಾಟ್‌ಸ್ಪಾಟ್"
                                AppLanguage.MARATHI -> "वॉर्ड 4 हॉटस्पॉट"
                            }
                            viewModel.generateSchemeApplication(
                                schemeName = nameToSave,
                                farmerCount = 12,
                                location = locToSave,
                                benefit = "₹2,40,000"
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MittiGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = PureWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.ENGLISH -> "Draft & Submit Pre-filled Form"
                                AppLanguage.HINDI -> "फॉर्म ड्राफ्ट करें और सबमिट करें"
                                AppLanguage.KANNADA -> "ಅರ್ಜಿ ನಮೂನೆಯನ್ನು ಸಿದ್ಧಪಡಿಸಿ ಸಲ್ಲಿಸಿ"
                                AppLanguage.MARATHI -> "अर्ज मसुदा तयार करून सबमिट करा"
                            },
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // --- Submitted Schemes Applications Header ---
        item {
            Text(
                text = when (currentLanguage) {
                    AppLanguage.ENGLISH -> "📁 Active Village Scheme Applications"
                    AppLanguage.HINDI -> "📁 सक्रिय ग्राम योजना आवेदन"
                    AppLanguage.KANNADA -> "📁 ಸಕ್ರಿಯ ಗ್ರಾಮ ಯೋಜನೆ ಅರ್ಜಿಗಳು"
                    AppLanguage.MARATHI -> "📁 सक्रिय ग्राम योजना अर्ज"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = RaatBlue,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (applications.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PureWhite)
                ) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.ENGLISH -> "No submitted schemes. Fill recommendations to generate apps."
                            AppLanguage.HINDI -> "कोई सबमिट किए गए आवेदन नहीं हैं। योजना शुरू करने के लिए अनुशंसा पर क्लिक करें।"
                            AppLanguage.KANNADA -> "ಯಾವುದೇ ಅರ್ಜಿಗಳು ಸಲ್ಲಿಕೆಯಾಗಿಲ್ಲ."
                            AppLanguage.MARATHI -> "कोणतेही सबमिट केलेले अर्ज नाहीत. नवीन अर्ज सुरू करण्यासाठी शिफारसीवर क्लिक करा."
                        },
                        modifier = Modifier.padding(20.dp),
                        textAlign = TextAlign.Center,
                        color = AshGrey,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(applications) { app ->
                SchemeApplicationCard(app, currentLanguage)
            }
        }
    }
}

@Composable
fun HotspotRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    badgeText: String,
    badgeBg: Color,
    badgeColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(MittiBeige, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MittiGreen, modifier = Modifier.size(18.dp))
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = RaatBlue
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = badgeBg),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Text(
                text = description,
                fontSize = 11.sp,
                color = AshGrey,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun SchemeApplicationCard(app: SchemeApplication, currentLanguage: AppLanguage) {
    val dateString = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(app.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = when (app.schemeName) {
                            "PM-KISAN Nidhi Yojna" -> when (currentLanguage) {
                                AppLanguage.ENGLISH -> "PM-KISAN Nidhi Yojna"
                                AppLanguage.HINDI -> "पीएम-किसान सम्मान निधि योजना"
                                AppLanguage.KANNADA -> "ಪಿಎಂ-ಕಿಸಾನ್ ಸಮ್ಮಾನ್ ನಿಧಿ ಯೋಜನೆ"
                                AppLanguage.MARATHI -> "पीएम-किसान सन्मान निधी योजना"
                            }
                            "Agricultural Lime Subsidy Scheme" -> when (currentLanguage) {
                                AppLanguage.ENGLISH -> "Agricultural Lime Subsidy Scheme"
                                AppLanguage.HINDI -> "कृषि चूना सब्सिडी योजना"
                                AppLanguage.KANNADA -> "ಕೃಷಿ ಸುಣ್ಣ ಸಬ್ಸಿಡಿ ಯೋಜನೆ"
                                AppLanguage.MARATHI -> "कृषी चुना अनुदान योजना"
                            }
                            else -> app.schemeName
                        },
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = RaatBlue
                    )
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.ENGLISH -> "Submitted: $dateString"
                            AppLanguage.HINDI -> "सबमिट किया गया: $dateString"
                            AppLanguage.KANNADA -> "ಸಲ್ಲಿಸಲಾಗಿದೆ: $dateString"
                            AppLanguage.MARATHI -> "सादर केले: $dateString"
                        },
                        fontSize = 11.sp,
                        color = AshGrey
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (app.status == "SUBMITTED") LeafTint else HarvestGold.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = when (app.status) {
                            "SUBMITTED" -> when (currentLanguage) {
                                AppLanguage.ENGLISH -> "SUBMITTED"
                                AppLanguage.HINDI -> "सबमिट हुआ"
                                AppLanguage.KANNADA -> "ಸಲ್ಲಿಸಲಾಗಿದೆ"
                                AppLanguage.MARATHI -> "सादर केले"
                            }
                            "DRAFT" -> when (currentLanguage) {
                                AppLanguage.ENGLISH -> "DRAFT"
                                AppLanguage.HINDI -> "ड्राफ्ट"
                                AppLanguage.KANNADA -> "ಕರಡು"
                                AppLanguage.MARATHI -> "मसुदा"
                            }
                            else -> app.status
                        },
                        color = if (app.status == "SUBMITTED") MittiGreen else Color(0xFFC07000),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp), color = MittiBeige)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.ENGLISH -> "FARMERS COUNT"
                            AppLanguage.HINDI -> "किसान संख्या"
                            AppLanguage.KANNADA -> "ರೈತರ ಸಂಖ್ಯೆ"
                            AppLanguage.MARATHI -> "शेतकरी संख्या"
                        },
                        fontSize = 9.sp,
                        color = AshGrey
                    )
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.ENGLISH -> "${app.farmerCount} Registered"
                            AppLanguage.HINDI -> "${app.farmerCount} पंजीकृत"
                            AppLanguage.KANNADA -> "${app.farmerCount} ನೊಂದಾಯಿತ"
                            AppLanguage.MARATHI -> "${app.farmerCount} नोंदणीकृत"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = RaatBlue
                    )
                }

                Column {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.ENGLISH -> "VILLAGE LOCATION"
                            AppLanguage.HINDI -> "ग्राम स्थान"
                            AppLanguage.KANNADA -> "ಗ್ರಾಮದ ಸ್ಥಳ"
                            AppLanguage.MARATHI -> "गावचे ठिकाण"
                        },
                        fontSize = 9.sp,
                        color = AshGrey
                    )
                    Text(
                        text = when (app.location) {
                            "Ramnagar Village" -> when (currentLanguage) {
                                AppLanguage.ENGLISH -> "Ramnagar Village"
                                AppLanguage.HINDI -> "रामनगर ग्राम"
                                AppLanguage.KANNADA -> "ರಾಮನಗರ ಗ್ರಾಮ"
                                AppLanguage.MARATHI -> "रामनगर गाव"
                            }
                            "Ramnagar Village (Acidic Soil Hotspot)" -> when (currentLanguage) {
                                AppLanguage.ENGLISH -> "Ramnagar Village (Acidic Hotspot)"
                                AppLanguage.HINDI -> "रामनगर ग्राम (अम्लीय मिट्टी)"
                                AppLanguage.KANNADA -> "ರಾಮನಗರ ಗ್ರಾಮ (ಆಮ್ಲೀಯ ಮಣ್ಣು)"
                                AppLanguage.MARATHI -> "रामनगर गाव (आम्लयुक्त माती)"
                            }
                            "Ward 4 Hotspot" -> when (currentLanguage) {
                                AppLanguage.ENGLISH -> "Ward 4 Hotspot"
                                AppLanguage.HINDI -> "वार्ड 4 हॉटस्पॉट"
                                AppLanguage.KANNADA -> "ವಾರ್ಡ್ 4 ಹಾಟ್‌ಸ್ಪಾಟ್"
                                AppLanguage.MARATHI -> "वॉर्ड 4 हॉटस्पॉट"
                            }
                            else -> app.location
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = RaatBlue
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.ENGLISH -> "ESTIMATED FUND"
                            AppLanguage.HINDI -> "अनुमानित निधि"
                            AppLanguage.KANNADA -> "ಅಂದಾಜು ಅನುದಾನ"
                            AppLanguage.MARATHI -> "अंदाजित निधी"
                        },
                        fontSize = 9.sp,
                        color = AshGrey
                    )
                    Text(text = app.estimatedBenefit, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MittiGreen)
                }
            }
        }
    }
}

fun getResilienceColor(score: Int): Color {
    return when {
        score < 50 -> DroughtRed
        score < 75 -> HarvestGold
        else -> MittiGreen
    }
}

fun getResilienceLabel(score: Int, lang: AppLanguage): String {
    return when {
        score < 50 -> when (lang) {
            AppLanguage.ENGLISH -> "🚨 Severe Vulnerability Alert"
            AppLanguage.HINDI -> "🚨 गंभीर संकट चेतावनी"
            AppLanguage.KANNADA -> "🚨 ತೀವ್ರ ಹಾನಿ ಎಚ್ಚರಿಕೆ"
            AppLanguage.MARATHI -> "🚨 तीव्र जोखीम चेतावणी"
        }
        score < 75 -> when (lang) {
            AppLanguage.ENGLISH -> "⚠️ Moderate Resilience Needs Support"
            AppLanguage.HINDI -> "⚠️ मध्यम अनुकूलता सुधार की आवश्यकता"
            AppLanguage.KANNADA -> "⚠️ ಮಧ್ಯಮ ಮಟ್ಟದ ಸ್ಥಿತಿ"
            AppLanguage.MARATHI -> "⚠️ मध्यम लवचिकता सुधारणेची गरज"
        }
        else -> when (lang) {
            AppLanguage.ENGLISH -> "✅ High Resilience Village"
            AppLanguage.HINDI -> "✅ उच्च अनुकूल ग्राम"
            AppLanguage.KANNADA -> "✅ ಉತ್ತಮ ಗ್ರಾಮ"
            AppLanguage.MARATHI -> "✅ उच्च अनुकूल गाव"
        }
    }
}
