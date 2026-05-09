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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

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

    val points = remember {
        mutableStateListOf<Location>()
    }

    var showDialog by remember {
        mutableStateOf(false)
    }

    var isTracking by remember {
        mutableStateOf(true)
    }

    LaunchedEffect(isTracking) {
        while (isTracking) {
            delay(1000)
            seconds++
        }
    }

    fun averageSpeed(): Double {

        val hours = seconds / 3600.0

        if (hours <= 0.0) return 0.0

        return distanceKm / hours
    }

    fun saveRoute(title: String) {
        scope.launch(Dispatchers.IO) {

            val routeId = db.routeDao().insertRoute(
                RouteEntity(
                    title = title,
                    distanceKm = distanceKm,
                    durationSec = seconds,
                    averageSpeedKmh = averageSpeed(),
                    date = System.currentTimeMillis()
                )
            )

            val routePoints = points.map {
                RoutePointEntity(
                    routeId = routeId,
                    lat = it.latitude,
                    lon = it.longitude,
                    timestamp = System.currentTimeMillis()
                )
            }

            db.routeDao().insertPoints(routePoints)

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

            val fusedClient = LocationServices
                .getFusedLocationProviderClient(ctx)

            val marker = Marker(map)
            map.overlays.add(marker)

            val polyline = Polyline()
            map.overlays.add(polyline)

            val request = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                5000
            ).build()

            val callback = object : LocationCallback() {

                override fun onLocationResult(result: LocationResult) {

                    if (!isTracking) return

                    val location = result.lastLocation ?: return

                    val point = GeoPoint(
                        location.latitude,
                        location.longitude
                    )

                    marker.position = point

                    points.add(location)

                    polyline.setPoints(
                        points.map {
                            GeoPoint(it.latitude, it.longitude)
                        }
                    )

                    map.controller.setCenter(point)
                    map.invalidate()

                    lastLocation?.let { prev ->

                        val res = FloatArray(1)

                        Location.distanceBetween(
                            prev.latitude,
                            prev.longitude,
                            location.latitude,
                            location.longitude,
                            res
                        )

                        distanceKm += res[0] / 1000.0
                    }

                            lastLocation = location
                }
            }

            fusedClient.requestLocationUpdates(
                request,
                callback,
                ctx.mainLooper
            )

            map
        })

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Card {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {

                    Text(
                        "Tempo: %02d:%02d"
                            .format(seconds / 60, seconds % 60)
                    )

                    Text(
                        "Distanza: %.2f km"
                            .format(distanceKm)
                    )

                    Text(
                        "Velocità media: %.2f km/h"
                            .format(averageSpeed())
                    )
                }
            }
        }
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

    if (showDialog) {

        var title by remember {
            mutableStateOf("")
        }

        AlertDialog(
            onDismissRequest = {},
            title = {
                Text("Titolo percorso")
            },
            text = {
                TextField(
                    value = title,
                    onValueChange = {
                        title = it
                    },
                    placeholder = {
                        Text("es. Giro lago")
                    }
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