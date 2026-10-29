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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.franferreira.enem2027.data.model.Onboarding
import com.franferreira.enem2027.ui.components.BotaoGamificado
import com.franferreira.enem2027.ui.components.floresFundo
import com.franferreira.enem2027.ui.components.cartaoRabisco
import com.franferreira.enem2027.ui.theme.Background
import com.franferreira.enem2027.ui.theme.BackgroundGradientTop
import com.franferreira.enem2027.ui.theme.PaletaClaro
import com.franferreira.enem2027.ui.theme.Primary
import com.franferreira.enem2027.ui.theme.Surface
import com.franferreira.enem2027.ui.theme.SurfaceBorder
import com.franferreira.enem2027.ui.theme.SurfaceRaised
import com.franferreira.enem2027.ui.theme.TextMuted
import com.franferreira.enem2027.ui.theme.TextPrimary
import com.franferreira.enem2027.ui.theme.TextSecondary

/**
 * Primeiro acesso, depois do nome: metas, agenda, notificação e a apresentação
 * do diagnóstico. Cada etapa diz o que está pedindo e por quê. Só o diagnóstico
 * é obrigatório; as três primeiras têm "Pular".
 */
@Composable
fun TelaOnboarding(
    etapa: String,
    agenda: Set<Pair<Int, String>>,
    aoSalvarMetas: (Int?, Int?) -> Unit,
    aoAlternarAgenda: (Int, String) -> Unit,
    aoSalvarAgenda: () -> Unit,
    aoPular: () -> Unit,
    aoAvancar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val indice = Onboarding.ETAPAS.indexOf(etapa).coerceAtLeast(0)
    val total = Onboarding.ETAPAS.size
    var nota by remember { mutableStateOf<Int?>(null) }
    var ano by remember { mutableStateOf<Int?>(null) }
    val lancadorPermissao = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { aoAvancar() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundGradientTop, Background)))
            .floresFundo(alpha = 0.35f)
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(48.dp))
        BarraDeProgresso(atual = indice, total = total)
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Passo ${indice + 1} de $total",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
        )
        Spacer(Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            when (etapa) {
                "metas" -> PassoMetas(
                    nota = nota,
                    aoNota = { nota = it },
                    ano = ano,
                    aoAno = { ano = it },
                )
                "agenda" -> PassoAgenda(
                    agenda = agenda,
                    aoAlternar = aoAlternarAgenda,
                )
                "notificacao" -> PassoNotificacao()
                else -> PassoDiagnostico()
            }
        }

        Spacer(Modifier.height(12.dp))
        when (etapa) {
            "metas" -> Acoes(
                principal = "Continuar",
                aoPrincipal = { aoSalvarMetas(nota, ano) },
                aoPular = aoPular,
                rotuloPular = "Pular por enquanto",
            )
            "agenda" -> Acoes(
                principal = "Salvar e continuar",
                aoPrincipal = aoSalvarAgenda,
                aoPular = aoPular,
                rotuloPular = "Pular por enquanto",
            )
            "notificacao" -> Acoes(
                principal = "Permitir lembretes",
                aoPrincipal = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        lancadorPermissao.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        aoAvancar()
                    }
                },
                aoPular = aoAvancar,
                rotuloPular = "Agora não",
            )
            else -> Acoes(
                principal = "Começar o diagnóstico",
                aoPrincipal = aoAvancar,
                aoPular = null,
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun BarraDeProgresso(atual: Int, total: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        repeat(total) { indice ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (indice <= atual) Primary else SurfaceRaised),
            )
        }
    }
}

@Composable
private fun Cabecalho(icone: ImageVector, titulo: String, texto: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Primary.copy(alpha = 0.15f)),
    ) {
        Icon(icone, contentDescription = null, tint = Primary, modifier = Modifier.size(34.dp))
    }
    Spacer(Modifier.height(18.dp))
    Text(
        text = titulo,
        style = MaterialTheme.typography.headlineMedium,
        color = TextPrimary,
    )
    Spacer(Modifier.height(10.dp))
    Text(
        text = texto,
        style = MaterialTheme.typography.bodyLarge,
        color = TextSecondary,
    )
    Spacer(Modifier.height(20.dp))
}

/** Por que a pergunta está sendo feita: o que a pessoa ganha com a resposta. */
@Composable
private fun PorQueCard(itens: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .cartaoRabisco(18.dp)
            .padding(16.dp),
    ) {
        Text(
            text = "POR QUE ESTOU PERGUNTANDO",
            style = MaterialTheme.typography.labelSmall,
            color = Primary,
        )
        Spacer(Modifier.height(10.dp))
        itens.forEach { item ->
            Row(modifier = Modifier.padding(bottom = 8.dp)) {
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Primary),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                )
            }
        }
    }
}

