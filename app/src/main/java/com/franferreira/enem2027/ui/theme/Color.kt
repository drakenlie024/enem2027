package com.franferreira.enem2027.ui.theme

import androidx.compose.ui.graphics.Color

// Lousa fofa: verde-pinheiro profundo, giz pastel.
val Background = Color(0xFF16241D)
val BackgroundGradientTop = Color(0xFF1D3026)
val Surface = Color(0xFF1F3329)
val SurfaceBorder = Color(0xFF33513F)
val SurfaceRaised = Color(0xFF2A4335)

// Giz: ação principal em menta, apoio em pêssego.
val Primary = Color(0xFF9BE7BE)
val Secondary = Color(0xFFFFC9A3)

// Texto: giz quente, nunca branco puro.
val TextPrimary = Color(0xFFF6F2E6)
val TextSecondary = Color(0xFFE7E2D3)
val TextMuted = Color(0xFFA9BAAC)
val TextMutedDark = Color(0xFF8FA396)

// Estados (tons de giz, sem neon).
val Danger = Color(0xFFFFA1A1)
val Warning = Color(0xFFF7DE9A)
val Info = Color(0xFF9BD3FF)

/**
 * Cor por área — giz de cor diferente por matéria.
 *
 * A cor diz a MATÉRIA, não o estado: a trilha de Matemática é sempre lavanda,
 * a de Linguagens sempre menta. O estado vem do formato (preenchido, pulsando,
 * vazio), não da cor.
 */
val AreaMatematica = Color(0xFFC7B6FF)
val AreaLinguagens = Color(0xFF9BE7BE)
val AreaHumanas = Color(0xFFF7DE9A)
val AreaNaturezas = Color(0xFF9BD3FF)

fun corDaArea(area: String?): Color = when (area) {
    "matematica" -> AreaMatematica
    "biologia", "ciencias-natureza" -> AreaNaturezas
    "portugues", "linguagens", "ingles", "espanhol" -> AreaLinguagens
    "historia", "ciencias-humanas", "geografia", "filosofia", "sociologia" -> AreaHumanas
    "fisica" -> Info
    "quimica" -> Secondary
    "artes" -> Warning
    "ed-fisica" -> Secondary
    "redacao" -> Primary
    else -> Primary
}