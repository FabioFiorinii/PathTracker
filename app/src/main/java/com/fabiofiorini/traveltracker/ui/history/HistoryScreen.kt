package com.fabiofiorini.traveltracker.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.fabiofiorini.traveltracker.data.DatabaseProvider
import com.fabiofiorini.traveltracker.data.RouteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit
) {

    val context = LocalContext.current
    val db = remember { DatabaseProvider.getDatabase(context) }

    var routes by remember {
        mutableStateOf<List<RouteEntity>>(emptyList())
    }

    var selectedRoute by remember {
        mutableStateOf<RouteEntity?>(null)
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                db.routeDao().getAllRoutes()
            }
            routes = result
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Storico percorsi") }
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedRoute = route
                        }
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

                        Text("%.2f km".format(route.distanceKm))
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
                    "Durata: %02d:%02d"
                        .format(
                            route.durationSec / 60,
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