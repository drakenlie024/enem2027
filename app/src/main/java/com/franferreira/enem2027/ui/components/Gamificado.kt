package com.franferreira.enem2027.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.franferreira.enem2027.ui.theme.Background
import com.franferreira.enem2027.ui.theme.Surface
import com.franferreira.enem2027.ui.theme.SurfaceBorder
import com.franferreira.enem2027.ui.theme.TextMuted
import com.franferreira.enem2027.ui.theme.TextPrimary
import com.franferreira.enem2027.ui.theme.TextSecondary

/**
 * Componentes do estilo "gamificado" (Duolingo).
 *
 * A ideia: a tela tem um herói (anel, botão grande, contador) e o resto
 * apoia. Tudo que é clicável é grande, redondo e tem brilho. O texto é
 * secundário — quem manda é o número.
 */

/**
 * Botão principal: pílula com borda de tinta e sombra dura.
 *
 * O gradiente e a sombra difusa saíram no DESIGN.md (estilo Doodle): o botão
 * parece um adesivo colado no papel, não um botão de vidro.
 */
@Composable
fun BotaoGamificado(
    texto: String,
    modifier: Modifier = Modifier,
    cor: Color = com.franferreira.enem2027.ui.theme.Primary,
    verde: Boolean = false,
    habilitado: Boolean = true,
    aoClicar: () -> Unit,
) {
    BotaoRabisco(
        texto = texto,
        aoClicar = aoClicar,
        modifier = modifier.fillMaxWidth().height(52.dp),
        fundo = if (verde) com.franferreira.enem2027.ui.theme.Primary else cor,
        habilitado = habilitado,
    )
}

/** Card com gradiente suave — o "bloco" padrão do estilo. */
@Composable
fun CartaoGamificado(
    modifier: Modifier = Modifier,
    conteudo: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF1A1F2C), Surface)))
            .border(1.dp, Color(0xFF262D3C), RoundedCornerShape(22.dp))
            .padding(14.dp),
    ) {
        conteudo()
    }
}

/** Pílula pequena (streak, nível, contador). */
@Composable
fun Pilula(
    texto: String,
    modifier: Modifier = Modifier,
    cor: Color = TextSecondary,
    fundo: Color = Color(0xFF232A38),
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(fundo)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = cor,
        )
    }
}

/**
 * Nó da trilha vertical.
 *
 * Três estados: concluído (verde, check), atual (roxo, com halo pulsante) e
 * travado (contorno tracejado, apagado). O halo é o que diz "é aqui".
 */
