package com.franferreira.enem2027.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.franferreira.enem2027.ui.components.Florzinha
import com.franferreira.enem2027.ui.components.floresFundo
import com.franferreira.enem2027.ui.theme.Background
import com.franferreira.enem2027.ui.theme.BackgroundGradientTop
import com.franferreira.enem2027.ui.theme.TextMuted

/**
 * Abertura: a Florzinha enquanto o app confere a sessão.
 *
 * Existe para o login não piscar na tela antes de o app saber se já há sessão.
 * É só espera — não pede nada.
 */
@Composable
fun TelaAbertura(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundGradientTop, Background)))
            .floresFundo(alpha = 0.35f),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Florzinha(tamanho = 92.dp)
            Spacer(Modifier.height(18.dp))
            Text(
                text = "Preparando seu estudo…",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
            )
        }
    }
}
