package com.franferreira.enem2027.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.franferreira.enem2027.data.model.Etapa
import com.franferreira.enem2027.data.model.Trilha
import com.franferreira.enem2027.ui.theme.Background
import com.franferreira.enem2027.ui.theme.BackgroundGradientTop
import com.franferreira.enem2027.ui.theme.Danger
import com.franferreira.enem2027.ui.theme.Secondary
import com.franferreira.enem2027.ui.theme.Surface
import com.franferreira.enem2027.ui.theme.SurfaceBorder
import com.franferreira.enem2027.ui.theme.SurfaceRaised
import com.franferreira.enem2027.ui.theme.TextMuted
import com.franferreira.enem2027.ui.theme.TextMutedDark
import com.franferreira.enem2027.ui.theme.TextPrimary
import com.franferreira.enem2027.ui.theme.TextSecondary
import com.franferreira.enem2027.ui.theme.corDaArea

/**
 * Tela da trilha: o caminho vertical, com os nós estilo Duolingo.
 *
 * Um nó por etapa. Os concluídos ficam verdes, o atual pulsa, os futuros
 * ficam apagados (e travados até o anterior ser concluído).
 */
@Composable
fun TelaTrilha(
    trilha: Trilha,
    aoVoltar: () -> Unit,
    aoJogar: (Etapa) -> Unit,
) {
    val cor = corDaArea(trilha.area)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundGradientTop, Background))),
    ) {
        // topo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.ArrowBack,
                contentDescription = "Voltar",
                tint = TextMuted,
                modifier = Modifier
                    .size(22.dp)
                    .clickable { aoVoltar() },
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = trilha.nome,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${trilha.concluidas} de ${trilha.totalEtapas} etapas",
                    style = MaterialTheme.typography.labelSmall,
                    color = cor,
                )
            }
        }

        // barra de progresso da trilha
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(6.dp)
                .clip(CircleShape)
                .background(SurfaceRaised),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((trilha.progresso / 100.0).toFloat().coerceIn(0f, 1f))
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(cor),
            )
        }

        Spacer(Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, bottom = 28.dp
            ),
        ) {
            item {
                Text(
                    text = trilha.descricao,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }

            itemsIndexed(trilha.etapas) { indice, etapa ->
                val eAtual = indice == trilha.proximaEtapa
                val eTravada = !etapa.concluida && !eAtual
                // Desloca os nós para dar o desenho de caminho serpentina.
                val deslocamento = when (indice % 4) {
                    0 -> 0f
                    1 -> 1f
                    2 -> 2f
                    else -> 1f
                }
                NoTrilha(
                    etapa = etapa,
                    cor = cor,
                    eAtual = eAtual,
                    eTravada = eTravada,
                    deslocamento = deslocamento,
                    aoJogar = { aoJogar(etapa) },
                )
            }
        }
    }
}

@Composable
private fun NoTrilha(
    etapa: Etapa,
    cor: Color,
    eAtual: Boolean,
    eTravada: Boolean,
    deslocamento: Float,
    aoJogar: () -> Unit,
) {
    // O nó atual pulsa devagar, como o Duolingo.
    val transicao = rememberInfiniteTransition(label = "pulso")
    val escala by transicao.animateFloat(
        initialValue = 1f,
        targetValue = if (eAtual) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "escala",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (deslocamento * 46).dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .scale(escala)
                    .clip(CircleShape)
                    .background(
                        when {
                            etapa.concluida -> Secondary
                            eAtual -> cor
                            else -> SurfaceRaised
                        }
                    )
                    .border(
                        width = if (eAtual) 3.dp else 1.dp,
                        color = when {
                            etapa.concluida -> Secondary.copy(alpha = 0.4f)
                            eAtual -> cor.copy(alpha = 0.5f)
                            else -> SurfaceBorder
                        },
                        shape = CircleShape,
                    )
                    .clickable(enabled = !eTravada) { aoJogar() },
                contentAlignment = Alignment.Center,
            ) {
                when {
                    etapa.concluida -> Icon(
                        Icons.Filled.Check,
                        contentDescription = "Concluída",
                        tint = Background,
                        modifier = Modifier.size(26.dp),
                    )
                    eTravada -> Icon(
                        Icons.Filled.Lock,
                        contentDescription = "Travada",
                        tint = TextMutedDark,
                        modifier = Modifier.size(20.dp),
                    )
                    else -> Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = "Jogar",
                        tint = Background,
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 12.dp),
        ) {
            Text(
                text = etapa.nome,
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    eTravada -> TextMutedDark
                    else -> TextPrimary
                },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.dp))
            if (etapa.concluida) {
                // Etapa concluída = resumo com NÚMERO: placar, tempo e XP.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = etapa.placar,
                        style = MaterialTheme.typography.labelLarge,
                        color = Secondary,
                    )
                    Text(
                        text = "  ·  ${etapa.tempo}  ·  +${etapa.xp} XP",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                    )
                }
            } else {
                Text(
                    text = if (eAtual) {
                        "${etapa.questoes.size} questões  ·  pronta para começar"
                    } else {
                        "${etapa.questoes.size} questões  ·  conclua a anterior"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (eAtual) cor else TextMutedDark,
                )
            }
        }
    }
}

/** Resumo numérico da etapa concluída, mostrado ao terminar. */
@Composable
fun ResumoEtapa(
    tema: String,
    acertos: Int,
    total: Int,
    segundos: Int,
    xp: Int,
    cor: Color,
    aoFechar: () -> Unit,
) {
    val tempo = when {
        segundos <= 0 -> "—"
        segundos < 60 -> "${segundos}s"
        else -> "${segundos / 60}min ${segundos % 60}s"
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Surface)
            .border(1.dp, cor.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "ETAPA CONCLUÍDA",
            style = MaterialTheme.typography.labelSmall,
            color = cor,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = tema,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            NumeroResumo(valor = "$acertos/$total", rotulo = "acertos", cor = cor)
            NumeroResumo(valor = tempo, rotulo = "tempo", cor = cor)
            NumeroResumo(valor = "+$xp", rotulo = "XP", cor = cor)
        }
        Spacer(Modifier.height(18.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(cor)
                .clickable { aoFechar() },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Continuar",
                style = MaterialTheme.typography.labelLarge,
                color = Background,
            )
        }
    }
}

@Composable
private fun NumeroResumo(valor: String, rotulo: String, cor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = valor,
            style = MaterialTheme.typography.titleLarge,
            color = cor,
        )
        Text(
            text = rotulo,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
        )
    }
}