@Composable
fun NoTrilha(
    estado: String, // "feito" | "atual" | "travado"
    modificador: Modifier = Modifier,
    cor: Color = com.franferreira.enem2027.ui.theme.Primary,
    conteudo: @Composable () -> Unit = {},
) {
    val (fundo, borda, texto) = when (estado) {
        "feito" -> Triple(Color(0xFF3DDC97), Color(0xFF3DDC97), Color(0xFF06231A))
        "atual" -> Triple(cor, cor, Color.White)
        else -> Triple(Color(0xFF1A1F2C), Color(0xFF2A3140), TextMuted)
    }
    Box(
        modifier = modificador
            .size(56.dp)
            .shadow(if (estado == "atual") 14.dp else 0.dp, CircleShape, spotColor = cor)
            .clip(CircleShape)
            .background(fundo)
            .border(
                width = if (estado == "travado") 2.dp else 0.dp,
                color = borda,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        conteudo()
    }
}

/** Linha vertical que liga os nós (verde quando a etapa passou). */
@Composable
fun Conector(
    concluido: Boolean,
    modificador: Modifier = Modifier,
    cor: Color = Color(0xFF3DDC97),
) {
    Box(
        modifier = modificador
            .width(3.dp)
            .height(26.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(if (concluido) cor else Color(0xFF232A38)),
    )
}

/** Alternativa em cartão — grande, tocável, com a letra em círculo. */
@Composable
fun AlternativaGamificada(
    letra: String,
    texto: String,
    estado: String = "neutra", // "neutra" | "selecionada" | "certa" | "errada"
    modificador: Modifier = Modifier,
    aoClicar: () -> Unit = {},
) {
    val (borda, fundo) = when (estado) {
        "certa" -> Color(0xFF3DDC97) to Color(0xFF3DDC97).copy(alpha = 0.12f)
        "errada" -> Color(0xFFFF6B6B) to Color(0xFFFF6B6B).copy(alpha = 0.12f)
        "selecionada" -> com.franferreira.enem2027.ui.theme.Primary to
            com.franferreira.enem2027.ui.theme.Primary.copy(alpha = 0.10f)
        else -> Color(0xFF232A38) to Surface
    }
    val corLetra = when (estado) {
        "certa" -> Color(0xFF06231A)
        "errada" -> Color(0xFF2A0B0B)
        else -> TextSecondary
    }
    Row(
        modifier = modificador
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(fundo)
            .border(1.5.dp, borda, RoundedCornerShape(18.dp))
            .clickable { aoClicar() }
            .padding(11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (estado == "certa") Color(0xFF3DDC97) else Color(0xFF232A38)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = letra,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = corLetra,
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            modifier = Modifier.weight(1f),
        )
    }
}

/** Barra de progresso com gradiente. */
@Composable
fun BarraGamificada(
    progresso: Float,
    modificador: Modifier = Modifier,
    cor: Color = com.franferreira.enem2027.ui.theme.Primary,
    altura: Int = 7,
) {
    Box(
        modifier = modificador
            .fillMaxWidth()
            .height(altura.dp)
            .bordaRabisco(cor.copy(alpha = 0.55f), raio = (altura / 2).dp, largura = 1.5.dp),
    ) {
        // Preenchimento chapado: no Doodle o traço é do papel, não gradiente.
        Box(
            modifier = Modifier
                .fillMaxWidth(progresso.coerceIn(0f, 1f))
                .height(altura.dp)
                .clip(CircleShape)
                .background(cor),
        )
    }
}

/**
 * Anel de resultado.
 *
 * O número do meio é o que importa; o anel é contexto. Verde quando a taxa
 * está boa, âmbar no meio, vermelho abaixo de 50%.
 */
@Composable
fun AnelGamificado(
    progresso: Float,
    valor: String,
    rotulo: String,
    modificador: Modifier = Modifier,
    tamanho: Int = 116,
) {
    val cor = when {
        progresso >= 0.7f -> Color(0xFF3DDC97)
        progresso >= 0.5f -> com.franferreira.enem2027.ui.theme.Warning
        else -> com.franferreira.enem2027.ui.theme.Danger
    }
    Box(
        modifier = modificador.size(tamanho.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val espessura = 10.dp.toPx()
            val diametro = size.minDimension - espessura
            val canto = androidx.compose.ui.geometry.Offset(
                (size.width - diametro) / 2,
                (size.height - diametro) / 2,
            )
            drawArc(
                color = Color(0xFF232A38),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = canto,
                size = androidx.compose.ui.geometry.Size(diametro, diametro),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = espessura,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                ),
            )
            drawArc(
                color = cor,
                startAngle = -90f,
                sweepAngle = 360f * progresso.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = canto,
                size = androidx.compose.ui.geometry.Size(diametro, diametro),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = espessura,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                ),
            )
        }
        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
            Text(
                text = valor,
                style = MaterialTheme.typography.titleLarge,
                color = com.franferreira.enem2027.ui.theme.TextPrimary,
            )
            Text(
                text = rotulo,
                style = MaterialTheme.typography.labelSmall,
                color = com.franferreira.enem2027.ui.theme.TextMuted,
            )
        }
    }
}

/** Títulos de seção no estilo do app. */
@Composable
fun SecaoGamificada(
    texto: String,
    modificador: Modifier = Modifier,
    cor: Color = TextMuted,
) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelSmall,
        color = cor,
        modifier = modificador,
    )
}
