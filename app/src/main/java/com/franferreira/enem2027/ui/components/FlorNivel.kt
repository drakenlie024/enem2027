package com.franferreira.enem2027.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.franferreira.enem2027.ui.theme.Primary
import com.franferreira.enem2027.ui.theme.Secondary
import com.franferreira.enem2027.ui.theme.TextMuted
import com.franferreira.enem2027.ui.theme.TextPrimary

/**
 * A flor do nível: oito fases que só crescem.
 *
 * Regra do desenho: nenhuma fase tira o que a anterior tinha. A flor dupla do
 * nível 6 continua dupla até o 10 — o que muda depois é a planta em volta
 * (folhas e uma flor companheira), nunca o miolo.
 *
 * Escolhi Canvas e não imagem: escala em qualquer tela e muda de cor junto com
 * o tema.
 */
object FlorNivel {

    /** Quantas fases o desenho tem. */
    const val FASES = 8

    /** Fase (1..8) para um nível. */
    fun faseDoNivel(nivel: Int): Int = faseDaPosicao(posicaoNoCiclo(nivel))

    /**
     * Onde o nível está dentro do ciclo de dez.
     *
     * A flor completa no nível 10; no 11 começa uma plantinha nova (broto) e a
     * anterior já está plantada no campo.
     */
    fun posicaoNoCiclo(nivel: Int): Int = ((nivel.coerceAtLeast(1) - 1) % 10) + 1

    private fun faseDaPosicao(posicao: Int): Int = when {
        posicao <= 1 -> 1
        posicao == 2 -> 2
        posicao == 3 -> 3
        posicao == 4 -> 4
        posicao == 5 -> 5
        posicao in 6..7 -> 6
        posicao in 8..9 -> 7
        else -> 8
    }

    fun nomeDaFase(fase: Int): String = when (fase) {
        1 -> "Broto"
        2 -> "Duas folhas"
        3 -> "Botão"
        4 -> "Aberta"
        5 -> "Seis pétalas"
        6 -> "Dupla"
        7 -> "Flor cheia"
        else -> "Jardim"
    }

    /** XP acumulado para CHEGAR ao nível (curva do servidor: 200 × nível). */
    fun xpParaChegar(nivel: Int): Int = if (nivel <= 1) 0 else 100 * nivel * (nivel - 1)

    /** Quanto de XP o nível atual pede no total. */
    fun xpDoNivel(nivel: Int): Int = 200 * nivel.coerceAtLeast(1)

    /** Progresso dentro do nível atual (0..1). */
    fun progressoNoNivel(xp: Int, nivel: Int): Float {
        val base = xpParaChegar(nivel)
        val alvo = xpDoNivel(nivel)
        if (alvo <= 0) return 0f
        return ((xp - base).toFloat() / alvo).coerceIn(0f, 1f)
    }

    /** Flores já completas no campo: uma a cada dez níveis. */
    fun floresNoCampo(nivel: Int): Int = ((nivel.coerceAtLeast(1) - 1) / 10).coerceAtLeast(0)
}

/** Desenha a flor de uma fase dentro do espaço disponível. */
@Composable
fun FlorDoNivel(
    nivel: Int,
    modifier: Modifier = Modifier,
    cor: Color = Primary,
    corEscura: Color = Secondary,
) {
    val fase = FlorNivel.faseDoNivel(nivel)
    Canvas(modifier = modifier) {
        desenhar(fase, cor, corEscura)
    }
}

