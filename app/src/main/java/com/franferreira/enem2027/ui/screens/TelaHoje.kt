package com.franferreira.enem2027.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.roundToInt
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.franferreira.enem2027.data.model.Etapa
import com.franferreira.enem2027.data.model.Trilha
import com.franferreira.enem2027.ui.EstadoHoje
import com.franferreira.enem2027.ui.components.BarraGamificada
import com.franferreira.enem2027.ui.components.CartaoGamificado
import com.franferreira.enem2027.ui.components.Pilula
import com.franferreira.enem2027.ui.components.bordaRabisco
import com.franferreira.enem2027.ui.components.cartaoRabisco
import com.franferreira.enem2027.ui.theme.Background
import com.franferreira.enem2027.ui.theme.BackgroundGradientTop
import com.franferreira.enem2027.ui.components.floresFundo
import com.franferreira.enem2027.ui.theme.PaletaAtual
import com.franferreira.enem2027.ui.theme.PaletaClaro
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
import com.franferreira.enem2027.ui.theme.corDaArea

/**
 * Tela "Hoje": uma meta, um botão, e as trilhas.
 *
 * A tela responde a uma pergunta só — "o que eu faço agora?" — e a resposta
 * é o botão grande do anel. As trilhas ficam abaixo, como o mapa do caminho.
 */
/**
 * A saudação conforme a hora do aparelho.
 *
 * "Bom dia" às 22h soava errado — a primeira coisa que a tela mostra é a
 * primeira impressão.
 */
private fun saudacao(): String = when (java.time.LocalTime.now().hour) {
    in 5..11 -> "Bom dia"
    in 12..17 -> "Boa tarde"
    else -> "Boa noite"
}

