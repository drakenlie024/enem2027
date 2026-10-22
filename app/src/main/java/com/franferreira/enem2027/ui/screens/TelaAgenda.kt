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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.franferreira.enem2027.notificacao.Notificador
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
 * Tela de Agenda: quando o usuário pode estudar.
 *
 * O app não manda notificação em horário aleatório — ele pergunta "quando
 * você consegue?" e usa a resposta pra montar a sessão e o aviso.
 */
@Composable
fun TelaAgenda(
    diasSelecionados: Set<Pair<Int, String>>,
    aoAlternar: (Int, String) -> Unit,
    aoSalvar: () -> Unit,
    mensagem: String? = null,
) {
    val dias = listOf(
        "Segunda" to 0,
        "Terça" to 1,
        "Quarta" to 2,
        "Quinta" to 3,
        "Sexta" to 4,
        "Sábado" to 5,
        "Domingo" to 6,
    )
    val periodos = listOf(
        "Manhã" to "manha",
        "Tarde" to "tarde",
        "Noite" to "noite",
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
            text = "AGENDA",
            style = MaterialTheme.typography.labelSmall,
            color = Primary,
        )
        Text(
            text = "Quando você consegue estudar",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Marque os períodos que cabem na sua rotina. O app usa isso para montar a sessão do dia e para te avisar na hora certa.",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
        )

        Spacer(Modifier.height(18.dp))

        val contexto = LocalContext.current
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { }
        if (!Notificador.podeNotificar(contexto)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(com.franferreira.enem2027.ui.theme.Danger.copy(alpha = 0.12f))
                    .clickable {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                    .padding(12.dp),
            ) {
                Text(
                    text = "Os lembretes estão desativados. Toque aqui para permitir as notificações.",
                    style = MaterialTheme.typography.bodySmall,
                    color = com.franferreira.enem2027.ui.theme.Danger,
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        dias.forEach { (nome, indice) ->
            Text(
                text = nome.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TextMutedDark,
            )
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                periodos.forEach { (rotulo, chave) ->
                    val ativo = (indice to chave) in diasSelecionados
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (ativo) Primary.copy(alpha = 0.16f) else Surface)
                            .border(
                                1.dp,
                                if (ativo) Primary.copy(alpha = 0.5f) else SurfaceBorder,
                                RoundedCornerShape(10.dp),
                            )
                            .clickable { aoAlternar(indice, chave) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (ativo) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = Secondary,
                                    modifier = Modifier.size(13.dp),
                                )
                                Spacer(Modifier.width(5.dp))
                            }
                            Text(
                                text = rotulo,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (ativo) TextPrimary else TextMuted,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (diasSelecionados.isEmpty()) SurfaceBorder else Primary)
                .clickable(enabled = diasSelecionados.isNotEmpty()) { aoSalvar() },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (diasSelecionados.isEmpty()) {
                    "Escolha pelo menos um período"
                } else {
                    "Salvar agenda (${diasSelecionados.size} períodos)"
                },
                style = MaterialTheme.typography.labelLarge,
                color = if (diasSelecionados.isEmpty()) TextMuted else Background,
            )
        }

        // Confirmação. Sem isto, clicar em salvar parecia não fazer nada —
        // a mensagem só aparecia na tela de Hoje, que ninguém estava vendo.
        mensagem?.let { texto ->
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceRaised)
                    .padding(12.dp),
            ) {
                Text(
                    text = texto,
                    style = MaterialTheme.typography.bodySmall,
                    color = Secondary,
                )
            }
        }

        Spacer(Modifier.height(28.dp))
    }
}
