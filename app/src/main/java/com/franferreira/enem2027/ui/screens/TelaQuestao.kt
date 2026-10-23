package com.franferreira.enem2027.ui.screens

import com.franferreira.enem2027.ui.theme.Background
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.franferreira.enem2027.data.model.Question
import com.franferreira.enem2027.data.repo.CanaisVideo
import com.franferreira.enem2027.data.repo.RascunhoStore
import com.franferreira.enem2027.ui.components.TelaRascunho
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
    aoResponder: (correta: Boolean, segundos: Float?, confianca: String?) -> Unit,
    aoVoltar: () -> Unit,
    aoPedirExplicacao: suspend () -> String?,
) {
    var respondida by remember { mutableStateOf(false) }
    var alternativaEscolhida by remember { mutableStateOf<String?>(null) }
    var explicacao by remember { mutableStateOf<String?>(null) }
    var carregandoExplicacao by remember { mutableStateOf(false) }
    var erroExplicacao by remember { mutableStateOf<String?>(null) }
    /**
     * A confiança que a pessoa tem na própria resposta: "sei" ou "palpite".
     *
     * É técnica de estudo clássica — responder no chute e ver depois que
     * errou é o que revela o buraco real. E vira sinal que o app não tinha.
     */
    var confianca by remember { mutableStateOf<String?>(null) }
    /** O papel digital: o rascunho onde a conta de matemática acontece. */
    var mostrarRascunho by remember { mutableStateOf(false) }
    // `contexto` já é o texto de apoio da questão, dentro do LazyColumn.
    // O contexto do Android precisa de outro nome.
    val app = LocalContext.current
    val vibracao = androidx.compose.ui.platform.LocalHapticFeedback.current
    var tracos by remember(questao.id) {
        mutableStateOf(RascunhoStore.carregar(app, questao.id))
    }
    val inicio = remember { System.currentTimeMillis() }
    val scope = rememberCoroutineScope()

    // Grava o rascunho pouco depois do último traço: um crash no meio não
    // pode levar a conta embora.
    LaunchedEffect(tracos) {
        if (tracos.isEmpty()) return@LaunchedEffect
        delay(600)
        RascunhoStore.salvar(app, questao.id, tracos)
    }

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

            // figuras
            if (questao.arquivos.isNotEmpty()) {
                item {
                    Column {
                        questao.arquivos.forEach { url ->
                            AsyncImage(
                                model = com.franferreira.enem2027.data.repo.ImagensAcervo.arquivoLocal(app, url) ?: url,
                                contentDescription = "Figura da questão",
                                contentScale = ContentScale.Fit,
placeholder = ColorPainter(SurfaceRaised),
error = ColorPainter(SurfaceRaised),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(16.dp))
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

            // ---- confiança, papel e vídeo-aula: as três pontas para quando trava
            item {
                LinhaApoio(
                    confianca = confianca,
                    aoMudarConfianca = { novo ->
                        confianca = if (confianca == novo) null else novo
                    },
                    temRascunho = tracos.isNotEmpty(),
                    aoAbrirRascunho = { mostrarRascunho = true },
                    canais = CanaisVideo.daArea(questao.area),
                    aoAbrirCanal = { url ->
                        runCatching {
                            app.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            )
                        }
                    },
                )
                Spacer(Modifier.height(14.dp))
            }

            // alternativas
            items(questao.alternativas, key = { it.letra }) { alternativa ->
                // O gabarito oficial é a fonte da verdade. O campo `correta`
                // que vem no acervo é fallback (para questão de IA, onde
                // não há gabarito oficial).
                val correta = if (gabarito != null) {
                    alternativa.letra.equals(gabarito, ignoreCase = true)
                } else {
                    alternativa.correta
                }
                val escolhida = alternativaEscolhida == alternativa.letra
                val mostrarCerto = respondida && correta
                val mostrarErrado = respondida && escolhida && !correta

                val corFundo = when {
                    mostrarCerto -> Secondary.copy(alpha = 0.12f)
                    mostrarErrado -> Danger.copy(alpha = 0.12f)
                    escolhida -> Primary.copy(alpha = 0.10f)
                    else -> Surface
                }
                val corBorda = when {
                    mostrarCerto -> Secondary
                    mostrarErrado -> Danger
                    escolhida -> Primary
                    else -> SurfaceBorder
                }
                val corTexto = when {
                    mostrarErrado -> TextPrimary
                    else -> TextSecondary
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(corFundo)
                        .border(1.5.dp, corBorda, RoundedCornerShape(18.dp))
                        .clickable(enabled = !respondida) {
                            alternativaEscolhida = alternativa.letra
                            respondida = true
                            // Mesmo critério da tela: gabarito oficial quando
                            // existe, senão o campo `correta` do acervo. Sem
                            // isto, questão sem gabarito aparecia verde e
                            // entrava no SRS como erro.
                            val corretaEscolhida = if (gabarito != null) {
                                alternativa.letra.equals(gabarito, ignoreCase = true)
                            } else {
                                alternativa.correta
                            }
                            if (corretaEscolhida) {
                                vibracao.performHapticFeedback(
                                    androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress
                                )
                            }
                            aoResponder(corretaEscolhida, (System.currentTimeMillis() - inicio) / 1000f, confianca)
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
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
                    val textoAlternativa = alternativa.texto
                    if (textoAlternativa.isNotBlank()) {
                        Text(
                            text = textoAlternativa,
                            style = MaterialTheme.typography.bodyMedium,
                            color = corTexto,
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        // A alternativa é uma figura, não texto. A imagem vem
                        // no campo da alternativa; se não vier, o app mostra
                        // o aviso e a figura da questão (que já está acima).
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "Alternativa em imagem",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMutedDark,
                            )
                            alternativa.arquivo?.let { url ->
                                Spacer(Modifier.height(6.dp))
                                AsyncImage(
                                    model = com.franferreira.enem2027.data.repo.ImagensAcervo.arquivoLocal(app, url) ?: url,
                                    contentDescription = "Alternativa ${alternativa.letra}",
                                    contentScale = ContentScale.Fit,
placeholder = ColorPainter(SurfaceRaised),
error = ColorPainter(SurfaceRaised),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(SurfaceRaised),
                                )
                            }
                        }
                    }
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
                    } else if (explicacao == null && questao.temExplicacao) {
                        // A explicação offline veio com o acervo: mostra na hora,
                        // sem esperar a IA.
                        Text(
                            text = "Por que essa é a resposta?",
                            style = MaterialTheme.typography.labelMedium,
                            color = com.franferreira.enem2027.ui.theme.Primary,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = questao.explicacao!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                        )
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

        // O papel digital, em janela própria: dá espaço para a conta sem
        // esconder a questão que está sendo feita.
        if (mostrarRascunho) {
            Dialog(
                onDismissRequest = { mostrarRascunho = false },
                properties = DialogProperties(usePlatformDefaultWidth = false),
            ) {
                TelaRascunho(
                    tracos = tracos,
                    aoMudar = { tracos = it },
                    aoFechar = { mostrarRascunho = false },
                )
            }
        }
    }
}

/**
 * A linha de apoio antes das alternativas: confiança, papel digital e
 * vídeo-aula.
 *
 * Três saídas para o momento em que a pessoa trava — e é exatamente o
 * momento em que mais rende aprender.
 */
@Composable
private fun LinhaApoio(
    confianca: String?,
    aoMudarConfianca: (String) -> Unit,
    temRascunho: Boolean,
    aoAbrirRascunho: () -> Unit,
    canais: List<CanaisVideo.Canal>,
    aoAbrirCanal: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Surface)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(18.dp))
            .padding(12.dp),
    ) {
        Text(
            text = "SUA CONFIANÇA NESTA RESPOSTA",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
        )
        Spacer(Modifier.height(7.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            ChipEscolha(
                texto = "SEI",
                selecionado = confianca == "alta",
                aoClicar = { aoMudarConfianca("alta") },
            )
            ChipEscolha(
                texto = "PALPITE",
                selecionado = confianca == "baixa",
                aoClicar = { aoMudarConfianca("baixa") },
            )
            Spacer(Modifier.width(3.dp))
            // o papel digital
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(13.dp))
                    .background(
                        if (temRascunho) Primary.copy(alpha = 0.16f) else SurfaceRaised
                    )
                    .clickable { aoAbrirRascunho() }
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = null,
                    tint = if (temRascunho) Primary else TextMuted,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (temRascunho) "RASCUNHO" else "FAZER RASCUNHO",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (temRascunho) Primary else TextMuted,
                )
            }
        }

        // a vídeo-aula de quem explica o assunto
        if (canais.isNotEmpty()) {
            Spacer(Modifier.height(11.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.OndemandVideo,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(13.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "VIDEO-AULA DO TEMA",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                )
            }
            Spacer(Modifier.height(7.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                canais.take(3).forEach { canal ->
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(13.dp))
                            .background(SurfaceRaised)
                            .clickable { aoAbrirCanal(canal.url) }
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                    ) {
                        Text(
                            text = canal.nome,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChipEscolha(
    texto: String,
    selecionado: Boolean,
    aoClicar: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(13.dp))
            .background(
                if (selecionado) Primary.copy(alpha = 0.18f) else SurfaceRaised
            )
            .border(
                width = 1.dp,
                color = if (selecionado) Primary.copy(alpha = 0.55f) else SurfaceBorder,
                shape = RoundedCornerShape(13.dp),
            )
            .clickable { aoClicar() }
            .padding(horizontal = 12.dp, vertical = 7.dp),
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall,
            color = if (selecionado) Primary else TextMuted,
        )
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
            .clip(RoundedCornerShape(14.dp))
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
