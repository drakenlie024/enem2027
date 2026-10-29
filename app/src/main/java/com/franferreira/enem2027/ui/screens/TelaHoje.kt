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
import com.franferreira.enem2027.ui.components.BotaoGamificado
import com.franferreira.enem2027.ui.components.CartaoGamificado
import com.franferreira.enem2027.ui.components.Pilula
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
) {
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
                        text = "HOJE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary,
                    )
                    Text(
                        // A saudação acompanha a hora: "Bom dia" às 22h
                        // estragava a primeira impressão da tela.
                        text = "${saudacao()}${estado.nome.takeIf { it.isNotBlank() }?.let { ", $it" } ?: ""}",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
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

            // ---- a meta do dia: um anel e um botão
            PlanoDeHoje(
                estado = estado,
                aoRevisar = aoRevisar,
                aoJogar = { trilha, etapa -> aoJogarEtapa(trilha, etapa) },
                aoAulas = aoAulas,
            )

            SemanaDeEstudo(
                estado = estado,
                aoMudarMeta = aoMudarMetaSemanal,
            )

            CartaoFoco(
                estado = estado,
                aoIniciar = aoIniciarFoco,
                aoParar = aoPararFoco,
            )

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


            CartaoMeta(
                estado = estado,
                aoJogar = {
                    val trilha = estado.trilhaParaJogar
                    val etapa = trilha?.etapaAtual
                    if (trilha != null && etapa != null) {
                        aoJogarEtapa(trilha, etapa)
                    }
                },
                aoAjustarMeta = aoAjustarMeta,
            )

            Spacer(Modifier.height(14.dp))

            // Revisão do SRS: virou cartão na Hoje em vez de aba própria.
            val vencendo = estado.fila.vencendo.size
            val novas = estado.fila.novas.size
            if (!estado.diagFeito && !estado.diagPendente) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .cartaoRabisco(18.dp)
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
            val semanaAtual = estado.cronograma.firstOrNull { it.atual }
            if (semanaAtual != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .cartaoRabisco(18.dp)
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
            if (estado.simuladoTemas.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .cartaoRabisco(18.dp)
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
                        .cartaoRabisco(18.dp)
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
            } else if (vencendo + novas > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .cartaoRabisco(18.dp)
                        .clickable { aoRevisar() }
                        .padding(14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "Revisão",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                            )
                            Text(
                                text = buildString {
                                    append("$vencendo vencendo")
                                    if (novas > 0) append(" · $novas novas")
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                            )
                        }
                        Text(
                            text = "Revisar",
                            style = MaterialTheme.typography.labelLarge,
                            color = Primary,
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
            }


            // ---- estatísticas rápidas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .cartaoRabisco(18.dp)
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
                            .clip(RoundedCornerShape(16.dp))
                            .background(Surface)
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
 * O cartão da meta: um anel de progresso com o botão dentro.
 *
 * O anel mostra quanto da meta diária já foi feito; o botão é a única ação
 * da tela. Se não houver nada para jogar, o botão diz por quê.
 */
@Composable
private fun CartaoMeta(
    estado: EstadoHoje,
    aoJogar: () -> Unit,
    aoAjustarMeta: (Int) -> Unit,
) {
    val meta = estado.metaDiaria
    // O anel mostra o que foi FEITO hoje. Antes usava o saldo do SRS (o que
    // falta), e cada resposta diminuía o anel — ao contrário do esperado.
    val feitas = estado.respondidasHoje.coerceAtMost(meta)
    val progresso = if (meta <= 0) 0f else feitas.toFloat() / meta
    val trilha = estado.trilhaParaJogar
    val etapa = trilha?.etapaAtual
    val cor = if (trilha != null) corDaArea(trilha.area) else Primary
    val podeJogar = etapa != null && etapa.jogavel

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    listOf(cor.copy(alpha = 0.10f), Surface)
                )
            )
            .border(1.dp, cor.copy(alpha = 0.28f), RoundedCornerShape(22.dp))
            .padding(18.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "META DE HOJE",
                style = MaterialTheme.typography.labelSmall,
                color = cor,
            )
            Spacer(Modifier.height(12.dp))

            // anel
            Box(
                modifier = Modifier.size(132.dp),
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
                                color = if (i < cheias) cor else SurfaceRaised,
                                topLeft = canto,
                                size = tamanhoPetala,
                            )
                            if (i >= cheias) {
                                drawOval(
                                    color = cor.copy(alpha = 0.22f),
                                    topLeft = canto,
                                    size = tamanhoPetala,
                                    style = Stroke(width = 1.dp.toPx()),
                                )
                            }
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$feitas",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                    )
                    Text(
                        text = "de $meta",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                    )
                }
            }

            // A meta é sugerida, mas ajustável: dois botões discretos ao lado
            // do anel mudam o alvo do dia sem tirar o foco do anel.
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                BotaoMeta(
                    simbolo = "−",
                    habilitado = meta > 4,
                    aoClicar = { aoAjustarMeta(-4) },
                )
                Spacer(Modifier.width(14.dp))
                BotaoMeta(
                    simbolo = "+",
                    habilitado = meta < 60,
                    aoClicar = { aoAjustarMeta(4) },
                )
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = when {
                    trilha == null -> "Nenhuma trilha pronta"
                    etapa == null -> "Trilha concluída! 🎉"
                    !etapa.jogavel -> "Baixe o acervo desta etapa"
                    else -> etapa.nome
                },
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (etapa != null && etapa.jogavel) {
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "${etapa.questoes.size} questões",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                )
            }

            Spacer(Modifier.height(14.dp))

            // o botão
            BotaoGamificado(
                texto = when {
                    etapa == null -> "Trilha completa"
                    !etapa.jogavel -> "Baixe o acervo"
                    else -> "JOGAR ETAPA"
                },
                cor = cor,
                habilitado = podeJogar,
                aoClicar = { aoJogar() },
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
            .border(1.dp, SurfaceBorder, RoundedCornerShape(22.dp))
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

/** Botão redondo e discreto que ajusta a meta diária. */
@Composable
private fun BotaoMeta(
    simbolo: String,
    habilitado: Boolean,
    aoClicar: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(SurfaceRaised)
            .border(1.dp, SurfaceBorder, CircleShape)
            .clickable(enabled = habilitado) { aoClicar() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = simbolo,
            style = MaterialTheme.typography.titleMedium,
            color = if (habilitado) TextSecondary else TextMutedDark,
        )
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

/** Uma ação do plano do dia. */
private data class AcaoDoDia(
    val titulo: String,
    val detalhe: String,
    val cor: androidx.compose.ui.graphics.Color,
    val aoClicar: () -> Unit,
)

/**
 * Plano de hoje: as três coisas para fazer, na ordem, sem a pessoa ter que
 * decidir. É a resposta para "não sei o que estudar".
 */
@Composable
private fun PlanoDeHoje(
    estado: EstadoHoje,
    aoRevisar: () -> Unit,
    aoJogar: (Trilha, Etapa) -> Unit,
    aoAulas: () -> Unit,
) {
    val vencendo = estado.fila.vencendo.size
    val trilha = estado.trilhaParaJogar
    val etapa = trilha?.etapaAtual

    val acoes = buildList {
        if (vencendo > 0) {
            add(AcaoDoDia("Revisar", "$vencendo questões vencendo", Primary, aoRevisar))
        }
        if (trilha != null && etapa != null) {
            add(
                AcaoDoDia(
                    titulo = "Continuar ${trilha.nome}",
                    detalhe = "etapa ${etapa.indice + 1} · ${etapa.questoes.size} questões",
                    cor = corDaArea(trilha.area),
                ) { aoJogar(trilha, etapa) }
            )
        }
        // Redação não é tarefa diária. O terceiro item recomenda duas
        // matérias para assistir aula — as que estão com pior taxa.
        val fracas = estado.progresso.porArea
            .filter { it.respondidas >= 5 }
            .sortedBy { it.taxa }
            .take(2)
            .map { nomeDaArea(it.area) }
        val foco = when (fracas.size) {
            0 -> "comece pelas aulas da sua trilha"
            1 -> "foco em ${fracas[0]}"
            else -> "foco em ${fracas[0]} e ${fracas[1]}"
        }
        add(AcaoDoDia("Assistir aulas", foco, Secondary, aoAulas))
    }.take(3)

    if (acoes.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .cartaoRabisco(22.dp)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(
            text = "PLANO DE HOJE",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
        )
        Spacer(Modifier.height(4.dp))
        acoes.forEachIndexed { indice, acao ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { acao.aoClicar() }
                    .padding(vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(acao.cor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "${indice + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        color = acao.cor,
                    )
                }
                Spacer(Modifier.width(11.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = acao.titulo,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                    )
                    Text(
                        text = acao.detalhe,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                    )
                }
                Text(
                    text = "›",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextMuted,
                )
            }
        }
    }
}

/** Nome legível da área a partir do slug que o servidor manda. */
private fun nomeDaArea(area: String): String = when (area) {
    "matematica" -> "Matemática"
    "linguagens" -> "Linguagens"
    "ciencias-humanas" -> "Humanas"
    "ciencias-natureza" -> "Natureza"
    "" -> "aulas"
    else -> area.replaceFirstChar { it.uppercase() }
}

/**
 * Semana de estudo: sete bolinhas, uma por dia, e a meta de dias por semana.
 *
 * O streak premia a sequência; aqui o dia perdido não zera nada, só deixa a
 * semana abaixo do alvo. Tocar no número muda a meta (3, 5, 6 ou 7 dias).
 */
@Composable
private fun SemanaDeEstudo(estado: EstadoHoje, aoMudarMeta: (Int) -> Unit) {
    val nomes = listOf("S", "T", "Q", "Q", "S", "S", "D")
    val feitos = estado.diasMarcados.size
    val meta = estado.metaSemanal
    val proximaMeta = when (meta) {
        3 -> 5
        5 -> 6
        6 -> 7
        else -> 3
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .cartaoRabisco(18.dp)
            .clickable { aoMudarMeta(proximaMeta) }
            .padding(horizontal = 14.dp, vertical = 11.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Semana",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "$feitos de $meta dias",
                style = MaterialTheme.typography.labelLarge,
                color = if (feitos >= meta) Secondary else TextMuted,
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            nomes.forEachIndexed { indice, letra ->
                val marcado = (indice + 1) in estado.diasMarcados
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f),
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (marcado) Primary else SurfaceRaised),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = letra,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (marcado) Background else TextMuted,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Foco de 25 minutos (pomodoro).
 *
 * O alarme é agendado no início, então o aviso chega mesmo se o app estiver
 * fechado; o contador aqui é só o que a tela mostra.
 */
@Composable
private fun CartaoFoco(
    estado: EstadoHoje,
    aoIniciar: () -> Unit,
    aoParar: () -> Unit,
) {
    val restante = estado.focoRestante.coerceAtLeast(0)
    val tempo = "%02d:%02d".format(restante / 60, restante % 60)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .cartaoRabisco(18.dp)
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "FOCO",
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary,
                )
                Text(
                    text = if (estado.focoAtivo) "Faltam $tempo" else "25 minutos sem celular",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
                Text(
                    text = if (estado.focoAtivo) {
                        "Um bloco de cada vez. O aviso chega mesmo com o app fechado."
                    } else {
                        "Um bloco de 25 minutos, depois cinco de pausa."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                )
            }
            Spacer(Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (estado.focoAtivo) SurfaceRaised else Primary)
                    .clickable { if (estado.focoAtivo) aoParar() else aoIniciar() }
                    .padding(horizontal = 14.dp, vertical = 9.dp),
            ) {
                Text(
                    text = if (estado.focoAtivo) "Parar" else "Começar",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (estado.focoAtivo) TextMuted else PaletaClaro.texto,
                )
            }
        }
    }
}
