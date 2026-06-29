package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.ChatMessage
import com.example.ui.viewmodel.MittiViewModel
import kotlinx.coroutines.launch

@Composable
fun VoiceHomeScreen(
    viewModel: MittiViewModel,
    onSpeechTrigger: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val chatLog by viewModel.chatLog.collectAsStateWithLifecycle()
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var textInput by remember { mutableStateOf("") }

    // Scroll to the latest chat message automatically
    LaunchedEffect(chatLog.size) {
        if (chatLog.isNotEmpty()) {
            listState.animateScrollToItem(chatLog.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- Multilingual Language Selector Bar ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppLanguage.values().forEach { lang ->
                    val isSelected = currentLanguage == lang
                    Button(
                        onClick = { viewModel.selectLanguage(lang) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MittiGreen else Color.Transparent,
                            contentColor = if (isSelected) PureWhite else RaatBlue
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text(
                            text = lang.displayName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // --- Krishi Sakhi Pulse Avatar Box ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.ENGLISH -> "Krishi Sakhi AI Elder"
                        AppLanguage.HINDI -> "कृषि सखी दीदी"
                        AppLanguage.KANNADA -> "ಕೃಷಿ ಸಖಿ ದೀದಿ"
                        AppLanguage.MARATHI -> "कृषी सखी दीदी"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = MittiGreen,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Avatar Graphic with Pulse Ring Animation
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(100.dp)
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1.0f,
                        targetValue = if (isSpeaking || isListening) 1.25f else 1.05f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "scale"
                    )

                    // Outer pulse rings
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = when {
                                        isListening -> listOf(HarvestGold.copy(alpha = 0.4f), Color.Transparent)
                                        isSpeaking -> listOf(MittiGreen.copy(alpha = 0.4f), Color.Transparent)
                                        else -> listOf(MittiGreen.copy(alpha = 0.15f), Color.Transparent)
                                    }
                                )
                            )
                    )

                    // Inner Avatar Sphere
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(MittiGreen, RaatBlue)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageOfSakhi(currentLanguage),
                            contentDescription = "Avatar",
                            tint = PureWhite,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Speaking Indicator Badge
                    if (isSpeaking) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .background(MittiGreen, CircleShape)
                                .padding(4.dp)
                        ) {
                            Icon(
                                Icons.Default.VolumeUp,
                                contentDescription = "Speaking",
                                tint = PureWhite,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    } else if (isListening) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .background(HarvestGold, CircleShape)
                                .padding(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = "Listening",
                                tint = PureWhite,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when {
                        isListening -> "सुन रही हूँ... बोलिए (Listening... Speak now)"
                        isSpeaking -> "बोल रही हूँ... (Speaking...)"
                        else -> "टैप करें और बोलें (Tap to talk)"
                    },
                    fontSize = 12.sp,
                    color = if (isListening) HarvestGold else AshGrey,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // --- Interactive Chat bubbles list ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(chatLog) { msg ->
                    ChatBubbleItem(msg)
                }
            }
        }

        // --- Quick Voice Shortcut Grid (ILLITERACY HELP) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = when (currentLanguage) {
                    AppLanguage.ENGLISH -> "⚡ Quick Voice Actions"
                    AppLanguage.HINDI -> "⚡ एक-टैप वॉयस शॉर्टकट"
                    AppLanguage.KANNADA -> "⚡ ಒಂದು-ಟ್ಯಾಪ್ ಧ್ವನಿ ಶಾರ್ಟ್‌ಕಟ್"
                    AppLanguage.MARATHI -> "⚡ एक-टॅप व्हॉइस शॉर्टकट"
                },
                fontSize = 12.sp,
                color = MittiGreen,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )

            val shortcuts = getShortcutsForLanguage(currentLanguage)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                shortcuts.take(2).forEach { action ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.submitQuery(action) },
                        colors = CardDefaults.cardColors(containerColor = LeafTint),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = MittiGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = action,
                                fontSize = 11.sp,
                                color = RaatBlue,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                shortcuts.drop(2).take(2).forEach { action ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.submitQuery(action) },
                        colors = CardDefaults.cardColors(containerColor = LeafTint),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = MittiGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = action,
                                fontSize = 11.sp,
                                color = RaatBlue,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // --- Bottom Interaction Bar: Microphone & Typing Input ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Speech Recognition Trigger Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(MittiGreen, Color(0xFF1B5E20))
                        )
                    )
                    .clickable {
                        if (isListening) {
                            viewModel.cancelListening()
                        } else {
                            onSpeechTrigger()
                        }
                    }
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = "Speak",
                    tint = PureWhite,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Keyboard/Type Input Form
            OutlinedTextField(
                value = textInput,
                onValueChange = { 
                    textInput = it
                    viewModel.stopSpeaking()
                },
                placeholder = {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.ENGLISH -> "Type message or ask..."
                            AppLanguage.HINDI -> "सवाल यहाँ लिखें..."
                            AppLanguage.KANNADA -> "ಪ್ರಶ್ನೆಯನ್ನು ಇಲ್ಲಿ ಬರೆಯಿರಿ..."
                            AppLanguage.MARATHI -> "प्रश्न येथे लिहा..."
                        },
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            viewModel.stopSpeaking()
                        }
                    },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = MittiGreen,
                    unfocusedBorderColor = AshGrey.copy(alpha = 0.5f),
                    focusedContainerColor = PureWhite,
                    unfocusedContainerColor = PureWhite
                ),
                trailingIcon = {
                    if (textInput.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                viewModel.submitQuery(textInput)
                                textInput = ""
                            }
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Send",
                                tint = MittiGreen
                            )
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun ChatBubbleItem(msg: ChatMessage) {
    val isUser = msg.sender == "User"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MittiGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.SupportAgent,
                    contentDescription = null,
                    tint = PureWhite,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) MittiGreen else PureWhite
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = msg.text,
                    fontSize = 14.sp,
                    color = if (isUser) PureWhite else RaatBlue,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

private fun imageOfSakhi(lang: AppLanguage) = Icons.Default.Face

private fun getShortcutsForLanguage(lang: AppLanguage): List<String> {
    return when (lang) {
        AppLanguage.ENGLISH -> listOf(
            "Check my soil health",
            "Show weather planning",
            "PM-KISAN eligibility checker",
            "Generate Sarpanch forms"
        )
        AppLanguage.HINDI -> listOf(
            "मेरी मिट्टी जाँचो",
            "सिंचाई की योजना दिखाओ",
            "पीएम-किसान पात्रता",
            "सरपंच सब्सिडी फॉर्म"
        )
        AppLanguage.KANNADA -> listOf(
            "ಮಣ್ಣಿನ ಪರೀಕ್ಷೆ ಮಾಡಿ",
            "ಹವಾಮಾನ ಮತ್ತು ನೀರಾವರಿ ಯೋಜನೆ",
            "ಪಿಎಂ ಕಿಸಾನ್ ಮಾಹಿತಿ",
            "ಅರ್ಜಿ ಪತ್ರಗಳು"
        )
        AppLanguage.MARATHI -> listOf(
            "माती परीक्षण करा",
            "हवामान नियोजन दाखवा",
            "पीएम किसान पात्रता",
            "योजना अर्ज पत्रे"
        )
    }
}