@Composable
fun TelaHoje(
    estado: EstadoHoje,
    aoFiltrarArea: (String?) -> Unit,
    aoFiltrarTema: (String?) -> Unit,
    aoBaixarAno: (Int) -> Unit,
    aoSincronizar: () -> Unit,
    aoAbrirQuestao: (Int) -> Unit,
    aoAbrirTrilha: (Trilha) -> Unit,
    aoJogarEtapa: (Trilha, Etapa) -> Unit,
    aoAjustarMeta: (Int) -> Unit,
    aoAtualizarAcervo: () -> Unit,
    aoIniciarFoco: () -> Unit = {},
    aoPararFoco: () -> Unit = {},
    aoRevisar: () -> Unit = {},
    aoAulas: () -> Unit = {},
    aoMudarMetaSemanal: (Int) -> Unit = {},
    aoFazerDiagnostico: () -> Unit = {},
    aoAbrirCronograma: () -> Unit = {},
    aoAbrirAula: (String) -> Unit = {},
    aoAbrirPreferencias: () -> Unit = {},
    aoFecharPerguntaPreferencias: () -> Unit = {},
    aoTentarAcervo: () -> Unit = {},
    aoAbrirUrl: (String) -> Unit = {},
) {
    if (estado.pedirPreferencias) {
        // Conta antiga, sem preferência: pergunta uma vez. Responder ou dispensar
        // marca como feito, então não volta.
        AlertDialog(
            onDismissRequest = aoFecharPerguntaPreferencias,
            containerColor = Surface,
            titleContentColor = TextPrimary,
            textContentColor = TextMuted,
            title = { Text("Quais matérias você prefere?") },
            text = {
                Text(
                    "Marque o que você mais e menos gosta — biologia, história, matemática. Muda a ordem do cronograma e da aula do dia.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    aoFecharPerguntaPreferencias()
                    aoAbrirPreferencias()
                }) {
                    Text("Preencher", color = Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = aoFecharPerguntaPreferencias) {
                    Text("Depois", color = TextMuted)
                }
            },
        )
    }
    // Tudo num único scroll: antes o cabeçalho (anel + estatísticas) ficava
    // fixo no topo e a lista de trilhas abaixo, dividindo a tela. Em tela
    // pequena o cabeçalho comia o espaço e a lista ficava espremida, sem
    // espaço para rolar.
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(BackgroundGradientTop, Background))
            )
            .floresFundo(
                alpha = if (PaletaAtual.atual.escuro) 0.22f else 0.42f
            ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp, end = 20.dp, bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        // A saudação acompanha a hora: "Bom dia" às 22h
                        // estragava a primeira impressão da tela.
                        text = "${saudacao()}${estado.nome.takeIf { it.isNotBlank() }?.let { ", $it" } ?: ""}",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                    )
                    // A semana é o contexto do dia. Antes vinha num cartão
                    // separado, ocupando metade da primeira tela.
        
            val semanaAtual = estado.cronograma.firstOrNull { it.atual }
                    Text(
                        text = if (semanaAtual != null) {
                            "semana ${semanaAtual.numero} de ${estado.cronograma.size}"
                        } else {
                            "seu plano de estudo"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(SurfaceRaised),
                    contentAlignment = Alignment.Center,
                ) {
                    com.franferreira.enem2027.ui.components.Florzinha(
                        tamanho = 42.dp,
                        dormindo = estado.respondidasHoje == 0 && estado.streak == 0,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Se o acervo não foi gravado, isso vem antes de tudo: sem ele
            // nenhuma das outras funções tem o que mostrar.
            estado.erroAcervo?.let { erro ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .cartaoRabisco(18.dp)
                        .padding(14.dp),
                ) {
                    Column {
                        Text(
                            text = "ACERVO",
                            style = MaterialTheme.typography.labelSmall,
                            color = Warning,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = erro,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                        )
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(Warning)
                                .clickable { aoTentarAcervo() }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = "Tentar de novo",
                                style = MaterialTheme.typography.labelMedium,
                                color = PaletaClaro.texto,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // ---- a flor: a decisão do dia fica no topo da tela
            CartaoMeta(
                estado = estado,
                treinos = estado.treinos,
                aoEstudar = aoRevisar,
            )

            Spacer(Modifier.height(14.dp))

            // A questão está na flor; a aula do dia é a outra metade do estudo.
            estado.aulaDoDia?.let { aula ->
                CartaoAulaDoDia(
                    aula = aula,
                    aoAssistir = {
                        val externa = aula.urlExterna
                        if (externa != null) aoAbrirUrl(externa) else aoAbrirAula(aula.id)
                    },
                )
                Spacer(Modifier.height(14.dp))
            }

            // O cronograma vem logo depois da aula, como na maquete: é a
            // terceira coisa que ela olha.
            val semanaAtual = estado.cronograma.firstOrNull { it.atual }
            if (semanaAtual != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .cartaoRabisco(18.dp, torto = 0.5f)
                        .clickable { aoAbrirCronograma() }
                        .padding(14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "Cronograma · semana ${semanaAtual.numero} de ${estado.cronograma.size}",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                            )
                            Text(
                                text = if (semanaAtual.temas.isEmpty()) {
                                    "Semana de revisão"
                                } else {
                                    semanaAtual.temas.take(2).joinToString(", ") { it.tema } +
                                        if (semanaAtual.pendentes > 2) " +${semanaAtual.pendentes - 2}" else ""
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Text(
                            text = "Ver",
                            style = MaterialTheme.typography.labelLarge,
                            color = Primary,
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
            }

            if (estado.esperandoRede > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Warning.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                ) {
                    Text(
                        text = if (estado.esperandoRede == 1) {
                            "1 resposta salva no aparelho, esperando rede para subir"
                        } else {
                            "${estado.esperandoRede} respostas salvas no aparelho, esperando rede"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Warning,
                    )
                }
                Spacer(Modifier.height(10.dp))
            }

            // Revisão do SRS: virou cartão na Hoje em vez de aba própria.
            val vencendo = estado.fila.vencendo.size
            val novas = estado.fila.novas.size
            if (!estado.diagFeito && !estado.diagPendente) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .cartaoRabisco(18.dp, torto = -0.6f)
                        .clickable { aoFazerDiagnostico() }
                        .padding(14.dp),
                ) {
                    Column {
                        Text(
                            text = "Diagnóstico inicial",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                        )
                        Text(
                            text = "40 questões, 10 de cada área. Mostra onde você está forte e onde precisa de treino.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
            }
            if (estado.simuladoTemas.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .cartaoRabisco(18.dp, torto = -0.5f)
                        .clickable { aoRevisar() }
                        .padding(14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "Revisar o simulado",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                            )
                            Text(
                                text = estado.simuladoTemas.take(3).joinToString(", "),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Text(
                            text = "Treinar",
                            style = MaterialTheme.typography.labelLarge,
                            color = Primary,
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
            }
            val treinos = estado.treinos
            if (treinos.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .cartaoRabisco(18.dp, torto = 0.6f)
                        .clickable { aoRevisar() }
                        .padding(14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "Treinar hoje",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                            )
                            Text(
                                text = treinos.take(3).joinToString(", ") { it.tema },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Text(
                            text = "Treinar",
                            style = MaterialTheme.typography.labelLarge,
                            color = Primary,
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
            }
            // O cartão "N vencendo · M novas" saiu: ele vinha do SRS por
            // questão, mas a tela de revisão agora trabalha por tema — a pessoa
            // via "50 para revisar", clicava e caía numa lista vazia.

            estado.mensagem?.let { mensagem ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
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

            // ---- rodapé: versão e atalho das matérias
            //
            // A versão fica visível para dar como conferir, no aparelho, se a
            // atualização chegou. Sem isso, "não apareceu nada" não tem como
            // ser diagnosticado de longe.
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "v${estado.versaoApp} · ${estado.totalLocal} questões · ${estado.trilhas.size} trilha(s)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMutedDark,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(SurfaceRaised)
                        .clickable { aoAbrirPreferencias() }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = "Matérias",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary,
                    )
                }
            }
        }

        if (estado.carregando) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Primary, strokeWidth = 2.dp)
                }
            }
        } else if (estado.semeando) {
            // O acervo ja esta no APK: ele esta sendo gravado no aparelho.
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Primary, strokeWidth = 2.dp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Preparando as provas do ENEM",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Elas já vieram no aplicativo: é só gravar no aparelho.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        } else if (estado.totalLocal == 0) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 32.dp),
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
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(2023, 2022, 2021, 2020, 2019).forEach { ano ->
                            BotaoAno(ano, estado.sincronizando) { aoBaixarAno(ano) }
                        }
                    }
                }
            }
            }
        } else {
            // ---- trilhas
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "TRILHAS",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        modifier = Modifier.weight(1f),
                    )
                    // Rebaixar o acervo: é o que traz a explicação nova
                    // para as questões que já estavam no aparelho.
                    Icon(
                        Icons.Filled.CloudDownload,
                        contentDescription = "Atualizar acervo",
                        tint = if (estado.sincronizando) TextMutedDark else Primary,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { aoAtualizarAcervo() },
                    )
                    Spacer(Modifier.width(14.dp))
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = "Sincronizar respostas",
                        tint = if (estado.sincronizando) TextMutedDark else Secondary,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { aoSincronizar() },
                    )
                }
            }

            if (estado.trilhas.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .cartaoRabisco(16.dp)
                            .padding(20.dp),
                    ) {
                        Text(
                            text = "Nenhuma trilha ainda. Baixe o acervo de um ano para as etapas se montarem.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                        )
                    }
                }
            } else {
                items(estado.trilhas, key = { it.id }) { trilha ->
                    CartaoTrilha(
                        trilha = trilha,
                        aoAbrir = { aoAbrirTrilha(trilha) },
                        aoJogar = { etapa -> aoJogarEtapa(trilha, etapa) },
                    )
                }
            }
        }
    }
}

