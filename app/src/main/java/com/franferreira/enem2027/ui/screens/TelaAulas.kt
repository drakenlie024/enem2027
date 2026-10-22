package com.franferreira.enem2027.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.franferreira.enem2027.data.model.AreaAulas
import com.franferreira.enem2027.data.model.AulaResumo
import com.franferreira.enem2027.data.model.AulasDeArea
import com.franferreira.enem2027.data.model.PacoteAula
import com.franferreira.enem2027.data.model.QuestaoAula
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
import com.franferreira.enem2027.ui.theme.corDaArea

/**
 * Lista de video-aulas: areas -> temas -> aulas.
 *
 * A aula e o video, o resumo em topicos e as questoes sobre o que foi
 * ensinado. Tudo pre-computado no servidor, entao funciona sem rede.
 */
@Composable
fun TelaAulas(
    areas: List<AreaAulas>,
    aoAbrirArea: (String) -> Unit,
    aoVoltar: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundGradientTop, Background))),
    ) {
        TopoAulas("VIDEO-AULAS", aoVoltar)

        if (areas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Nenhuma video-aula disponível ainda.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                )
            }
            return
        }

        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text(
                    text = "Aulas com resumo e questoes sobre o que foi ensinado. " +
                        "O gabarito de cada questao tem o minuto da video onde a " +
                        "resposta aparece, entao da para conferir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMutedDark,
                )
                Spacer(Modifier.height(4.dp))
            }
            items(areas, key = { it.area }) { area ->
                CartaoAreaAulas(area = area, aoClicar = { aoAbrirArea(area.area) })
            }
        }
    }
}

/** As aulas de uma area, agrupadas por tema. */
@Composable
fun TelaAulasDeArea(
    dados: AulasDeArea,
    aoAbrirAula: (AulaResumo) -> Unit,
    aoVoltar: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundGradientTop, Background))),
    ) {
        TopoAulas(dados.area.replace('-', ' ').uppercase(), aoVoltar)

        if (dados.temas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Nenhuma aula nesta area.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                )
            }
            return
        }

        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            dados.temas.forEach { grupo ->
                item(key = "t-${grupo.tema}") {
                    Text(
                        text = grupo.tema.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    Spacer(Modifier.height(6.dp))
                }
                items(grupo.aulas, key = { it.id }) { aula ->
                    CartaoAula(aula = aula, aoClicar = { aoAbrirAula(aula) })
                }
            }
        }
    }
}

/**
 * A aula: o video, o resumo e as questoes.
 *
 * A questao responde o que a pessoa aprendeu NA video. Depois de responder,
 * aparece a justificativa e o botao "ver na video" que abre o YouTube no
 * minuto exato — a resposta nunca e so a palavra da IA.
 */
