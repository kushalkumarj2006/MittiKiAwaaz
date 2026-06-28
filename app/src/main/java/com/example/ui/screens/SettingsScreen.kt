package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.MittiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MittiViewModel,
    modifier: Modifier = Modifier
) {
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val loggedInPhone by viewModel.loggedInPhone.collectAsStateWithLifecycle()
    val loggedInName by viewModel.loggedInName.collectAsStateWithLifecycle()
    val selectedLocation by viewModel.selectedLocation.collectAsStateWithLifecycle()

    var inputLocation by remember { mutableStateOf(selectedLocation) }
    var showLocationEdit by remember { mutableStateOf(false) }
    var pinUpdateText by remember { mutableStateOf("****") }
    var showPinUpdateDialog by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val langLabel = when (currentLanguage) {
        AppLanguage.ENGLISH -> "App Language"
        AppLanguage.HINDI -> "ऐप की भाषा"
        AppLanguage.KANNADA -> "ಅಪ್ಲಿಕೇಶನ್ ಭಾಷೆ"
        AppLanguage.MARATHI -> "अॅपची भाषा"
    }

    val locationLabel = when (currentLanguage) {
        AppLanguage.ENGLISH -> "Farmer Location"
        AppLanguage.HINDI -> "किसान का स्थान"
        AppLanguage.KANNADA -> "ರೈತರ ಸ್ಥಳ"
        AppLanguage.MARATHI -> "शेतकऱ्याचे स्थान"
    }

    val secLabel = when (currentLanguage) {
        AppLanguage.ENGLISH -> "Security PIN"
        AppLanguage.HINDI -> "सुरक्षा पिन"
        AppLanguage.KANNADA -> "ಭದ್ರತಾ ಪಿನ್"
        AppLanguage.MARATHI -> "सुरक्षा पिन"
    }

    val feedbackLabel = when (currentLanguage) {
        AppLanguage.ENGLISH -> "System Info & Reset"
        AppLanguage.HINDI -> "सिस्टम जानकारी और रीसेट"
        AppLanguage.KANNADA -> "ಸಿಸ್ಟಮ್ ಮಾಹಿತಿ ಮತ್ತು ಮರುಹೊಂದಿಸಿ"
        AppLanguage.MARATHI -> "सिस्टम माहिती आणि रीसेट"
    }

    val logoutLabel = when (currentLanguage) {
        AppLanguage.ENGLISH -> "Sign Out"
        AppLanguage.HINDI -> "लॉग आउट करें"
        AppLanguage.KANNADA -> "ಸೈನ್ ಔಟ್"
        AppLanguage.MARATHI -> "लॉग आउट"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- PROFILE HEADER CARD ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(HarvestGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = loggedInName.take(1).uppercase(),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = RaatBlue
                            )
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = loggedInName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                        Text(
                            text = "📞 $loggedInPhone",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        )
                    }
                }
            }
        }

        // --- LANGUAGE SWITCHER SECTION ---
        item {
            Text(
                text = "🌐 $langLabel",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    AppLanguage.values().forEach { lang ->
                        val isSelected = currentLanguage == lang
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
                                .clickable {
                                    viewModel.selectLanguage(lang)
                                    snackbarMessage = when (lang) {
                                        AppLanguage.ENGLISH -> "Language changed to English!"
                                        AppLanguage.HINDI -> "भाषा बदलकर हिंदी की गई!"
                                        AppLanguage.KANNADA -> "ಭಾಷೆಯನ್ನು ಕನ್ನಡಕ್ಕೆ ಬದಲಾಯಿಸಲಾಗಿದೆ!"
                                        AppLanguage.MARATHI -> "भाषा मराठीत बदलली!"
                                    }
                                }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = lang.displayName,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = when (lang) {
                                        AppLanguage.ENGLISH -> "Companion speaks in English"
                                        AppLanguage.HINDI -> "सखी हिंदी में बात करेगी"
                                        AppLanguage.KANNADA -> "ಸಖಿ ಕನ್ನಡದಲ್ಲಿ ಮಾತನಾಡುತ್ತಾರೆ"
                                        AppLanguage.MARATHI -> "सखी मराठीत बोलेल"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    viewModel.selectLanguage(lang)
                                    snackbarMessage = "Language changed successfully!"
                                }
                            )
                        }
                    }
                }
            }
        }

        // --- LOCATION SETTING (INTEGRATED WITH AI FORECASTS) ---
        item {
            Text(
                text = "📍 $locationLabel",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Current Region / क्षेत्र:",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Text(
                                text = selectedLocation,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Button(
                            onClick = { showLocationEdit = !showLocationEdit },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HarvestGold,
                                contentColor = RaatBlue
                            )
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Change")
                        }
                    }

                    AnimatedVisibility(visible = showLocationEdit) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = inputLocation,
                                onValueChange = { inputLocation = it },
                                label = { Text("Village / District / State") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = {
                                    if (inputLocation.isNotBlank()) {
                                        viewModel.updateLocation(inputLocation)
                                        showLocationEdit = false
                                        snackbarMessage = "Region updated! Re-fetching climate weather advisory..."
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Update & Fetch AI Climate Outlook")
                            }
                        }
                    }
                }
            }
        }

        // --- SECURITY PIN CARD ---
        item {
            Text(
                text = "🛡️ $secLabel",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPinUpdateDialog = true }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Login Security PIN",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Currently set to four-digit security lock",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }
        }

        // --- SYSTEM INFO AND STATISTICS ---
        item {
            Text(
                text = "⚙️ $feedbackLabel",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Mitti App Version", style = MaterialTheme.typography.bodyMedium)
                        Text("v1.4.2-Resilient", fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Gemini AI Core Status", style = MaterialTheme.typography.bodyMedium)
                        Text("ONLINE (Ready)", color = Color.Green, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TTS Voice Engine", style = MaterialTheme.typography.bodyMedium)
                        Text("Ready (Offline)", color = HarvestGold, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- LOGOUT BUTTON ---
        item {
            Button(
                onClick = { viewModel.logout() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("logout_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(logoutLabel, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    }

    // PIN Dialog
    if (showPinUpdateDialog) {
        var pinInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPinUpdateDialog = false },
            title = { Text("Update Security PIN") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter new 4-digit security code for login:")
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinInput = it },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput.length == 4) {
                            showPinUpdateDialog = false
                            snackbarMessage = "Security PIN updated successfully!"
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinUpdateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Snackbar notification overlay
    if (snackbarMessage != null) {
        LaunchedEffect(snackbarMessage) {
            kotlinx.coroutines.delay(3000)
            snackbarMessage = null
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = RaatBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = snackbarMessage!!,
                    color = Color.White,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
