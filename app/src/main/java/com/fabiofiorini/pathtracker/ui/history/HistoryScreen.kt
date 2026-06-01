package com.fabiofiorini.pathtracker.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.res.painterResource
import com.fabiofiorini.pathtracker.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fabiofiorini.pathtracker.data.RouteEntity
import com.fabiofiorini.pathtracker.ui.theme.Dark
import com.fabiofiorini.pathtracker.ui.theme.Orange
import com.fabiofiorini.pathtracker.ui.theme.Red
import com.fabiofiorini.pathtracker.ui.theme.White
import com.fabiofiorini.pathtracker.viewmodel.TrackingViewModel
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

    var searchQuery by remember { mutableStateOf("") }

    val viewModel: TrackingViewModel = viewModel()

    val routes by viewModel.routes.collectAsState(initial = emptyList())

    val filteredRoutes = when {
        searchQuery.isBlank() -> routes
        else -> routes.filter {
            it.title.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Indietro",
                            tint = White
                        )
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_map),
                            contentDescription = null,
                            tint = Red
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                "Storico percorsi",
                                color = Red,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "I tuoi percorsi",
                                color = White.copy(alpha = 0.5f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Dark
                )
            )
        },
        containerColor = Dark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                placeholder = {
                    Text(
                        "Cerca percorso",
                        color = White.copy(alpha = 0.4f)
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = White.copy(alpha = 0.5f)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Red,
                    unfocusedBorderColor = White.copy(alpha = 0.2f),
                    cursorColor = Red,
                    focusedTextColor = White,
                    unfocusedTextColor = White
                )
            )

            if (filteredRoutes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_map),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                            tint = Color.White.copy(alpha = 0.3f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            if (searchQuery.isBlank()) "Nessun percorso registrato"
                            else "Nessun risultato",
                            color = Color.White.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredRoutes) { route ->
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
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        "%.2f km".format(route.distanceKm),
                                        color = Red,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "${route.steps} passi",
                                        color = Orange,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
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
                                            painter = painterResource(R.drawable.ic_map),
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
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
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
                                            painter = painterResource(R.drawable.ic_delete),
                                            contentDescription = null,
                                            modifier = Modifier.scale(1.5f)
                                        )
                                    }
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
                        Text(
                            "Passi: ${route.steps}",
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