@Composable
fun TelaAula(
    pacote: PacoteAula,
    aoVoltar: () -> Unit,
) {
    val contexto = LocalContext.current

    fun abrirVideo(segundos: Int = 0) {
        val url = if (segundos > 0) {
            "${pacote.url}?t=${segundos}"
        } else {
            pacote.url
        }
        runCatching {
            contexto.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundGradientTop, Background))),
    ) {
        TopoAulas(pacote.tema.uppercase(), aoVoltar)

        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, bottom = 28.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // ---- o video
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Surface)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
                        .clickable { abrirVideo() }
                        .padding(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceRaised),
                        contentAlignment = Alignment.Center,
                    ) {
                        AsyncImage(
                            model = "https://i.ytimg.com/vi/${pacote.id}/hqdefault.jpg",
                            contentDescription = "Capa da video-aula",
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Primary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Filled.PlayArrow,
                                contentDescription = "Assistir",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp),
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = pacote.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${pacote.canal} · ${pacote.palavras} palavras de transcricao",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                    )
                }
            }

            // ---- o resumo
            if (pacote.resumo.isNotEmpty()) {
                item {
                    Text(
                        text = "O QUE A AULA ENSINA",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    Spacer(Modifier.height(6.dp))
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Surface)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
                            .padding(13.dp),
                    ) {
                        pacote.resumo.forEach { topico ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 5.dp)
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(corDaArea(pacote.area)),
                                )
                                Spacer(Modifier.width(9.dp))
                                Text(
                                    text = topico,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                )
                            }
                        }
                    }
                }
            }

            // ---- as questoes
            if (pacote.questoes.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.padding(top = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "QUESTÕES DA AULA",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "${pacote.questoes.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Secondary,
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                }
                items(pacote.questoes, key = { it.enunciado.take(40) }) { questao ->
                    CartaoQuestaoAula(
                        questao = questao,
                        aoVerNoVideo = { segundos -> abrirVideo(segundos) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CartaoQuestaoAula(questao: QuestaoAula, aoVerNoVideo: (Int) -> Unit) {
    var escolhida by remember { mutableStateOf<String?>(null) }
    val respondida = escolhida != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
            .padding(13.dp),
    ) {
        Text(
            text = questao.enunciado,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
        )
        Spacer(Modifier.height(10.dp))

        questao.alternativas.forEach { alternativa ->
            val correta = alternativa.letra.equals(questao.gabarito, ignoreCase = true)
            val escolhidaAqui = escolhida == alternativa.letra
            val mostrarCerto = respondida && correta
            val mostrarErrado = respondida && escolhidaAqui && !correta
            val corFundo = when {
                mostrarCerto -> Secondary.copy(alpha = 0.13f)
                mostrarErrado -> Danger.copy(alpha = 0.13f)
                escolhidaAqui -> Primary.copy(alpha = 0.10f)
                else -> SurfaceRaised
            }
            val corBorda = when {
                mostrarCerto -> Secondary.copy(alpha = 0.45f)
                mostrarErrado -> Danger.copy(alpha = 0.45f)
                escolhidaAqui -> Primary.copy(alpha = 0.45f)
                else -> SurfaceBorder
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(corFundo)
                    .border(1.dp, corBorda, RoundedCornerShape(10.dp))
                    .clickable(enabled = !respondida) { escolhida = alternativa.letra }
                    .padding(horizontal = 11.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(21.dp)
                        .clip(CircleShape)
                        .background(corBorda.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = alternativa.letra,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary,
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = alternativa.texto,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }

        if (respondida) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Gabarito: ${questao.gabarito}",
                style = MaterialTheme.typography.labelMedium,
                color = Secondary,
            )
            if (questao.justificativa.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = questao.justificativa,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
            val segundos = questao.timestamp
                .split(":")
                .mapNotNull { it.toIntOrNull() }
                .let { if (it.size >= 2) it[0] * 60 + it[1] else 0 }
            if (segundos > 0) {
                Spacer(Modifier.height(9.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .background(Primary.copy(alpha = 0.13f))
                        .clickable { aoVerNoVideo(segundos) }
                        .padding(horizontal = 11.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Filled.OndemandVideo,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        text = "Ver na video em ${questao.timestamp}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun CartaoAreaAulas(area: AreaAulas, aoClicar: () -> Unit) {
    val cor = corDaArea(area.area)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
            .clickable { aoClicar() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(cor),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = area.area.replace('-', ' ').replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = "${area.aulas} aulas · ${area.questoes} questões · ${area.temas.size} temas",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
        }
        Icon(
            Icons.Filled.OndemandVideo,
            contentDescription = null,
            tint = cor,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun CartaoAula(aula: AulaResumo, aoClicar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
            .clickable { aoClicar() }
            .padding(11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(width = 74.dp, height = 44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceRaised),
        ) {
            AsyncImage(
                model = "https://i.ytimg.com/vi/${aula.id}/mqdefault.jpg",
                contentDescription = null,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = aula.titulo,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = "${aula.canal} · ${aula.totalQuestoes} questões",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
        }
    }
}

@Composable
private fun TopoAulas(titulo: String, aoVoltar: () -> Unit) {
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
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelMedium,
            color = Primary,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
