package com.franferreira.enem2027.ui.screens

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.franferreira.enem2027.data.model.QuestaoPendente
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
 * Fila de aprovação das questões geradas por IA.
 *
 * Questão de IA NUNCA entra no estudo automaticamente: a IA gera, uma
 * segunda passada de IA valida, e só depois o usuário aprova. O gabarito
 * de uma questão oficial do Inep é outra história — aquele nunca é
 * questionado.
 */
@Composable
fun TelaAprovacao(
    itens: List<QuestaoPendente>,
    aoAprovar: (String) -> Unit,
    aoRejeitar: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundGradientTop, Background))),
    ) {
        Column(Modifier.padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "APROVAÇÃO",
                style = MaterialTheme.typography.labelSmall,
                color = Primary,
            )
            Text(
                text = "Questões criadas pela IA",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Nenhuma entra no seu estudo sem você aprovar. A IA pode errar o gabarito — por isso o seu aval.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
            )
            Spacer(Modifier.height(14.dp))
        }

        if (itens.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SurfaceRaised),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = Secondary,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Nada na fila",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Quando a IA gerar questões, elas aparecem aqui para você decidir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(itens, key = { it.id }) { item ->
                CartaoPendente(item, aoAprovar, aoRejeitar)
            }
        }
    }
}

@Composable
private fun CartaoPendente(
    item: QuestaoPendente,
    aoAprovar: (String) -> Unit,
    aoRejeitar: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Surface)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(18.dp))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = item.tema.ifBlank { "Sem tema" },
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        when {
                            item.confianca >= 0.7 -> Secondary.copy(alpha = 0.16f)
                            item.confianca >= 0.4 -> Primary.copy(alpha = 0.16f)
                            else -> Danger.copy(alpha = 0.16f)
                        }
                    )
                    .padding(horizontal = 9.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "confiança ${(item.confianca * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                        item.confianca >= 0.7 -> Secondary
                        item.confianca >= 0.4 -> Primary
                        else -> Danger
                    },
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        Text(
            text = item.enunciado.ifBlank { "(sem enunciado)" },
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
        )

        Spacer(Modifier.height(10.dp))
        item.alternativas.forEach { alternativa ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = "${alternativa.letra})",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (alternativa.letra == item.gabarito) Secondary else TextMutedDark,
                    modifier = Modifier.width(22.dp),
                )
                Text(
                    text = alternativa.texto,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (alternativa.letra == item.gabarito) TextSecondary else TextMutedDark,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        if (item.explicacao.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceRaised)
                    .padding(10.dp),
            ) {
                Text(
                    text = item.explicacao,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(SurfaceRaised)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(18.dp))
                    .clickable { aoRejeitar(item.id) },
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = null,
                        tint = Danger,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Rejeitar",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Secondary, Primary)
                        )
                    )
                    .clickable { aoAprovar(item.id) },
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = com.franferreira.enem2027.ui.theme.PaletaClaro.texto,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Aprovar",
                        style = MaterialTheme.typography.labelMedium,
                        color = com.franferreira.enem2027.ui.theme.PaletaClaro.texto,
                    )
                }
            }
        }
    }
}
