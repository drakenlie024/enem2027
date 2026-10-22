package com.franferreira.enem2027.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta floral em duas versões: clara (dia) e noturna (ameixa profunda).
 *
 * As duas usam a mesma linguagem — rosa para ação, lilás/sálvia/manteiga/céu
 * para as matérias — só trocam o fundo e a temperatura. O app escolhe pelo
 * tema do sistema; a noite evita o clarão na cara de quem estuda de madrugada.
 */
data class Paleta(
    val fundo: Color,
    val fundoTopo: Color,
    val cartao: Color,
    val cartaoAlto: Color,
    val borda: Color,
    val texto: Color,
    val textoSecundario: Color,
    val textoFraco: Color,
    val textoFracoEscuro: Color,
    val acao: Color,
    val acao2: Color,
    val perigo: Color,
    val aviso: Color,
    val info: Color,
    val mat: Color,
    val ling: Color,
    val hum: Color,
    val nat: Color,
    val flor: Color,
    val escuro: Boolean,
)

val PaletaClaro = Paleta(
    fundo = Color(0xFFFBF4F2),
    fundoTopo = Color(0xFFFDF9F8),
    cartao = Color(0xFFFFFFFF),
    cartaoAlto = Color(0xFFFFFCFB),
    borda = Color(0xFFF1DCE1),
    texto = Color(0xFF3B2A31),
    textoSecundario = Color(0xFF5A434A),
    textoFraco = Color(0xFF8A7378),
    textoFracoEscuro = Color(0xFFA98E94),
    acao = Color(0xFFE98AA3),
    acao2 = Color(0xFFC9B6E4),
    perigo = Color(0xFFD96A6A),
    aviso = Color(0xFFE8C170),
    info = Color(0xFF8FB8D9),
    mat = Color(0xFFC9B6E4),
    ling = Color(0xFFA8C3A0),
    hum = Color(0xFFF3DEA6),
    nat = Color(0xFF9BC7D9),
    flor = Color(0xFFF7C9D4),
    escuro = false,
)

val PaletaNoite = Paleta(
    fundo = Color(0xFF241A22),
    fundoTopo = Color(0xFF2D2029),
    cartao = Color(0xFF2E2229),
    cartaoAlto = Color(0xFF392B33),
    borda = Color(0xFF4A3742),
    texto = Color(0xFFFBF3F4),
    textoSecundario = Color(0xFFEFE2E5),
    textoFraco = Color(0xFFC0A7AE),
    textoFracoEscuro = Color(0xFF9C848C),
    acao = Color(0xFFF2A9B8),
    acao2 = Color(0xFFD3C2F0),
    perigo = Color(0xFFFFA8A8),
    aviso = Color(0xFFF3D9A0),
    info = Color(0xFFA9CDE8),
    mat = Color(0xFFD3C2F0),
    ling = Color(0xFFB7D1AE),
    hum = Color(0xFFF3D9A0),
    nat = Color(0xFFA9CDE8),
    flor = Color(0xFF6B4A58),
    escuro = true,
)

/** Paleta em uso. O tema troca antes de compor as telas. */
object PaletaAtual {
    var atual: Paleta = PaletaClaro
}

val Background: Color get() = PaletaAtual.atual.fundo
val BackgroundGradientTop: Color get() = PaletaAtual.atual.fundoTopo
val Surface: Color get() = PaletaAtual.atual.cartao
val SurfaceRaised: Color get() = PaletaAtual.atual.cartaoAlto
val SurfaceBorder: Color get() = PaletaAtual.atual.borda
val TextPrimary: Color get() = PaletaAtual.atual.texto
val TextSecondary: Color get() = PaletaAtual.atual.textoSecundario
val TextMuted: Color get() = PaletaAtual.atual.textoFraco
val TextMutedDark: Color get() = PaletaAtual.atual.textoFracoEscuro
val Primary: Color get() = PaletaAtual.atual.acao
val Secondary: Color get() = PaletaAtual.atual.acao2
val Danger: Color get() = PaletaAtual.atual.perigo
val Warning: Color get() = PaletaAtual.atual.aviso
val Info: Color get() = PaletaAtual.atual.info
val AreaMatematica: Color get() = PaletaAtual.atual.mat
val AreaLinguagens: Color get() = PaletaAtual.atual.ling
val AreaHumanas: Color get() = PaletaAtual.atual.hum
val AreaNaturezas: Color get() = PaletaAtual.atual.nat
val FlorFundo: Color get() = PaletaAtual.atual.flor

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