@Composable
private fun PassoMetas(
    nota: Int?,
    aoNota: (Int?) -> Unit,
    ano: Int?,
    aoAno: (Int?) -> Unit,
) {
    Cabecalho(
        icone = Icons.Filled.Star,
        titulo = "Qual é a sua meta?",
        texto = "Escolha a nota que você quer e o ano da prova. Eu uso isso para mostrar " +
            "quanto falta e quantos dias por semana valem a pena.",
    )
    Text(
        text = "NOTA-ALVO",
        style = MaterialTheme.typography.labelSmall,
        color = TextMuted,
    )
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(600, 650, 700, 750, 800).forEach { valor ->
            Escolha(
                texto = valor.toString(),
                selecionada = nota == valor,
                aoClicar = { aoNota(if (nota == valor) null else valor) },
                modifier = Modifier.weight(1f),
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
                aoClicar = { aoAno(if (ano == valor) null else valor) },
                modifier = Modifier.weight(1f),
            )
        }
    }
    Spacer(Modifier.height(22.dp))
    PorQueCard(
        listOf(
            "A nota-alvo aparece no resultado do diagnóstico, com a distância até ela.",
            "O ano da prova diz quantos dias por semana você precisa estudar.",
            "Se pular, dá para escolher depois, na aba Nota.",
        ),
    )
}

@Composable
private fun PassoAgenda(
    agenda: Set<Pair<Int, String>>,
    aoAlternar: (Int, String) -> Unit,
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

    Cabecalho(
        icone = Icons.Filled.DateRange,
        titulo = "Quando você consegue estudar?",
        texto = "Marque os períodos que cabem na sua semana. É neles que eu te chamo.",
    )
    dias.forEach { (nomeDia, indice) ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp),
        ) {
            Text(
                text = nomeDia,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                modifier = Modifier.width(84.dp),
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
    Spacer(Modifier.height(14.dp))
    PorQueCard(
        listOf(
            "Só mando lembrete nos períodos que você marcar.",
            "A agenda também organiza a sessão do dia.",
            "Dá para mudar depois, na aba Agenda.",
        ),
    )
}

@Composable
private fun PassoNotificacao() {
    Cabecalho(
        icone = Icons.Filled.Notifications,
        titulo = "Posso te lembrar?",
        texto = "Um aviso no período que você marcou, dizendo quantas questões estão " +
            "vencendo para revisar.",
    )
    PorQueCard(
        listOf(
            "Nada chega fora dos horários que você escolheu.",
            "Dá para silenciar em horas específicas, na aba Agenda.",
            "Se preferir não, o app funciona igual, só sem lembrete.",
        ),
    )
}

@Composable
private fun PassoDiagnostico() {
    Cabecalho(
        icone = Icons.Filled.Check,
        titulo = "Vamos ver por onde começar",
        texto = "São 40 questões, 10 de cada área do ENEM. Leva uns 15 minutos. " +
            "Não é prova: é para eu entender onde você está forte e onde precisa treinar.",
    )
    listOf(
        "Responda uma questão por vez. Não tem gabarito na hora.",
        "No fim, você vê pontos fortes, pontos fracos e uma previsão de nota.",
        "Os temas fracos entram primeiro nos seus treinos.",
    ).forEachIndexed { indice, texto ->
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(bottom = 14.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Primary),
            ) {
                Text(
                    text = "${indice + 1}",
                    style = MaterialTheme.typography.labelMedium,
                    color = PaletaClaro.texto,
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = texto,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                modifier = Modifier.weight(1f),
            )
        }
    }
    Text(
        text = "Suas respostas contam no seu progresso, como qualquer treino.",
        style = MaterialTheme.typography.bodySmall,
        color = TextMuted,
    )
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
            .cartaoRabisco(999.dp, fundo = if (selecionada) Primary else SurfaceRaised, borda = if (selecionada) Primary else SurfaceBorder)
            .clickable { aoClicar() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            color = if (selecionada) PaletaClaro.texto else TextPrimary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun Acoes(
    principal: String,
    aoPrincipal: () -> Unit,
    aoPular: (() -> Unit)?,
    rotuloPular: String = "Pular",
) {
    BotaoGamificado(
        texto = principal,
        cor = Primary,
        aoClicar = aoPrincipal,
    )
    if (aoPular != null) {
        Spacer(Modifier.height(10.dp))
        Text(
            text = rotuloPular,
            style = MaterialTheme.typography.labelLarge,
            color = TextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { aoPular() }
                .padding(vertical = 10.dp),
        )
    }
}
