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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.franferreira.enem2027.data.model.Diagnostico
import com.franferreira.enem2027.data.model.PrevisaoNota
import com.franferreira.enem2027.data.model.Question
import com.franferreira.enem2027.data.model.ResultadoDiagnostico
import com.franferreira.enem2027.data.model.TemaDiagnostico
import com.franferreira.enem2027.ui.components.cartaoRabisco
import com.franferreira.enem2027.ui.theme.Background
import com.franferreira.enem2027.ui.theme.BackgroundGradientTop
import com.franferreira.enem2027.ui.theme.Danger
import com.franferreira.enem2027.ui.theme.PaletaClaro
import com.franferreira.enem2027.ui.theme.Primary
import com.franferreira.enem2027.ui.theme.Secondary
import com.franferreira.enem2027.ui.theme.Surface
import com.franferreira.enem2027.ui.theme.SurfaceBorder
import com.franferreira.enem2027.ui.theme.SurfaceRaised
import com.franferreira.enem2027.ui.theme.TextMuted
import com.franferreira.enem2027.ui.theme.TextPrimary

/** Uma questão do diagnóstico por vez. Sem gabarito na hora: o resultado sai no fim. */
@Composable
fun TelaDiagnostico(
    questao: Question,
    indice: Int,
    total: Int,
    podeSair: Boolean,
    aoSair: () -> Unit,
    aoConfirmar: (String) -> Unit,
) {
    var escolhida by remember(questao.id) { mutableStateOf<String?>(null) }
    val rolagem = rememberScrollState()
    val pronto = escolhida != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundGradientTop, Background)))
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "DIAGNÓSTICO INICIAL",
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary,
                )
                Text(
                    text = "Questão ${indice + 1} de $total",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                )
            }
            if (podeSair) {
                Text(
                    text = "Fechar",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { aoSair() }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(SurfaceRaised),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((indice.toFloat() / total).coerceIn(0f, 1f))
                    .height(6.dp)
                    .background(Primary),
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rolagem)
                .padding(vertical = 14.dp),
        ) {
            Text(
                text = listOfNotNull(
                    questao.area?.let { Diagnostico.nomeDaArea(it) },
                    questao.tema?.takeIf { it.isNotBlank() },
                ).joinToString(" · "),
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
            Spacer(Modifier.height(10.dp))
            questao.contexto?.takeIf { it.isNotBlank() }?.let { contexto ->
                Text(
                    text = contexto,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                )
                Spacer(Modifier.height(12.dp))
            }
            Text(
                text = questao.enunciado.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
            )
            Spacer(Modifier.height(14.dp))
            questao.alternativas.forEach { alternativa ->
                val marcada = escolhida == alternativa.letra
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .cartaoRabisco(14.dp, fundo = if (marcada) Primary.copy(alpha = 0.16f) else Surface, borda = if (marcada) Primary else SurfaceBorder)
                        .clickable { escolhida = alternativa.letra }
                        .padding(12.dp),
                ) {
                    Text(
                        text = "${alternativa.letra})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (marcada) Primary else TextMuted,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = alternativa.texto,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 20.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(if (pronto) Primary else SurfaceRaised)
                .clickable(enabled = pronto) { escolhida?.let(aoConfirmar) }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (indice + 1 == total) "Ver resultado" else "Confirmar",
                style = MaterialTheme.typography.labelLarge,
                color = if (pronto) PaletaClaro.texto else TextMuted,
            )
        }
    }
}

/** O quadro de partida: acerto por área, pontos fortes e o que precisa de treino. */
@Composable
fun TelaResultadoDiagnostico(
    resultado: ResultadoDiagnostico,
    previsao: PrevisaoNota?,
    notaAlvo: Int,
    aoComecar: () -> Unit,
) {
    val rolagem = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundGradientTop, Background)))
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(12.dp))
        Text(
            text = "DIAGNÓSTICO CONCLUÍDO",
            style = MaterialTheme.typography.labelSmall,
            color = Primary,
        )
        Text(
            text = "Seu ponto de partida",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
        )
        Spacer(Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rolagem),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .cartaoRabisco(18.dp)
                    .padding(14.dp),
            ) {
                Text(
                    text = "${resultado.acertos} de ${resultado.total} acertos",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${(resultado.taxa * 100).toInt()}% no total",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                )
            }

            previsao?.notaMedia?.let { media ->
                val (minimo, maximo) = Diagnostico.faixaDaNota(media)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .cartaoRabisco(18.dp, fundo = Surface, borda = Primary.copy(alpha = 0.4f))
                        .padding(14.dp),
                ) {
                    Text(
                        text = "PREVISÃO INICIAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary,
                    )
                    Text(
                        text = "$minimo a $maximo",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                    )
                    Spacer(Modifier.height(4.dp))
                    val falta = notaAlvo - media
                    Text(
                        text = if (falta > 0) {
                            "Faltam cerca de $falta pontos para a sua meta de $notaAlvo."
                        } else {
                            "Você já está na sua meta de $notaAlvo."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Estimativa com 40 questões: é uma faixa, não a nota final.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                    )
                }
            }

            Text(
                text = "POR ÁREA",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
            resultado.areas.forEach { area ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Surface)
                        .padding(12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = Diagnostico.nomeDaArea(area.area),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "${area.acertos}/${area.total}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Primary,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(SurfaceRaised),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(area.taxa.toFloat().coerceIn(0f, 1f))
                                .height(8.dp)
                                .background(Primary),
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            Text(
                text = "PONTOS FORTES",
                style = MaterialTheme.typography.labelSmall,
                color = Secondary,
            )
            ListaTemasDiagnostico(
                temas = resultado.fortes,
                vazio = "Ainda sem tema forte com questões suficientes.",
            )

            Text(
                text = "PRECISA DE TREINO",
                style = MaterialTheme.typography.labelSmall,
                color = Danger,
            )
            ListaTemasDiagnostico(
                temas = resultado.fracos,
                vazio = "Nenhum tema fraco. Bom sinal.",
            )

            if (resultado.semAmostra > 0) {
                Text(
                    text = "${resultado.semAmostra} temas tiveram só uma questão no diagnóstico. " +
                        "Ficaram sem rótulo de forte ou fraco.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 20.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Primary)
                .clickable { aoComecar() }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Começar a estudar",
                style = MaterialTheme.typography.labelLarge,
                color = PaletaClaro.texto,
            )
        }
    }
}

@Composable
private fun ListaTemasDiagnostico(temas: List<TemaDiagnostico>, vazio: String) {
    if (temas.isEmpty()) {
        Text(
            text = vazio,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
        )
        return
    }
    temas.forEach { tema ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Surface)
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = tema.tema,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                )
                Text(
                    text = Diagnostico.nomeDaArea(tema.area),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                )
            }
            Text(
                text = "${tema.acertos}/${tema.total}",
                style = MaterialTheme.typography.labelMedium,
                color = TextPrimary,
            )
        }
    }
}
