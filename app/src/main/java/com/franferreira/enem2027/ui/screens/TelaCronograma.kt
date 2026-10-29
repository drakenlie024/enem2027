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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.franferreira.enem2027.data.model.SemanaCronograma
import com.franferreira.enem2027.data.model.TemaDaSemana
import com.franferreira.enem2027.ui.components.cartaoRabisco
import com.franferreira.enem2027.ui.theme.Background
import com.franferreira.enem2027.ui.theme.BackgroundGradientTop
import com.franferreira.enem2027.ui.theme.Danger
import com.franferreira.enem2027.ui.theme.PaletaClaro
import com.franferreira.enem2027.ui.theme.Primary
import com.franferreira.enem2027.ui.theme.Secondary
import com.franferreira.enem2027.ui.theme.Surface
import com.franferreira.enem2027.ui.theme.SurfaceBorder
import com.franferreira.enem2027.ui.theme.SurfaceRaised
import com.franferreira.enem2027.ui.theme.TextMuted
import com.franferreira.enem2027.ui.theme.TextPrimary
import com.franferreira.enem2027.ui.theme.corDaArea

/**
 * O plano até a prova: o que estudar em cada semana e quanto do conteúdo já
 * foi visto. Recalculado a cada abertura, então atraso não quebra o plano.
 */
@Composable
fun TelaCronograma(
    semanas: List<SemanaCronograma>,
    cobertura: Map<String, Pair<Int, Int>>,
    aoVoltar: () -> Unit,
) {
    val atual = semanas.firstOrNull { it.atual }
    val indiceAtual = semanas.indexOfFirst { it.atual }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundGradientTop, Background))),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 14.dp),
        ) {
            Icon(
                Icons.Filled.ArrowBack,
                contentDescription = "Voltar",
                tint = TextMuted,
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .clickable { aoVoltar() },
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "CRONOGRAMA",
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary,
                )
                Text(
                    text = if (atual != null) {
                        "Semana ${atual.numero} de ${semanas.size}"
                    } else {
                        "${semanas.size} semanas até a prova"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        if (semanas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Sem temas no acervo para montar o plano.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                )
            }
            return
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Cobertura(cobertura)
                Spacer(Modifier.height(6.dp))
            }
            // Mostra da semana anterior até três à frente: a lista inteira de
            // 50 semanas não ajuda a decidir o que fazer hoje.
            val recorte = semanas.drop((indiceAtual - 1).coerceAtLeast(0)).take(4)
            items(recorte, key = { it.numero }) { semana ->
                CartaoSemana(semana)
            }
        }
    }
}

@Composable
private fun Cobertura(cobertura: Map<String, Pair<Int, Int>>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .cartaoRabisco(18.dp)
            .padding(14.dp),
    ) {
        Text(
            text = "CONTEÚDO JÁ VISTO",
            style = MaterialTheme.typography.labelSmall,
            color = Primary,
        )
        Spacer(Modifier.height(10.dp))
        cobertura.forEach { (area, par) ->
            val (feitos, total) = par
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 7.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(corDaArea(area)),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = area.replace('-', ' '),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary,
                    modifier = Modifier.width(120.dp),
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(SurfaceRaised),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (total == 0) 0f else feitos.toFloat() / total)
                            .height(6.dp)
                            .background(corDaArea(area)),
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "$feitos/$total",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                )
            }
        }
    }
}

@Composable
private fun CartaoSemana(semana: SemanaCronograma) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .cartaoRabisco(18.dp, fundo = if (semana.atual) Primary.copy(alpha = 0.10f) else Surface, borda = if (semana.atual) Primary.copy(alpha = 0.5f) else SurfaceBorder)
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (semana.atual) "ESTA SEMANA" else "SEMANA ${semana.numero}",
                style = MaterialTheme.typography.labelSmall,
                color = if (semana.atual) Primary else TextMuted,
                modifier = Modifier.weight(1f),
            )
            if (semana.atrasada) {
                Text(
                    text = "atrasada",
                    style = MaterialTheme.typography.labelSmall,
                    color = Danger,
                )
            } else if (semana.temas.isNotEmpty() && semana.pendentes == 0) {
                Text(
                    text = "concluída",
                    style = MaterialTheme.typography.labelSmall,
                    color = Secondary,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        semana.temas.forEach { tema -> LinhaTema(tema) }
        if (semana.temas.isEmpty()) {
            Text(
                text = "Sem tema novo: use a semana para revisar.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
            )
        }
        if (semana.revisao.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "REVISAR: " + semana.revisao.take(4).joinToString(", ") { it.tema },
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
        }
    }
}

@Composable
private fun LinhaTema(tema: TemaDaSemana) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 3.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(if (tema.coberto) Secondary.copy(alpha = 0.25f) else SurfaceRaised),
        ) {
            if (tema.coberto) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "Coberto",
                    tint = Secondary,
                    modifier = Modifier.size(12.dp),
                )
            }
        }
        Spacer(Modifier.width(9.dp))
        Text(
            text = tema.tema,
            style = MaterialTheme.typography.bodyMedium,
            color = if (tema.coberto) TextMuted else TextPrimary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "${tema.totalQuestoes}",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
        )
    }
}
