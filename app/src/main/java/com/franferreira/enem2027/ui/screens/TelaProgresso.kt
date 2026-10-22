package com.franferreira.enem2027.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.franferreira.enem2027.data.model.AreaStats
import com.franferreira.enem2027.data.model.ProgressoResponse
import com.franferreira.enem2027.ui.theme.Background
import com.franferreira.enem2027.ui.theme.BackgroundGradientTop
import com.franferreira.enem2027.ui.theme.Danger
import com.franferreira.enem2027.ui.theme.Primary
import com.franferreira.enem2027.ui.theme.Secondary
import com.franferreira.enem2027.ui.theme.Surface
import com.franferreira.enem2027.ui.theme.SurfaceBorder
import com.franferreira.enem2027.ui.theme.SurfaceRaised
import com.franferreira.enem2027.ui.theme.TextMuted
import com.franferreira.enem2027.ui.theme.TextMutedDark
import com.franferreira.enem2027.ui.theme.TextPrimary
import com.franferreira.enem2027.ui.theme.TextSecondary
import com.franferreira.enem2027.ui.theme.Warning
import kotlin.math.roundToInt

/**
 * Tela de Progresso: a evolução por área, por competência, o streak e as
 * conquistas.
 *
 * A ideia é que o número não seja o suficiente: o que importa é saber
 * QUAL área está fraca. Por isso o destaque vai na menor taxa de acerto.
 */
@Composable
fun TelaProgresso(
    progresso: ProgressoResponse,
    aoAbrirAgenda: () -> Unit,
    aoAbrirAprovacao: () -> Unit,
    pendentesAprovacao: Int,
) {
    val fraca = progresso.porArea.minByOrNull { it.taxa }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundGradientTop, Background))),
    ) {
        Column(Modifier.padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "PROGRESSO",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary,
                    )
                    Text(
                        text = "Onde você está mandando bem",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Surface)
                        .border(1.dp, SurfaceBorder, CircleShape)
                        .clickable { aoAbrirAgenda() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "Agenda",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // fila de aprovação da IA
            if (pendentesAprovacao > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Primary.copy(alpha = 0.10f))
                        .border(1.dp, Primary.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .clickable { aoAbrirAprovacao() }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Filled.FactCheck,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(17.dp),
                    )
                    Spacer(Modifier.width(9.dp))
                    Text(
                        text = "$pendentesAprovacao questões da IA esperando sua aprovação",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(10.dp))
            }

            // topo: nível, streak, taxa
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CartaoNumero(
                    valor = "${progresso.nivel}",
                    rotulo = "nível",
                    cor = Primary,
                    modifier = Modifier.weight(1f),
                )
                CartaoNumero(
                    valor = "${progresso.streak}",
                    rotulo = "dias seguidos",
                    cor = Secondary,
                    modifier = Modifier.weight(1f),
                )
                CartaoNumero(
                    valor = "${progresso.taxaGeral.roundToInt()}%",
                    rotulo = "acerto geral",
                    cor = if (progresso.taxaGeral >= 70) Secondary else Warning,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(12.dp))

            // gráfico de 7 dias
            CartaoSemana(progresso.porDia)

            // foco
            if (fraca != null && fraca.respondidas >= 3) {
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Primary.copy(alpha = 0.10f))
                        .border(1.dp, Primary.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        .padding(14.dp),
                ) {
                    Column {
                        Text(
                            text = "SEU PONTO DE ATENÇÃO",
                            style = MaterialTheme.typography.labelSmall,
                            color = Primary,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "${areaLegivel(fraca.area)} — ${fraca.taxa.roundToInt()}% de acerto em ${fraca.respondidas} questões",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${fraca.pendentes} questões dessa área estão vencendo. É por aqui que o SRS vai puxar a sua próxima sessão.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (progresso.porArea.isNotEmpty()) {
                item { TituloSecao("POR ÁREA") }
                items(progresso.porArea.size) { i ->
                    LinhaArea(progresso.porArea[i])
                }
            }

            if (progresso.porCompetencia.isNotEmpty()) {
                item { TituloSecao("POR COMPETÊNCIA") }
                items(progresso.porCompetencia.size) { i ->
                    val c = progresso.porCompetencia[i]
                    LinhaCompetencia(
                        rotulo = c.competencia,
                        taxa = c.taxa,
                        respondidas = c.respondidas,
                    )
                }
            }

            if (progresso.conquistas.isNotEmpty()) {
                item { TituloSecao("CONQUISTAS") }
                item { GradeConquistas(progresso.conquistas) }
            }
        }
    }
}

@Composable
private fun CartaoNumero(
    valor: String,
    rotulo: String,
    cor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
    ) {
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

@Composable
private fun CartaoSemana(porDia: List<Int>) {
    val maximo = (porDia.maxOrNull() ?: 0).coerceAtLeast(1)
    val dias = listOf("S", "T", "Q", "Q", "S", "S", "D")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.LocalFireDepartment,
                contentDescription = null,
                tint = Secondary,
                modifier = Modifier.size(15.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "ÚLTIMOS 7 DIAS",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            porDia.forEachIndexed { i, valor ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((6 + 44f * valor / maximo).dp)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(
                                if (valor > 0) Secondary else SurfaceRaised
                            ),
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = dias.getOrElse(i) { "" },
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMutedDark,
                    )
                }
            }
        }
    }
}