private fun DrawScope.desenhar(fase: Int, cor: Color, corEscura: Color) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val caule = Color(0xFF8FBF9F)
    val miolo = Color(0xFFF3DEA6)

    // Caule: sempre presente, mais grosso nas fases altas.
    val espessura = if (fase >= 6) w * 0.035f else w * 0.030f
    drawLine(
        color = caule,
        start = Offset(cx, h * 0.97f),
        end = Offset(cx, h * 0.62f),
        strokeWidth = espessura,
        cap = StrokeCap.Round,
    )

    // Folhas: nascem na fase 1 e ganham um par na 7.
    if (fase >= 1) {
        folha(cx, h * 0.88f, w * 0.20f, esquerda = true, cor = caule)
        folha(cx, h * 0.88f, w * 0.20f, esquerda = false, cor = caule)
    }
    if (fase >= 2) {
        folha(cx, h * 0.80f, w * 0.26f, esquerda = true, cor = caule)
        folha(cx, h * 0.74f, w * 0.26f, esquerda = false, cor = caule)
    }
    if (fase >= 7) {
        folha(cx, h * 0.68f, w * 0.22f, esquerda = false, cor = caule)
    }

    val centroFlor = Offset(cx, h * 0.50f)
    val raioPetala = w * 0.13f

    when {
        fase == 1 || fase == 2 -> Unit // ainda não tem flor
        fase == 3 -> {
            // Botão fechado.
            drawOval(
                color = cor,
                topLeft = Offset(cx - w * 0.10f, h * 0.40f),
                size = Size(w * 0.20f, h * 0.24f),
            )
            drawArc(
                color = corEscura,
                startAngle = 200f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(cx - w * 0.10f, h * 0.40f),
                size = Size(w * 0.20f, h * 0.24f),
                style = Stroke(width = w * 0.012f),
            )
        }
        fase == 4 || fase == 5 -> {
            val petalas = if (fase == 4) 5 else 6
            repita(petalas, centroFlor) { i ->
                rotacionar(-90f + i * (360f / petalas), centroFlor) {
                    drawOval(
                        color = cor,
                        topLeft = Offset(centroFlor.x - raioPetala * 0.62f, centroFlor.y - h * 0.30f),
                        size = Size(raioPetala * 1.24f, h * 0.22f),
                    )
                }
            }
            drawCircle(miolo, w * 0.115f, centroFlor)
        }
        else -> {
            // Dupla: anel externo escuro (8) + anel interno (8). Fica igual até a fase 8.
            repita(8, centroFlor) { i ->
                rotacionar(i * 45f + 22.5f, centroFlor) {
                    drawOval(
                        color = corEscura,
                        topLeft = Offset(centroFlor.x - raioPetala * 0.52f, centroFlor.y - h * 0.33f),
                        size = Size(raioPetala * 1.04f, h * 0.19f),
                    )
                }
            }
            repita(8, centroFlor) { i ->
                rotacionar(i * 45f, centroFlor) {
                    drawOval(
                        color = cor,
                        topLeft = Offset(centroFlor.x - raioPetala * 0.62f, centroFlor.y - h * 0.26f),
                        size = Size(raioPetala * 1.24f, h * 0.21f),
                    )
                }
            }
            drawCircle(miolo, w * 0.12f, centroFlor)
        }
    }

    // Fase 8: a flor companheira, menor, à esquerda.
    if (fase >= 8) {
        val cx2 = w * 0.20f
        val cy2 = h * 0.68f
        drawLine(
            color = caule,
            start = Offset(cx2, h * 0.97f),
            end = Offset(cx2, cy2),
            strokeWidth = w * 0.022f,
            cap = StrokeCap.Round,
        )
        folha(cx2, h * 0.86f, w * 0.14f, esquerda = true, cor = caule)
        repita(5, Offset(cx2, cy2)) { i ->
            rotacionar(-90f + i * 72f, Offset(cx2, cy2)) {
                drawOval(
                    color = cor,
                    topLeft = Offset(cx2 - w * 0.045f, cy2 - h * 0.115f),
                    size = Size(w * 0.09f, h * 0.10f),
                )
            }
        }
        drawCircle(miolo, w * 0.055f, Offset(cx2, cy2))
    }
}

private inline fun repita(vezes: Int, centro: Offset, bloco: (Int) -> Unit) {
    for (i in 0 until vezes) bloco(i)
}

