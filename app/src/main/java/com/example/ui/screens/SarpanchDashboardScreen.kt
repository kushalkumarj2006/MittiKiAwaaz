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
                        text = "Ramnagar Village • Climate Resilience & Scheme Integrator",
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
                            text = "Based on aggregate soil pH, water scans, and community active hazard reports.",
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
                        text = "📍 Community Environmental Hotspots",
                        fontWeight = FontWeight.Bold,
                        color = RaatBlue,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Soil pH Warning
                    HotspotRowItem(
                        icon = Icons.Default.Terrain,
                        title = "Soil Acidity (Ward 4 Hotspot)",
                        description = "12 farms registered pH below 5.5. Immediate lime distribution advised.",
                        badgeText = "CRITICAL",
                        badgeBg = DroughtRed.copy(alpha = 0.1f),
                        badgeColor = DroughtRed
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Water Level
                    HotspotRowItem(
                        icon = Icons.Default.WaterDrop,
                        title = "Groundwater Aquifer Depth",
                        description = "Village well average aquifer down by 1.8 meters since last monsoon.",
                        badgeText = "WARNING",
                        badgeBg = HarvestGold.copy(alpha = 0.15f),
                        badgeColor = Color(0xFFC07000)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Sanitation Hotspot
                    HotspotRowItem(
                        icon = Icons.Default.DeleteOutline,
                        title = "Compost & Organic Conversion Pits",
                        description = "8 village compost sites fully utilized. Swachh Bharat grant eligibility confirmed.",
                        badgeText = "OPTIMAL",
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
                        text = "💡 Auto-Matched Subsidy Scheme Recommendation",
                        fontWeight = FontWeight.Bold,
                        color = MittiGreen,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Agricultural Lime Subsidy Scheme",
                        fontWeight = FontWeight.ExtraBold,
                        color = RaatBlue,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Target Audience: 12 Farmers with Acidic Soil (Ward 4)\nTotal Benefit: ₹2,40,000 Gov Subsidy Support",
                        fontSize = 12.sp,
                        color = AshGrey,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.generateSchemeApplication(
                                schemeName = "Agricultural Lime Subsidy Scheme",
                                farmerCount = 12,
                                location = "Ward 4 Hotspot",
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
                            text = "Draft & Submit Pre-filled Form",
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
                text = "📁 Active Village Scheme Applications",
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
                        text = "No submitted schemes. Fill recommendations to generate apps.",
                        modifier = Modifier.padding(20.dp),
                        textAlign = TextAlign.Center,
                        color = AshGrey,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(applications) { app ->
                SchemeApplicationCard(app)
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
fun SchemeApplicationCard(app: SchemeApplication) {
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
                        text = app.schemeName,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = RaatBlue
                    )
                    Text(
                        text = "Submitted: $dateString",
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
                        text = app.status,
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
                    Text(text = "FARMERS COUNT", fontSize = 9.sp, color = AshGrey)
                    Text(text = "${app.farmerCount} Registered", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = RaatBlue)
                }

                Column {
                    Text(text = "VILLAGE LOCATION", fontSize = 9.sp, color = AshGrey)
                    Text(text = app.location, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = RaatBlue)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "ESTIMATED FUND", fontSize = 9.sp, color = AshGrey)
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