@Composable
private fun TituloSecao(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelSmall,
        color = TextMuted,
        modifier = Modifier.padding(top = 6.dp),
    )
}

@Composable
private fun LinhaArea(area: AreaStats) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = areaLegivel(area.area),
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
            )
            Spacer(Modifier.height(4.dp))
            BarraTaxa(area.taxa)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${area.respondidas} respondidas · ${area.acertos} acertos" +
                    if (area.pendentes > 0) " · ${area.pendentes} vencendo" else "",
                style = MaterialTheme.typography.labelSmall,
                color = TextMutedDark,
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = "${area.taxa.roundToInt()}%",
            style = MaterialTheme.typography.titleMedium,
            color = when {
                area.taxa >= 70 -> Secondary
                area.taxa >= 50 -> Warning
                else -> Danger
            },
        )
    }
}

@Composable
private fun BarraTaxa(taxa: Double) {
    val cor = when {
        taxa >= 70 -> Secondary
        taxa >= 50 -> Warning
        else -> Danger
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(5.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(SurfaceRaised),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(taxa.coerceIn(0.0, 100.0).toFloat() / 100f)
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(cor),
        )
    }
}

@Composable
private fun LinhaCompetencia(rotulo: String, taxa: Double, respondidas: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = rotulo,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
            )
            Spacer(Modifier.height(4.dp))
            BarraTaxa(taxa)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "$respondidas respondidas",
                style = MaterialTheme.typography.labelSmall,
                color = TextMutedDark,
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = "${taxa.roundToInt()}%",
            style = MaterialTheme.typography.titleMedium,
            color = if (taxa >= 70) Secondary else Warning,
        )
    }
}

@Composable
private fun GradeConquistas(conquistas: List<String>) {
    // Grade tipo jogo: o troféu conquistado brilha, o que falta fica apagado.
    // Os ids são os mesmos que o backend emite (app/routers/progresso.py).
    val todas = listOf(
        "primeira_questao", "dez_questoes", "cem_questoes", "mil_questoes",
        "sequencia_3", "sequencia_7", "sequencia_30",
        "nivel_5", "nivel_10", "acertos_50", "acertos_200",
        "area_cem", "sete_dias",
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF1A1F2C), Surface)))
            .border(1.dp, Color(0xFF262D3C), RoundedCornerShape(18.dp))
            .padding(13.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            todas.chunked(4).forEach { linha ->
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    linha.forEach { id ->
                        val ganha = id in conquistas
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (ganha) {
                                        Brush.verticalGradient(
                                            listOf(Warning.copy(alpha = 0.22f), Surface)
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            listOf(Color(0xFF141821), Color(0xFF141821))
                                        )
                                    }
                                )
                                .border(
                                    1.dp,
                                    if (ganha) Warning.copy(alpha = 0.4f) else SurfaceBorder,
                                    RoundedCornerShape(12.dp),
                                )
                                .padding(vertical = 9.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                Icons.Filled.EmojiEvents,
                                contentDescription = null,
                                tint = if (ganha) Warning else TextMutedDark,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = rotuloCurtoConquista(id),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (ganha) TextPrimary else TextMutedDark,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                maxLines = 2,
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = "${conquistas.size} de ${todas.size} conquistas",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
        )
    }
}

internal fun areaLegivel(area: String): String = when (area) {
    "matematica" -> "Matemática"
    "linguagens" -> "Linguagens"
    "ciencias-humanas" -> "Ciências Humanas"
    "ciencias-natureza" -> "Ciências da Natureza"
    "sem-area" -> "Sem área"
    else -> area.replace('-', ' ').replaceFirstChar { it.uppercase() }
}

/** Rótulo curto, para caber no quadradinho da grade de conquistas. */
internal fun rotuloCurtoConquista(id: String): String = when (id) {
    "primeira_questao" -> "1ª questão"
    "dez_questoes" -> "10 questões"
    "cem_questoes" -> "100 questões"
    "mil_questoes" -> "1.000 questões"
    "sequencia_3" -> "3 dias"
    "sequencia_7" -> "7 dias"
    "sequencia_30" -> "30 dias"
    "nivel_5" -> "Nível 5"
    "nivel_10" -> "Nível 10"
    "acertos_50" -> "50 acertos"
    "acertos_200" -> "200 acertos"
    "area_cem" -> "100 na área"
    "sete_dias" -> "7 dias seguidos"
    else -> id.replace('_', ' ')
}

internal fun rotuloConquista(id: String): String = when (id) {
    "primeira_questao" -> "Primeira questão respondida"
    "dez_questoes" -> "10 questões respondidas"
    "cem_questoes" -> "100 questões respondidas"
    "mil_questoes" -> "1.000 questões respondidas"
    "sequencia_3" -> "3 dias seguidos"
    "sequencia_7" -> "7 dias seguidos"
    "sequencia_30" -> "30 dias seguidos"
    "nivel_5" -> "Alcançou o nível 5"
    "nivel_10" -> "Alcançou o nível 10"
    "acertos_50" -> "50 acertos"
    "acertos_200" -> "200 acertos"
    "area_cem" -> "100 questões de uma mesma área"
    "sete_dias" -> "Estudou nos 7 dias da semana"
    else -> id.replace('_', ' ')
}
