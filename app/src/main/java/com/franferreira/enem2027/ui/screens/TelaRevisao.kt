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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.franferreira.enem2027.ui.components.cartaoRabisco
import com.franferreira.enem2027.ui.theme.Background
import com.franferreira.enem2027.ui.theme.BackgroundGradientTop
import com.franferreira.enem2027.ui.theme.Danger
import com.franferreira.enem2027.ui.theme.PaletaClaro
import com.franferreira.enem2027.ui.theme.corDaArea
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
    aoAbrir: (Question) -> Unit,
    aoSincronizar: () -> Unit,
    sincronizando: Boolean,
    treinos: List<com.franferreira.enem2027.data.model.TemaTreino> = emptyList(),
    treinoAberto: com.franferreira.enem2027.data.model.TemaTreino? = null,
    treinoQuestoes: List<Question> = emptyList(),
    aoAbrirTreino: (com.franferreira.enem2027.data.model.TemaTreino) -> Unit = {},
    aoFecharTreino: () -> Unit = {},
    buscaTexto: String = "",
    resultadoBusca: List<Question> = emptyList(),
    aoBuscar: (String) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundGradientTop, Background))),
    ) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (treinoAberto != null) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = TextMuted,
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { aoFecharTreino() },
                    )
                    Spacer(Modifier.width(10.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        text = if (treinoAberto != null) "TREINO" else "TREINOS DE HOJE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary,
                    )
                    Text(
                        text = treinoAberto?.tema ?: "O que estudar agora",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
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

            Spacer(Modifier.height(6.dp))
            Text(
                text = if (treinoAberto != null) {
                    "${treinoQuestoes.size} questões novas deste tema"
                } else {
                    "O app escolhe os temas que vencem hoje, e sempre traz questão nova."
                },
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
            )

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                    shape = RoundedCornerShape(14.dp),
                value = buscaTexto,
                onValueChange = aoBuscar,
                singleLine = true,
                placeholder = { Text("Buscar no acervo (texto, tema)", color = TextMuted) },
                textStyle = MaterialTheme.typography.bodyMedium,
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = SurfaceBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = Primary,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(14.dp))
        }

        val buscando = buscaTexto.trim().length >= 2
        when {
            buscando -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        Text(
                            text = if (resultadoBusca.isEmpty()) {
                                "Nenhuma questão com esse texto"
                            } else {
                                "${resultadoBusca.size} questões encontradas"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                        )
                    }
                    items(resultadoBusca) { questao ->
                        CartaoFila(questao, Primary) { aoAbrir(questao) }
                    }
                }
            }

            treinoAberto != null -> {
                if (treinoQuestoes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Este tema já foi todo treinado. Volte quando o intervalo vencer.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(treinoQuestoes, key = { it.id }) { q ->
                            CartaoFila(q, Primary) { aoAbrir(q) }
                        }
                    }
                }
            }

            treinos.isEmpty() -> {
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
                            text = "Tudo em dia",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Nenhum tema venceu hoje. Você pode treinar uma trilha ou assistir uma aula.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(treinos, key = { it.tema + it.area }) { t ->
                        CartaoTreino(t, aoClicar = { aoAbrirTreino(t) })
                    }
                }
            }
        }
    }
}

/** Um tema devido hoje: o que treinar, quanto já rendeu e o botão. */
@Composable
private fun CartaoTreino(
    t: com.franferreira.enem2027.data.model.TemaTreino,
    aoClicar: () -> Unit,
) {
    val cor = corDaArea(t.area)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .cartaoRabisco(18.dp)
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(cor),
            )
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = t.tema,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = t.area.replace('-', ' ') + " · " + t.disponiveis + " questões disponíveis",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                )
            }
            Text(
                text = if (t.novo) "NOVO" else "${t.percentual}%",
                style = MaterialTheme.typography.labelMedium,
                color = cor,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (t.novo) {
                "Nunca treinado. Começa pelas questões mais antigas."
            } else {
                val quando = t.diasDesde?.let { "há $it dia(s)" } ?: "agora"
                "Último treino $quando · volta a cada ${t.intervaloDias} dias"
            },
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
        )
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(cor)
                .clickable { aoClicar() }
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Text(
                text = "Treinar",
                style = MaterialTheme.typography.labelMedium,
                color = PaletaClaro.texto,
            )
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
            .clip(RoundedCornerShape(20.dp))
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    listOf(cor.copy(alpha = 0.16f), Surface),
                )
            )
            .border(1.dp, cor.copy(alpha = 0.30f), RoundedCornerShape(20.dp))
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
            .cartaoRabisco(16.dp)
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
                    .clip(RoundedCornerShape(12.dp))
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
