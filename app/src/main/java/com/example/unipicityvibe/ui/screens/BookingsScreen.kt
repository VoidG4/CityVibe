package com.example.unipicityvibe.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Euro
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.unipicityvibe.R
import com.example.unipicityvibe.model.Booking
import com.example.unipicityvibe.model.fetchUserBookings
import com.example.unipicityvibe.ui.components.ShadcnBadge
import com.example.unipicityvibe.ui.components.ShadcnColors
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BookingsScreen(isDarkTheme: Boolean, language: String) {
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        fetchUserBookings { fetched ->
            bookings = fetched
            isLoading = false
        }
    }

    val totalSpent = bookings.sumOf { it.totalPrice }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // --- HEADER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ShadcnColors.BrandGradient)
                .padding(vertical = 40.dp, horizontal = 24.dp)
        ) {
            Column {
                Text(
                    stringResource(R.string.my_bookings),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    stringResource(R.string.bookings_subtitle),
                    fontSize = 16.sp,
                    color = Color(0xFFE0E7FF)
                )
            }
        }

        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatsCard(
                    title = stringResource(R.string.stat_bookings),
                    value = "${bookings.size}",
                    icon = Icons.Outlined.ConfirmationNumber,
                    colorStart = Color(0xFF6366F1),
                    colorEnd = Color(0xFF4F46E5),
                    modifier = Modifier.weight(1f),
                    isDarkTheme = isDarkTheme
                )
                StatsCard(
                    title = stringResource(R.string.stat_spent),
                    value = "€${totalSpent.toInt()}",
                    icon = Icons.Outlined.Euro,
                    colorStart = Color(0xFFEC4899),
                    colorEnd = Color(0xFFDB2777),
                    modifier = Modifier.weight(1f),
                    isDarkTheme = isDarkTheme
                )
            }

            Spacer(Modifier.height(24.dp))

            if (isLoading) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ShadcnColors.Brand)
                }
            } else if (bookings.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ConfirmationNumber, null,
                            tint = if (isDarkTheme) Color(0xFFCBD5E1) else ShadcnColors.MutedForeground,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(stringResource(R.string.no_bookings_yet), color = if (isDarkTheme) Color(0xFFCBD5E1) else ShadcnColors.MutedForeground, fontSize = 18.sp)
                    }
                }
            } else {
                bookings.forEach { booking ->
                    BookingCard(
                        booking = booking,
                        isDarkTheme = isDarkTheme,
                        language = language
                    )
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun StatsCard(
    title: String,
    value: String,
    icon: ImageVector,
    colorStart: Color,
    colorEnd: Color,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color.Transparent),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(colorStart, colorEnd)))
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(title, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Icon(icon, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.height(8.dp))
                Text(value, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BookingCard(
    booking: Booking,
    isDarkTheme: Boolean,
    language: String
) {
    val cardBg = if (isDarkTheme) Color(0xFF0F172A) else Color.White
    val borderColor = if (isDarkTheme) Color(0xFF1E293B) else ShadcnColors.Border
    val textColor = if (isDarkTheme) Color(0xFFE0E7FF) else ShadcnColors.Primary
    val mutedTextColor = if (isDarkTheme) Color(0xFF94A3B8) else ShadcnColors.MutedForeground

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .background(if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF8FAFC))
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(booking.eventTitle, fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 1, color = textColor)

                // Translate Status
                val statusText = if (booking.status == "confirmed") stringResource(R.string.confirmed) else stringResource(R.string.cancelled)

                ShadcnBadge(
                    text = statusText,
                    variant = if (booking.status == "confirmed") "outline" else "destructive"
                )
            }

            Divider(color = borderColor)

            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.booking_details), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textColor)
                Spacer(Modifier.height(8.dp))
                BookingDetailRow(Icons.Default.Person, booking.customerName, mutedTextColor)
                BookingDetailRow(Icons.Default.ConfirmationNumber, "${booking.quantity} ticket(s)", mutedTextColor)

                // Localization logic for date parsing
                val locale = when(language) { "Greek" -> Locale("el", "GR"); "Spanish" -> Locale("es", "ES"); else -> Locale.US }

                // 1. Calculate formatted date string (Logic only)
                val formattedDateStr = remember(booking.bookingDate, locale) {
                    try {
                        if (booking.bookingDate.isNotEmpty()) {
                            val bookingDate = LocalDateTime.parse(booking.bookingDate)
                            bookingDate.format(DateTimeFormatter.ofPattern("MMM dd").withLocale(locale))
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        null
                    }
                }

                // 2. Display in UI (Composable calls)
                val dateText = if (formattedDateStr != null) {
                    stringResource(R.string.booked_on, formattedDateStr)
                } else {
                    stringResource(R.string.booked_recently)
                }

                BookingDetailRow(Icons.Default.DateRange, dateText, mutedTextColor)

                Divider(color = borderColor, modifier = Modifier.padding(vertical = 16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.total_amount), color = mutedTextColor, fontSize = 14.sp)
                    Text("€${"%.2f".format(booking.totalPrice)}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF4F46E5))
                }
            }
        }
    }
}

@Composable
fun BookingDetailRow(icon: ImageVector, text: String, textColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Icon(icon, null, tint = textColor, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 14.sp, color = textColor)
    }
}