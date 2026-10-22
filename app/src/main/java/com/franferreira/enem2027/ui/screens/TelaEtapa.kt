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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.franferreira.enem2027.data.model.Etapa
import com.franferreira.enem2027.data.model.Question
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
import com.franferreira.enem2027.ui.theme.corDaArea
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

/**
 * Tela da etapa: as 12 questões de um tema, uma de cada vez.
 *
 * Diferente do simulado (que é prova com tempo e folha de respostas), aqui
 * não há pressa: o usuário responde, vê o gabarito e a explicação offline, e
 * segue. O resultado sai no resumo com os números.
 */
/** O que o usuário respondeu numa questão: a letra e se acertou. */
private data class Resposta(val letra: String, val acertou: Boolean)

/**
 * Serializa questão → resposta para o Bundle.
 *
 * Sem isso, sair da etapa (ou o sistema matar o app) fazia o "Concluir etapa"
 * mandar `acertos = 0`: as respostas já tinham ido para o SRS, mas o placar
 * não sabia de nada.
 */
private val SaverRespondidas: Saver<Map<String, Resposta>, ArrayList<String>> = Saver(
    save = { mapa ->
        ArrayList(
            mapa.map { (id, r) -> "$id|${r.letra}|${if (r.acertou) 1 else 0}" }
        )
    },
    restore = { lista ->
        lista.mapNotNull { linha ->
            val partes = linha.split("|")
            if (partes.size == 3) partes[0] to Resposta(partes[1], partes[2] == "1") else null
        }.toMap()
    },
)

