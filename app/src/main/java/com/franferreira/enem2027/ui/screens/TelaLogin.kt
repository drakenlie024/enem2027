package com.franferreira.enem2027.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.franferreira.enem2027.ui.components.BotaoGamificado
import com.franferreira.enem2027.ui.theme.Background
import com.franferreira.enem2027.ui.theme.BackgroundGradientTop
import com.franferreira.enem2027.ui.theme.Danger
import com.franferreira.enem2027.ui.theme.Primary
import com.franferreira.enem2027.ui.theme.Secondary
import com.franferreira.enem2027.ui.theme.Surface
import com.franferreira.enem2027.ui.theme.SurfaceBorder
import com.franferreira.enem2027.ui.theme.TextMuted
import com.franferreira.enem2027.ui.theme.TextPrimary
import com.franferreira.enem2027.ui.theme.TextSecondary
import kotlinx.coroutines.launch

/** Tela de entrada: login ou cadastro. */
@Composable
fun TelaLogin(
    aoEntrar: suspend (email: String, senha: String, nome: String, criar: Boolean) -> String?,
) {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var nome by remember { mutableStateOf("") }
    var criando by remember { mutableStateOf(false) }
    var mostrarSenha by remember { mutableStateOf(false) }
    var carregando by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun enviar() {
        if (email.isBlank() || senha.isBlank()) {
            erro = "Preencha e-mail e senha"
            return
        }
        carregando = true
        erro = null
        scope.launch {
            val problema = aoEntrar(email.trim(), senha, nome.trim(), criando)
            if (problema != null) erro = problema
            carregando = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundGradientTop, Background)))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(72.dp))

        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.linearGradient(listOf(Primary, Secondary))),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "L",
                style = MaterialTheme.typography.headlineMedium,
                color = Background,
            )
        }

        Spacer(Modifier.height(20.dp))
        Text(
            text = "Letícia",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
        )
        Text(
            text = "Seu estudo para o ENEM 2027",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
        )

        Spacer(Modifier.height(32.dp))

        if (criando) {
            Campo(
                valor = nome,
                aoMudar = { nome = it },
                rotulo = "Nome",
            )
            Spacer(Modifier.height(12.dp))
        }

        Campo(
            valor = email,
            aoMudar = { email = it },
            rotulo = "E-mail",
            teclado = KeyboardType.Email,
        )
        Spacer(Modifier.height(12.dp))

        Campo(
            valor = senha,
            aoMudar = { senha = it },
            rotulo = "Senha",
            senha = true,
            mostrarSenha = mostrarSenha,
            aoAlternarVisibilidade = { mostrarSenha = !mostrarSenha },
        )

        erro?.let {
            Spacer(Modifier.height(12.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = Danger,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(20.dp))

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
                texto = if (criando) "CRIAR CONTA" else "ENTRAR",
                cor = Primary,
                aoClicar = { enviar() },
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = if (criando) "Já tem conta? Entrar" else "Não tem conta? Criar agora",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            modifier = Modifier.clickable {
                criando = !criando
                erro = null
            },
        )

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun Campo(
    valor: String,
    aoMudar: (String) -> Unit,
    rotulo: String,
    teclado: KeyboardType = KeyboardType.Text,
    senha: Boolean = false,
    mostrarSenha: Boolean = false,
    aoAlternarVisibilidade: (() -> Unit)? = null,
) {
    OutlinedTextField(
        value = valor,
        onValueChange = aoMudar,
        label = { Text(rotulo) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        visualTransformation = if (senha && !mostrarSenha) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        trailingIcon = if (senha && aoAlternarVisibilidade != null) {
            {
                Icon(
                    imageVector = if (mostrarSenha) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (mostrarSenha) "Ocultar senha" else "Ver senha",
                    tint = TextMuted,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { aoAlternarVisibilidade() },
                )
            }
        } else {
            null
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primary,
            unfocusedBorderColor = SurfaceBorder,
            focusedLabelColor = Primary,
            unfocusedLabelColor = TextMuted,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = Primary,
            focusedContainerColor = Surface,
            unfocusedContainerColor = Surface,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}
