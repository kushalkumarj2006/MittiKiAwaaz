package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import com.example.data.db.SoilScan
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.MittiViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// CameraX & Permission Imports
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SoilScanScreen(
    viewModel: MittiViewModel,
    modifier: Modifier = Modifier
) {
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val scannedPh by viewModel.scannedPh.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val scanResult by viewModel.scanResult.collectAsStateWithLifecycle()
    val soilScans by viewModel.soilScans.collectAsStateWithLifecycle()

    val uploadedPhImageUri by viewModel.uploadedPhImageUri.collectAsStateWithLifecycle()
    val isUploadingPhImage by viewModel.isUploadingPhImage.collectAsStateWithLifecycle()

    var showCameraView by remember { mutableStateOf(false) }

    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )
    var hasRequestedPermission by remember { mutableStateOf(false) }

    LaunchedEffect(cameraPermissionState.status.isGranted) {
        if (cameraPermissionState.status.isGranted && hasRequestedPermission) {
            showCameraView = true
            hasRequestedPermission = false
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.uploadSoilPhImage(it.toString())
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (showCameraView) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.Black)
            ) {
                // Real Camera Preview
                if (cameraPermissionState.status.isGranted) {
                    val context = LocalContext.current
                    val lifecycleOwner = LocalLifecycleOwner.current
                    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

                    DisposableEffect(lifecycleOwner) {
                        onDispose {
                            try {
                                if (cameraProviderFuture.isDone) {
                                    cameraProviderFuture.get().unbindAll()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx).apply {
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                            }
                            val executor = ContextCompat.getMainExecutor(ctx)
                            cameraProviderFuture.addListener({
                                try {
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build().also {
                                        it.setSurfaceProvider(previewView.surfaceProvider)
                                    }
                                    val cameraSelector = when {
                                        cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) -> CameraSelector.DEFAULT_BACK_CAMERA
                                        cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) -> CameraSelector.DEFAULT_FRONT_CAMERA
                                        else -> CameraSelector.DEFAULT_BACK_CAMERA
                                    }
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }, executor)
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // Overlay Viewfinder & UI
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.ENGLISH -> "📸 Position pH Strip Inside Target"
                                AppLanguage.HINDI -> "📸 पीएच पट्टी को बॉक्स के अंदर रखें"
                                AppLanguage.KANNADA -> "📸 pH ಪಟ್ಟಿಯನ್ನು ಬಾಕ್ಸ್ ಒಳಗೆ ಇರಿಸಿ"
                                AppLanguage.MARATHI -> "📸 pH पट्टी बॉक्समध्ये ठेवा"
                            },
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    // VIEW FINDER target box in center
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .border(3.dp, HarvestGold, RoundedCornerShape(16.dp))
                            .background(Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        // Horizontal green line scanning across the card
                        val infiniteTransition = rememberInfiniteTransition(label = "scanLine")
                        val translateY by infiniteTransition.animateFloat(
                            initialValue = -100f,
                            targetValue = 100f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1500, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "translate"
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .offset(y = translateY.dp)
                                .background(MittiGreen)
                        )

                        // Mock pH paper representation inside box (as a helpful scanning target instruction)
                        Card(
                            modifier = Modifier.size(140.dp, 40.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = getColorForPh(scannedPh).copy(alpha = 0.7f)
                            ),
                            border = BorderStroke(1.dp, PureWhite)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "pH ${String.format(Locale.US, "%.1f", scannedPh)}",
                                    color = if (scannedPh < 6.5) RaatBlue else PureWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    // Buttons overlay at the bottom
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Button(
                                onClick = { showCameraView = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
                            ) {
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.ENGLISH -> "Cancel"
                                        AppLanguage.HINDI -> "रद्द करें"
                                        AppLanguage.KANNADA -> "ರದ್ದುಮಾಡಿ"
                                        AppLanguage.MARATHI -> "रद्द करा"
                                    },
                                    color = PureWhite
                                )
                            }

                            Button(
                                onClick = {
                                    showCameraView = false
                                    viewModel.triggerSoilScanSimulation()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MittiGreen)
                            ) {
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.ENGLISH -> "Capture & Diagnose"
                                        AppLanguage.HINDI -> "जाँच शुरू करें"
                                        AppLanguage.KANNADA -> "ಪರೀಕ್ಷಿಸಿ"
                                        AppLanguage.MARATHI -> "तपासा"
                                    },
                                    color = PureWhite
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // --- Normal Interactive Scanning Screen ---
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.ENGLISH -> "🧪 Digital Soil Health Scan"
                                    AppLanguage.HINDI -> "🧪 डिजिटल मिट्टी स्वास्थ्य परीक्षण"
                                    AppLanguage.KANNADA -> "🧪 ಡಿಜಿಟಲ್ ಮಣ್ಣಿನ ಆರೋಗ್ಯ ಪರೀಕ್ಷೆ"
                                    AppLanguage.MARATHI -> "🧪 डिजिटल माती आरोग्य चाचणी"
                                },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MittiGreen
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.ENGLISH -> "Match the strip color or tap Camera to scan in real-time."
                                    AppLanguage.HINDI -> "पट्टी के रंग को मिलाएँ या कैमरे से लाइव स्कैन करें।"
                                    AppLanguage.KANNADA -> "ಬಣ್ಣವನ್ನು ಹೊಂದಿಸಿ ಅಥವಾ ಲೈವ್ ಸ್ಕ್ಯಾನ್ ಮಾಡಲು ಕ್ಯಾಮೆರಾ ಟ್ಯಾಪ್ ಮಾಡಿ."
                                    AppLanguage.MARATHI -> "रंग जुळवा किंवा लाईव्ह स्कॅनिंगसाठी कॅमेरा दाबा."
                                },
                                fontSize = 12.sp,
                                color = AshGrey,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Interactive pH Selector Scale (Illiteracy fallback helper)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.ENGLISH -> "1. Select pH Test Strip Color"
                                    AppLanguage.HINDI -> "1. परीक्षण पट्टी का रंग चुनें"
                                    AppLanguage.KANNADA -> "1. ಪಟ್ಟಿಯ ಬಣ್ಣವನ್ನು ಆಯ್ಕೆಮಾಡಿ"
                                    AppLanguage.MARATHI -> "1. चाचणी पट्टीचा रंग निवडा"
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = RaatBlue
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Large colored pH value display
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                getColorForPh(scannedPh),
                                                getColorForPh(scannedPh).copy(alpha = 0.7f)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "pH ${String.format(Locale.US, "%.1f", scannedPh)}",
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (scannedPh < 6.5) RaatBlue else PureWhite
                                    )
                                    Text(
                                        text = getPhLabel(scannedPh, currentLanguage),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (scannedPh < 6.5) RaatBlue.copy(alpha = 0.8f) else PureWhite.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Color chip indicators 4.0 to 9.0
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val phOptions = listOf(4.0, 5.0, 5.8, 6.5, 7.2, 8.0, 9.0)
                                phOptions.forEach { ph ->
                                    val isSelected = Math.abs(scannedPh - ph) < 0.2
                                    Box(
                                        modifier = Modifier
                                            .size(width = 44.dp, height = 36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(getColorForPh(ph))
                                            .border(
                                                width = if (isSelected) 3.dp else 0.dp,
                                                color = if (isSelected) RaatBlue else Color.Transparent,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { viewModel.updatePh(ph) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = ph.toString(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = getTextColorForPh(ph)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Nutrient overview (N, P, K)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.ENGLISH -> "2. Soil N-P-K Nutrients"
                                    AppLanguage.HINDI -> "2. मिट्टी के मुख्य पोषक तत्व (N-P-K)"
                                    AppLanguage.KANNADA -> "2. ಮಣ್ಣಿನ ಪೋಷಕಾಂಶಗಳು"
                                    AppLanguage.MARATHI -> "2. मातीचे पोषक घटक"
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = RaatBlue
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                NutrientBadge("Nitrogen (N)", "LOW", Color.Red.copy(alpha = 0.1f), Color.Red, Modifier.weight(1f))
                                NutrientBadge("Phosphorus (P)", "MEDIUM", HarvestGold.copy(alpha = 0.15f), Color(0xFFD48B00), Modifier.weight(1f))
                                NutrientBadge("Potassium (K)", "HIGH", MittiGreen.copy(alpha = 0.1f), MittiGreen, Modifier.weight(1f))
                            }
                        }
                    }
                }

                // Diagnostic actions & Image Upload
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.ENGLISH -> "3. Analyze pH Strip Image"
                                    AppLanguage.HINDI -> "3. पीएच पट्टी छवि विश्लेषण"
                                    AppLanguage.KANNADA -> "3. pH ಪಟ್ಟಿ ಚಿತ್ರ ವಿಶ್ಲೇಷಣೆ"
                                    AppLanguage.MARATHI -> "3. pH पट्टी चित्र विश्लेषण"
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = RaatBlue,
                                modifier = Modifier.align(Alignment.Start)
                            )

                            // Previews of uploaded photo
                            if (uploadedPhImageUri != null || isUploadingPhImage) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.DarkGray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isUploadingPhImage) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            CircularProgressIndicator(color = HarvestGold)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                "Analyzing Strip Colors...",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    } else {
                                        // Display simulated strip image with diagnostic text
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        listOf(getColorForPh(scannedPh), RaatBlue)
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(Icons.Default.TaskAlt, contentDescription = null, tint = HarvestGold, modifier = Modifier.size(32.dp))
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    when (currentLanguage) {
                                                         AppLanguage.ENGLISH -> "PH STRIP IMAGE REGISTERED"
                                                         AppLanguage.HINDI -> "पीएच पट्टी का चित्र पंजीकृत"
                                                         AppLanguage.KANNADA -> "pH ಪಟ್ಟಿ ಚಿತ್ರ ನೊಂದಾಯಿಸಲಾಗಿದೆ"
                                                         AppLanguage.MARATHI -> "pH पट्टी चित्र नोंदवले गेले"
                                                     },
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                                Text(
                                                    when (currentLanguage) {
                                                         AppLanguage.ENGLISH -> "Analyzed pH: ${String.format(Locale.US, "%.1f", scannedPh)}"
                                                         AppLanguage.HINDI -> "विश्लेषित पीएच: ${String.format(Locale.US, "%.1f", scannedPh)}"
                                                         AppLanguage.KANNADA -> "ವಿಶ್ಲೇಷಿಸಿದ pH: ${String.format(Locale.US, "%.1f", scannedPh)}"
                                                         AppLanguage.MARATHI -> "विश्लेषित पीएच: ${String.format(Locale.US, "%.1f", scannedPh)}"
                                                     },
                                                    color = HarvestGold,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Preset Soil pH Strip simulator buttons (tactile & educational fallback)
                            Text(
                                text = when (currentLanguage) {
                                     AppLanguage.ENGLISH -> "Or test with quick sample strips:"
                                     AppLanguage.HINDI -> "या त्वरित नमूना पट्टियों से जांचें:"
                                     AppLanguage.KANNADA -> "ಅಥವಾ ಶೀಘ್ರ ಮಾದರಿ ಪಟ್ಟಿಗಳಿಂದ ಪರೀಕ್ಷಿಸಿ:"
                                     AppLanguage.MARATHI -> "किंवा त्वरित नमुना पट्ट्यांसह तपासा:"
                                 },
                                style = MaterialTheme.typography.labelSmall,
                                color = AshGrey,
                                modifier = Modifier.align(Alignment.Start)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Acidic
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFF8F00).copy(alpha = 0.2f))
                                        .border(1.dp, Color(0xFFFF8F00), RoundedCornerShape(8.dp))
                                        .clickable { viewModel.uploadSoilPhImage("preset_acidic") }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                         text = when (currentLanguage) {
                                             AppLanguage.ENGLISH -> "Acidic pH 5.5"
                                             AppLanguage.HINDI -> "अम्लीय पीएच 5.5"
                                             AppLanguage.KANNADA -> "ಆಮ್ಲೀಯ pH 5.5"
                                             AppLanguage.MARATHI -> "आम्लयुक्त पीएच 5.5"
                                         },
                                         fontSize = 11.sp,
                                         color = Color(0xFFD48B00),
                                         fontWeight = FontWeight.Bold
                                     )
                                }

                                // Neutral
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF4CAF50).copy(alpha = 0.2f))
                                        .border(1.dp, Color(0xFF4CAF50), RoundedCornerShape(8.dp))
                                        .clickable { viewModel.uploadSoilPhImage("preset_neutral") }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                         text = when (currentLanguage) {
                                             AppLanguage.ENGLISH -> "Neutral pH 7.0"
                                             AppLanguage.HINDI -> "उदासीन पीएच 7.0"
                                             AppLanguage.KANNADA -> "ತಟಸ್ಥ pH 7.0"
                                             AppLanguage.MARATHI -> "उदासीन पीएच 7.0"
                                         },
                                         fontSize = 11.sp,
                                         color = Color(0xFF2E7D32),
                                         fontWeight = FontWeight.Bold
                                     )
                                }

                                // Alkaline
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF3F51B5).copy(alpha = 0.2f))
                                        .border(1.dp, Color(0xFF3F51B5), RoundedCornerShape(8.dp))
                                        .clickable { viewModel.uploadSoilPhImage("preset_alkaline") }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                         text = when (currentLanguage) {
                                             AppLanguage.ENGLISH -> "Alkaline pH 8.2"
                                             AppLanguage.HINDI -> "क्षारीय पीएच 8.2"
                                             AppLanguage.KANNADA -> "ಕ್ಷಾರೀಯ pH 8.2"
                                             AppLanguage.MARATHI -> "क्षारयुक्त पीएच 8.2"
                                         },
                                         fontSize = 11.sp,
                                         color = Color(0xFF283593),
                                         fontWeight = FontWeight.Bold
                                     )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Camera
                                Button(
                                    onClick = { 
                                        if (cameraPermissionState.status.isGranted) {
                                            showCameraView = true 
                                        } else {
                                            hasRequestedPermission = true
                                            cameraPermissionState.launchPermissionRequest()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = HarvestGold),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                ) {
                                    Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = RaatBlue, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Camera",
                                        color = RaatBlue,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }

                                // Upload
                                Button(
                                    onClick = { imagePickerLauncher.launch("image/*") },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                ) {
                                    Icon(Icons.Default.UploadFile, contentDescription = null, tint = PureWhite, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Upload Image",
                                        color = PureWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            // Full Analyze Button
                            Button(
                                onClick = { viewModel.triggerSoilScanSimulation() },
                                colors = ButtonDefaults.buttonColors(containerColor = MittiGreen),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isScanning,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                            ) {
                                if (isScanning) {
                                    CircularProgressIndicator(color = PureWhite, modifier = Modifier.size(24.dp))
                                } else {
                                    Icon(Icons.Default.Analytics, contentDescription = null, tint = PureWhite)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when (currentLanguage) {
                                            AppLanguage.ENGLISH -> "Analyze Soil with Gemini AI"
                                            AppLanguage.HINDI -> "मिट्टी की जाँच करें (Gemini AI)"
                                            AppLanguage.KANNADA -> "ಮಣ್ಣಿನ ತಪಾಸಣೆ ಮಾಡಿ"
                                            AppLanguage.MARATHI -> "माती तपासणी करा"
                                        },
                                        color = PureWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }




                // AI Response / Advice display
                item {
                    AnimatedVisibility(
                        visible = scanResult != null,
                        enter = fadeIn() + expandVertically()
                    ) {
                        scanResult?.let { scan ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = LeafTint),
                                border = BorderStroke(1.dp, MittiGreen)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "✨ Gemini AI Recommendation",
                                            fontWeight = FontWeight.Bold,
                                            color = MittiGreen,
                                            fontSize = 15.sp
                                        )

                                        IconButton(onClick = { viewModel.speakOut(scan.recommendations) }) {
                                            Icon(Icons.Default.VolumeUp, contentDescription = "Listen", tint = MittiGreen)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = scan.recommendations,
                                        fontSize = 13.sp,
                                        color = RaatBlue,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // History of past scans
                item {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.ENGLISH -> "📜 Saved Soil History"
                            AppLanguage.HINDI -> "📜 पुराना मिट्टी रिकॉर्ड"
                            AppLanguage.KANNADA -> "📜 ಉಳಿಸಿದ ಮಣ್ಣಿನ ವರದಿಗಳು"
                            AppLanguage.MARATHI -> "📜 जुने माती रेकॉर्ड"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = RaatBlue,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                if (soilScans.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "No scans yet. Create your first scan!",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                textAlign = TextAlign.Center,
                                color = AshGrey,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    items(soilScans) { scan ->
                        HistoryScanItem(scan, currentLanguage, onDelete = { viewModel.deleteSoilScan(scan.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun NutrientBadge(name: String, level: String, bgColor: Color, textColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = name, fontSize = 10.sp, color = AshGrey, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = level, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}

@Composable
fun HistoryScanItem(scan: SoilScan, lang: AppLanguage, onDelete: () -> Unit) {
    val dateString = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(scan.timestamp))

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
                        text = scan.farmerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = RaatBlue
                    )
                    Text(
                        text = dateString,
                        fontSize = 11.sp,
                        color = AshGrey
                    )
                }

                // pH Badge
                Card(
                    colors = CardDefaults.cardColors(containerColor = getColorForPh(scan.ph)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "pH ${scan.ph}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = getTextColorForPh(scan.ph)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = scan.recommendations,
                fontSize = 12.sp,
                color = RaatBlue,
                lineHeight = 16.sp,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f))
                }
            }
        }
    }
}

fun getColorForPh(ph: Double): Color {
    return when {
        ph < 4.5 -> Color(0xFFD84315) // Deep Orange-Red (Highly Acidic)
        ph < 5.5 -> Color(0xFFFF8F00) // Light Orange
        ph < 6.5 -> Color(0xFFFDD835) // Yellow (Mildly Acidic)
        ph < 7.5 -> Color(0xFF4CAF50) // Bright Green (Neutral)
        ph < 8.5 -> Color(0xFF00ACC1) // Teal (Alkaline)
        else -> Color(0xFF3F51B5)     // Deep Indigo (Highly Alkaline)
    }
}

fun getTextColorForPh(ph: Double): Color {
    return if (ph < 5.5 || ph >= 8.0) PureWhite else RaatBlue
}

fun getPhLabel(ph: Double, lang: AppLanguage): String {
    return when {
        ph < 5.5 -> when (lang) {
            AppLanguage.ENGLISH -> "Highly Acidic (अम्लीय)"
            AppLanguage.HINDI -> "अत्यंत अम्लीय"
            AppLanguage.KANNADA -> "ಹೆಚ್ಚು ಆಮ್ಲೀಯ"
            AppLanguage.MARATHI -> "अत्यंत आम्लयुक्त"
        }
        ph < 6.5 -> when (lang) {
            AppLanguage.ENGLISH -> "Moderately Acidic"
            AppLanguage.HINDI -> "मध्यम अम्लीय"
            AppLanguage.KANNADA -> "ಮಧ್ಯಮ ಆಮ್ಲೀಯ"
            AppLanguage.MARATHI -> "मध्यम आम्लयुक्त"
        }
        ph < 7.5 -> when (lang) {
            AppLanguage.ENGLISH -> "Optimal Neutral (उदासीन)"
            AppLanguage.HINDI -> "उत्तम उदासीन"
            AppLanguage.KANNADA -> "ತಟಸ್ಥ ಮಣ್ಣು"
            AppLanguage.MARATHI -> "उदासीन माती"
        }
        else -> when (lang) {
            AppLanguage.ENGLISH -> "Alkaline (क्षारीय)"
            AppLanguage.HINDI -> "क्षारीय"
            AppLanguage.KANNADA -> "ಪ್ರತ್ಯಾಮ್ಲೀಯ"
            AppLanguage.MARATHI -> "क्षारयुक्त"
        }
    }
}
