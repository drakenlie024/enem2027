@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.franferreira.enem2027.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.franferreira.enem2027.R

private fun esquema(p: Paleta) = if (p.escuro) {
    darkColorScheme(
        primary = p.acao,
        onPrimary = p.fundo,
        primaryContainer = p.cartaoAlto,
        onPrimaryContainer = p.texto,
        secondary = p.acao2,
        onSecondary = p.fundo,
        secondaryContainer = p.cartaoAlto,
        onSecondaryContainer = p.texto,
        background = p.fundo,
        onBackground = p.texto,
        surface = p.cartao,
        onSurface = p.texto,
        surfaceVariant = p.cartaoAlto,
        onSurfaceVariant = p.textoFraco,
        outline = p.borda,
        outlineVariant = p.borda,
        error = p.perigo,
        onError = p.fundo,
    )
} else {
    lightColorScheme(
        primary = p.acao,
        onPrimary = p.cartao,
        primaryContainer = p.cartaoAlto,
        onPrimaryContainer = p.texto,
        secondary = p.acao2,
        onSecondary = p.cartao,
        secondaryContainer = p.cartaoAlto,
        onSecondaryContainer = p.texto,
        background = p.fundo,
        onBackground = p.texto,
        surface = p.cartao,
        onSurface = p.texto,
        surfaceVariant = p.cartaoAlto,
        onSurfaceVariant = p.textoFraco,
        outline = p.borda,
        outlineVariant = p.borda,
        error = p.perigo,
        onError = p.cartao,
    )
}

/**
 * Fredoka para títulos e números: arredondada, simpática, com peso variável.
 * Nunito para texto corrido: arredondada também, mas legível em bloco.
 */
private val Fredoka = FontFamily(
    Font(
        R.font.fredoka,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500)),
    ),
    Font(
        R.font.fredoka,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600)),
    ),
    Font(
        R.font.fredoka,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700)),
    ),
)

private val Nunito = FontFamily(
    Font(
        R.font.nunito,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400)),
    ),
    Font(
        R.font.nunito,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500)),
    ),
    Font(
        R.font.nunito,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600)),
    ),
    Font(
        R.font.nunito,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700)),
    ),
)

private val FloralTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = Fredoka,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.2).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = Fredoka,
        fontSize = 27.sp,
        lineHeight = 33.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.2).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Fredoka,
        fontSize = 21.sp,
        lineHeight = 27.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Fredoka,
        fontSize = 17.sp,
        lineHeight = 23.sp,
        fontWeight = FontWeight.Medium,
    ),
    titleSmall = TextStyle(
        fontFamily = Fredoka,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium,
    ),
    bodyLarge = TextStyle(
        fontFamily = Nunito,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Normal,
    ),
    bodyMedium = TextStyle(
        fontFamily = Nunito,
        fontSize = 13.sp,
        lineHeight = 19.sp,
        fontWeight = FontWeight.Normal,
    ),
    bodySmall = TextStyle(
        fontFamily = Nunito,
        fontSize = 12.sp,
        lineHeight = 17.sp,
        fontWeight = FontWeight.Normal,
    ),
    labelLarge = TextStyle(
        fontFamily = Nunito,
        fontSize = 13.5.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    labelMedium = TextStyle(
        fontFamily = Nunito,
        fontSize = 11.5.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    labelSmall = TextStyle(
        fontFamily = Nunito,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.6.sp,
    ),
)

@Composable
fun EnemTheme(
    escuro: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val paleta = if (escuro) PaletaNoite else PaletaClaro
    // Define a paleta ANTES de compor as telas; `key` reinicia a subárvore
    // quando o tema do sistema muda, para todo mundo reler as cores.
    PaletaAtual.atual = paleta
    key(paleta) {
        MaterialTheme(
            colorScheme = esquema(paleta),
            typography = FloralTypography,
            content = content,
        )
    }
}