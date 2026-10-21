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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.franferreira.enem2027.data.model.DiagnosticoSimulado
import com.franferreira.enem2027.data.model.SimuladoMontado
import com.franferreira.enem2027.data.model.SimuladoQuestao
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
import kotlinx.coroutines.delay

/**
 * Tela de simulado: cronômetro, navegação de questão e folha de respostas.
 *
 * O cronômetro é honesto: mostra o tempo que resta, avisa quando está
 * acabando, e o resultado é enviado com o tempo real gasto.
 */
@Composable
fun TelaSimulado(
    simulado: SimuladoMontado,
    aoFinalizar: (respostas: List<Map<String, String>>, duracaoSegundos: Int) -> Unit,
    aoVoltar: () -> Unit,
) {
    var indice by remember { mutableStateOf(0) }
    var respostas by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var segundosRestantes by remember {
        mutableStateOf(simulado.duracaoMinutos * 60)
    }
    var terminado by remember { mutableStateOf(false) }

    LaunchedEffect(simulado.id) {
        while (segundosRestantes > 0 && !terminado) {
            delay(1000)
            segundosRestantes -= 1
        }
        if (segundosRestantes <= 0) {
            terminado = true
            onFinalizar(respostas, simulado.duracaoMinutos * 60 - segundosRestantes)
        }
    }

    val questao: SimuladoQuestao? = simulado.questoes.getOrNull(indice)
    val respondidas = respostas.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundGradientTop, Background))),
    ) {
        // barra de progresso + cronômetro
        Column(Modifier.padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Timer,
                    contentDescription = null,
                    tint = if (segundosRestantes < 300) Danger else TextMuted,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = formatarTempo(segundosRestantes),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (segundosRestantes < 300) Danger else TextPrimary,
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "${respondidas}/${simulado.questoes.size} respondidas",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "Encerrar",
                    style = MaterialTheme.typography.labelMedium,
                    color = Danger,
                    modifier = Modifier.clickable {
                        terminado = true
                        onFinalizar(respostas, simulado.duracaoMinutos * 60 - segundosRestantes)
                    },
                )
            }
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(SurfaceRaised),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(
                            if (simulado.questoes.isEmpty()) 0f
                            else (indice + 1f) / simulado.questoes.size
                        )
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Primary),
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = simulado.label,
                style = MaterialTheme.typography.labelSmall,
                color = TextMutedDark,
            )
            Spacer(Modifier.height(10.dp))
        }

        if (questao == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Simulado sem questões", color = TextMuted)
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, bottom = 12.dp
            ),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "QUESTÃO ${indice + 1} DE ${simulado.questoes.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = listOfNotNull(questao.area, questao.tema)
                            .joinToString(" · ")
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMutedDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(10.dp))
            }

            questao.contexto?.takeIf { it.isNotBlank() && it.length > 3 }?.let { contexto ->
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Surface)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                    ) {
                        Text(
                            text = contexto,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }

            item {
                Text(
                    text = questao.enunciado ?: "(questão com figura)",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(14.dp))
            }

            itemsIndexed(questao.alternativas) { _, alternativa ->
                val escolhida = respostas[questao.id] == alternativa.letra
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (escolhida) Primary.copy(alpha = 0.16f) else Surface)
                        .border(
                            1.dp,
                            if (escolhida) Primary.copy(alpha = 0.5f) else SurfaceBorder,
                            RoundedCornerShape(12.dp),
                        )
                        .clickable {
                            respostas = respostas + (questao.id to alternativa.letra)
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(if (escolhida) Primary else SurfaceRaised),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = alternativa.letra,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (escolhida) Background else TextMuted,
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = alternativa.texto,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        // navegação
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(Surface)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(11.dp))
                    .clickable(enabled = indice > 0) { indice-- },
                contentAlignment = Alignment.Center,
            ) {
                Text("Anterior", style = MaterialTheme.typography.labelLarge, color = TextMuted)
            }
            Box(
                modifier = Modifier
                    .weight(2f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (respondidas == simulado.questoes.size) Primary else SurfaceRaised)
                    .clickable {
                        if (indice < simulado.questoes.size - 1) {
                            indice++
                        } else {
                            terminado = true
                            onFinalizar(respostas, simulado.duracaoMinutos * 60 - segundosRestantes)
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (indice < simulado.questoes.size - 1) "Próxima" else "Finalizar",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (respondidas == simulado.questoes.size) Background else TextSecondary,
                )
            }
        }
    }
}

/**
 * Tela de resultado: a nota e, principalmente, o plano do que revisar.
 */
@Composable
fun TelaResultadoSimulado(
    diagnostico: DiagnosticoSimulado,
    aoFechar: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundGradientTop, Background)))
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            text = "RESULTADO",
            style = MaterialTheme.typography.labelSmall,
            color = Primary,
        )
        Spacer(Modifier.height(6.dp))

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "${diagnostico.acertos}",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
            )
            Text(
                text = " / ${diagnostico.total}  ·  ${diagnostico.taxa.toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }

        Spacer(Modifier.height(18.dp))

        if (diagnostico.porArea.isNotEmpty()) {
            Text(
                text = "POR ÁREA",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
            Spacer(Modifier.height(8.dp))
            diagnostico.porArea.forEach { (area, dados) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = area.replace('-', ' ').replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.weight(1f),
                    )
                    Box(
                        modifier = Modifier
                            .width(90.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(SurfaceRaised),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(dados.taxa.coerceIn(0.0, 100.0).toFloat() / 100f)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (dados.taxa >= 70) Secondary
                                    else if (dados.taxa >= 50) com.franferreira.enem2027.ui.theme.Warning
                                    else Danger
                                ),
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "${dados.taxa.toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary,
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "SEU PRÓXIMO PASSO",
            style = MaterialTheme.typography.labelSmall,
            color = Primary,
        )
        Spacer(Modifier.height(8.dp))
        diagnostico.plano.forEach { passo ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Surface)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Primary),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = passo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Primary)
                .clickable { aoFechar() },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Fechar",
                style = MaterialTheme.typography.labelLarge,
                color = Background,
            )
        }
        Spacer(Modifier.height(28.dp))
    }
}

private fun formatarTempo(segundos: Int): String {
    val minutos = segundos / 60
    val resto = segundos % 60
    return "%02d:%02d".format(minutos, resto)
}
