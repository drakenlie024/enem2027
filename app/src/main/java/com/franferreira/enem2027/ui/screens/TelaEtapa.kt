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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
@Composable
fun TelaEtapa(
    etapa: Etapa,
    area: String,
    aoResponder: (questionId: String, correta: Boolean, segundos: Float?) -> Unit,
    aoConcluir: (acertos: Int, segundos: Int) -> Unit,
    aoVoltar: () -> Unit,
) {
    var indice by remember { mutableStateOf(0) }
    var escolhida by remember { mutableStateOf<String?>(null) }
    var respondida by remember { mutableStateOf(false) }
    var acertos by remember { mutableStateOf(0) }
    val inicio = remember { System.currentTimeMillis() }
    val scope = rememberCoroutineScope()

    val questao: Question? = etapa.questoes.getOrNull(indice)
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
                            if (etapa.questoes.isEmpty()) 0f
                            else (indice + 1f) / etapa.questoes.size
                        )
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(cor),
                )
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
                                    .clip(RoundedCornerShape(12.dp))
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
                val escolhida = escolhida == alternativa.letra
                val mostrarCerto = respondida && correta
                val mostrarErrado = respondida && escolhida && !correta

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                mostrarCerto -> Secondary.copy(alpha = 0.12f)
                                mostrarErrado -> com.franferreira.enem2027.ui.theme.Danger.copy(alpha = 0.12f)
                                escolhida -> SurfaceRaised
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
                            RoundedCornerShape(12.dp),
                        )
                        .clickable(enabled = !respondida) {
                            escolhida = alternativa.letra
                            respondida = true
                            val acertou = gabarito != null &&
                                alternativa.letra.equals(gabarito, ignoreCase = true)
                            if (acertou) acertos += 1
                            aoResponder(
                                questao.id,
                                acertou,
                                (System.currentTimeMillis() - inicio) / 1000f,
                            )
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
                            .clip(RoundedCornerShape(12.dp))
                            .background(Surface)
                            .border(1.dp, Primary.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (indice == 0) SurfaceRaised else SurfaceRaised)
                    .clickable(enabled = indice > 0) {
                        indice -= 1
                        escolhida = null
                        respondida = false
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (respondida) cor else SurfaceRaised)
                    .clickable {
                        if (!respondida) {
                            // Sem resposta ainda: não deixa avançar.
                            return@clickable
                        }
                        if (indice < etapa.questoes.lastIndex) {
                            indice += 1
                            escolhida = null
                            respondida = false
                            inicio = System.currentTimeMillis()
                        } else {
                            val gasto =
                                ((System.currentTimeMillis() - inicio) / 1000).toInt()
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
