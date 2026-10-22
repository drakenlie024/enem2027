package com.franferreira.enem2027.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.franferreira.enem2027.ui.theme.Primary

/**
 * Gizinho: o giz que mora na lousa.
 *
 * Desenhado à mão no Canvas de propósito: nada de ícone vetorial perfeito, ele
 * é o traço de giz que dá personalidade ao app. Usado na Hoje, nos estados
 * vazios e no resultado da etapa.
 */
@Composable
fun Gizinho(
    modifier: Modifier = Modifier,
    cor: Color = Primary,
    tamanho: Dp = 44.dp,
    dormindo: Boolean = false,
) {
    Canvas(modifier = modifier.size(tamanho)) {
        val w = size.width
        val h = size.height
        val traco = w * 0.055f
        val centro = Offset(w / 2f, h * 0.54f)
        val raio = w * 0.33f

        // Corpo: levemente irregular, como giz em lousa.
        val corpo = Path().apply {
            moveTo(centro.x - raio, centro.y)
            cubicTo(
                centro.x - raio, centro.y - raio * 1.15f,
                centro.x + raio, centro.y - raio * 1.15f,
                centro.x + raio, centro.y,
            )
            cubicTo(
                centro.x + raio, centro.y + raio * 1.05f,
                centro.x - raio, centro.y + raio * 1.05f,
                centro.x - raio, centro.y,
            )
            close()
        }
        drawPath(corpo, color = cor.copy(alpha = 0.10f))
        drawPath(corpo, color = cor, style = Stroke(width = traco, cap = StrokeCap.Round))
        drawPath(
            corpo,
            color = cor.copy(alpha = 0.35f),
            style = Stroke(width = traco * 0.6f, cap = StrokeCap.Round),
        )

        // Olhos.
        val olhoY = centro.y - raio * 0.12f
        val olhoX = raio * 0.36f
        if (dormindo) {
            drawLine(
                cor,
                Offset(centro.x - olhoX - w * 0.05f, olhoY),
                Offset(centro.x - olhoX + w * 0.05f, olhoY),
                strokeWidth = traco * 0.9f,
                cap = StrokeCap.Round,
            )
            drawLine(
                cor,
                Offset(centro.x + olhoX - w * 0.05f, olhoY),
                Offset(centro.x + olhoX + w * 0.05f, olhoY),
                strokeWidth = traco * 0.9f,
                cap = StrokeCap.Round,
            )
        } else {
            drawCircle(cor, radius = w * 0.035f, center = Offset(centro.x - olhoX, olhoY))
            drawCircle(cor, radius = w * 0.035f, center = Offset(centro.x + olhoX, olhoY))
        }

        // Sorriso.
        val sorriso = Path().apply {
            val y = centro.y + raio * 0.28f
            moveTo(centro.x - raio * 0.28f, y)
            quadraticBezierTo(centro.x, y + raio * 0.30f, centro.x + raio * 0.28f, y)
        }
        drawPath(sorriso, color = cor, style = Stroke(width = traco * 0.9f, cap = StrokeCap.Round))

        // Bochechas.
        drawCircle(
            cor.copy(alpha = 0.22f),
            radius = w * 0.055f,
            center = Offset(centro.x - raio * 0.52f, centro.y + raio * 0.14f),
        )
        drawCircle(
            cor.copy(alpha = 0.22f),
            radius = w * 0.055f,
            center = Offset(centro.x + raio * 0.52f, centro.y + raio * 0.14f),
        )

        // Estrelinha.
        val estrela = Offset(w * 0.80f, h * 0.20f)
        val r = w * 0.075f
        drawLine(cor, Offset(estrela.x - r, estrela.y), Offset(estrela.x + r, estrela.y), strokeWidth = traco * 0.8f, cap = StrokeCap.Round)
        drawLine(cor, Offset(estrela.x, estrela.y - r), Offset(estrela.x, estrela.y + r), strokeWidth = traco * 0.8f, cap = StrokeCap.Round)
    }
}