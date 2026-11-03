package com.franferreira.enem2027.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.franferreira.enem2027.data.model.TemaTreino
import com.franferreira.enem2027.data.repo.AulasAssistidas
import com.franferreira.enem2027.data.repo.AulasPorTema
import com.franferreira.enem2027.ui.components.BarraGamificada
import com.franferreira.enem2027.ui.components.cartaoRabisco
import com.franferreira.enem2027.ui.theme.Background
import com.franferreira.enem2027.ui.theme.BackgroundGradientTop
import com.franferreira.enem2027.ui.theme.PaletaClaro
import com.franferreira.enem2027.ui.theme.Primary
import com.franferreira.enem2027.ui.theme.SurfaceRaised
import com.franferreira.enem2027.ui.theme.TextMuted
import com.franferreira.enem2027.ui.theme.TextPrimary
import com.franferreira.enem2027.ui.theme.corDaArea

/**
 * A sessão de hoje: um tema por vez, com a aula dele em cima e as questões
 * embaixo. Ao terminar um tema, o próximo já vem aberto.
 *
 * Antes o "Começar" abria uma lista de temas, e a pessoa tinha que decidir por
 * onde ir. Aqui ela só segue.
 */
@Composable
fun TelaSessao(
    temas: List<TemaTreino>,
    indice: Int,
    aoTreinar: (TemaTreino) -> Unit,
    aoAbrirAula: (String) -> Unit,
    aoEncerrar: () -> Unit,
    aoPular: () -> Unit = {},
    aoAbrirUrl: (String) -> Unit = {},
    /** XP no começo da sessão e agora, para mostrar o ganho no fim. */
    xpInicial: Int = 0,
    xpAtual: Int = 0,
    respondidasHoje: Int = 0,
    metaDiaria: Int = 0,
) {
    val atual = temas.getOrNull(indice)
    if (atual == null) {
        FimDaSessao(
            vazia = temas.isEmpty(),
            temasFeitos = temas.size,
            xpGanho = (xpAtual - xpInicial).coerceAtLeast(0),
            respondidasHoje = respondidasHoje,
            metaDiaria = metaDiaria,
            aoEncerrar = aoEncerrar,
        )
        return
    }

    // Meta do dia batida: o aviso aparece no corpo da sessão, sem trocar de tela.
    val metaBatida = metaDiaria > 0 && respondidasHoje == metaDiaria

    val contexto = LocalContext.current
    // Sem cache: ao voltar da aula, o passo 1 já sai da tela.
    val aula = AulasPorTema.aulasDoTema(contexto, atual.tema)
        .firstOrNull { !AulasAssistidas.foiAssistida(contexto, it.id) }
    // Tema sem aula no acervo: a playlist da matéria é um extra, não o passo 1.
    // Antes ela entrava como "ASSISTIR" e parecia aula do tema.
    val canal = if (aula == null) AulasPorTema.canalDoTema(contexto, atual.tema) else null
    val cor = corDaArea(atual.area)
    val proximo = temas.getOrNull(indice + 1)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundGradientTop, Background)))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.ArrowBack,
                contentDescription = "Sair da sessão",
                tint = TextMuted,
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .clickable { aoEncerrar() },
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "TEMA ${indice + 1} DE ${temas.size}",
                style = MaterialTheme.typography.labelSmall,
                color = Primary,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${atual.vistas} de ${atual.total}",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
        }

        Spacer(Modifier.height(8.dp))
        BarraGamificada(
            progresso = if (temas.isEmpty()) 0f else indice.toFloat() / temas.size,            cor = cor,
            altura = 6,
        )

        Spacer(Modifier.height(14.dp))
        Text(
            text = atual.tema,
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = atual.area.replace('-', ' ') + " · " + if (atual.emRevisao) {
                "${atual.total} questões para revisar"
            } else {
                "${atual.disponiveis} questões disponíveis"
            },
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
        )

        Spacer(Modifier.height(14.dp))
        aula?.let { sugerida ->
            CartaoPasso(
                numero = "1",
                rotulo = "ASSISTIR",
                titulo = sugerida.titulo,
                detalhe = sugerida.subtema.ifBlank { "vídeo curto do tema" },
                cor = cor,
                botao = "Assistir",
                aoClicar = { aoAbrirAula(sugerida.id) },
            )
            Spacer(Modifier.height(10.dp))
        }

        CartaoPasso(
            numero = if (aula != null) "2" else "1",
            rotulo = "PRATICAR",
            titulo = "${atual.disponiveis.coerceAtLeast(0)} questões do tema",
            detalhe = if (atual.emRevisao) "revisão do que você já viu" else "questões novas",
            cor = cor,
            botao = if (atual.vistas == 0) "Começar as questões" else "Continuar as questões",
            aoClicar = { aoTreinar(atual) },
            destaque = true,
        )

        canal?.let { playlist ->
            Spacer(Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .cartaoRabisco(16.dp)
                    .clickable { playlist.urlExterna?.let { aoAbrirUrl(it) } }
                    .padding(12.dp),
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Este tema não tem aula no acervo",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                    )
                    Text(
                        text = "Playlist de ${playlist.titulo.substringAfter("Aulas de ").substringBefore(" ·")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                    )
                }
                Text(
                    text = "Ver aulas",
                    style = MaterialTheme.typography.labelMedium,
                    color = Primary,
                )
            }
        }

        if (metaBatida) {
            Spacer(Modifier.height(14.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .cartaoRabisco(18.dp, fundo = Primary.copy(alpha = 0.10f), borda = Primary.copy(alpha = 0.45f))
                    .padding(14.dp),
            ) {
                Text(
                    text = "$respondidasHoje de $metaDiaria questões hoje",
                    style = MaterialTheme.typography.titleSmall,
                    color = Primary,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "meta batida",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                )
            }
        }

        if (proximo != null) {
            Spacer(Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .cartaoRabisco(18.dp)
                    .padding(14.dp),
            ) {
                Text(
                    text = "PRÓXIMO TEMA",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = proximo.tema,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "abre quando você terminar este",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                )
                Spacer(Modifier.height(6.dp))
                // Sem saída, um tema travado prendia a sessão para sempre.
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(SurfaceRaised)
                        .clickable { aoPular() }
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                ) {
                    Text(
                        text = "Pular para este tema",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary,
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text(
            text = if (proximo != null) {
                "Ao sair do treino, o próximo tema já vem aberto."
            } else {
                "Último tema da sessão de hoje."
            },
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun CartaoPasso(
    numero: String,
    rotulo: String,
    titulo: String,
    detalhe: String,
    cor: androidx.compose.ui.graphics.Color,
    botao: String,
    aoClicar: () -> Unit,
    destaque: Boolean = false,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .cartaoRabisco(
                18.dp,
                sombra = destaque,
                borda = if (destaque) cor.copy(alpha = 0.55f) else com.franferreira.enem2027.ui.theme.SurfaceBorder,
            )
            .padding(14.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = "$numero · $rotulo",
                style = MaterialTheme.typography.labelSmall,
                color = if (destaque) cor else TextMuted,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = detalhe,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
            )
        }
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(if (destaque) cor else SurfaceRaised)
                .clickable { aoClicar() }
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = botao,
                style = MaterialTheme.typography.labelMedium,
                color = if (destaque) PaletaClaro.texto else TextPrimary,
            )
        }
    }
}

/**
 * Fim da sessão: parabéns, o que ela fez hoje e quanto XP ganhou.
 *
 * É o único momento de comemoração — a ideia é marcar o fim do dia de estudo,
 * não premiar cada questão (o que vira ruído).
 */
@Composable
private fun FimDaSessao(
    vazia: Boolean,
    temasFeitos: Int,
    xpGanho: Int,
    respondidasHoje: Int,
    metaDiaria: Int,
    aoEncerrar: () -> Unit,
) {
    val xpAnimado = androidx.compose.animation.core.animateIntAsState(
        targetValue = xpGanho,
        animationSpec = androidx.compose.animation.core.tween(900),
        label = "xpGanho",
    ).value
    val escala = androidx.compose.animation.core.animateFloatAsState(
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.45f, stiffness = 220f),
        label = "florFim",
    ).value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundGradientTop, Background)))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (vazia) {
            Text(
                text = "Nada para estudar agora",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Nenhum tema venceu hoje. Você pode abrir uma trilha ou assistir uma aula.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                textAlign = TextAlign.Center,
            )
        } else {
            // Flor aberta: as pétalas cheias, do mesmo traço do resto do app.
            Canvas(
                modifier = Modifier
                    .size(120.dp)
                    .scale(escala),
            ) {
                val centro = center
                val lado = size.minDimension
                val largura = lado * 0.16f
                val altura = lado * 0.23f
                val distancia = lado * 0.13f
                for (i in 0 until 5) {
                    rotate(degrees = -90f + i * 72f, pivot = centro) {
                        drawOval(
                            color = Primary,
                            topLeft = androidx.compose.ui.geometry.Offset(
                                centro.x - largura / 2f,
                                centro.y - distancia - altura,
                            ),
                            size = androidx.compose.ui.geometry.Size(largura, altura),
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Sessão concluída",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (metaDiaria > 0) {
                    "$respondidasHoje de $metaDiaria questões hoje · $temasFeitos tema(s)"
                } else {
                    "$temasFeitos tema(s) estudado(s)"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                textAlign = TextAlign.Center,
            )
            if (xpGanho > 0) {
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "+${xpAnimado}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Primary,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "XP",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMuted,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Primary)
                .clickable { aoEncerrar() }
                .padding(horizontal = 26.dp, vertical = 11.dp),
        ) {
            Text(
                text = "Voltar para a Hoje",
                style = MaterialTheme.typography.labelLarge,
                color = PaletaClaro.texto,
            )
        }
    }
}
