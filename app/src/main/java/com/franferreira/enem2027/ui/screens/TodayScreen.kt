package com.franferreira.enem2027.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import com.franferreira.enem2027.ui.theme.Background
import com.franferreira.enem2027.ui.theme.BackgroundGradientTop
import com.franferreira.enem2027.ui.theme.EnemTheme
import com.franferreira.enem2027.ui.theme.Primary
import com.franferreira.enem2027.ui.theme.Secondary
import com.franferreira.enem2027.ui.theme.Surface
import com.franferreira.enem2027.ui.theme.SurfaceBorder
import com.franferreira.enem2027.ui.theme.TextMuted
import com.franferreira.enem2027.ui.theme.TextMutedDark
import com.franferreira.enem2027.ui.theme.TextPrimary
import com.franferreira.enem2027.ui.theme.TextSecondary

@Composable
fun TodayScreen(
    userName: String = "Fran",
    goalProgress: Float = 0.68f,
    pendingQuestions: Int = 12,
    streakDays: Int = 12,
    nextTopic: String = "Funções exponenciais e logaritmos",
    nextArea: String = "Matemática",
    weekActivity: List<Float> = listOf(0.40f, 0.62f, 0.28f, 0.85f, 0.52f, 0.70f, 0.33f),
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(BackgroundGradientTop, Background)))
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp, bottom = 24.dp),
    ) {
        TodayHeader(userName = userName)

        Spacer(Modifier.height(16.dp))

        GoalRingCard(
            progress = goalProgress,
            pendingQuestions = pendingQuestions,
            streakDays = streakDays,
        )

        Spacer(Modifier.height(12.dp))

        NextTopicCard(topic = nextTopic, area = nextArea)

        Spacer(Modifier.height(12.dp))

        WeekChartCard(activity = weekActivity)
    }
}

@Composable
private fun TodayHeader(userName: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = "SESSÃO DE HOJE",
                style = MaterialTheme.typography.labelSmall,
                color = Primary,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = "Bom dia, $userName",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
            )
        }
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Brush.linearGradient(listOf(Primary, Secondary))),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = userName.take(2).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = Background,
            )
        }
    }
}

@Composable
private fun GoalRingCard(
    progress: Float,
    pendingQuestions: Int,
    streakDays: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProgressRing(progress = progress, percentLabel = "${(progress * 100).toInt()}%")
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                text = "Faltam $pendingQuestions questões",
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Sequência de $streakDays dias. Mantenha o embalo.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
            )
        }
    }
}

@Composable
private fun ProgressRing(progress: Float, percentLabel: String) {
    val track = SurfaceBorder
    val progressColor = Primary
    Box(modifier = Modifier.size(74.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(74.dp)) {
            val stroke = 8.dp.toPx()
            val inset = stroke / 2
            val arcSize = androidx.compose.ui.geometry.Size(
                size.width - stroke,
                size.height - stroke,
            )
            drawArc(
                color = track,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = percentLabel,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
            )
            Text(
                text = "META",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
        }
    }
}

@Composable
private fun NextTopicCard(topic: String, area: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Primary.copy(alpha = 0.16f),
                        Secondary.copy(alpha = 0.07f),
                    ),
                ),
            )
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .padding(13.dp),
    ) {
        Text(
            text = "PRÓXIMO · ${area.uppercase()}",
            style = MaterialTheme.typography.labelSmall,
            color = Secondary,
        )
        Spacer(Modifier.height(5.dp))
        Text(
            text = topic,
            style = MaterialTheme.typography.titleMedium,
            color = TextSecondary,
        )
        Spacer(Modifier.height(11.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Primary),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Retomar revisão",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun WeekChartCard(activity: List<Float>) {
    val labels = listOf("S", "T", "Q", "Q", "S", "S", "D")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .padding(13.dp),
    ) {
        Text(
            text = "Últimos 7 dias",
            style = MaterialTheme.typography.labelMedium,
            color = TextMuted,
        )
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            activity.forEachIndexed { index, value ->
                val isBest = value == activity.max()
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .height((52 * value).dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (isBest) {
                                Brush.verticalGradient(listOf(Secondary, Secondary.copy(alpha = 0.5f)))
                            } else {
                                Brush.verticalGradient(
                                    listOf(SurfaceBorder, SurfaceBorder.copy(alpha = 0.6f)),
                                )
                            },
                        ),
                )
                if (index >= labels.size) return@forEachIndexed
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMutedDark,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview
@Composable
private fun TodayScreenPreview() {
    EnemTheme { TodayScreen() }
}
