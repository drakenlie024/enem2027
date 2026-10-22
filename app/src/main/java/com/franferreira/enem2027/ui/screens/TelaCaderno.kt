package com.franferreira.enem2027.ui.screens

import com.franferreira.enem2027.ui.theme.Primary
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
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.franferreira.enem2027.data.model.ItemCaderno
import com.franferreira.enem2027.ui.theme.Background
import com.franferreira.enem2027.ui.theme.BackgroundGradientTop
import com.franferreira.enem2027.ui.theme.Danger
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
 * O caderno de erros: o histórico do que já foi errado.
 *
 * Diferente da fila de revisão, que é sobre o que VENCE agora. Aqui é a
 * memória do erro — a lista do que você precisa releer porque errou antes.
 * É onde a repetição passiva vira aprendizado de verdade.
 */
@Composable
fun TelaCaderno(
    itens: List<ItemCaderno>,
    aoAbrir: (String) -> Unit,
    aoVoltar: () -> Unit,
) {
    var areaFiltro by remember { mutableStateOf<String?>(null) }
    var soReincidentes by remember { mutableStateOf(false) }

    val areas = remember(itens) { itens.mapNotNull { it.area }.distinct() }
    val filtrados = remember(itens, areaFiltro, soReincidentes) {
        itens.filter { item ->
            (areaFiltro == null || item.area == areaFiltro) &&
                (!soReincidentes || item.reincidente)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundGradientTop, Background))),
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
                    text = "CADERNO DE ERROS",
                    style = MaterialTheme.typography.labelSmall,
                    color = com.franferreira.enem2027.ui.theme.Primary,
                )
                Text(
                    text = if (itens.isEmpty()) {
                        "Nada anotado ainda"
                    } else {
                        "${itens.size} questões que você já errou"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                )
            }
        }

        if (itens.isNotEmpty()) {
            Column(Modifier.padding(horizontal = 16.dp)) {
                // filtro por área
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ChipCaderno(
                        texto = "Todas",
                        selecionado = areaFiltro == null,
                        aoClicar = { areaFiltro = null },
                    )
                    areas.forEach { area ->
                        ChipCaderno(
                            texto = area.replace('-', ' ').replaceFirstChar { it.uppercase() },
                            selecionado = areaFiltro == area,
                            cor = corDaArea(area),
                            aoClicar = { areaFiltro = if (areaFiltro == area) null else area },
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // só as que erraram mais de uma vez
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (soReincidentes) Danger.copy(alpha = 0.16f) else SurfaceRaised)
                        .clickable { soReincidentes = !soReincidentes }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (soReincidentes) Danger else TextMutedDark),
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        text = "Só as que eu errei mais de uma vez",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (soReincidentes) Danger else TextMuted,
                    )
                }
                Spacer(Modifier.height(10.dp))
            }
        }

        if (filtrados.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (itens.isEmpty()) {
                            "Seu caderno está vazio.\nCada questão errada entra aqui para você revisar depois."
                        } else {
                            "Nada com esse filtro."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            return
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(filtrados, key = { it.id }) { item ->
                CartaoErro(item = item, aoClicar = { aoAbrir(item.id) })
            }
        }
    }
}

@Composable
private fun CartaoErro(item: ItemCaderno, aoClicar: () -> Unit) {
    val cor = corDaArea(item.area)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Surface)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(18.dp))
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
            Spacer(Modifier.width(9.dp))
            Text(
                text = "${item.ano} · ${item.tema ?: item.area ?: "Revisão"}",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            // reincidência: errou mais de uma vez
            if (item.reincidente) {
                Text(
                    text = "${item.vezesErrada}× errada",
                    style = MaterialTheme.typography.labelSmall,
                    color = Danger,
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = item.enunciado?.take(150) ?: "(questão com figura)",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Gabarito ${item.gabarito ?: "—"}",
                style = MaterialTheme.typography.labelSmall,
                color = Secondary,
            )
            Spacer(Modifier.width(10.dp))
            if (item.vezesAcertada > 0) {
                Text(
                    text = "já acertou ${item.vezesAcertada}×",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMutedDark,
                )
            }
        }
    }
}

@Composable
private fun ChipCaderno(
    texto: String,
    selecionado: Boolean,
    cor: androidx.compose.ui.graphics.Color = com.franferreira.enem2027.ui.theme.Primary,
    aoClicar: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selecionado) cor.copy(alpha = 0.18f) else SurfaceRaised)
            .border(
                1.dp,
                if (selecionado) cor.copy(alpha = 0.5f) else SurfaceBorder,
                CircleShape,
            )
            .clickable { aoClicar() }
            .padding(horizontal = 13.dp, vertical = 6.dp),
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            color = if (selecionado) cor else TextMuted,
        )
    }
}
