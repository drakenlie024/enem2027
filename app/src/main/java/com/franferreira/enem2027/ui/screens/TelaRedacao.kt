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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.franferreira.enem2027.ui.RascunhoRedacao
import com.franferreira.enem2027.ui.RedacaoSalva
import com.franferreira.enem2027.ui.components.BotaoGamificado
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.franferreira.enem2027.data.model.EssayCorrection
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Tela de redação.
 *
 * A correção vem de DOIS modelos que avaliam em paralelo; se divergirem
 * mais de 80 pontos, um terceiro desempata. O resultado mostra a FAIXA
 * (mínimo-máximo) e a confiança — porque nota de modelo de linguagem é
 * estimativa, e fingir precisão seria mentira.
 */
@Composable
fun TelaRedacao(
    temas: List<String>,
    aoCorrigir: suspend (tema: String, texto: String) -> Result<EssayCorrection>,
    historico: List<RedacaoSalva>,
    rascunho: RascunhoRedacao? = null,
    aoSalvarRascunho: (tema: String, texto: String) -> Unit = { _, _ -> },
) {
    var temaSelecionado by remember { mutableStateOf(rascunho?.tema ?: temas.firstOrNull() ?: "") }
    var texto by remember(rascunho?.id) { mutableStateOf(rascunho?.texto ?: "") }
    var corrigindo by remember { mutableStateOf(false) }
    var resultado by remember { mutableStateOf<EssayCorrection?>(null) }
    var erro by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Grava o rascunho no aparelho, um instante depois de o usuário parar
    // de digitar. A correção pela IA leva minutos — o texto não pode
    // depender da memória do app.
    LaunchedEffect(texto, temaSelecionado) {
        if (texto.isBlank()) return@LaunchedEffect
        delay(800)
        aoSalvarRascunho(temaSelecionado, texto)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundGradientTop, Background))),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "REDação",
                style = MaterialTheme.typography.labelSmall,
                color = Primary,
            )
            Text(
                text = "Dois corretores, um desempate",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
            )
            Spacer(Modifier.height(16.dp))

            // temas
            Text(
                text = "TEMA",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
            Spacer(Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                temas.take(6).forEach { tema ->
                    val selecionado = tema == temaSelecionado
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selecionado) Primary.copy(alpha = 0.16f) else Surface)
                            .border(
                                1.dp,
                                if (selecionado) Primary.copy(alpha = 0.5f) else SurfaceBorder,
                                RoundedCornerShape(10.dp),
                            )
                            .clickable { temaSelecionado = tema }
                            .padding(11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = tema,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (selecionado) TextPrimary else TextSecondary,
                            modifier = Modifier.weight(1f),
                        )
                        if (selecionado) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Secondary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // editor
            Text(
                text = "SEU TEXTO",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                placeholder = {
                    Text(
                        text = "Escreva sua redação aqui…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMutedDark,
                    )
                },
                minLines = 10,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = SurfaceBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = Primary,
                    focusedContainerColor = Surface,
                    unfocusedContainerColor = Surface,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(6.dp))
            Text(
                text = "${texto.length} caracteres · o ENEM pede entre 300 e 1500",
                style = MaterialTheme.typography.labelSmall,
                color = if (texto.isNotEmpty() && texto.length < 300) Danger else TextMutedDark,
            )

            erro?.let {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = Danger,
                )
            }

            Spacer(Modifier.height(14.dp))

            if (corrigindo) {
                // Gamificado: enquanto os corretores leem, o botão vira um
                // card que mostra o que está acontecendo.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(listOf(Color(0xFF1A1F2C), Surface))
                        )
                        .border(1.dp, Primary.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Primary,
                            strokeWidth = 2.dp,
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Dois corretores estão lendo…",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextPrimary,
                            )
                            Text(
                                text = "se divergirem, um terceiro desempata",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                            )
                        }
                    }
                }
            } else {
                BotaoGamificado(
                    texto = "CORRIGIR REDAÇÃO",
                    cor = Primary,
                    habilitado = texto.length >= 300 && temaSelecionado.isNotBlank(),
                    aoClicar = {
                        corrigindo = true
                        erro = null
                        resultado = null
                        scope.launch {
                            aoCorrigir(temaSelecionado, texto)
                                .onSuccess { resultado = it }
                                .onFailure {
                                    erro = "Não consegui corrigir agora. A IA pode estar ocupada — tente de novo em instantes."
                                }
                            corrigindo = false
                        }
                    },
                )
            }

            resultado?.let { correcao ->
                Spacer(Modifier.height(18.dp))
                CartaoCorrecao(correcao)
            }

            if (historico.isNotEmpty()) {
                Spacer(Modifier.height(18.dp))
                Text(
                    text = "SUAS REDAÇÕES",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                )
                Spacer(Modifier.height(8.dp))
                historico.take(6).forEach { redacao ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            // `.tema` e `.nota` por nome: desestruturar pegaria
                            // `id` e `tema` na ordem dos componentes.
                            text = redacao.tema,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = redacao.nota?.let { "$it" } ?: "—",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (redacao.nota != null) Secondary else TextMutedDark,
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CartaoCorrecao(correcao: EssayCorrection) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .padding(15.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "NOTA ESTIMADA",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${correcao.nota}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                    )
                    Text(
                        text = " / 1000",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }
            }
            FaixaConfianca(correcao)
        }

        Spacer(Modifier.height(10.dp))

        // faixa: a nota real é um intervalo, não um ponto
        Text(
            text = "Faixa estimada: ${correcao.notaMin} a ${correcao.notaMax}" +
                if (correcao.divergencia > 0) "  ·  divergência de ${correcao.divergencia} pontos entre os corretores" else "",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
        )

        if (correcao.usadoValidador) {
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(13.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Os corretores divergiram, então um terceiro desempatou.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary,
                )
            }
        }

        if (correcao.resumo.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = correcao.resumo,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }

        Spacer(Modifier.height(14.dp))

        correcao.competencias.forEach { competencia ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "C${competencia.numero}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary,
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${competencia.nota}/200",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary,
                        )
                        if (competencia.notasIndividuais.size > 1) {
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "( ${competencia.notasIndividuais.joinToString(" · ")} )",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMutedDark,
                            )
                        }
                    }
                    if (competencia.feedback.isNotBlank()) {
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = competencia.feedback,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                }
            }
        }

        correcao.avisos.forEach { aviso ->
            Spacer(Modifier.height(8.dp))
            Text(
                text = aviso,
                style = MaterialTheme.typography.labelSmall,
                color = Danger,
            )
        }
    }
}

@Composable
private fun FaixaConfianca(correcao: EssayCorrection) {
    val (cor, rotulo) = when (correcao.confianca) {
        "alta" -> Secondary to "Alta confiança"
        "baixa" -> Danger to "Baixa confiança"
        else -> Primary to "Confiança média"
    }
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(cor.copy(alpha = 0.15f))
            .padding(horizontal = 11.dp, vertical = 6.dp),
    ) {
        Text(
            text = rotulo,
            style = MaterialTheme.typography.labelSmall,
            color = cor,
        )
    }
}
