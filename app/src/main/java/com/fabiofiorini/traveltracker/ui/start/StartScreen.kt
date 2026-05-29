package com.fabiofiorini.traveltracker.ui.start

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import com.fabiofiorini.traveltracker.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fabiofiorini.traveltracker.ui.theme.Dark
import com.fabiofiorini.traveltracker.ui.theme.Orange
import com.fabiofiorini.traveltracker.ui.theme.Red
import com.fabiofiorini.traveltracker.ui.theme.White
import com.fabiofiorini.traveltracker.viewmodel.TrackingViewModel
import kotlinx.coroutines.delay

@Composable
fun StartScreen(
    onStartTracking: () -> Unit,
    onHistory: () -> Unit,
    viewModel: TrackingViewModel = viewModel()
) {
    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        startAnimation = true
    }

    val heroAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(600)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Dark)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))

        HeroSection(alpha = heroAlpha)

        Spacer(Modifier.height(20.dp))

        StatsBar(startAnimation, viewModel)

        Spacer(Modifier.height(24.dp))

        FeaturePills(startAnimation)

        Spacer(Modifier.weight(1f))

        CTASection(onStartTracking, onHistory)

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun HeroSection(alpha: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.alpha(alpha)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_near_me),
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = Red
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "TravelTracker",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = Red
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Traccia i tuoi percorsi outdoor con precisione GPS",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StatsBar(startAnimation: Boolean, viewModel: TrackingViewModel) {
    val routeCount by viewModel.routeCount.collectAsState(initial = 0)
    val totalKm by viewModel.totalDistanceKm.collectAsState(initial = 0f)
    val totalSec by viewModel.totalDurationSec.collectAsState(initial = 0L)

    val offsetY by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 24f,
        animationSpec = tween(420), label = "offset"
    )
    val barAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(420), label = "alpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(barAlpha)
            .offset(y = offsetY.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(value = "$routeCount", label = "Percorsi")
            StatItem(value = "%.1f".format(totalKm), label = "km")
            StatItem(value = "${totalSec / 3600}", label = "ore")
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 24.sp),
            fontWeight = FontWeight.Bold,
            color = Red
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun FeaturePills(startAnimation: Boolean) {
    val pills = listOf(
        PillData(iconRes = R.drawable.ic_map, label = "GPS"),
        PillData(iconRes = R.drawable.ic_schedule, label = "Statistiche"),
        PillData(iconRes = R.drawable.ic_save, label = "Salva")
    )

    val offsetY by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 20f,
        animationSpec = tween(420), label = "offset"
    )
    val pillsAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(420), label = "alpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(pillsAlpha)
            .offset(y = offsetY.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        pills.forEach { pill ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Color(0xFF2A2A2A)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(pill.iconRes),
                        contentDescription = null,
                        tint = Red,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = pill.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun CTASection(
    onStartTracking: () -> Unit,
    onHistory: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = onStartTracking,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Red)
        ) {
            Text(
                text = "Registra nuovo percorso",
                style = MaterialTheme.typography.titleMedium,
                color = White,
                fontWeight = FontWeight.SemiBold
            )
        }

        OutlinedButton(
            onClick = onHistory,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Orange)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_history),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Storico percorsi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private data class PillData(val iconRes: Int, val label: String)