@Composable
fun TelaEtapa(
    etapa: Etapa,
    area: String,
    aoResponder: (questionId: String, correta: Boolean, segundos: Float?) -> Unit,
    aoConcluir: (acertos: Int, segundos: Int) -> Unit,
    aoVoltar: () -> Unit,
    /** Etapa já concluída: é revisão, não dá XP nem mexe no SRS. */
    revisando: Boolean = false,
) {
    var indice by rememberSaveable { mutableIntStateOf(0) }
    var escolhida by rememberSaveable { mutableStateOf<String?>(null) }
    // As respondidas são guardadas por ID, não por estado de tela. Antes, o
    // botão "Anterior" zerava a flag e a questão podia ser respondida de
    // novo — o XP contava duas vezes e o SRS era aplicado duas vezes.
    // O valor carrega a LETRA escolhida, para o botão "Anterior" restaurar
    // a alternativa marcada.
    var respondidas by rememberSaveable(stateSaver = SaverRespondidas) {
        mutableStateOf<Map<String, Resposta>>(emptyMap())
    }
    // `var`: a cada questão o cronômetro reinicia, para medir só o tempo
    // da questão que está na tela.
    var inicio by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }
    // Início da ETAPA: é o que vai para o placar. Misturar com o início da
    // questão fazia a etapa de 20 minutos registrar 15 segundos.
    val inicioEtapa = rememberSaveable { System.currentTimeMillis() }
    val scope = rememberCoroutineScope()

    val questao: Question? = etapa.questoes.getOrNull(indice)
    val respondida = questao?.id?.let { respondidas.containsKey(it) } ?: false
    val acertos = respondidas.count { it.value.acertou }
    val cor = corDaArea(area)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundGradientTop, Background))),
    ) {
        // topo: etapa, progresso e sair
        Column(Modifier.padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Sair",
                    tint = TextMuted,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { aoVoltar() },
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = etapa.nome,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "ETAPA ${indice + 1} DE ${etapa.questoes.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = cor,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            // Pips: um segmento por questão. Verde = respondida e acertada,
            // vermelho = respondida e errada, cor da área = a atual.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                etapa.questoes.forEachIndexed { i, q ->
                    val acertou = respondidas[q.id]?.acertou
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    acertou == true -> Secondary
                                    acertou == false ->
                                        com.franferreira.enem2027.ui.theme.Danger
                                    i == indice -> cor
                                    else -> SurfaceRaised
                                }
                            ),
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        if (questao == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Esta etapa não tem questões no aparelho.\nBaixe o ano correspondente.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                )
            }
            return
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, bottom = 16.dp
            ),
        ) {
            // figuras
            if (questao.arquivos.isNotEmpty()) {
                item {
                    Column {
                        questao.arquivos.forEach { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = "Figura da questão",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SurfaceRaised),
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                }
            }

            // contexto
            questao.contexto?.takeIf { it.isNotBlank() && it.length > 3 }?.let { contexto ->
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Surface)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
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

            // enunciado
            item {
                Text(
                    text = questao.enunciado ?: "(enunciado em imagem)",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(14.dp))
            }

            // alternativas
            itemsIndexed(questao.alternativas) { _, alternativa ->
                val gabarito = questao.gabarito?.trim()?.uppercase()
                val correta = if (gabarito != null) {
                    alternativa.letra.equals(gabarito, ignoreCase = true)
                } else {
                    alternativa.correta
                }
                // `marcada` (e não `escolhida`): o nome sombreava a `var`
                // de fora do lambda, e o compilador recusava a atribuição.
                val marcada = alternativa.letra == escolhida
                val mostrarCerto = respondida && correta
                val mostrarErrado = respondida && marcada && !correta

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            when {
                                mostrarCerto -> Secondary.copy(alpha = 0.12f)
                                mostrarErrado -> com.franferreira.enem2027.ui.theme.Danger.copy(alpha = 0.12f)
                                marcada -> SurfaceRaised
                                else -> Surface
                            }
                        )
                        .border(
                            1.dp,
                            when {
                                mostrarCerto -> Secondary.copy(alpha = 0.6f)
                                mostrarErrado -> com.franferreira.enem2027.ui.theme.Danger.copy(alpha = 0.6f)
                                else -> SurfaceBorder
                            },
                            RoundedCornerShape(16.dp),
                        )
                        .clickable(enabled = !respondida) {
                            escolhida = alternativa.letra
                            val acertou = gabarito != null &&
                                alternativa.letra.equals(gabarito, ignoreCase = true)
                            respondidas = respondidas + (questao.id to Resposta(alternativa.letra, acertou))
                            if (!revisando) {
                                // Em revisão, a resposta NÃO vale XP nem mexe no
                                // SRS: repetir etapa concluída não pode ser XP
                                // de graça.
                                aoResponder(
                                    questao.id,
                                    acertou,
                                    (System.currentTimeMillis() - inicio) / 1000f,
                                )
                            }
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(
                                when {
                                    mostrarCerto -> Secondary
                                    mostrarErrado -> com.franferreira.enem2027.ui.theme.Danger
                                    else -> SurfaceRaised
                                }
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = alternativa.letra,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (mostrarCerto || mostrarErrado) Background else TextMuted,
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = alternativa.texto.ifBlank { "Alternativa em imagem" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            // explicação offline (vem com o acervo)
            if (respondida && questao.temExplicacao) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Surface)
                            .border(1.dp, Primary.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                            .padding(12.dp),
                    ) {
                        Column {
                            Text(
                                text = "POR QUE ESSA É A RESPOSTA",
                                style = MaterialTheme.typography.labelSmall,
                                color = Primary,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = questao.explicacao!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }

        // rodapé: navegação
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (indice == 0) SurfaceRaised else SurfaceRaised)
                    .clickable(enabled = indice > 0) {
                        indice -= 1
                        // A resposta fica guardada por ID: voltar não
                        // "desresponde", só mostra o que já foi feito.
                        escolhida = respondidas[etapa.questoes.getOrNull(indice)?.id]?.letra
                        inicio = System.currentTimeMillis()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Anterior",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (indice > 0) TextPrimary else TextMutedDark,
                )
            }

            Box(
                modifier = Modifier
                    .weight(1.4f)
                    .height(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (respondida) cor else SurfaceRaised)
                    .clickable {
                        if (!respondida) {
                            // Sem resposta ainda: não deixa avançar.
                            return@clickable
                        }
                        if (indice < etapa.questoes.lastIndex) {
                            indice += 1
                            escolhida = respondidas[etapa.questoes.getOrNull(indice)?.id]?.letra
                            inicio = System.currentTimeMillis()
                        } else {
                            // O tempo da ETAPA, não o da última questão.
                            val gasto =
                                ((System.currentTimeMillis() - inicioEtapa) / 1000).toInt()
                            aoConcluir(acertos, gasto)
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (!respondida) {
                        "Responda para seguir"
                    } else if (indice < etapa.questoes.lastIndex) {
                        "Próxima"
                    } else {
                        "Concluir etapa"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = if (respondida) Background else TextMuted,
                )
            }
        }
    }
}
