package com.fabiofiorini.traveltracker.ui.map

import android.annotation.SuppressLint
import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.fabiofiorini.traveltracker.data.*
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.*

@SuppressLint("MissingPermission")
@Composable
fun MapScreen(
    onStop: () -> Unit
) {

    val context = LocalContext.current
    val db = remember { DatabaseProvider.getDatabase(context) }

    val scope = rememberCoroutineScope()

    var seconds by remember { mutableStateOf(0) }
    var distanceKm by remember { mutableStateOf(0.0) }

    var lastLocation by remember { mutableStateOf<Location?>(null) }
    val points = remember { mutableStateListOf<Location>() }

    var isTracking by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }

    // TIMER
    LaunchedEffect(isTracking) {
        while (isTracking) {
            delay(1000)
            seconds++
        }
    }

    fun formatTime(sec: Int): String {
        val m = sec / 60
        val s = sec % 60
        return "%02d:%02d".format(m, s)
    }

    fun saveRoute(title: String) {
        scope.launch(Dispatchers.IO) {

            val routeId = db.routeDao().insertRoute(
                RouteEntity(
                    title = title,
                    distanceKm = distanceKm,
                    durationSec = seconds,
                    date = System.currentTimeMillis()
                )
            )

            val pointEntities = points.map {
                RoutePointEntity(
                    routeId = routeId,
                    lat = it.latitude,
                    lon = it.longitude,
                    timestamp = System.currentTimeMillis()
                )
            }

            db.routeDao().insertPoints(pointEntities)

            withContext(Dispatchers.Main) {
                onStop()
            }
        }
    }

    Box(Modifier.fillMaxSize()) {

        AndroidView(factory = { ctx ->

            val map = MapView(ctx)
            map.setTileSource(TileSourceFactory.MAPNIK)
            map.setMultiTouchControls(true)
            map.controller.setZoom(18.0)

            val fusedClient = LocationServices.getFusedLocationProviderClient(ctx)

            val marker = Marker(map)
            map.overlays.add(marker)

            val request = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                2000
            ).build()

            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    if (!isTracking) return

                    val loc = result.lastLocation ?: return
                    val point = GeoPoint(loc.latitude, loc.longitude)

                    marker.position = point
                    map.controller.setCenter(point)
                    map.invalidate()

                    // salva punti
                    points.add(loc)

                    // distanza
                    lastLocation?.let { prev ->
                        val res = FloatArray(1)
                        Location.distanceBetween(
                            prev.latitude, prev.longitude,
                            loc.latitude, loc.longitude,
                            res
                        )
                        distanceKm += res[0] / 1000.0
                    }

                    lastLocation = loc
                }
            }

            fusedClient.requestLocationUpdates(
                request,
                callback,
                ctx.mainLooper
            )

            map
        })

        // HUD
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Card {
                Column(Modifier.padding(12.dp)) {
                    Text("Tempo: ${formatTime(seconds)}")
                    Text("Distanza: %.2f km".format(distanceKm))
                }
            }
        }

        // STOP
        Button(
            onClick = {
                isTracking = false
                showDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(20.dp)
        ) {
            Text("STOP")
        }
    }

    // 📌 POPUP TITOLO
    if (showDialog) {

        var title by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = {},
            title = { Text("Nome percorso") },
            text = {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("es. Corsa al parco") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            saveRoute(title)
                        }
                    }
                ) {
                    Text("Salva")
                }
            }
        )
    }
}