package com.franferreira.enem2027.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.franferreira.enem2027.ui.theme.FlorFundo
import com.franferreira.enem2027.ui.theme.Primary
import com.franferreira.enem2027.ui.theme.Secondary
import com.franferreira.enem2027.ui.theme.TextPrimary

/**
 * Florzinha: a mascote do app.
 *
 * Cinco pétalas, miolo com carinha. É o que dá o tom "fofo" sem infantilizar:
 * ela aparece na Hoje, no login e no resultado da etapa.
 */
@Composable
fun Florzinha(
    modifier: Modifier = Modifier,
    petala: Color = Primary,
    miolo: Color = androidx.compose.ui.graphics.Color(0xFFF3DEA6),
    tamanho: Dp = 46.dp,
    dormindo: Boolean = false,
) {
    Canvas(modifier = modifier.size(tamanho)) {
        val c = center
        val r = size.minDimension
        val petaloLargo = r * 0.30f
        val petaloAlto = r * 0.40f
        val distancia = r * 0.26f

        for (i in 0 until 5) {
            rotate(degrees = i * 72f, pivot = c) {
                drawOval(
                    color = petala,
                    topLeft = Offset(c.x - petaloLargo / 2f, c.y - distancia - petaloAlto),
                    size = Size(petaloLargo, petaloAlto),
                )
                drawOval(
                    color = petala.copy(alpha = 0.25f),
                    topLeft = Offset(c.x - petaloLargo / 2f, c.y - distancia - petaloAlto),
                    size = Size(petaloLargo, petaloAlto),
                    style = Stroke(width = r * 0.012f),
                )
            }
        }

        drawCircle(miolo, radius = r * 0.21f, center = c)
        drawCircle(miolo.copy(alpha = 0.5f), radius = r * 0.21f, center = c, style = Stroke(width = r * 0.014f))

        val olhoY = c.y - r * 0.03f
        val olhoX = r * 0.075f
        if (dormindo) {
            drawLine(
                TextPrimary.copy(alpha = 0.75f),
                Offset(c.x - olhoX - r * 0.03f, olhoY),
                Offset(c.x - olhoX + r * 0.03f, olhoY),
                strokeWidth = r * 0.02f,
                cap = StrokeCap.Round,
            )
            drawLine(
                TextPrimary.copy(alpha = 0.75f),
                Offset(c.x + olhoX - r * 0.03f, olhoY),
                Offset(c.x + olhoX + r * 0.03f, olhoY),
                strokeWidth = r * 0.02f,
                cap = StrokeCap.Round,
            )
        } else {
            drawCircle(TextPrimary.copy(alpha = 0.8f), radius = r * 0.026f, center = Offset(c.x - olhoX, olhoY))
            drawCircle(TextPrimary.copy(alpha = 0.8f), radius = r * 0.026f, center = Offset(c.x + olhoX, olhoY))
        }

        val sorriso = Path().apply {
            val y = c.y + r * 0.085f
            moveTo(c.x - r * 0.065f, y)
            quadraticBezierTo(c.x, y + r * 0.07f, c.x + r * 0.065f, y)
        }
        drawPath(sorriso, color = TextPrimary.copy(alpha = 0.8f), style = Stroke(width = r * 0.02f, cap = StrokeCap.Round))

        // Bochechas coradas.
        drawCircle(petala.copy(alpha = 0.55f), radius = r * 0.035f, center = Offset(c.x - r * 0.13f, c.y + r * 0.055f))
        drawCircle(petala.copy(alpha = 0.55f), radius = r * 0.035f, center = Offset(c.x + r * 0.13f, c.y + r * 0.055f))

        // Brilhinho.
        val estrela = Offset(r * 0.84f, r * 0.18f)
        val e = r * 0.055f
        drawLine(Secondary, Offset(estrela.x - e, estrela.y), Offset(estrela.x + e, estrela.y), strokeWidth = r * 0.016f, cap = StrokeCap.Round)
        drawLine(Secondary, Offset(estrela.x, estrela.y - e), Offset(estrela.x, estrela.y + e), strokeWidth = r * 0.016f, cap = StrokeCap.Round)
    }
}

/**
 * FundoFloral: flores de cinco pétalas espalhadas, em opacidade baixa.
 *
 * Fica atrás do conteúdo das telas principais. É desenho, não imagem: não pesa
 * no APK e acompanha a paleta (clara ou noturna).
 */
@Composable
fun FundoFloral(modifier: Modifier = Modifier, cor: Color = FlorFundo, alpha: Float = 0.45f) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val posicoes = listOf(
            Triple(0.03f, 0.12f, 0.11f),
            Triple(0.92f, 0.23f, 0.14f),
            Triple(0.10f, 0.62f, 0.09f),
            Triple(0.88f, 0.74f, 0.16f),
            Triple(0.48f, 0.94f, 0.07f),
            Triple(0.70f, 0.06f, 0.06f),
        )
        posicoes.forEach { (px, py, r) ->
            desenharFlor(Offset(w * px, h * py), w * r, cor.copy(alpha = alpha))
        }
    }
}

/**
 * Desenha as flores atrás do conteúdo, sem ocupar espaço de layout.
 *
 * Usar `drawBehind` evita envolver cada tela num Box só para o fundo: o
 * modificador entra na cadeia existente e as flores ficam atrás dos filhos.
 */
fun Modifier.floresFundo(cor: Color = FlorFundo, alpha: Float = 0.40f): Modifier = drawBehind {
    val w = size.width
    val h = size.height
    val posicoes = listOf(
        Triple(0.02f, 0.10f, 0.13f),
        Triple(0.94f, 0.20f, 0.16f),
        Triple(0.08f, 0.58f, 0.10f),
        Triple(0.90f, 0.70f, 0.18f),
        Triple(0.52f, 0.96f, 0.08f),
        Triple(0.72f, 0.04f, 0.07f),
    )
    posicoes.forEach { (px, py, r) ->
        desenharFlor(Offset(w * px, h * py), w * r, cor.copy(alpha = alpha))
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.desenharFlor(
    centro: Offset,
    raio: Float,
    cor: Color,
) {
    val traco = raio * 0.10f
    for (i in 0 until 5) {
        rotate(degrees = i * 72f, pivot = centro) {
            drawOval(
                color = cor,
                topLeft = Offset(centro.x - raio * 0.26f, centro.y - raio * 0.95f),
                size = Size(raio * 0.52f, raio * 0.72f),
                style = Stroke(width = traco),
            )
        }
    }
    drawCircle(cor, radius = raio * 0.12f, center = centro, style = Stroke(width = traco))
}