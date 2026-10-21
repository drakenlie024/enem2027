package com.franferreira.enem2027.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.franferreira.enem2027.data.model.Question
import com.franferreira.enem2027.ui.theme.Danger
import com.franferreira.enem2027.ui.theme.Secondary
import com.franferreira.enem2027.ui.theme.Surface
import com.franferreira.enem2027.ui.theme.SurfaceBorder
import com.franferreira.enem2027.ui.theme.SurfaceRaised
import com.franferreira.enem2027.ui.theme.TextMuted
import com.franferreira.enem2027.ui.theme.TextMutedDark
import com.franferreira.enem2027.ui.theme.TextPrimary
import com.franferreira.enem2027.ui.theme.TextSecondary
import kotlinx.coroutines.launch

/**
 * Tela de revisão: a questão, as alternativas e o feedback.
 *
 * O gabarito vem da fonte oficial (Inep) — o app só mostra. A IA entra
 * depois, para explicar.
 */
@Composable
fun TelaQuestao(
    questao: Question,
    aoResponder: (correta: Boolean, segundos: Float?) -> Unit,
    aoVoltar: () -> Unit,
    aoPedirExplicacao: suspend () -> String?,
) {
    varRespondida by remember { mutableStateOf(false) }
    var alternativaEscolhida by remember { mutableStateOf<String?>(null) }
    var explicacao by remember { mutableStateOf<String?>(null) }
    var carregandoExplicacao by remember { mutableStateOf(false) }
    var erroExplicacao by remember { mutableStateOf<String?>(null) }
    val inicio = remember { System.currentTimeMillis() }
    val scope = rememberCoroutineScope()

    val gabarito = questao.gabarito?.trim()?.uppercase()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(com.franferreira.enem2027.ui.theme.BackgroundGradientTop, com.franferreira.enem2027.ui.theme.Background)
                )
            ),
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
                    text = "${questao.ano} · ${questao.temaExibicao}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted,
                )
                if (questao.oficial) {
                    Text(
                        text = "Gabarito oficial do Inep",
                        style = MaterialTheme.typography.labelSmall,
                        color = Secondary.copy(alpha = 0.8f),
                    )
                } else {
                    Text(
                        text = "Gerada por IA · aguardando sua aprovação",
                        style = MaterialTheme.typography.labelSmall,
                        color = Danger.copy(alpha = 0.85f),
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        ) {
            // texto de apoio
            val contexto = questao.contexto?.trim()
            if (!contexto.isNullOrBlank() && contexto.length > 3) {
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
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceRaised),
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }

            // enunciado
            questao.enunciado?.takeIf { it.isNotBlank() }?.let { enunciado ->
                item {
                    Text(
                        text = enunciado,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary,
                    )
                    Spacer(Modifier.height(14.dp))
                }
            }

            // alternativas
            items(questao.alternativas, key = { it.letra }) { alternativa ->
                val correta = alternativa.correta
                val escolhida = alternativaEscolhida == alternativa.letra
                val mostrarCerto = Respondida && correta
                val mostrarErrado = Respondida && escolhida && !correta

                val corFundo = when {
                    mostrarCerto -> Secondary.copy(alpha = 0.12f)
                    mostrarErrado -> Danger.copy(alpha = 0.12f)
                    escolhida -> SurfaceRaised
                    else -> Surface
                }
                val corBorda = when {
                    mostrarCerto -> Secondary.copy(alpha = 0.6f)
                    mostrarErrado -> Danger.copy(alpha = 0.6f)
                    else -> SurfaceBorder
                }
                val corTexto = when {
                    mostrarErrado -> TextPrimary
                    else -> TextSecondary
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(corFundo)
                        .border(1.dp, corBorda, RoundedCornerShape(12.dp))
                        .clickable(enabled = !respondida) {
                            alternativaEscolhida = alternativa.letra
                            respondida = true
                            val corretaEscolhida = gabarito != null &&
                                alternativa.letra.equals(gabarito, ignoreCase = true)
                            aoResponder(corretaEscolhida, (System.currentTimeMillis() - inicio) / 1000f)
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
                                    mostrarErrado -> Danger
                                    else -> SurfaceRaised
                                }
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = alternativa.letra,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (mostrarCerto || mostrarErrado) com.franferreira.enem2027.ui.theme.Background else TextMuted,
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = alternativa.texto,
                        style = MaterialTheme.typography.bodyMedium,
                        color = corTexto,
                        modifier = Modifier.weight(1f),
                    )
                    if (mostrarCerto) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = "Correta",
                            tint = Secondary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        // rodapé: explicação via IA
        if (respondida) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .padding(16.dp),
            ) {
                Column {
                    if (carregandoExplicacao) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = com.franferreira.enem2027.ui.theme.Primary,
                                strokeWidth = 2.dp,
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Explicando…",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                            )
                        }
                    } else if (erroExplicacao != null) {
                        Text(
                            text = erroExplicacao!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = Danger,
                        )
                        Spacer(Modifier.height(8.dp))
                        BotaoExplicacao(texto = "Tentar de novo", carregando = false) {
                            carregandoExplicacao = true
                            erroExplicacao = null
                            scope.launch {
                                runCatching { aoPedirExplicacao() }
                                    .onSuccess { explicacao = it }
                                    .onFailure { erroExplicacao = "Não consegui falar com o servidor. Tente mais tarde." }
                                carregandoExplicacao = false
                            }
                        }
                    } else if (explicacao == null) {
                        BotaoExplicacao(texto = "Por que essa é a resposta?", carregando = false) {
                            carregandoExplicacao = true
                            scope.launch {
                                runCatching { aoPedirExplicacao() }
                                    .onSuccess { explicacao = it }
                                    .onFailure { erroExplicacao = "Não consegui falar com o servidor. Tente mais tarde." }
                                carregandoExplicacao = false
                            }
                        }
                    } else {
                        Text(
                            text = "Explicação",
                            style = MaterialTheme.typography.labelMedium,
                            color = com.franferreira.enem2027.ui.theme.Primary,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = explicacao!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BotaoExplicacao(
    texto: String,
    carregando: Boolean,
    aoClicar: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(com.franferreira.enem2027.ui.theme.Primary)
            .clickable(enabled = !carregando) { aoClicar() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
        )
    }
}
