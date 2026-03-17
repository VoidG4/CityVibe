package com.example.unipicityvibe.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Color Definitions
object ShadcnColors {
    // 👇 ΕΔΩ ΠΡΟΣΤΕΘΗΚΕ ΤΟ BRAND COLOR ΠΟΥ ΕΛΕΙΠΕ
    val Brand = Color(0xFF4F46E5)

    val Primary = Color(0xFF020617) // Black
    val PrimaryForeground = Color(0xFFFFFFFF)
    val Secondary = Color(0xFFF1F5F9)
    val SecondaryForeground = Color(0xFF0F172A)
    val Destructive = Color(0xFFEF4444)
    val DestructiveForeground = Color(0xFFFFFFFF)
    val Muted = Color(0xFFF1F5F9)
    val MutedForeground = Color(0xFF475569) // Dark Gray
    val Background = Color(0xFFFFFFFF)
    val Border = Color(0xFFE2E8F0)
    val Input = Color(0xFFE2E8F0)
    val BrandGradient = Brush.horizontalGradient(colors = listOf(Color(0xFF4F46E5), Color(0xFF9333EA)))
}

enum class ButtonVariant { DEFAULT, DESTRUCTIVE, OUTLINE, SECONDARY, GHOST }

/**
 * Custom Button Component styled after Shadcn UI.
 */
@Composable
fun ShadcnButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.DEFAULT,
    size: Dp = 52.dp,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val containerColor = when (variant) {
        ButtonVariant.DEFAULT -> ShadcnColors.Brand // Χρήση του Brand εδώ
        ButtonVariant.DESTRUCTIVE -> ShadcnColors.Destructive
        ButtonVariant.SECONDARY -> ShadcnColors.Secondary
        else -> Color.Transparent
    }
    val contentColor = when (variant) {
        ButtonVariant.DEFAULT -> ShadcnColors.PrimaryForeground
        ButtonVariant.DESTRUCTIVE -> ShadcnColors.DestructiveForeground
        ButtonVariant.SECONDARY -> ShadcnColors.SecondaryForeground
        ButtonVariant.OUTLINE -> Color.Black
        else -> ShadcnColors.Primary
    }
    val border = if (variant == ButtonVariant.OUTLINE) BorderStroke(1.5.dp, ShadcnColors.Border) else null

    Button(
        onClick = onClick,
        modifier = modifier.height(size),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = ShadcnColors.Muted,
            disabledContentColor = ShadcnColors.MutedForeground
        ),
        border = border,
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        ProvideTextStyle(value = LocalTextStyle.current.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold)) {
            content()
        }
    }
}

/**
 * Custom Badge/Chip component for categories and status.
 */
@Composable
fun ShadcnBadge(text: String, variant: String = "default", modifier: Modifier = Modifier) {
    val (bgColor, textColor, border) = when (variant) {
        "outline" -> Triple(Color.White, Color.Black, BorderStroke(1.5.dp, ShadcnColors.Border))
        "destructive" -> Triple(Color(0xFFFEF2F2), Color(0xFFDC2626), null)
        "theater" -> Triple(Color(0xFFF3E8FF), Color(0xFF7E22CE), BorderStroke(1.dp, Color(0xFFE9D5FF)))
        "concert" -> Triple(Color(0xFFFCE7F3), Color(0xFFBE185D), BorderStroke(1.dp, Color(0xFFFBCFE8)))
        "cinema" -> Triple(Color(0xFFDBEAFE), Color(0xFF1D4ED8), BorderStroke(1.dp, Color(0xFFBFDBFE)))
        "festival" -> Triple(Color(0xFFFFEDD5), Color(0xFFC2410C), BorderStroke(1.dp, Color(0xFFFED7AA)))
        "exhibition" -> Triple(Color(0xFFDCFCE7), Color(0xFF15803D), BorderStroke(1.dp, Color(0xFFBBF7D0)))
        "sports" -> Triple(Color(0xFFFEE2E2), Color(0xFFB91C1C), BorderStroke(1.dp, Color(0xFFFECACA)))
        else -> Triple(ShadcnColors.Primary, ShadcnColors.PrimaryForeground, null)
    }

    Surface(
        color = bgColor,
        contentColor = textColor,
        shape = RoundedCornerShape(100.dp),
        border = border,
        modifier = modifier
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

/**
 * Custom Input TextField component.
 */
@Composable
fun ShadcnInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector? = null,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    isDarkTheme: Boolean = false // Added logic to support Dark Theme inside inputs if needed
) {
    val containerColor = if(isDarkTheme) Color(0xFF334155) else Color.White
    val textColor = if(isDarkTheme) Color.White else Color.Black

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.height(60.dp),
        placeholder = { Text(placeholder, fontSize = 16.sp, color = Color.Gray) },
        leadingIcon = if (leadingIcon != null) {
            { Icon(leadingIcon, null, tint = textColor, modifier = Modifier.size(22.dp)) }
        } else null,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = ShadcnColors.Border,
            focusedBorderColor = ShadcnColors.Brand, // Χρήση του Brand και εδώ
            unfocusedContainerColor = containerColor,
            focusedContainerColor = containerColor,
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            cursorColor = ShadcnColors.Brand
        ),
        readOnly = readOnly,
        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, color = textColor),
        singleLine = true
    )
}

/**
 * Skeleton loading placeholder with pulse animation.
 */
@Composable
fun ShadcnSkeleton(modifier: Modifier = Modifier, shape: Shape = RoundedCornerShape(8.dp)) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    Box(modifier = modifier.background(Color.Gray.copy(alpha = alpha), shape).clip(shape))
}