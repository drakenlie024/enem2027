package com.franferreira.enem2027.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.franferreira.enem2027.data.repo.Atualizacao
import com.franferreira.enem2027.ui.theme.Primary
import com.franferreira.enem2027.ui.theme.Surface
import com.franferreira.enem2027.ui.theme.TextMuted
import com.franferreira.enem2027.ui.theme.TextPrimary

/**
 * Aviso de versão nova. Aparece em qualquer tela, uma vez por abertura do app.
 * "Atualizar" baixa o APK e abre a instalação do sistema.
 */
@Composable
fun AvisoAtualizacao(
    nova: Atualizacao.Nova,
    baixando: Boolean,
    erro: String?,
    aoAtualizar: () -> Unit,
    aoDispensar: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!baixando) aoDispensar() },
        containerColor = Surface,
        titleContentColor = TextPrimary,
        textContentColor = TextMuted,
        title = { Text("Versão ${nova.versionName} disponível") },
        text = {
            Text(
                when {
                    baixando -> "Baixando a atualização..."
                    erro != null -> erro
                    else -> "Tem uma versão nova do app. A instalação é só confirmar."
                },
            )
        },
        confirmButton = {
            TextButton(onClick = aoAtualizar, enabled = !baixando) {
                Text("Atualizar", color = Primary)
            }
        },
        dismissButton = {
            TextButton(onClick = aoDispensar, enabled = !baixando) {
                Text("Depois", color = TextMuted)
            }
        },
    )
}
