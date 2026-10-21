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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.franferreira.enem2027.ui.EstadoHoje
import com.franferreira.enem2027.ui.theme.Background
import com.franferreira.enem2027.ui.theme.BackgroundGradientTop
import com.franferreira.enem2027.ui.theme.Primary
import com.franferreira.enem2027.ui.theme.Secondary
import com.franferreira.enem2027.ui.theme.Surface
import com.franferreira.enem2027.ui.theme.SurfaceBorder
import com.franferreira.enem2027.ui.theme.SurfaceRaised
import com.franferreira.enem2027.ui.theme.TextMuted
import com.franferreira.enem2027.ui.theme.TextMutedDark
import com.franferreira.enem2027.ui.theme.TextPrimary
import com.franferreira.enem2027.ui.theme.TextSecondary

/**
 * Tela "Hoje": o que estudar agora, com os filtros de área e tema.
 *
 * O app é offline-first: esta tela lê do banco local. Se não há nada
 * baixado ainda, ela mostra o caminho para baixar.
 */
@Composable
fun TelaHoje(
    estado: EstadoHoje,
    aoFiltrarArea: (String?) -> Unit,
    aoFiltrarTema: (String?) -> Unit,
    aoBaixarAno: (Int) -> Unit,
    aoSincronizar: () -> Unit,
    aoAbrirQuestao: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(BackgroundGradientTop, Background))
            ),
    ) {
        Column(Modifier.padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "SESSÃO DE HOJE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary,
                    )
                    Text(
                        text = "Bom dia, Letícia",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.linearGradient(listOf(Primary, Secondary))),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "FR",
                        style = MaterialTheme.typography.labelLarge,
                        color = Background,
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // faixa de progresso
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Surface)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ColunaStat(
                        valor = "${estado.pendentes}",
                        rotulo = "para revisar",
                        modifier = Modifier.weight(1f),
                    )
                    ColunaStat(
                        valor = "${estado.streak}",
                        rotulo = "dias seguidos",
                        modifier = Modifier.weight(1f),
                    )
                    ColunaStat(
                        valor = "${estado.xp}",
                        rotulo = "XP · nível ${estado.nivel}",
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // filtros de área
            Text(
                text = "ÁREA",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ChipFiltro(
                    texto = "Todas",
                    selecionado = estado.areaSelecionada == null,
                    aoClicar = { aoFiltrarArea(null) },
                )
                estado.areas.forEach { area ->
                    ChipFiltro(
                        texto = area.replace('-', ' ').replaceFirstChar { it.uppercase() },
                        selecionado = estado.areaSelecionada == area,
                        aoClicar = { aoFiltrarArea(area) },
                    )
                }
            }

            if (estado.temas.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "TEMA",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ChipFiltro(
                        texto = "Todos",
                        selecionado = estado.temaSelecionado == null,
                        aoClicar = { aoFiltrarTema(null) },
                    )
                    estado.temas.take(20).forEach { tema ->
                        ChipFiltro(
                            texto = tema,
                            selecionado = estado.temaSelecionado == tema,
                            aoClicar = { aoFiltrarTema(tema) },
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            estado.mensagem?.let { mensagem ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceRaised)
                        .padding(12.dp),
                ) {
                    Text(
                        text = mensagem,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
                Spacer(Modifier.height(12.dp))
            }
        }

        if (estado.carregando) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary, strokeWidth = 2.dp)
            }
        } else if (estado.totalLocal == 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.CloudDownload,
                        contentDescription = null,
                        tint = TextMutedDark,
                        modifier = Modifier.size(40.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Baixe o acervo para começar",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "As questões ficam salvas no aparelho: você estuda sem internet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(2023, 2022, 2021, 2020, 2019).forEach { ano ->
                            BotaoAno(ano, estado.sincronizando) { aoBaixarAno(ano) }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp, end = 16.dp, bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "NA FILA (${estado.sessao.size})",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = "Sincronizar",
                            tint = if (estado.sincronizando) TextMutedDark else Secondary,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { aoSincronizar() },
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                }

                items(estado.sessao, key = { it.id }) { questao ->
                    CartaoQuestao(questao = questao, aoClicar = { aoAbrirQuestao(estado.sessao.indexOf(questao)) })
                }

                if (estado.sessao.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Surface)
                                .padding(20.dp),
                        ) {
                            Text(
                                text = "Nada pendente por aqui. Troque o filtro ou baixe outro ano.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColunaStat(valor: String, rotulo: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = valor,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
        )
        Text(
            text = rotulo,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
        )
    }
}

@Composable
fun ChipFiltro(texto: String, selecionado: Boolean, aoClicar: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selecionado) Primary.copy(alpha = 0.18f) else Surface)
            .border(
                1.dp,
                if (selecionado) Primary.copy(alpha = 0.5f) else SurfaceBorder,
                CircleShape,
            )
            .clickable { aoClicar() }
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            color = if (selecionado) TextPrimary else TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun BotaoAno(ano: Int, carregando: Boolean, aoClicar: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (carregando) SurfaceRaised else Primary)
            .clickable(enabled = !carregando) { aoClicar() }
            .padding(horizontal = 14.dp, vertical = 9.dp),
    ) {
        Text(
            text = ano.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = if (carregando) TextMuted else Background,
        )
    }
}

@Composable
private fun CartaoQuestao(
    questao: com.franferreira.enem2027.data.model.Question,
    aoClicar: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
            .clickable { aoClicar() }
            .padding(13.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${questao.ano} · ${questao.temaExibicao}",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!questao.oficial) {
                Text(
                    text = "IA",
                    style = MaterialTheme.typography.labelSmall,
                    color = com.franferreira.enem2027.ui.theme.Danger,
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = questao.enunciado?.take(140) ?: "(enunciado em imagem)",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
