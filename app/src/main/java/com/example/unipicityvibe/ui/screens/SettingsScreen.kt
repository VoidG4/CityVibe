package com.example.unipicityvibe.ui.screens

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unipicityvibe.R
import com.example.unipicityvibe.ui.components.ShadcnButton
import com.example.unipicityvibe.ui.components.ShadcnColors
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    language: String,  // Keeps current language selection for dropdown
    onThemeChanged: (Boolean) -> Unit,
    onLanguageChanged: (String) -> Unit
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("UnipiSettings", Context.MODE_PRIVATE)

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val userEmail = currentUser?.email ?: "No email"

    var theme by remember { mutableStateOf(if (isDarkTheme) "Dark" else "Light") }
    var languageState by remember { mutableStateOf(language) }
    var isSaving by remember { mutableStateOf(false) }

    val bgColor = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF8FAFC)
    val cardColor = if (isDarkTheme) Color(0xFF334155) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.Black

    val settingsSavedMsg = stringResource(R.string.settings_saved)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .verticalScroll(rememberScrollState())
    ) {
        // --- HEADER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ShadcnColors.BrandGradient)
                .padding(vertical = 40.dp, horizontal = 24.dp)
        ) {
            Column {
                Text(stringResource(R.string.settings_title), fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color.White)
                Text(stringResource(R.string.settings_desc), fontSize = 16.sp, color = Color(0xFFE0E7FF))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            // PROFILE CARD
            SettingsCard(
                title = stringResource(R.string.user_profile),
                icon = Icons.Default.Person,
                headerColor = if (isDarkTheme) Color(0xFF6366F1) else Color(0xFF6366F1),
                cardBackground = cardColor,
                textColor = textColor,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(stringResource(R.string.email), fontWeight = FontWeight.Medium, fontSize = 14.sp, color = textColor)
                // Use a ShadcnInput but read-only for display
                // Note: Make sure ShadcnInput is accessible here (import or same package)
                OutlinedTextField(
                    value = userEmail,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Lock, null) }
                )
            }

            // THEME CARD
            SettingsCard(
                title = stringResource(R.string.appearance),
                icon = Icons.Default.Palette,
                headerColor = if (isDarkTheme) Color(0xFFD946EF) else Color(0xFFD946EF),
                cardBackground = cardColor,
                textColor = textColor,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(stringResource(R.string.theme), fontWeight = FontWeight.Medium, fontSize = 14.sp, color = textColor)
                SettingsDropdown(options = listOf("Light", "Dark"), selectedOption = theme, onOptionSelected = { theme = it }, isDarkTheme = isDarkTheme)
            }

            // LANGUAGE CARD
            SettingsCard(
                title = stringResource(R.string.language),
                icon = Icons.Default.Language,
                headerColor = if (isDarkTheme) Color(0xFFEC4899) else Color(0xFFEC4899),
                cardBackground = cardColor,
                textColor = textColor,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(stringResource(R.string.display_language), fontWeight = FontWeight.Medium, fontSize = 14.sp, color = textColor)
                SettingsDropdown(options = listOf("English", "Greek", "Spanish"), selectedOption = languageState, onOptionSelected = { languageState = it }, isDarkTheme = isDarkTheme)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SAVE BUTTON
            ShadcnButton(
                onClick = {
                    isSaving = true
                    val darkModeEnabled = (theme == "Dark")
                    onThemeChanged(darkModeEnabled)
                    prefs.edit().apply { putString("theme", theme); putString("language", languageState); apply() }
                    onLanguageChanged(languageState)
                    Handler(Looper.getMainLooper()).postDelayed({
                        isSaving = false
                        Toast.makeText(context, settingsSavedMsg, Toast.LENGTH_SHORT).show()
                    }, 500)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.saving))
                } else {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.save_settings))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


// --- Helper Components ---

@Composable
fun SettingsCard(
    title: String,
    icon: ImageVector,
    headerColor: Color,
    cardBackground: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(Brush.horizontalGradient(listOf(headerColor, headerColor.copy(alpha = 0.8f))))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
        Column(modifier = Modifier.padding(20.dp)) { content() }
    }
}

@Composable
fun SettingsDropdown(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    isDarkTheme: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val bgColor = if (isDarkTheme) Color(0xFF334155) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.Black

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedCard(
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, ShadcnColors.Border),
            colors = CardDefaults.cardColors(containerColor = bgColor),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clickable { expanded = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(selectedOption, fontSize = 14.sp, color = textColor, fontWeight = FontWeight.Medium)
                Icon(if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown, null, tint = textColor)
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(bgColor)
                .fillMaxWidth(0.85f)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = textColor) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}