package com.franferreira.enem2027.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.franferreira.enem2027.data.model.DesempenhoArea
import com.franferreira.enem2027.data.model.PosicaoFaixa
import com.franferreira.enem2027.data.model.PrevisaoNota
import com.franferreira.enem2027.data.model.Previsor
import com.franferreira.enem2027.data.repo.DadosTri
import com.franferreira.enem2027.ui.components.BarraGamificada
import com.franferreira.enem2027.ui.components.CartaoGamificado
import com.franferreira.enem2027.ui.theme.Background
import com.franferreira.enem2027.ui.theme.BackgroundGradientTop
import com.franferreira.enem2027.ui.theme.Surface
import com.franferreira.enem2027.ui.theme.SurfaceBorder
import com.franferreira.enem2027.ui.theme.SurfaceRaised
import com.franferreira.enem2027.ui.theme.TextMuted
import com.franferreira.enem2027.ui.theme.TextMutedDark
import com.franferreira.enem2027.ui.theme.TextPrimary
import com.franferreira.enem2027.ui.theme.TextSecondary
import com.franferreira.enem2027.ui.theme.corDaArea

/**
 * Tela "Nota": onde o usuário está em nota, e o que falta para a nota-alvo.
 *
 * A estimativa sai das medianas de acerto→nota dos microdados do Inep. É uma
 * ordem de grandeza — o TRI oficial usa a TRI bruta de cada participante, que
 * o app não tem. A tela diz isso na cara, porque promessa de nota é a forma
 * mais rápida de perder a confiança de quem estuda.
 */
@Composable
fun TelaNota(
    previsao: PrevisaoNota,
    aoTrocarAno: (Int) -> Unit,
    aoTrocarAlvo: (Int) -> Unit,
    aoVoltar: () -> Unit,
) {
    val scroll = rememberScrollState()

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
            Text(
                text = "SUA NOTA",
                style = MaterialTheme.typography.labelSmall,
                color = com.franferreira.enem2027.ui.theme.Primary,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .verticalScrollCompat(),
        ) {
            Spacer(Modifier.height(6.dp))

            // ---- a nota estimada
            CartaoGamificado {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "NOTA ESTIMADA",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = previsao.notaMedia?.toString() ?: "—",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary,
                        )
                        Text(
                            text = if (previsao.respondidas == 0) {
                                "responda questões para estimar"
                            } else {
                                "em ${previsao.respondidas} questões respondidas"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "META ${previsao.notaAlvo}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                        )
                        Spacer(Modifier.height(4.dp))
                        val faltam = previsao.notaMedia?.let { previsao.notaAlvo - it }
                        Text(
                            text = if (faltam != null && faltam > 0) {
                                "faltam ~$faltam pontos"
                            } else {
                                "no alvo"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = if (faltam != null && faltam > 0) {
                                com.franferreira.enem2027.ui.theme.Warning
                            } else {
                                com.franferreira.enem2027.ui.theme.Secondary
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- alvo
            Text(
                text = "NOTA-ALVO",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.horizontalScroll(scroll),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                listOf(500, 600, 650, 700, 750, 800, 900).forEach { nota ->
                    Chip(
                        texto = nota.toString(),
                        selecionado = previsao.notaAlvo == nota,
                        aoClicar = { aoTrocarAlvo(nota) },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- ano de referência do corte
            Text(
                text = "COTE DE ${previsao.anoAlvo}",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                DadosTri.anosComCorte.forEach { ano ->
                    Chip(
                        texto = ano.toString(),
                        selecionado = previsao.anoAlvo == ano,
                        aoClicar = { aoTrocarAno(ano) },
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // ---- por área
            Text(
                text = "POR ÁREA",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
            Spacer(Modifier.height(8.dp))

            previsao.areas.forEach { area ->
                CartaoArea(
                    area = area,
                    posicao = Previsor.posicaoNaFaixa(area.area, area.notaEstimada, previsao.anoAlvo),
                    ano = previsao.anoAlvo,
                )
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(10.dp))

            // ---- o aviso honesto
            Text(
                text = "Estimativa baseada nas medianas de acerto→nota do Inep " +
                    "(ENEM 2025) e nas faixas de nota apuradas na UFSC. " +
                    "O TRI oficial usa a TRI bruta de cada participante — " +
                    "este número é uma referência de estudo, não uma previsão.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMutedDark,
            )

            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun CartaoArea(
    area: DesempenhoArea,
    posicao: PosicaoFaixa,
    ano: Int,
) {
    val cor = corDaArea(area.area)
    val faixa = DadosTri.faixaDoAno(ano, area.area)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .padding(13.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(cor),
            )
            Spacer(Modifier.width(9.dp))
            Text(
                text = area.nome,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = area.notaEstimada?.toString() ?: "—",
                style = MaterialTheme.typography.titleLarge,
                color = cor,
            )
        }

        Spacer(Modifier.height(8.dp))

        // cobertura: quanto da área você já respondeu
        Text(
            text = "${area.acertos} de ${area.respondidas} respondidas  ·  " +
                "${area.taxa}% de acerto  ·  ${(area.cobertura * 100).toInt()}% da área",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
        )

        Spacer(Modifier.height(8.dp))
        BarraGamificada(progresso = area.cobertura, cor = cor, altura = 5)

        Spacer(Modifier.height(8.dp))

        // o que falta para a nota-alvo
        val faltam = area.acertosFaltamParaAlvo
        Text(
            text = when {
                area.respondidas < 5 -> "responda mais 5 questões para estimar"
                faltam == null -> ""
                faltam == 0 -> "você já bateu a mediana de $notaAlvo nesta área"
                else -> "faltam $faltam acertos para a mediana de ${area.acertosParaAlvo} ($notaAlvo)"
            },
            style = MaterialTheme.typography.labelSmall,
            color = if (faltam != null && faltam > 0) com.franferreira.enem2027.ui.theme.Warning else TextMuted,
        )

        // onde a nota cai na faixa daquele ano
        if (faixa != null && area.notaEstimada != null) {
            Spacer(Modifier.height(6.dp))
            val corPosicao = when (posicao) {
                PosicaoFaixa.ACIMA -> com.franferreira.enem2027.ui.theme.Secondary
                PosicaoFaixa.DENTRO -> com.franferreira.enem2027.ui.theme.Primary
                PosicaoFaixa.ABAIXO -> com.franferreira.enem2027.ui.theme.Danger
                PosicaoFaixa.DESCONHECIDA -> TextMutedDark
            }
            Text(
                text = "corte de $ano: ${faixa.first.toInt()}–${faixa.second.toInt()}  ·  " +
                    "você está ${posicao.rotulo}",
                style = MaterialTheme.typography.labelSmall,
                color = corPosicao,
            )
        }
    }
}

@Composable
private fun Chip(texto: String, selecionado: Boolean, aoClicar: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selecionado) com.franferreira.enem2027.ui.theme.Primary else SurfaceRaised)
            .border(
                1.dp,
                if (selecionado) com.franferreira.enem2027.ui.theme.Primary else SurfaceBorder,
                CircleShape,
            )
            .clickable { aoClicar() }
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            color = if (selecionado) Background else TextSecondary,
        )
    }
}

/** `verticalScroll` sem o import repetido nas assinaturas. */
@Composable
private fun Modifier.verticalScrollCompat(): Modifier =
    this.then(androidx.compose.foundation.verticalScroll(rememberScrollState()))
