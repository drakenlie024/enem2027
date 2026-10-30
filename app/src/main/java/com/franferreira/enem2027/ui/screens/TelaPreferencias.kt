package com.franferreira.enem2027.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.franferreira.enem2027.data.model.Diagnostico
import com.franferreira.enem2027.data.model.TemaDaSemana
import com.franferreira.enem2027.data.repo.PreferenciasEstudo
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
 * O que a aluna gosta mais e menos. Toque no tema para girar entre
 * Normal, Mais e Menos. "Mais" sobe no cronograma e na aula do dia; "menos"
 * desce. Nenhum tema some do plano.
 */
@Composable
fun TelaPreferencias(
    temas: List<TemaDaSemana>,
    preferencias: Map<String, PreferenciasEstudo.Nivel>,
    aoMudar: (TemaDaSemana, PreferenciasEstudo.Nivel) -> Unit,
    aoVoltar: () -> Unit,
) {
    val porArea = Diagnostico.AREAS.mapNotNull { area ->
        temas.filter { it.area == area }
            .sortedBy { it.tema }
            .takeIf { it.isNotEmpty() }
            ?.let { area to it }
    }

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
                    text = "PREFERÊNCIAS",
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary,
                )
                Text(
                    text = "O que você gosta",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text(
                    text = "Toque no tema para girar: Normal, Mais ou Menos. O que você marca como Mais vem antes no cronograma e na aula do dia.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                )
                Spacer(Modifier.height(4.dp))
            }
            porArea.forEach { (area, lista) ->
                item(key = "area-$area") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(corDaArea(area)),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = area.replace('-', ' ').uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                        )
                    }
                }
                items(lista, key = { "${it.area}|${it.tema}" }) { tema ->
                    val nivel = preferencias[Diagnostico.chaveTema(tema.area, tema.tema)]
                        ?: PreferenciasEstudo.Nivel.NORMAL
                    LinhaPreferencia(
                        tema = tema,
                        nivel = nivel,
                        aoClicar = { aoMudar(tema, nivel.proximo()) },
                    )
                }
            }
        }
    }
}

@Composable
private fun LinhaPreferencia(
    tema: TemaDaSemana,
    nivel: PreferenciasEstudo.Nivel,
    aoClicar: () -> Unit,
) {
    val fundo = when (nivel) {
        PreferenciasEstudo.Nivel.MAIS -> Primary
        else -> SurfaceRaised
    }
    val texto = when (nivel) {
        PreferenciasEstudo.Nivel.MAIS -> PaletaClaro.texto
        else -> TextMuted
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .cartaoRabisco(14.dp)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(
            text = tema.tema,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(fundo)
                .clickable { aoClicar() }
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text(
                text = nivel.rotulo,
                style = MaterialTheme.typography.labelMedium,
                color = texto,
            )
        }
    }
}