/**
 * A aula do dia: a próxima aula ainda não assistida dos temas da semana.
 *
 * Usa o mesmo formato de cartão da revisão, para a Hoje não virar outro tipo de tela.
 */
@Composable
private fun CartaoAulaDoDia(
    aula: com.franferreira.enem2027.data.model.AulaSugerida,
    aoAssistir: () -> Unit,
) {
    val cor = corDaArea(aula.area)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .cartaoRabisco(18.dp)
            .padding(14.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "AULA DO DIA",
                style = MaterialTheme.typography.labelSmall,
                color = cor,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = aula.titulo,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(cor)
                .clickable { aoAssistir() }
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = "Assistir",
                style = MaterialTheme.typography.labelMedium,
                color = PaletaClaro.texto,
            )
        }
    }
}

/**
 * "Estudar hoje": a flor da meta, com o número no miolo.
 *
 * É o cartão que decide a tela. A meta ajustável e o botão da trilha saíram
 * daqui: viraram consulta em outras telas, para o cartão responder só
 * "o que eu faço agora?".
 */
@Composable
private fun CartaoMeta(
    estado: EstadoHoje,
    treinos: List<com.franferreira.enem2027.data.model.TemaTreino>,
    aoEstudar: () -> Unit,
) {
    val meta = estado.metaDiaria
    val feitas = estado.respondidasHoje.coerceAtMost(meta)
    val alvo = if (meta <= 0) 0f else feitas.toFloat() / meta
    // A pétala cresce em vez de pular: o progresso vira recompensa, não número.
    val progresso by androidx.compose.animation.core.animateFloatAsState(
        targetValue = alvo,
        animationSpec = androidx.compose.animation.core.tween(650),
        label = "progressoFlor",
    )
    // Toque: a flor encolhe um pouco enquanto o dedo está nela.
    val interacao = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val pressionada by interacao.collectIsPressedAsState()
    val escala by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (pressionada) 0.96f else 1f,
        animationSpec = androidx.compose.animation.core.spring(stiffness = 700f),
        label = "escalaFlor",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .cartaoRabisco(18.dp, sombra = true, borda = Primary.copy(alpha = 0.55f))
            .padding(14.dp),
    ) {
        Text(
            text = "ESTUDAR HOJE",
            style = MaterialTheme.typography.labelSmall,
            color = Primary,
        )
        Spacer(Modifier.height(6.dp))

        // A flor é o botão: tocar nela começa a estudar.
        Box(
            modifier = Modifier
                .size(132.dp)
                .graphicsLayer { scaleX = escala; scaleY = escala }
                .clickable(
                    interactionSource = interacao,
                    indication = null,
                ) { aoEstudar() },
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // A meta vira uma flor: uma pétala por questão do dia.
                val centro = center
                val total = if (meta in 1..12) meta else 12
                val cheias = (progresso * total).roundToInt().coerceIn(0, total)
                val lado = size.minDimension
                val largura = lado * 0.16f
                val altura = lado * 0.23f
                val distancia = lado * 0.13f
                for (i in 0 until total) {
                    rotate(degrees = -90f + i * (360f / total), pivot = centro) {
                        val canto = androidx.compose.ui.geometry.Offset(
                            centro.x - largura / 2f,
                            centro.y - distancia - altura,
                        )
                        val tamanhoPetala = androidx.compose.ui.geometry.Size(largura, altura)
                        drawOval(
                            color = if (i < cheias) Primary else SurfaceRaised,
                            topLeft = canto,
                            size = tamanhoPetala,
                        )
                        if (i >= cheias) {
                            drawOval(
                                color = Primary.copy(alpha = 0.22f),
                                topLeft = canto,
                                size = tamanhoPetala,
                                style = Stroke(width = 1.dp.toPx()),
                            )
                        }
                    }
                }
            }

            // O miolo é o número do dia: é o que a pessoa olha primeiro.
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$feitas",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    text = "/ $meta questões",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
        }

        if (treinos.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = treinos.take(2).joinToString(" e ") { it.tema },
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Primary)
                .clickable { aoEstudar() }
                .padding(horizontal = 26.dp, vertical = 10.dp),
        ) {
            Text(
                text = if (feitas == 0) "Começar" else "Continuar",
                style = MaterialTheme.typography.labelLarge,
                color = PaletaClaro.texto,
            )
        }
    }
}

