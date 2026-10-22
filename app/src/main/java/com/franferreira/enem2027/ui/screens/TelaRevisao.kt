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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.franferreira.enem2027.data.model.Question
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

/**
 * Tela de revisão: a fila do SRS.
 *
 * Mostra as questões que vencem hoje, separadas por estado de memória —
 * o que está vencendo (revisão), o que é novo e o que o aluno errou
 * (relearning). É o retrato do que a repetição espaçada decides.
 */
@Composable
fun TelaRevisao(
    vencendo: List<Question>,
    novas: List<Question>,
    erradas: List<Question>,
    aoAbrir: (Question) -> Unit,
    aoSincronizar: () -> Unit,
    sincronizando: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundGradientTop, Background))),
    ) {
        Column(Modifier.padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "REVISÃO",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary,
                    )
                    Text(
                        text = "O que o SRS mandou voltar",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                    )
                }
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = "Sincronizar",
                    tint = if (sincronizando) TextMutedDark else Secondary,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { aoSincronizar() },
                )
            }

            Spacer(Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CartaoContagem(
                    valor = vencendo.size,
                    rotulo = "para revisar",
                    cor = Primary,
                    modifier = Modifier.weight(1f),
                )
                CartaoContagem(
                    valor = novas.size,
                    rotulo = "novas",
                    cor = Secondary,
                    modifier = Modifier.weight(1f),
                )
                CartaoContagem(
                    valor = erradas.size,
                    rotulo = "erradas",
                    cor = Danger,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(16.dp))
        }

        if (vencendo.isEmpty() && novas.isEmpty() && erradas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Secondary,
                        modifier = Modifier.size(40.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Nada pendente",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Baixe mais anos no \"Hoje\" para encher a fila.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (erradas.isNotEmpty()) {
                    item { CabecalhoGrupo("VOCÊ ERROU — VOLTA EM BREVE", Danger) }
                    items(erradas, key = { it.id }) { q ->
                        CartaoFila(q, Danger) { aoAbrir(q) }
                    }
                }
                if (vencendo.isNotEmpty()) {
                    item { CabecalhoGrupo("VENCENDO AGORA", Primary) }
                    items(vencendo, key = { it.id }) { q ->
                        CartaoFila(q, Primary) { aoAbrir(q) }
                    }
                }
                if (novas.isNotEmpty()) {
                    item { CabecalhoGrupo("NUNCA VISTAS", Secondary) }
                    items(novas, key = { it.id }) { q ->
                        CartaoFila(q, Secondary) { aoAbrir(q) }
                    }
                }
            }
        }
    }
}

@Composable
private fun CabecalhoGrupo(texto: String, cor: androidx.compose.ui.graphics.Color) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelSmall,
        color = cor,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
    )
}

@Composable
private fun CartaoContagem(
    valor: Int,
    rotulo: String,
    cor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    listOf(cor.copy(alpha = 0.16f), Surface),
                )
            )
            .border(1.dp, cor.copy(alpha = 0.30f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = valor.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = cor,
        )
        Text(
            text = rotulo,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
        )
    }
}

@Composable
private fun CartaoFila(
    questao: Question,
    cor: androidx.compose.ui.graphics.Color,
    aoClicar: () -> Unit,
) {
    var mostrandoExplicacao by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
            .clickable { aoClicar() }
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(cor),
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "${questao.ano} · ${questao.temaExibicao}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = questao.enunciado?.take(110) ?: "(questão com figura)",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(8.dp))
            if (!questao.oficial) {
                Text(
                    text = "IA",
                    style = MaterialTheme.typography.labelSmall,
                    color = Danger,
                )
            }
        }

        // Botão de explicar, direto no card: na revisão a resposta é o
        // assunto, então não faz sentido mandar o usuário para outra tela.
        if (questao.temExplicacao) {
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceRaised)
                    .clickable { mostrandoExplicacao = !mostrandoExplicacao }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    text = if (mostrandoExplicacao) "OCULTAR EXPLICAÇÃO" else "POR QUE ESSA É A RESPOSTA?",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (mostrandoExplicacao) TextMuted else Primary,
                )
            }
            if (mostrandoExplicacao) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = questao.explicacao!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }
    }
}
