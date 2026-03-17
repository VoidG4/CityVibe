package com.example.unipicityvibe.ui.screens

import android.os.Build
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.unipicityvibe.R
import com.example.unipicityvibe.model.Booking
import com.example.unipicityvibe.model.Event
import com.example.unipicityvibe.model.fetchEventById
import com.example.unipicityvibe.model.saveBookingToFirestore
import com.example.unipicityvibe.ui.components.ShadcnBadge
import com.example.unipicityvibe.ui.components.ShadcnButton
import com.example.unipicityvibe.ui.components.ShadcnInput
import com.example.unipicityvibe.ui.components.ButtonVariant
import com.example.unipicityvibe.ui.components.ShadcnColors
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventDetailsScreen(
    isDarkTheme: Boolean,
    language: String, // Used solely for TTS locale configuration
    eventId: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    var event by remember { mutableStateOf<Event?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Booking Form State
    var showBookingForm by remember { mutableStateOf(false) }
    var quantity by remember { mutableStateOf(1) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    // TTS State
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    var isSpeaking by remember { mutableStateOf(false) }

    // Initialize TTS
    DisposableEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Configure TTS language based on app language setting
                val locale = when (language) {
                    "Greek" -> Locale("el", "GR")
                    "Spanish" -> Locale("es", "ES")
                    else -> Locale.US
                }
                val result = tts?.setLanguage(locale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(context, "TTS Language not supported!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    LaunchedEffect(eventId) {
        fetchEventById(eventId) { fetchedEvent ->
            event = fetchedEvent
            isLoading = false
        }
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = ShadcnColors.Brand)
        }
        return
    }

    if (event == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.event_not_found), color = Color.Gray)
        }
        return
    }

    val currentEvent = event!!

    // Theme Colors
    val bgColor = if (isDarkTheme) Color(0xFF020617) else Color(0xFFF8FAFC)
    val cardColor = if (isDarkTheme) Color(0xFF0F172A) else Color.White
    val textColor = if (isDarkTheme) Color(0xFFE0E7FF) else ShadcnColors.Primary
    val secondaryText = if (isDarkTheme) Color(0xFF94A3B8) else ShadcnColors.MutedForeground
    val priceColor = if (isDarkTheme) Color(0xFF818CF8) else Color(0xFF4F46E5)

    val fillFieldsMsg = stringResource(R.string.fill_fields)
    val bookingConfirmedMsg = stringResource(R.string.booking_confirmed)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(bgColor)
            .padding(24.dp)
    ) {
        // --- Event Header Card ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            border = BorderStroke(1.dp, if (isDarkTheme) Color(0xFF1E293B) else ShadcnColors.Border),
            elevation = CardDefaults.cardElevation(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .height(260.dp)
                    .fillMaxWidth()
                    .background(
                        if (isDarkTheme) Brush.verticalGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
                        else Brush.linearGradient(listOf(Color(0xFFE0E7FF), Color(0xFFF3E8FF)))
                    )
            ) {
                if (currentEvent.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = currentEvent.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Outlined.ConfirmationNumber,
                        null,
                        tint = if (isDarkTheme) Color(0xFF6366F1) else Color(0xFFA5B4FC),
                        modifier = Modifier.align(Alignment.Center).size(100.dp)
                    )
                }

                Box(modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                    ShadcnBadge(currentEvent.category, variant = currentEvent.category)
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    currentEvent.title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor,
                    lineHeight = 34.sp
                )

                Spacer(Modifier.height(20.dp))

                var dateStr = currentEvent.date
                var timeStr = ""
                try {
                    val dateObj = LocalDateTime.parse(currentEvent.date)
                    dateStr = dateObj.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                    timeStr = dateObj.format(DateTimeFormatter.ofPattern("HH:mm"))
                } catch (_: Exception) { timeStr = "N/A" }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailRow(Icons.Default.CalendarToday, text = dateStr, isPrice = false, isDarkTheme = isDarkTheme)
                    DetailRow(Icons.Default.AccessTime, text = timeStr, isPrice = false, isDarkTheme = isDarkTheme)
                    DetailRow(Icons.Default.LocationOn, text = currentEvent.venue, isPrice = false, isDarkTheme = isDarkTheme)
                    DetailRow(Icons.Default.Euro, text = "${currentEvent.price}", isPrice = true, isDarkTheme = isDarkTheme)
                }

                Divider(color = if (isDarkTheme) Color(0xFF1E293B) else ShadcnColors.Border, modifier = Modifier.padding(vertical = 24.dp))

                // --- About Section with TTS Button ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.about_event), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor)

                    IconButton(
                        onClick = {
                            if (tts?.isSpeaking == true) {
                                tts?.stop()
                                isSpeaking = false
                            } else {
                                val textToRead = "${currentEvent.title}. ${currentEvent.description}"
                                tts?.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null, null)
                                isSpeaking = true
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (tts?.isSpeaking == true) Icons.Default.StopCircle else Icons.Default.VolumeUp,
                            contentDescription = "Read description",
                            tint = if (isDarkTheme) Color(0xFF818CF8) else Color(0xFF4F46E5),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(currentEvent.description, fontSize = 16.sp, color = secondaryText, lineHeight = 24.sp)

                Spacer(Modifier.height(24.dp))

                if (currentEvent.availableTickets > 0) {
                    ShadcnBadge(stringResource(R.string.tickets_left, currentEvent.availableTickets), variant = "outline")
                } else {
                    ShadcnBadge(stringResource(R.string.sold_out), variant = "destructive")
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // --- Booking Card ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(ShadcnColors.BrandGradient),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ConfirmationNumber, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.book_tickets), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                AnimatedVisibility(visible = !showBookingForm) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("€${currentEvent.price}", fontSize = 32.sp, fontWeight = FontWeight.Black, color = priceColor)
                        Text(stringResource(R.string.per_ticket), color = secondaryText)

                        Spacer(Modifier.height(20.dp))

                        ShadcnButton(
                            onClick = { showBookingForm = true },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = currentEvent.availableTickets > 0
                        ) {
                            Text(if (currentEvent.availableTickets > 0) stringResource(R.string.book_tickets) else stringResource(R.string.sold_out))
                        }
                    }
                }

                AnimatedVisibility(visible = showBookingForm) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(stringResource(R.string.number_of_tickets), fontWeight = FontWeight.Medium, color = textColor)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ShadcnButton(onClick = { quantity = (quantity - 1).coerceAtLeast(1) }, variant = ButtonVariant.OUTLINE, size = 44.dp) { Icon(Icons.Default.Remove, null) }
                            ShadcnInput(value = "$quantity", onValueChange = {}, placeholder = "", modifier = Modifier.width(80.dp), readOnly = true)
                            ShadcnButton(onClick = { quantity = (quantity + 1).coerceAtMost(currentEvent.availableTickets) }, variant = ButtonVariant.OUTLINE, size = 44.dp) { Icon(Icons.Default.Add, null) }
                        }

                        Text(stringResource(R.string.full_name), fontWeight = FontWeight.Medium, color = textColor)
                        ShadcnInput(value = name, onValueChange = { name = it }, placeholder = "John Doe", leadingIcon = Icons.Default.Person)

                        Text(stringResource(R.string.email), fontWeight = FontWeight.Medium, color = textColor)
                        ShadcnInput(value = email, onValueChange = { email = it }, placeholder = "john@example.com", leadingIcon = Icons.Default.Email)

                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.total), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor)
                            Text("€${"%.2f".format(currentEvent.price * quantity)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = priceColor)
                        }

                        Spacer(Modifier.height(8.dp))

                        ShadcnButton(onClick = {
                            if (name.isBlank() || email.isBlank()) {
                                Toast.makeText(context, fillFieldsMsg, Toast.LENGTH_SHORT).show()
                            } else {
                                val newBooking = Booking(
                                    eventId = currentEvent.id,
                                    eventTitle = currentEvent.title,
                                    customerName = name,
                                    customerEmail = email,
                                    quantity = quantity,
                                    totalPrice = currentEvent.price * quantity,
                                    status = "confirmed"
                                )
                                saveBookingToFirestore(
                                    booking = newBooking,
                                    onSuccess = {
                                        Toast.makeText(context, bookingConfirmedMsg, Toast.LENGTH_SHORT).show()
                                        showBookingForm = false
                                        onBackClick()
                                    },
                                    onFailure = { errorMsg ->
                                        Toast.makeText(context, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.confirm_booking)) }

                        ShadcnButton(onClick = { showBookingForm = false }, variant = ButtonVariant.OUTLINE, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.cancel)) }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(icon: ImageVector, text: String, isPrice: Boolean = false, isDarkTheme: Boolean) {
    val iconColor = if (isPrice) if (isDarkTheme) Color(0xFF818CF8) else Color(0xFF4F46E5) else if (isDarkTheme) Color(0xFFE0E7FF) else ShadcnColors.MutedForeground
    val textColor = if (isPrice) if (isDarkTheme) Color(0xFF818CF8) else Color(0xFF4F46E5) else if (isDarkTheme) Color(0xFFE0E7FF) else ShadcnColors.Primary

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(text, fontSize = 16.sp, fontWeight = if (isPrice) FontWeight.Bold else FontWeight.Normal, color = textColor)
    }
}