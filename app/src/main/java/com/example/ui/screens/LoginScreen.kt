package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.MittiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: MittiViewModel,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("Rajesh Kumar") }
    var phoneNumber by remember { mutableStateOf("9876543210") }
    var passwordPin by remember { mutableStateOf("1234") }
    var selectedLanguage by remember { mutableStateOf(AppLanguage.HINDI) }
    var isPinVisible by remember { mutableStateOf(false) }

    var phoneError by remember { mutableStateOf<String?>(null) }
    var pinError by remember { mutableStateOf<String?>(null) }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(MittiGreen, RaatBlue)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradientBrush)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 450.dp)
                .testTag("login_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Logo & Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(HarvestGold, CircleShape)
                        .border(2.dp, PureWhite, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Agriculture,
                        contentDescription = "App Logo",
                        tint = RaatBlue,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = "Mitti Ki Awaaz",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = HarvestGold
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "कृषि और जलवायु लचीलापन साथी\nClimate Resilience Companion",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // --- LANGUAGE SELECTION ---
                Text(
                    text = "अपनी भाषा चुनें / Select App Language:",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.align(Alignment.Start)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppLanguage.values().forEach { lang ->
                        val isSelected = selectedLanguage == lang
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) HarvestGold else MaterialTheme.colorScheme.surface
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) PureWhite else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedLanguage = lang }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = lang.displayName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) RaatBlue else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = when (lang) {
                                        AppLanguage.ENGLISH -> "English"
                                        AppLanguage.HINDI -> "हिंदी"
                                        AppLanguage.KANNADA -> "ಕನ್ನಡ"
                                        AppLanguage.MARATHI -> "मराठी"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) RaatBlue.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // --- NAME INPUT ---
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("किसान का नाम / Farmer's Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HarvestGold,
                        focusedLabelColor = HarvestGold
                    )
                )

                // --- PHONE NUMBER INPUT ---
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = {
                        if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                            phoneNumber = it
                            phoneError = null
                        }
                    },
                    label = { Text("मोबाइल नंबर / Phone Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    placeholder = { Text("10 Digits") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    isError = phoneError != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("phone_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HarvestGold,
                        focusedLabelColor = HarvestGold
                    )
                )
                if (phoneError != null) {
                    Text(
                        text = phoneError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }

                // --- PIN / PASSWORD INPUT ---
                OutlinedTextField(
                    value = passwordPin,
                    onValueChange = {
                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                            passwordPin = it
                            pinError = null
                        }
                    },
                    label = { Text("4-Digit PIN / पिन") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { isPinVisible = !isPinVisible }) {
                            Icon(
                                imageVector = if (isPinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle PIN Visibility"
                            )
                        }
                    },
                    visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    isError = pinError != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pin_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HarvestGold,
                        focusedLabelColor = HarvestGold
                    )
                )
                if (pinError != null) {
                    Text(
                        text = pinError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- SUBMIT LOGIN ---
                Button(
                    onClick = {
                        var hasError = false
                        if (phoneNumber.length != 10) {
                            phoneError = "Please enter a valid 10-digit mobile number."
                            hasError = true
                        }
                        if (passwordPin.length != 4) {
                            pinError = "Please enter a 4-digit security PIN."
                            hasError = true
                        }

                        if (!hasError) {
                            viewModel.login(phoneNumber, passwordPin, selectedLanguage, name)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("login_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HarvestGold,
                        contentColor = RaatBlue
                    )
                ) {
                    Icon(Icons.Default.Login, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "प्रवेश करें / Login",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Text(
                    text = "Demo PIN: Any 4 digits | Secure & Private",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
