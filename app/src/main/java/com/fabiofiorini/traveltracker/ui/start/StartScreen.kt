package com.fabiofiorini.traveltracker.ui.start

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StartScreen(
    onStartTracking: () -> Unit,
    onHistory: () -> Unit
) {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Button(
                onClick = onStartTracking,
                modifier = Modifier.width(220.dp)
            ) {
                Text("Registra nuovo percorso")
            }

            Button(
                onClick = onHistory,
                modifier = Modifier.width(220.dp)
            ) {
                Text("Storico percorsi")
            }
        }
    }
}