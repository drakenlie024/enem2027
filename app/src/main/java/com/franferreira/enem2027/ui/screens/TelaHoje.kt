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
            ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp, end = 16.dp, bottom = 24.dp
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
                    com.franferreira.enem2027.ui.components.Gizinho(
                        tamanho = 40.dp,
                        dormindo = estado.respondidasHoje == 0 && estado.streak == 0,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- a meta do dia: um anel e um botão
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

            // ---- estatísticas rápidas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Surface)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(18.dp))
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
                    val espessura = 11.dp.toPx()
                    val diametro = size.minDimension - espessura
                    val canto = androidx.compose.ui.geometry.Offset(
                        (size.width - diametro) / 2,
                        (size.height - diametro) / 2,
                    )
                    drawArc(
                        color = SurfaceRaised,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = canto,
                        size = androidx.compose.ui.geometry.Size(diametro, diametro),
                        style = Stroke(width = espessura, cap = StrokeCap.Round),
                    )
                    drawArc(
                        color = cor,
                        startAngle = -90f,
                        sweepAngle = 360f * progresso,
                        useCenter = false,
                        topLeft = canto,
                        size = androidx.compose.ui.geometry.Size(diametro, diametro),
                        style = Stroke(width = espessura, cap = StrokeCap.Round),
                    )
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
