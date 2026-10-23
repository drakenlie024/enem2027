package com.franferreira.enem2027.ui.screens

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import com.franferreira.enem2027.data.model.Onboarding
import com.franferreira.enem2027.ui.theme.Background
import com.franferreira.enem2027.ui.theme.BackgroundGradientTop
import com.franferreira.enem2027.ui.theme.PaletaClaro
import com.franferreira.enem2027.ui.theme.Primary
import com.franferreira.enem2027.ui.theme.Surface
import com.franferreira.enem2027.ui.theme.SurfaceBorder
import com.franferreira.enem2027.ui.theme.SurfaceRaised
import com.franferreira.enem2027.ui.theme.TextMuted
import com.franferreira.enem2027.ui.theme.TextPrimary

/**
 * Primeiro acesso, antes do diagnóstico: metas, agenda e notificação.
 * Cada etapa tem "Pular"; nenhuma trava o app.
 */
@Composable
fun TelaOnboarding(
    etapa: String,
    agenda: Set<Pair<Int, String>>,
    aoSalvarMetas: (Int?, Int?) -> Unit,
    aoAlternarAgenda: (Int, String) -> Unit,
    aoSalvarAgenda: () -> Unit,
    aoPular: () -> Unit,
    aoConcluirNotificacao: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundGradientTop, Background)))
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(12.dp))
        Text(
            text = "PRIMEIRO ACESSO · ${Onboarding.ETAPAS.indexOf(etapa) + 1} de ${Onboarding.ETAPAS.size}",
            style = MaterialTheme.typography.labelSmall,
            color = Primary,
        )
        Spacer(Modifier.height(10.dp))
        when (etapa) {
            "metas" -> PassoMetas(aoSalvar = aoSalvarMetas, aoPular = aoPular)
            "agenda" -> PassoAgenda(
                agenda = agenda,
                aoAlternar = aoAlternarAgenda,
                aoSalvar = aoSalvarAgenda,
                aoPular = aoPular,
            )
            else -> PassoNotificacao(aoConcluir = aoConcluirNotificacao)
        }
    }
}

@Composable
private fun PassoMetas(aoSalvar: (Int?, Int?) -> Unit, aoPular: () -> Unit) {
    var nota by remember { mutableStateOf<Int?>(null) }
    var ano by remember { mutableStateOf<Int?>(null) }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Text(
                text = "Para onde você mira?",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Opcional. Serve para mostrar quanto falta para a sua meta.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
            )
            Spacer(Modifier.height(18.dp))
            Text(
                text = "NOTA-ALVO (GERAL)",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(600, 650, 700, 750, 800).forEach { valor ->
                    Escolha(
                        texto = valor.toString(),
                        selecionada = nota == valor,
                        aoClicar = { nota = valor },
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            Text(
                text = "ANO DA PROVA",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(2026, 2027).forEach { valor ->
                    Escolha(
                        texto = "ENEM $valor",
                        selecionada = ano == valor,
                        aoClicar = { ano = valor },
                    )
                }
            }
        }
        Botoes(
            principal = "Continuar",
            aoPrincipal = { aoSalvar(nota, ano) },
            aoPular = aoPular,
        )
    }
}

@Composable
private fun PassoAgenda(
    agenda: Set<Pair<Int, String>>,
    aoAlternar: (Int, String) -> Unit,
    aoSalvar: () -> Unit,
    aoPular: () -> Unit,
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
    val periodos = listOf("Manhã" to "manha", "Tarde" to "tarde", "Noite" to "noite")

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Text(
                text = "Quando você consegue estudar?",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Marque os períodos que cabem na sua rotina. O lembrete chega no período marcado.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
            )
            Spacer(Modifier.height(16.dp))
            dias.forEach { (nomeDia, indice) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp),
                ) {
                    Text(
                        text = nomeDia,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        modifier = Modifier.width(74.dp),
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        periodos.forEach { (rotulo, chave) ->
                            Escolha(
                                texto = rotulo,
                                selecionada = (indice to chave) in agenda,
                                aoClicar = { aoAlternar(indice, chave) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
        Botoes(
            principal = "Salvar e continuar",
            aoPrincipal = aoSalvar,
            aoPular = aoPular,
        )
    }
}

@Composable
private fun PassoNotificacao(aoConcluir: () -> Unit) {
    val lancador = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { aoConcluir() }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Text(
                text = "Lembrete no seu horário",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "O app manda um lembrete no período que você marcou na agenda, " +
                    "com quantas questões estão vencendo. Você pode silenciar em horas " +
                    "específicas, e desligar tudo quando quiser.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
            )
        }
        Botoes(
            principal = "Permitir lembretes",
            aoPrincipal = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    lancador.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    aoConcluir()
                }
            },
            aoPular = aoConcluir,
            rotuloPular = "Agora não",
        )
    }
}

@Composable
private fun Escolha(
    texto: String,
    selecionada: Boolean,
    aoClicar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selecionada) Primary else SurfaceRaised)
            .border(
                1.dp,
                if (selecionada) Primary else SurfaceBorder,
                RoundedCornerShape(999.dp),
            )
            .clickable { aoClicar() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            color = if (selecionada) PaletaClaro.texto else TextPrimary,
        )
    }
}

@Composable
private fun Botoes(
    principal: String,
    aoPrincipal: () -> Unit,
    aoPular: () -> Unit,
    rotuloPular: String = "Pular",
) {
    Column(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 20.dp)) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(999.dp))
                .background(Primary)
                .clickable { aoPrincipal() }
                .padding(vertical = 14.dp),
        ) {
            Text(
                text = principal,
                style = MaterialTheme.typography.labelLarge,
                color = PaletaClaro.texto,
            )
        }
        Spacer(Modifier.height(8.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(999.dp))
                .background(Surface)
                .clickable { aoPular() }
                .padding(vertical = 12.dp),
        ) {
            Text(
                text = rotuloPular,
                style = MaterialTheme.typography.labelLarge,
                color = TextMuted,
            )
        }
    }
}
