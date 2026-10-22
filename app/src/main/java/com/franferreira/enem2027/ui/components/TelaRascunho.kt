package com.franferreira.enem2027.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Undo
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.franferreira.enem2027.data.repo.RascunhoStore
import com.franferreira.enem2027.ui.theme.Danger
import com.franferreira.enem2027.ui.theme.Surface
import com.franferreira.enem2027.ui.theme.SurfaceBorder
import com.franferreira.enem2027.ui.theme.SurfaceRaised
import com.franferreira.enem2027.ui.theme.TextMuted
import com.franferreira.enem2027.ui.theme.TextPrimary

/**
 * Papel digital: o rascunho da questão.
 *
 * Sem isso, a conta de matemática vai para o papelão e morre com a resposta.
 * O traço fica colado na questão, e o app guarda por questão.
 *
 * As coordenadas são normalizadas (0 a 1), então o rascunho continua alinhado
 * se o aparelho girar ou a tela mudar de tamanho.
 */
@Composable
fun TelaRascunho(
    tracos: List<RascunhoStore.Traco>,
    aoMudar: (List<RascunhoStore.Traco>) -> Unit,
    aoFechar: () -> Unit,
) {
    val densidade = LocalDensity.current
    val paleta = remember {
        listOf(
            Color(0xFFE6E9F0), // grafite
            Color(0xFF6FA8FF), // azul
            Color(0xFFFF7A7A), // vermelho
            Color(0xFF3DDC97), // verde
        )
    }
    val espessuras = remember { listOf(2f, 4f, 8f) }

    var cor by remember { mutableStateOf(0) }
    var espessura by remember { mutableStateOf(1) }
    var tracoAtual by remember { mutableStateOf<List<RascunhoStore.Ponto>>(emptyList()) }
    var tamanho by remember { mutableStateOf(Size.Zero) }

    fun ponto(offset: Offset): RascunhoStore.Ponto {
        val w = if (tamanho.width > 0f) tamanho.width else 1f
        val h = if (tamanho.height > 0f) tamanho.height else 1f
        return RascunhoStore.Ponto(
            x = (offset.x / w).coerceIn(0f, 1f),
            y = (offset.y / h).coerceIn(0f, 1f),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface),
    ) {
        // ---- barra de ferramentas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "RASCUNHO",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                modifier = Modifier.weight(1f),
            )

            IconBotao(Icons.Filled.Undo, "Desfazer") {
                if (tracos.isNotEmpty()) aoMudar(tracos.dropLast(1))
            }
            Spacer(Modifier.width(6.dp))
            IconBotao(Icons.Filled.Delete, "Limpar") {
                aoMudar(emptyList())
            }
            Spacer(Modifier.width(10.dp))
            IconBotao(Icons.Filled.Close, "Fechar") { aoFechar() }
        }

        // ---- cores
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            paleta.forEachIndexed { indice, corCor ->
                Box(
                    modifier = Modifier
                        .size(if (indice == cor) 26.dp else 22.dp)
                        .padding(2.dp)
                        .background(corCor, CircleShape)
                        .border(
                            width = if (indice == cor) 2.dp else 0.dp,
                            color = if (indice == cor) TextPrimary else Color.Transparent,
                            shape = CircleShape,
                        )
                        .clickable { cor = indice },
                )
                Spacer(Modifier.width(10.dp))
            }
            Spacer(Modifier.width(6.dp))
            espessuras.forEachIndexed { indice, _ ->
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(
                            if (indice == espessura) TextPrimary.copy(alpha = 0.18f)
                            else SurfaceRaised
                        )
                        .border(
                            width = if (indice == espessura) 1.5.dp else 1.dp,
                            color = if (indice == espessura) TextPrimary else SurfaceBorder,
                            shape = CircleShape,
                        )
                        .clickable { espessura = indice },
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size((2 + indice * 3).dp)
                            .background(TextMuted, CircleShape),
                    )
                }
                Spacer(Modifier.width(8.dp))
            }
        }

        Spacer(Modifier.height(8.dp))

        // ---- área de desenho
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(SurfaceRaised)
                .onSizeChanged { tamanho = Size(it.width.toFloat(), it.height.toFloat()) }
                .pointerInput(cor, espessura) {
                    detectDragGestures(
                        onDragStart = { offset -> tracoAtual = listOf(ponto(offset)) },
                        onDrag = { change, _ ->
                            change.consume()
                            tracoAtual = tracoAtual + ponto(change.position)
                        },
                        onDragEnd = {
                            if (tracoAtual.size > 1) {
                                aoMudar(
                                    tracos + RascunhoStore.Traco(
                                        pontos = tracoAtual,
                                        cor = cor,
                                        espessura = espessuras[espessura],
                                    )
                                )
                            }
                            tracoAtual = emptyList()
                        },
                    )
                },
        ) {
            fun desenhar(traço: RascunhoStore.Traco) {
                if (traço.pontos.size < 2) return
                val caminho = Path()
                caminho.moveTo(
                    traço.pontos.first().x * size.width,
                    traço.pontos.first().y * size.height,
                )
                for (p in traço.pontos.drop(1)) {
                    caminho.lineTo(p.x * size.width, p.y * size.height)
                }
                drawPath(
                    path = caminho,
                    color = paleta.getOrElse(traço.cor) { paleta[0] },
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = traço.espessura * densidade.density,
                    ),
                )
            }

            tracos.forEach { desenhar(it) }
            if (tracoAtual.size > 1) {
                desenhar(
                    RascunhoStore.Traco(
                        pontos = tracoAtual,
                        cor = cor,
                        espessura = espessuras[espessura],
                    )
                )
            }
        }
    }
}

@Composable
private fun IconBotao(
    icone: androidx.compose.ui.graphics.vector.ImageVector,
    descricao: String,
    aoClicar: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(SurfaceRaised)
            .border(1.dp, SurfaceBorder, CircleShape)
            .clickable { aoClicar() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icone,
            contentDescription = descricao,
            tint = TextMuted,
            modifier = Modifier.size(17.dp),
        )
    }
}
