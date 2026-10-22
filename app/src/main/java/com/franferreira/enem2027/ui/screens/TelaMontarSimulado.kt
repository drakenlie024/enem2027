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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.franferreira.enem2027.ui.components.BotaoGamificado
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

/**
 * Tela de montagem do simulado: escolhe o tipo, a área e o tamanho.
 *
 * O botão adaptativo é o mais interessante: ele monta a prova priorizando
 * as questões que você já errou.
 */
@Composable
fun TelaMontarSimulado(
    anosDisponiveis: List<Int>,
    carregando: Boolean,
    diagnosticoPrevio: String?,
    aoIniciar: (tipo: String, area: String?, ano: Int?, quantidade: Int) -> Unit,
) {
    var tipo by remember { mutableStateOf("area") }
    var area by remember { mutableStateOf("matematica") }
    var ano by remember { mutableStateOf<Int?>(null) }
    var quantidade by remember { mutableStateOf(20) }

    val areas = listOf(
        "matematica" to "Matemática",
        "linguagens" to "Linguagens",
        "ciencias-humanas" to "Ciências Humanas",
        "ciencias-natureza" to "Ciências da Natureza",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundGradientTop, Background)))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(12.dp))
        Text(
            text = "SIMULADO",
            style = MaterialTheme.typography.labelSmall,
            color = Primary,
        )
        Text(
            text = "Monte uma prova",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
        )
        Spacer(Modifier.height(18.dp))

        // tipo
        Text("TIPO", style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Spacer(Modifier.height(8.dp))
        listOf(
            "area" to "Por área",
            "completo" to "Prova completa",
            "adaptativo" to "Adaptativo (prioriza seus erros)",
        ).forEach { (chave, rotulo) ->
            val selecionado = tipo == chave
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selecionado) Primary.copy(alpha = 0.14f) else Surface)
                    .border(
                        1.dp,
                        if (selecionado) Primary.copy(alpha = 0.5f) else SurfaceBorder,
                        RoundedCornerShape(12.dp),
                    )
                    .clickable { tipo = chave }
                    .padding(13.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(if (selecionado) Primary else SurfaceRaised),
                    contentAlignment = Alignment.Center,
                ) {
                    if (selecionado) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Background),
                        )
                    }
                }
                Spacer(Modifier.width(11.dp))
                Text(
                    text = rotulo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selecionado) TextPrimary else TextSecondary,
                    modifier = Modifier.weight(1f),
                )
                if (chave == "adaptativo") {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(15.dp),
                    )
                }
            }
        }

        if (tipo == "area") {
            Spacer(Modifier.height(12.dp))
            Text("ÁREA", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Spacer(Modifier.height(8.dp))
            areas.forEach { (chave, rotulo) ->
                val selecionado = area == chave
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selecionado) Primary.copy(alpha = 0.14f) else Surface)
                        .border(
                            1.dp,
                            if (selecionado) Primary.copy(alpha = 0.5f) else SurfaceBorder,
                            RoundedCornerShape(10.dp),
                        )
                        .clickable { area = chave }
                        .padding(12.dp),
                ) {
                    Text(
                        text = rotulo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selecionado) TextPrimary else TextSecondary,
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("ANO", style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            listOf(null to "Todos").plus(anosDisponiveis.take(7).map { it to it.toString() })
                .forEach { (valor, rotulo) ->
                    val selecionado = ano == valor
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (selecionado) Primary.copy(alpha = 0.16f) else Surface)
                            .border(
                                1.dp,
                                if (selecionado) Primary.copy(alpha = 0.5f) else SurfaceBorder,
                                RoundedCornerShape(9.dp),
                            )
                            .clickable { ano = valor },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = rotulo,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selecionado) TextPrimary else TextMuted,
                        )
                    }
                }
        }

        Spacer(Modifier.height(12.dp))
        Text("QUANTIDADE", style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(10, 20, 45, 60).forEach { n ->
                val selecionado = quantidade == n
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (selecionado) Primary.copy(alpha = 0.16f) else Surface)
                        .border(
                            1.dp,
                            if (selecionado) Primary.copy(alpha = 0.5f) else SurfaceBorder,
                            RoundedCornerShape(9.dp),
                        )
                        .clickable { quantidade = n },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = n.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selecionado) TextPrimary else TextMuted,
                    )
                }
            }
        }

        diagnosticoPrevio?.let {
            Spacer(Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceRaised)
                    .padding(12.dp),
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        if (carregando) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceBorder),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Secondary,
                    strokeWidth = 2.dp,
                )
            }
        } else {
            BotaoGamificado(
                texto = "COMEÇAR SIMULADO",
                cor = Primary,
                aoClicar = {
                    aoIniciar(tipo, if (tipo == "area") area else null, ano, quantidade)
                },
            )
        }

        Spacer(Modifier.height(30.dp))
    }
}
