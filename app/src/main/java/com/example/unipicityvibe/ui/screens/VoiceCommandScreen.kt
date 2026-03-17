package com.example.unipicityvibe.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.util.Locale

@Composable
fun VoiceCommandScreen(
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current

    // States
    var stateText by remember { mutableStateOf("Initializing...") }
    var isListening by remember { mutableStateOf(false) }
    var userCommand by remember { mutableStateOf("") }

    // State to hold route until speech finishes
    var targetRoute by remember { mutableStateOf<String?>(null) }

    // Animation States
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.5f else 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    // --- LOGIC ---
    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    var tts: TextToSpeech? by remember { mutableStateOf(null) }

    fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US)
        }

        Handler(Looper.getMainLooper()).post {
            try {
                speechRecognizer.startListening(intent)
                isListening = true
                stateText = "Listening..."
            } catch (e: Exception) {
                Log.e("Voice", "Error starting listener: ${e.message}")
            }
        }
    }

    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US

                // Use listener to wait until TTS finishes before listening or navigating
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        isListening = false
                        if (utteranceId == "NAV_ID") {
                            stateText = "Processing..."
                        } else {
                            stateText = "Speaking..."
                        }
                    }

                    override fun onDone(utteranceId: String?) {
                        // 1. Welcome finished -> start listening
                        if (utteranceId == "WelcomeID") {
                            startListening()
                        }
                        // 2. Navigation prompt finished -> Navigate
                        else if (utteranceId == "NAV_ID") {
                            // Ensure navigation happens on Main Thread
                            Handler(Looper.getMainLooper()).post {
                                targetRoute?.let { route ->
                                    onNavigate(route)
                                }
                            }
                        }
                        // 3. Retry prompt finished -> start listening again
                        else if (utteranceId == "RetryID") {
                            startListening()
                        }
                    }

                    override fun onError(utteranceId: String?) {}
                })

                val welcomeMessage = "Where would you like to navigate? " +
                        "Say 'Home', 'Map', 'Bookings', or 'Settings'."

                stateText = "Welcome!"
                tts?.speak(welcomeMessage, TextToSpeech.QUEUE_FLUSH, null, "WelcomeID")
            }
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { isListening = false }
            override fun onError(error: Int) {
                stateText = "Tap to retry"
                isListening = false
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val command = matches[0].lowercase()
                    userCommand = command

                    // --- NAVIGATION LOGIC ---
                    when {
                        command.contains("home") -> {
                            targetRoute = "home"
                            tts?.speak("Navigating to Home", TextToSpeech.QUEUE_FLUSH, null, "NAV_ID")
                        }
                        command.contains("map") -> {
                            targetRoute = "map"
                            tts?.speak("Opening Map", TextToSpeech.QUEUE_FLUSH, null, "NAV_ID")
                        }
                        command.contains("booking") || command.contains("ticket") -> {
                            targetRoute = "bookings"
                            tts?.speak("Showing your Bookings", TextToSpeech.QUEUE_FLUSH, null, "NAV_ID")
                        }
                        command.contains("setting") -> {
                            targetRoute = "settings"
                            tts?.speak("Opening Settings", TextToSpeech.QUEUE_FLUSH, null, "NAV_ID")
                        }
                        else -> {
                            stateText = "Unknown command."
                            tts?.speak("I didn't understand. Please say Home, Map, Bookings, or Settings.", TextToSpeech.QUEUE_ADD, null, "RetryID")
                        }
                    }
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    DisposableEffect(Unit) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
            speechRecognizer.destroy()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(context, "Microphone permission needed!", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // --- UI ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1E1B4B), Color(0xFF4C1D95)))),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .scale(scale)
                .background(Color(0x33818CF8), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(scale * 0.8f)
                .background(Color(0x66A78BFA), CircleShape)
        )

        Button(
            onClick = { startListening() },
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC4B5FD))
        ) {
            Icon(Icons.Default.Mic, null, tint = Color(0xFF4C1D95), modifier = Modifier.size(40.dp))
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stateText,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            if (userCommand.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "You said: \"$userCommand\"",
                    color = Color(0xFFE9D5FF),
                    fontSize = 18.sp
                )
            }
        }
    }
}