package com.fabiofiorini.traveltracker.ui.history


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fabiofiorini.traveltracker.data.RouteEntity
import com.fabiofiorini.traveltracker.viewmodel.TrackingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    navController: NavController
) {


    var selectedRoute by remember {
        mutableStateOf<RouteEntity?>(null)
    }

    val viewModel: TrackingViewModel = viewModel()

    val scope = rememberCoroutineScope()

    val routes by viewModel.routes.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Storico percorsi")
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            items(routes) { route ->

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(route.title)

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            SimpleDateFormat(
                                "dd/MM/yyyy HH:mm",
                                Locale.getDefault()
                            ).format(Date(route.date))
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text("%.2f km".format(route.distanceKm))

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                { navController.navigate("routeMap/${route.id}") }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                Text("Mappa")
                            }

                            Button(
                                onClick = {
                                    selectedRoute = route
                                }
                            ) {
                                Text("Info")
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.deleteRoute(route)
                                }
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
        selectedRoute?.let { route ->

            AlertDialog(
                onDismissRequest = {
                    selectedRoute = null
                },
                confirmButton = {
                    Button(
                        onClick = {
                            selectedRoute = null
                        }
                    ) {
                        Text("Chiudi")
                    }
                },
                title = {
                    Text(route.title)
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
                                    ).format(Date(route.date))
                        )

                        Text("Distanza: %.2f km".format(route.distanceKm))

                        Text(
                            "Durata: %02d:%02d:%02d"
                                .format(
                                    route.durationSec / 3600,
                                    (route.durationSec % 3600) / 60,
                                    route.durationSec % 60
                                )
                        )

                        Text(
                            "Velocità media: %.2f km/h"
                                .format(route.averageSpeedKmh)
                        )
                    }
                }
            )
        }
    }
}