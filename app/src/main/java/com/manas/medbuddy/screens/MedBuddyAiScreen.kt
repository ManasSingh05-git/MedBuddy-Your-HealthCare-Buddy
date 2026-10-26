package com.manas.medbuddy.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manas.medbuddy.ai.GroqAiEngine
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.manas.medbuddy.ui.components.BottomNavigationBar
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.components.MedBuddyMascot
import com.manas.medbuddy.ui.theme.BlueGradient
import com.manas.medbuddy.ui.theme.CyanPrimary
import com.manas.medbuddy.ui.theme.DarkBackground
import com.manas.medbuddy.ui.theme.DarkSurface
import com.manas.medbuddy.ui.theme.EmeraldPrimary
import com.manas.medbuddy.ui.theme.TextPrimaryDark as TextPrimary
import com.manas.medbuddy.ui.theme.TextSecondaryDark as TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedBuddyAiScreen(
    onBack: () -> Unit = {},
    onTabSelected: (String) -> Unit = {}
) {
    var selectedPrompt by remember { mutableStateOf("") }
    var aiAnswerText by remember { mutableStateOf("") }
    var isAiLoading by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun askGroqAi(prompt: String) {
        selectedPrompt = prompt
        isAiLoading = true
        aiAnswerText = "Groq AI is analyzing..."
        coroutineScope.launch {
            val answer = GroqAiEngine.getAiResponse(prompt)
            aiAnswerText = answer
            isAiLoading = false
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "waveAnim")
    val waveScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave"
    )

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("MedBuddy AI", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Online • Always here for you", color = EmeraldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        bottomBar = {
            BottomNavigationBar(
                selectedTab = "assistant",
                onTabSelected = onTabSelected
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Chat Greeting Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Hi Manas! 👋", color = EmeraldPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            "I'm MedBuddy AI.\nHow can I help you today?",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            lineHeight = 22.sp
                        )
                    }

                    MedBuddyMascot(size = 95.dp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Prompt Pills
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PromptPill("🤒 I have a headache", Modifier.weight(1f)) { askGroqAi("I have a headache. What should I do?") }
                    PromptPill("🥗 Meal suggestions", Modifier.weight(1f)) { askGroqAi("Give me healthy meal suggestions.") }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PromptPill("📄 Explain my report", Modifier.weight(1f)) { askGroqAi("Explain standard blood test report terms.") }
                    PromptPill("⏰ Remind my medicine", Modifier.weight(1f)) { askGroqAi("How to set medicine reminders?") }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PromptPill("💊 Side effects", Modifier.weight(1f)) { askGroqAi("Tell me about medicine side effects safety.") }
                    PromptPill("🌙 Sleep tips", Modifier.weight(1f)) { askGroqAi("Give me 3 practical sleep improvement tips.") }
                }
            }

            if (selectedPrompt.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("MedBuddy AI Answer (Groq):", color = EmeraldPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        if (isAiLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(color = CyanPrimary, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Text("Thinking...", color = TextSecondary, fontSize = 13.sp)
                            }
                        } else {
                            Text(
                                text = aiAnswerText,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Voice Waveform & Microphone Button
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Audio Waveform Graphic
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val barCount = 28
                    val barWidth = w / (barCount * 2)

                    for (i in 0 until barCount) {
                        val barHeight = (h * (0.2f + 0.8f * (i % 5 + 1) / 5f)) * (if (isListening) waveScale else 0.6f)
                        val x = i * (barWidth * 2) + barWidth / 2
                        drawLine(
                            brush = Brush.verticalGradient(listOf(CyanPrimary, BlueGradient)),
                            start = Offset(x, h / 2 - barHeight / 2),
                            end = Offset(x, h / 2 + barHeight / 2),
                            strokeWidth = barWidth
                        )
                    }
                }

                // Microphone Button
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    if (isListening) EmeraldPrimary else CyanPrimary,
                                    BlueGradient
                                )
                            )
                        )
                        .clickable { isListening = !isListening },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = if (isListening) "Listening..." else "Tap to speak",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun PromptPill(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = DarkSurface,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = text,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
