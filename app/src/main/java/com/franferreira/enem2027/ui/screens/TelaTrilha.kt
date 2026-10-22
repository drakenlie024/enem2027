package com.franferreira.enem2027.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.franferreira.enem2027.data.model.Etapa
import com.franferreira.enem2027.data.model.Trilha
import com.franferreira.enem2027.ui.components.BarraGamificada
import com.franferreira.enem2027.ui.components.CartaoGamificado
import com.franferreira.enem2027.ui.components.Conector
import com.franferreira.enem2027.ui.components.NoTrilha
import com.franferreira.enem2027.ui.components.Pilula
import com.franferreira.enem2027.ui.theme.Background
import com.franferreira.enem2027.ui.theme.BackgroundGradientTop
import com.franferreira.enem2027.ui.theme.Secondary
import com.franferreira.enem2027.ui.theme.TextMuted
import com.franferreira.enem2027.ui.theme.TextMutedDark
import com.franferreira.enem2027.ui.theme.TextPrimary
import com.franferreira.enem2027.ui.theme.TextSecondary
import com.franferreira.enem2027.ui.theme.corDaArea

/**
 * Tela da trilha: o caminho vertical com os nós estilo Duolingo.
 *
 * Um nó por etapa. Concluídos ficam verdes, o atual pulsa com halo, os
 * futuros ficam apagados e travados até o anterior ser concluído.
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
                .padding(horizontal = 14.dp, vertical = 10.dp),
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

        Spacer(Modifier.height(4.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, bottom = 28.dp
            ),
        ) {
            item {
                CartaoGamificado {
                    Text(
                        text = trilha.descricao,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                    Spacer(Modifier.height(10.dp))
                    BarraGamificada(
                        progresso = (trilha.progresso / 100.0).toFloat(),
                        cor = cor,
                    )
                }
                Spacer(Modifier.height(18.dp))
            }

            itemsIndexed(trilha.etapas) { indice, etapa ->
                // "Atual" é a primeira etapa que dá para JOGAR (tem questões).
                // Usar a primeira não-concluída trava a trilha: com um ano só
                // baixado, uma etapa sem questões viraria a atual e o usuário
                // não conseguiria passar dela.
                val eAtual = indice == trilha.primeiraJogavel
                val eTravada = !etapa.concluida && !eAtual
                val estado = when {
                    etapa.concluida -> "feito"
                    eAtual -> "atual"
                    else -> "travado"
                }

                // O nó atual pulsa devagar, como o Duolingo.
                val transicao = rememberInfiniteTransition(label = "pulso")
                val escala by transicao.animateFloat(
                    initialValue = 1f,
                    targetValue = if (eAtual) 1.09f else 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1100),
                        repeatMode = RepeatMode.Reverse,
                    ),
                    label = "escala",
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // coluna do nó + conector
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.scale(escala),
                            contentAlignment = Alignment.Center,
                        ) {
                            NoTrilha(estado = estado, cor = cor) {
                                when (estado) {
                                    "feito" -> Icon(
                                        Icons.Filled.Check,
                                        contentDescription = "Concluída",
                                        tint = Color(0xFF06231A),
                                        modifier = Modifier.size(28.dp),
                                    )
                                    "travado" -> Icon(
                                        Icons.Filled.Lock,
                                        contentDescription = "Travada",
                                        tint = TextMutedDark,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    else -> Icon(
                                        Icons.Filled.PlayArrow,
                                        contentDescription = "Jogar",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp),
                                    )
                                }
                            }
                        }
                        if (indice < trilha.etapas.lastIndex) {
                            Conector(
                                concluido = etapa.concluida,
                                cor = cor,
                                modificador = Modifier.padding(top = 4.dp),
                            )
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    // cartão da etapa
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                when {
                                    eAtual -> cor.copy(alpha = 0.12f)
                                    else -> Color(0xFF141821)
                                }
                            )
                            .clickable(enabled = !eTravada) { aoJogar(etapa) }
                            .padding(13.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = etapa.nome,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (eTravada) TextMutedDark else TextPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            if (etapa.concluida) {
                                Spacer(Modifier.width(6.dp))
                                Pilula(
                                    texto = etapa.placar,
                                    cor = Secondary,
                                    fundo = Secondary.copy(alpha = 0.16f),
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        if (etapa.concluida) {
                            Text(
                                text = "${etapa.tempo}  ·  +${etapa.xp} XP",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                            )
                        } else {
                            Text(
                                text = if (eAtual) {
                                    "${etapa.questoes.size} questões · pronta para começar"
                                } else {
                                    "${etapa.questoes.size} questões · conclua a anterior"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (eAtual) cor else TextMutedDark,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
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
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(listOf(cor.copy(alpha = 0.16f), Color(0xFF141821))))
            .padding(20.dp),
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
        Spacer(Modifier.height(18.dp))
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
                .height(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.horizontalGradient(listOf(cor, cor.copy(alpha = 0.7f))))
                .clickable { aoFechar() },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Continuar",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
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