private inline fun DrawScope.rotacionar(graus: Float, pivot: Offset, bloco: () -> Unit) {
    rotate(degrees = graus, pivot = pivot) { bloco() }
}

private fun DrawScope.folha(cx: Float, y: Float, largura: Float, esquerda: Boolean, cor: Color) {
    val sinal = if (esquerda) -1f else 1f
    drawOval(
        color = cor,
        topLeft = Offset(
            if (esquerda) cx - largura else cx,
            y,
        ),
        size = Size(largura, largura * 0.42f),
    )
}

/**
 * O campo: cada flor completa vira uma flor pequena; a do ciclo atual aparece
 * maior, à direita, ainda crescendo.
 */
@Composable
fun CampoDeFlores(
    nivel: Int,
    modifier: Modifier = Modifier,
) {
    val completas = FlorNivel.floresNoCampo(nivel)
    val visiveis = completas.coerceAtMost(6)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .cartaoRabisco(18.dp, fundo = Color(0xFFF4F7F1), borda = Color(0xFFBFD8C2))
            .padding(14.dp),
    ) {
        Text(
            text = if (completas == 0) "SEU CAMPO" else "SEU CAMPO · $completas ${if (completas == 1) "FLOR" else "FLORES"}",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF4F7A57),
        )
        Spacer(Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth().height(92.dp)) {
            Canvas(modifier = Modifier.fillMaxWidth().height(92.dp)) {
                // grama
                drawRect(
                    color = Color(0xFFDFEBDC),
                    topLeft = Offset(0f, size.height * 0.78f),
                    size = Size(size.width, size.height * 0.22f),
                )
                if (visiveis == 0) {
                    // ainda nada plantado: só a plantinha atual, maior
                    desenharFlor(1f, size.width * 0.50f, size.height, 0.55f, Color(0xFFE98AA3), Color(0xFFC96A85))
                } else {
                    val passo = size.width / (visiveis + 1.6f)
                    for (i in 0 until visiveis) {
                        val x = passo * (i + 0.9f)
                        desenharFlor(8f, x, size.height, 0.34f, Color(0xFFE98AA3), Color(0xFFC96A85))
                    }
                    // a que está crescendo, à direita e maior
                    desenharFlor(
                        FlorNivel.faseDoNivel(nivel).toFloat(),
                        size.width * 0.88f,
                        size.height,
                        0.42f,
                        Color(0xFFE98AA3),
                        Color(0xFFC96A85),
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (completas == 0) {
                "Quando a flor do nível 10 fechar, ela é plantada aqui."
            } else {
                "A próxima flor completa entra no campo."
            },
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
        )
    }
}

/** Flor pequena para o campo (desenho simplificado). */
private fun DrawScope.desenharFlor(
    fase: Float,
    cx: Float,
    alturaTotal: Float,
    escala: Float,
    cor: Color,
    corEscura: Color,
) {
    val caule = Color(0xFF8FBF9F)
    val miolo = Color(0xFFF3DEA6)
    val h = alturaTotal * escala
    val base = alturaTotal * 0.95f
    val centro = Offset(cx, base - h * 0.55f)

    drawLine(
        color = caule,
        start = Offset(cx, base),
        end = Offset(cx, centro.y),
        strokeWidth = alturaTotal * 0.018f,
        cap = StrokeCap.Round,
    )
    val r = alturaTotal * 0.075f * escala * 2f
    val petalas = if (fase <= 2f) 0 else if (fase <= 5f) 6 else 8
    if (petalas > 0) {
        for (i in 0 until petalas) {
            rotate(degrees = i * (360f / petalas), pivot = centro) {
                drawOval(
                    color = if (fase >= 6f && i % 2 == 0) corEscura else cor,
                    topLeft = Offset(cx - r * 0.42f, centro.y - r * 1.5f),
                    size = Size(r * 0.84f, r * 1.1f),
                )
            }
        }
        drawCircle(miolo, r * 0.62f, centro)
    }
}
