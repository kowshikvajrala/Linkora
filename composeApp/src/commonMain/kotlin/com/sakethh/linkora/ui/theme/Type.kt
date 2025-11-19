package com.sakethh.linkora.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.sakethh.linkora.preferences.AppPreferences
import com.sakethh.linkora.ui.domain.Font
import linkora.composeapp.generated.resources.Res
import linkora.composeapp.generated.resources.googleSansFlex
import linkora.composeapp.generated.resources.medium
import linkora.composeapp.generated.resources.playwrite
import linkora.composeapp.generated.resources.regular
import linkora.composeapp.generated.resources.semibold
import org.jetbrains.compose.resources.Font

private val googleSansFlexFontFamily
    @Composable get() = FontFamily(
        Font(Res.font.googleSansFlex, weight = FontWeight.Normal),
        Font(Res.font.googleSansFlex, weight = FontWeight.Medium),
        Font(Res.font.googleSansFlex, weight = FontWeight.SemiBold),
    )

private val playWriteFontFamily
    @Composable get() = FontFamily(Font(resource = Res.font.playwrite, weight = FontWeight.Normal))

private val poppinsFontFamily
    @Composable get() = FontFamily(
        Font(resource = Res.font.regular, weight = FontWeight.Normal),
        Font(resource = Res.font.medium, weight = FontWeight.Medium),
        Font(resource = Res.font.semibold, weight = FontWeight.SemiBold)
    )

private val currentFontFamily
    @Composable get() = if (AppPreferences.selectedFont == Font.GOOGLE_SANS_FLEX) googleSansFlexFontFamily else poppinsFontFamily

val LinkoraTypography
    @Composable get() = Typography(
        titleLarge = TextStyle(fontFamily = currentFontFamily, fontWeight = FontWeight.SemiBold),
        titleMedium = TextStyle(fontFamily = currentFontFamily, fontWeight = FontWeight.Medium),
        titleSmall = TextStyle(fontFamily = currentFontFamily, fontWeight = FontWeight.Normal),
        bodyMedium = TextStyle(fontFamily = currentFontFamily, fontWeight = FontWeight.Normal),
        labelSmall = TextStyle(fontFamily = playWriteFontFamily, fontWeight = FontWeight.Normal)
    )