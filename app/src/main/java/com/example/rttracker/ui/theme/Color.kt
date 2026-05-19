package com.example.rttracker.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.rttracker.R

val ObsidianBackground = Color(0xFF171717)
val ObsidianSurface = Color(0xFF202020)
val ObsidianPurple = Color(0xFFCCCCFF) // Light Periwinkle
val ObsidianText = Color(0xFFE5E5E5)
val ObsidianTextDim = Color(0xFFA3A3A3)
val ObsidianAccent = Color(0xFF8E8EFF) // Vibrant Periwinkle Accent
val ObsidianTertiary = Color(0xFF4D4D99) // Deep Slate Indigo

// Properly load the converted OpenDyslexic TrueType font files from resources
val OpenDyslexic = FontFamily(
    Font(resId = R.font.opendyslexic_regular, weight = FontWeight.Normal),
    Font(resId = R.font.opendyslexic_bold, weight = FontWeight.Bold)
)
