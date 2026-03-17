package com.example.unipicityvibe.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import com.example.unipicityvibe.model.Event
import com.example.unipicityvibe.model.fetchEventsFromFirestore
import com.example.unipicityvibe.ui.components.ShadcnBadge
import com.example.unipicityvibe.ui.components.ShadcnInput
import com.example.unipicityvibe.ui.components.ShadcnColors
import com.example.unipicityvibe.R
import androidx.compose.foundation.border

@Composable
fun HomeScreen(
    isDarkTheme: Boolean,
    onEventClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("all") }

    var events by remember { mutableStateOf<List<Event>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        fetchEventsFromFirestore { fetchedEvents ->
            events = fetchedEvents
            isLoading = false
        }
    }

    val categories = listOf("all", "theater", "concert", "cinema", "festival", "exhibition", "sports")

    val filteredEvents = events.filter { event ->
        (selectedCategory == "all" || event.category.equals(selectedCategory, ignoreCase = true)) &&
                (searchQuery.isEmpty() || event.title.contains(searchQuery, ignoreCase = true))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(if (isDarkTheme) Color(0xFF020617) else Color(0xFFF8FAFC))
    ) {

        // --- HERO SECTION ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ShadcnColors.BrandGradient)
                .padding(vertical = 56.dp, horizontal = 28.dp)
        ) {
            Column {
                Text(
                    stringResource(R.string.app_name),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = (-1).sp
                )
                Text(
                    stringResource(R.string.discover_events),
                    fontSize = 18.sp,
                    color = Color(0xFFE0E7FF),
                    modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ShadcnInput(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = stringResource(R.string.search_placeholder),
                        leadingIcon = Icons.Default.Search,
                        modifier = Modifier.fillMaxWidth()
                    )

                    CategoryDropdown(
                        isDarkTheme = isDarkTheme,
                        categories = categories,
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it }
                    )
                }
            }
        }

        // --- CONTENT GRID ---
        Column(modifier = Modifier.padding(28.dp)) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ShadcnColors.Brand)
                }
            } else {
                if (filteredEvents.isEmpty()) {
                    Text(
                        stringResource(R.string.no_events_found),
                        color = if (isDarkTheme) Color(0xFF94A3B8) else ShadcnColors.MutedForeground,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                } else {
                    filteredEvents.forEach { event ->
                        EventCard(
                            event = event,
                            isDarkTheme = isDarkTheme,
                            onClick = { onEventClick(event.id) }
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

// ---------------- Event Card ----------------
@Composable
fun EventCard(
    event: Event,
    isDarkTheme: Boolean,
    onClick: () -> Unit
) {
    val cardColor = if (isDarkTheme) Color(0xFF0F172A) else Color.White
    val borderColor = if (isDarkTheme) Color(0xFF1E293B) else ShadcnColors.Border
    val titleColor = if (isDarkTheme) Color.White else ShadcnColors.Primary
    val textColor = if (isDarkTheme) Color(0xFFCBD5E1) else ShadcnColors.MutedForeground

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {

        Box(
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth()
        ) {
            if (event.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = event.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    Icons.Outlined.ConfirmationNumber,
                    null,
                    tint = if (isDarkTheme) Color(0xFF6366F1) else Color(0xFFA5B4FC),
                    modifier = Modifier.align(Alignment.Center).size(80.dp)
                )
            }

            Box(modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                ShadcnBadge(event.category, variant = event.category)
            }
        }

        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                event.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = titleColor,
                lineHeight = 30.sp
            )

            Text(
                event.description,
                fontSize = 16.sp,
                color = textColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "€${event.price}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isDarkTheme) Color(0xFF818CF8) else Color(0xFF4F46E5)
                )

                if (event.availableTickets == 0) {
                    ShadcnBadge(stringResource(R.string.sold_out), variant = "destructive")
                } else {
                    ShadcnBadge(
                        stringResource(R.string.tickets_left, event.availableTickets),
                        variant = "outline"
                    )
                }
            }
        }
    }
}

// ---------------- Category Dropdown ----------------
@Composable
fun CategoryDropdown(
    isDarkTheme: Boolean,
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val bg = if (isDarkTheme) Color(0xFF020617) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val borderColor = if (isDarkTheme) Color(0xFF1E293B) else ShadcnColors.Border

    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = bg,
            border = BorderStroke(1.dp, borderColor),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clickable { expanded = !expanded }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Display selected category
                val displayText = if (selectedCategory == "all") {
                    stringResource(R.string.all_categories)
                } else {
                    stringResource(getCategoryStringId(selectedCategory))
                }

                Text(
                    text = displayText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(bg)
                .border(1.dp, borderColor, RoundedCornerShape(4.dp))
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = {
                        val catText = if (category == "all") stringResource(R.string.all_categories)
                        else stringResource(getCategoryStringId(category))

                        Text(
                            text = catText,
                            fontSize = 14.sp,
                            fontWeight = if (category == selectedCategory) FontWeight.Bold else FontWeight.Normal,
                            color = if (category == selectedCategory) Color(0xFF4F46E5) else textColor
                        )
                    },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                )
            }
        }
    }
}

/**
 * Maps category strings to resource IDs for translation.
 */
fun getCategoryStringId(category: String): Int {
    return when(category.lowercase()) {
        "theater" -> R.string.cat_theater
        "concert" -> R.string.cat_concert
        "festival" -> R.string.cat_festival
        "exhibition" -> R.string.cat_exhibition
        "cinema" -> R.string.cat_cinema
        "sports" -> R.string.cat_sports
        else -> R.string.all_categories // Default fallback
    }
}