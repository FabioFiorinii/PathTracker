package com.fabiofiorini.traveltracker.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fabiofiorini.traveltracker.data.RouteEntity
import com.fabiofiorini.traveltracker.ui.theme.Dark
import com.fabiofiorini.traveltracker.ui.theme.Orange
import com.fabiofiorini.traveltracker.ui.theme.Red
import com.fabiofiorini.traveltracker.ui.theme.White
import com.fabiofiorini.traveltracker.viewmodel.TrackingViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onRouteSelected: (Long) -> Unit
) {
    var selectedRoute by remember {
        mutableStateOf<RouteEntity?>(null)
    }

    var toDeleteRoute by remember {
        mutableStateOf<RouteEntity?>(null)
    }

    val viewModel: TrackingViewModel = viewModel()

    val routes by viewModel.routes.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Storico percorsi", color = White, fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Dark
                )
            )
        },
        containerColor = Dark
    ) { padding ->
        if (routes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Route,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.White.copy(alpha = 0.3f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Nessun percorso registrato",
                        color = Color.White.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(routes) { route ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2A2A2A)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                route.title,
                                color = White,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                SimpleDateFormat(
                                    "dd/MM/yyyy HH:mm",
                                    Locale.getDefault()
                                ).format(Date(route.date)),
                                color = White.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "%.2f km".format(route.distanceKm),
                                color = Red,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    { onRouteSelected(route.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Red
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Map,
                                        contentDescription = null,
                                        tint = White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Mappa", color = White)
                                }

                                Button(
                                    onClick = {
                                        selectedRoute = route
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Orange
                                    )
                                ) {
                                    Text("Info", color = White)
                                }

                                OutlinedButton(
                                    onClick = {
                                        toDeleteRoute = route
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Red
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        selectedRoute?.let { route ->
            AlertDialog(
                onDismissRequest = {
                    selectedRoute = null
                },
                containerColor = Color(0xFF2A2A2A),
                confirmButton = {
                    Button(
                        onClick = {
                            selectedRoute = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Red
                        )
                    ) {
                        Text("Chiudi", color = White)
                    }
                },
                title = {
                    Text(route.title, color = Red)
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Data: " +
                                    SimpleDateFormat(
                                        "dd/MM/yyyy HH:mm",
                                        Locale.getDefault()
                                    ).format(Date(route.date)),
                            color = White.copy(alpha = 0.8f)
                        )
                        Text(
                            "Distanza: %.2f km".format(route.distanceKm),
                            color = White.copy(alpha = 0.8f)
                        )
                        Text(
                            "Durata: %02d:%02d:%02d"
                                .format(
                                    route.durationSec / 3600,
                                    (route.durationSec % 3600) / 60,
                                    route.durationSec % 60
                                ),
                            color = White.copy(alpha = 0.8f)
                        )
                        Text(
                            "Velocità media: %.2f km/h"
                                .format(route.averageSpeedKmh),
                            color = White.copy(alpha = 0.8f)
                        )
                    }
                }
            )
        }

        toDeleteRoute?.let { route ->
            AlertDialog(
                onDismissRequest = {
                    toDeleteRoute = null
                },
                containerColor = Color(0xFF2A2A2A),
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteRoute(route)
                            toDeleteRoute = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Red
                        )
                    ) {
                        Text("Cancella", color = White)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            toDeleteRoute = null
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = White
                        )
                    ) {
                        Text("Annulla")
                    }
                },
                title = {
                    Text(route.title, color = Red)
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Sei sicuro di voler eliminare il percorso?",
                            color = White.copy(alpha = 0.8f)
                        )
                    }
                }
            )
        }
    }
}
