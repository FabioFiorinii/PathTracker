package com.fabiofiorini.pathtracker.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
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
import com.fabiofiorini.pathtracker.data.RoutePointEntity
import com.fabiofiorini.pathtracker.ui.theme.Dark
import com.fabiofiorini.pathtracker.ui.theme.Orange
import com.fabiofiorini.pathtracker.ui.theme.Red
import com.fabiofiorini.pathtracker.ui.theme.White
import com.fabiofiorini.pathtracker.util.RouteStatsCalculator
import com.fabiofiorini.pathtracker.viewmodel.TrackingViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

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

    var routePoints by remember { mutableStateOf(emptyList<RoutePointEntity>()) }
    var movingTimeSec by remember { mutableStateOf(0L) }
    var calories by remember { mutableStateOf(0) }

    val viewModel: TrackingViewModel = viewModel()

    val routes by viewModel.routes.collectAsState(initial = emptyList())

    val filteredRoutes = when {
        searchQuery.isBlank() -> routes
        else -> routes.filter {
            it.title.contains(searchQuery, ignoreCase = true)
        }
    }

    LaunchedEffect(selectedRoute) {
        val route = selectedRoute ?: return@LaunchedEffect
        val points = viewModel.getPoints(route.id)
        routePoints = points
        val durSec = route.durationSec
        val moving = if (points.size >= 2) {
            val speeds = RouteStatsCalculator.calculateSpeeds(points)
            var stoppedSec = 0L
            for (i in 0 until points.size - 1) {
                if (speeds[i] < 0.5f) {
                    stoppedSec += (points[i + 1].timestampSec - points[i].timestampSec).toLong()
                }
            }
            (durSec - stoppedSec).coerceAtLeast(0L)
        } else durSec
        movingTimeSec = moving
        calories = (3.5f * 75 * (moving / 3600f)).roundToInt()
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
            ModalBottomSheet(
                onDismissRequest = { selectedRoute = null },
                containerColor = Color(0xFF2A2A2A)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(40.dp)
                                .background(Red, RoundedCornerShape(2.dp))
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                route.title,
                                color = White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                    .format(Date(route.date)),
                                color = White.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = Red.copy(alpha = 0.3f), thickness = 1.dp)
                    Spacer(Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_map),
                                contentDescription = null,
                                tint = Orange,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "%.2f".format(route.distanceKm),
                                color = Red,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "km",
                                color = White.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_schedule),
                                contentDescription = null,
                                tint = Orange,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "%02d:%02d:%02d".format(
                                    route.durationSec / 3600,
                                    (route.durationSec % 3600) / 60,
                                    route.durationSec % 60
                                ),
                                color = Red,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_pace),
                                contentDescription = null,
                                tint = Orange,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            val paceMinPerKm = if (route.distanceKm > 0)
                                (route.durationSec / 60f) / route.distanceKm else 0f
                            val paceMin = paceMinPerKm.toInt()
                            val paceSec = ((paceMinPerKm - paceMin) * 60).roundToInt()
                            Text(
                                "$paceMin'%02d\"".format(paceSec),
                                color = Red,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "/km",
                                color = White.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_footsteps),
                                contentDescription = null,
                                tint = Orange,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "${route.steps}",
                                color = Red,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_fire),
                                contentDescription = null,
                                tint = Orange,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "$calories",
                                color = Red,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "kcal",
                                color = White.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Orange,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "%02d:%02d:%02d".format(
                                    movingTimeSec / 3600,
                                    (movingTimeSec % 3600) / 60,
                                    movingTimeSec % 60
                                ),
                                color = Red,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = { selectedRoute = null },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Red)
                    ) {
                        Text("Chiudi", color = White)
                    }
                }
            }
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
