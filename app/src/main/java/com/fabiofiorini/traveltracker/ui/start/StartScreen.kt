package com.fabiofiorini.traveltracker.ui.start

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fabiofiorini.traveltracker.ui.theme.Dark
import com.fabiofiorini.traveltracker.ui.theme.Orange
import com.fabiofiorini.traveltracker.ui.theme.Red
import com.fabiofiorini.traveltracker.ui.theme.White
import kotlinx.coroutines.delay

@Composable
fun StartScreen(
    onStartTracking: () -> Unit,
    onHistory: () -> Unit
) {
    val scrollState = rememberScrollState()

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
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(60.dp))

        HeroSection(heroAlpha)

        Spacer(Modifier.height(40.dp))

        FeatureCards(startAnimation)

        Spacer(Modifier.height(32.dp))

        StatsCard()

        Spacer(Modifier.height(40.dp))

        CTASection(onStartTracking, onHistory)

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun HeroSection(alpha: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.alpha(alpha)
    ) {
        Icon(
            imageVector = Icons.Default.NearMe,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = Red
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "TravelTracker",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = Red
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Traccia i tuoi percorsi outdoor\ncon precisione GPS",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FeatureCards(startAnimation: Boolean) {
    val features = listOf(
        FeatureData(
            icon = Icons.Default.Route,
            title = "Registrazione GPS",
            desc = "Cattura ogni tuo spostamento con precisione in tempo reale"
        ),
        FeatureData(
            icon = Icons.Default.Timer,
            title = "Statistiche di viaggio",
            desc = "Visualizza distanza, durata e velocità media dei percorsi"
        ),
        FeatureData(
            icon = Icons.Default.SaveAlt,
            title = "Salva e condividi",
            desc = "Archivia i percorsi completati e rivedili in qualsiasi momento"
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        features.forEachIndexed { index, feature ->
            FeatureCard(
                feature = feature,
                delayMs = 150L + index * 100L,
                startAnimation = startAnimation
            )
        }
    }
}

@Composable
private fun FeatureCard(
    feature: FeatureData,
    delayMs: Long,
    startAnimation: Boolean
) {
    var visible by remember { mutableStateOf(!startAnimation) }

    LaunchedEffect(startAnimation) {
        if (startAnimation) {
            delay(delayMs)
            visible = true
        }
    }

    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 32f,
        animationSpec = tween(420),
        label = "offset"
    )

    val cardAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(420),
        label = "alpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
            .offset(y = offsetY.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A2A)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Red),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column {
                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = feature.desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun StatsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A2A)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(value = "0", label = "Percorsi")
            StatItem(value = "0", label = "km totali")
            StatItem(value = "0", label = "h tracciate")
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp),
            fontWeight = FontWeight.Bold,
            color = Red
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.6f)
        )
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
            colors = ButtonDefaults.buttonColors(
                containerColor = Red
            )
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
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Orange
            )
        ) {
            Icon(
                imageVector = Icons.Default.History,
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

private data class FeatureData(
    val icon: ImageVector,
    val title: String,
    val desc: String
)