/** Um cartão de trilha na lista: nome, progresso e a próxima etapa. */
@Composable
private fun CartaoTrilha(
    trilha: Trilha,
    aoAbrir: () -> Unit,
    aoJogar: (Etapa) -> Unit,
) {
    val cor = corDaArea(trilha.area)
    val etapa = trilha.etapaAtual

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.verticalGradient(listOf(SurfaceRaised, Surface)))
            .bordaRabisco(SurfaceBorder, raio = 22.dp)
            .clickable { aoAbrir() }
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // marcador com a cor da área
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(cor),
            )
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = trilha.nome,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (trilha.adaptativa) {
                    Text(
                        text = "MONTADA PELO SEU DESEMPENHO",
                        style = MaterialTheme.typography.labelSmall,
                        color = cor,
                    )
                }
            }
            Pilula(
                texto = "${trilha.concluidas}/${trilha.totalEtapas}",
                cor = cor,
                fundo = cor.copy(alpha = 0.14f),
            )
        }

        Spacer(Modifier.height(10.dp))

        // barra de progresso
        BarraGamificada(
            progresso = (trilha.progresso / 100.0).toFloat(),
            cor = cor,
            altura = 6,
        )

        if (etapa != null) {
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = etapa.nome,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(cor.copy(alpha = 0.16f))
                        .clickable(enabled = etapa.jogavel) { aoJogar(etapa) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = "Jogar",
                        style = MaterialTheme.typography.labelMedium,
                        color = cor,
                    )
                }
            }
        }
    }
}

@Composable
private fun BotaoAno(ano: Int, carregando: Boolean, aoClicar: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
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
