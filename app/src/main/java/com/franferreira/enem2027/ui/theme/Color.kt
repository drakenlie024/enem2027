package com.franferreira.enem2027.ui.theme

import androidx.compose.ui.graphics.Color

// Base escura do mockup C
val Background = Color(0xFF0B0D11)
val BackgroundGradientTop = Color(0xFF0E1117)
val Surface = Color(0xFF141821)
val SurfaceBorder = Color(0xFF1F2532)
val SurfaceRaised = Color(0xFF232A38)

// Acentos
val Primary = Color(0xFF7C6CFF)
val Secondary = Color(0xFF3DDC97)

// Texto
val TextPrimary = Color(0xFFF2F3F7)
val TextSecondary = Color(0xFFEDEFF5)
val TextMuted = Color(0xFF7C8496)
val TextMutedDark = Color(0xFF7C8496)

// Modais
val Danger = Color(0xFFFF6B6B)
val Warning = Color(0xFFFFB020)
val Info = Color(0xFF4DA3FF)

/**
 * Cor por área.
 *
 * A cor diz a MATÉRIA, não o estado: a trilha de Matemática é sempre roxa,
 * a de Humanas sempre âmbar. Assim o app é lido de longe, e o estado de cada
 * nó vem do formato (preenchido, pulsando, vazio), não da cor.
 */
val AreaMatematica = Color(0xFF7C6CFF)
val AreaLinguagens = Color(0xFF3DDC97)
val AreaHumanas = Color(0xFFFFB020)
val AreaNaturezas = Color(0xFF4DA3FF)

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
