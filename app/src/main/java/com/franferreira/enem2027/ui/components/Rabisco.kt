package com.franferreira.enem2027.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.franferreira.enem2027.ui.theme.PaletaClaro
import com.franferreira.enem2027.ui.theme.Primary
import com.franferreira.enem2027.ui.theme.Surface
import com.franferreira.enem2027.ui.theme.SurfaceBorder
import com.franferreira.enem2027.ui.theme.SurfaceRaised
import com.franferreira.enem2027.ui.theme.TextMuted
import com.franferreira.enem2027.ui.theme.TextPrimary

/** O amarelo do miolo da Florzinha, usado nos chips ativos. */
val MioloFlor: Color = Color(0xFFF3DEA6)

/**
 * O traço à mão do DESIGN.md, em Compose.
 *
 * O Compose não tem borda tracejada pronta: `Modifier.border` só desenha linha
 * contínua. Aqui o traço é desenhado no canvas (`PathEffect`), e a sombra é
 * deslocada e dura, sem desfoque — é o que faz o cartão parecer adesivo em vez
 * de cartão de banco.
 */
fun Modifier.bordaRabisco(
    cor: Color,
    raio: Dp = 16.dp,
    largura: Dp = 1.5.dp,
    tracejada: Boolean = true,
): Modifier = drawWithContent {
    drawContent()
    val efeito = if (tracejada) PathEffect.dashPathEffect(floatArrayOf(7f, 5f)) else null
    drawRoundRect(
        color = cor,
        cornerRadius = CornerRadius(raio.toPx(), raio.toPx()),
        style = Stroke(width = largura.toPx(), pathEffect = efeito),
    )
}

/** Sombra deslocada e opaca, atrás do conteúdo. */
fun Modifier.sombraDura(
    cor: Color,
    deslocamento: Dp = 2.dp,
    raio: Dp = 16.dp,
): Modifier = drawBehind {
    val d = deslocamento.toPx()
    drawRoundRect(
        color = cor,
        topLeft = Offset(d, d),
        size = Size(size.width, size.height),
        cornerRadius = CornerRadius(raio.toPx(), raio.toPx()),
    )
}

/**
 * O cartão do app: fundo claro, borda tracejada e, se pedido, sombra dura.
 *
 * Substitui a trinca `clip + background + border` que se repetia em todas as
 * telas, para o estilo ficar num lugar só. É composable porque as cores vêm do
 * tema (claro ou noturno).
 */
@Composable
fun Modifier.cartaoRabisco(
    raio: Dp = 16.dp,
    fundo: Color = Surface,
    borda: Color = SurfaceBorder,
    sombra: Boolean = false,
): Modifier {
    var m = this
    if (sombra) m = m.sombraDura(TextPrimary, raio = raio)
    return m
        .clip(RoundedCornerShape(raio))
        .background(fundo)
        .bordaRabisco(borda, raio = raio)
}

/** Botão do app: pílula, borda de tinta e sombra dura embaixo. */
@Composable
fun BotaoRabisco(
    texto: String,
    aoClicar: () -> Unit,
    modifier: Modifier = Modifier,
    fundo: Color = Primary,
    corTexto: Color = PaletaClaro.texto,
    habilitado: Boolean = true,
    preenchimento: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 11.dp),
) {
    val raio = 999.dp
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .then(if (habilitado) Modifier.sombraDura(TextPrimary, raio = raio) else Modifier)
            .clip(RoundedCornerShape(raio))
            .background(if (habilitado) fundo else SurfaceRaised)
            .border(
                1.5.dp,
                if (habilitado) TextPrimary else SurfaceBorder,
                RoundedCornerShape(raio),
            )
            .clickable(enabled = habilitado) { aoClicar() }
            .padding(preenchimento),
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelLarge,
            color = if (habilitado) corTexto else TextMuted,
        )
    }
}

/** Chip do app: borda de tinta, fundo claro, e o ativo no amarelo do miolo. */
@Composable
fun ChipRabisco(
    texto: String,
    selecionado: Boolean,
    aoClicar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selecionado) MioloFlor else Surface)
            .border(1.5.dp, TextPrimary, RoundedCornerShape(999.dp))
            .clickable { aoClicar() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            color = TextPrimary,
        )
    }
}

/**
 * Borda tracejada desenhada um pouco torta.
 *
 * O cartão do mockup é levemente inclinado. Inclinar o cartão inteiro desalinha
 * a área de toque; aqui só o TRAÇO gira, o fundo e o clique continuam retos.
 */
fun Modifier.cartaoRabiscoTorto(
    raio: Dp = 16.dp,
    fundo: Color = Color.Unspecified,
    borda: Color = Color.Unspecified,
    graus: Float = -0.6f,
): Modifier = this
    .then(if (fundo == Color.Unspecified) Modifier else Modifier.background(fundo, RoundedCornerShape(raio)))
    .drawWithContent {
        drawContent()
        rotate(graus) {
            drawRoundRect(
                color = if (borda == Color.Unspecified) Color(0xFFF1DCE1) else borda,
                cornerRadius = CornerRadius(raio.toPx(), raio.toPx()),
                style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f, 5f))),
            )
        }
    }

/** Divisor rabiscado: uma linha tracejada, não um fio reto. */
@Composable
fun DivisorRabisco(
    modifier: Modifier = Modifier,
    cor: Color = SurfaceBorder,
) {
    androidx.compose.foundation.Canvas(modifier = modifier.fillMaxWidth().height(2.dp)) {
        drawLine(
            color = cor,
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f, 5f)),
        )
    }
}

