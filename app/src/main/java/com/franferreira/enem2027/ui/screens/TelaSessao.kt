package com.franferreira.enem2027.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.franferreira.enem2027.data.model.TemaTreino
import com.franferreira.enem2027.data.repo.AulasAssistidas
import com.franferreira.enem2027.data.repo.AulasPorTema
import com.franferreira.enem2027.ui.components.BarraGamificada
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
 * A sessão de hoje: um tema por vez, com a aula dele em cima e as questões
 * embaixo. Ao terminar um tema, o próximo já vem aberto.
 *
 * Antes o "Começar" abria uma lista de temas, e a pessoa tinha que decidir por
 * onde ir. Aqui ela só segue.
 */
@Composable
fun TelaSessao(
    temas: List<TemaTreino>,
    indice: Int,
    aoTreinar: (TemaTreino) -> Unit,
    aoAbrirAula: (String) -> Unit,
    aoEncerrar: () -> Unit,
) {
    val atual = temas.getOrNull(indice)
    if (atual == null) {
        // Sessão concluída: em vez de fechar sozinha no meio da composição,
        // mostra o fim e deixa ela sair quando quiser.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(BackgroundGradientTop, Background)))
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = if (temas.isEmpty()) "Nada para estudar agora" else "Sessão concluída",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (temas.isEmpty()) {
                    "Nenhum tema venceu hoje. Você pode abrir uma trilha ou assistir uma aula."
                } else {
                    "Você passou pelos ${temas.size} temas de hoje."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Primary)
                    .clickable { aoEncerrar() }
                    .padding(horizontal = 24.dp, vertical = 10.dp),
            ) {
                Text(
                    text = "Voltar para a Hoje",
                    style = MaterialTheme.typography.labelLarge,
                    color = PaletaClaro.texto,
                )
            }
        }
        return
    }

    val contexto = LocalContext.current
    // Sem cache: ao voltar da aula, o passo 1 já sai da tela.
    val aula = AulasPorTema.aulasDoTema(contexto, atual.tema)
        .firstOrNull { !AulasAssistidas.foiAssistida(contexto, it.id) }
    val cor = corDaArea(atual.area)
    val proximo = temas.getOrNull(indice + 1)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundGradientTop, Background)))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.ArrowBack,
                contentDescription = "Sair da sessão",
                tint = TextMuted,
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .clickable { aoEncerrar() },
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "TEMA ${indice + 1} DE ${temas.size}",
                style = MaterialTheme.typography.labelSmall,
                color = Primary,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${atual.vistas} de ${atual.total}",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
        }

        Spacer(Modifier.height(8.dp))
        BarraGamificada(
            progresso = if (temas.isEmpty()) 0f else indice.toFloat() / temas.size,            cor = cor,
            altura = 6,
        )

        Spacer(Modifier.height(14.dp))
        Text(
            text = atual.tema,
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = atual.area.replace('-', ' ') + " · " + if (atual.emRevisao) {
                "${atual.total} questões para revisar"
            } else {
                "${atual.disponiveis} questões disponíveis"
            },
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
        )

        Spacer(Modifier.height(14.dp))
        aula?.let { sugerida ->
            CartaoPasso(
                numero = "1",
                rotulo = "ASSISTIR",
                titulo = sugerida.titulo,
                detalhe = "vídeo curto do tema",
                cor = cor,
                botao = "Assistir",
                aoClicar = { aoAbrirAula(sugerida.id) },
            )
            Spacer(Modifier.height(10.dp))
        }

        CartaoPasso(
            numero = if (aula != null) "2" else "1",
            rotulo = "PRATICAR",
            titulo = "${atual.disponiveis.coerceAtLeast(0)} questões do tema",
            detalhe = if (atual.emRevisao) "revisão do que você já viu" else "questões novas",
            cor = cor,
            botao = if (atual.vistas == 0) "Começar as questões" else "Continuar as questões",
            aoClicar = { aoTreinar(atual) },
            destaque = true,
        )

        if (proximo != null) {
            Spacer(Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .cartaoRabisco(18.dp)
                    .padding(14.dp),
            ) {
                Text(
                    text = "PRÓXIMO TEMA",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = proximo.tema,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "abre quando você terminar este",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        Text(
            text = if (proximo != null) {
                "Ao sair do treino, o próximo tema já vem aberto."
            } else {
                "Último tema da sessão de hoje."
            },
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun CartaoPasso(
    numero: String,
    rotulo: String,
    titulo: String,
    detalhe: String,
    cor: androidx.compose.ui.graphics.Color,
    botao: String,
    aoClicar: () -> Unit,
    destaque: Boolean = false,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .cartaoRabisco(
                18.dp,
                sombra = destaque,
                borda = if (destaque) cor.copy(alpha = 0.55f) else com.franferreira.enem2027.ui.theme.SurfaceBorder,
            )
            .padding(14.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = "$numero · $rotulo",
                style = MaterialTheme.typography.labelSmall,
                color = if (destaque) cor else TextMuted,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = detalhe,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
            )
        }
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(if (destaque) cor else SurfaceRaised)
                .clickable { aoClicar() }
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = botao,
                style = MaterialTheme.typography.labelMedium,
                color = if (destaque) PaletaClaro.texto else TextPrimary,
            )
        }
    }
}
