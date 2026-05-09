package com.fabiofiorini.traveltracker.ui.map

import android.annotation.SuppressLint
import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.fabiofiorini.traveltracker.data.DatabaseProvider
import com.fabiofiorini.traveltracker.data.RouteEntity
import com.google.android.gms.location.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(onStop: () -> Unit) {

    val context = LocalContext.current
    val db = remember { DatabaseProvider.getDatabase(context) }
    val scope = rememberCoroutineScope()

    // IMPORTANTISSIMO: evita mappa bianca
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    var mapView by remember { mutableStateOf<MapView?>(null) }

    var currentPoint by remember { mutableStateOf<GeoPoint?>(null) }

    var elapsedSeconds by remember { mutableStateOf(0L) }
    var distanceMeters by remember { mutableStateOf(0f) }

    var showDialog by remember { mutableStateOf(false) }
    var routeTitle by remember { mutableStateOf("") }

    val routePoints = remember { mutableStateListOf<GeoPoint>() }

    var lastLocation by remember { mutableStateOf<Location?>(null) }

    val smoothPoints = remember { mutableListOf<GeoPoint>() }
    // timer
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            elapsedSeconds++
        }
    }

    Box(Modifier.fillMaxSize()) {

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->

                val map = MapView(ctx)

                map.onResume()
                map.controller.setCenter(GeoPoint(0.0, 0.0))
                map.setTileSource(TileSourceFactory.MAPNIK)
                map.setMultiTouchControls(true)
                map.controller.setZoom(18.0)

                val marker = Marker(map)
                marker.title = "Tu sei qui"
                map.overlays.add(marker)

                val polyline = Polyline()
                polyline.setColor( android.graphics.Color.RED)
                polyline.setWidth(100f)
                map.overlays.add(polyline)

                val fusedClient =
                    LocationServices.getFusedLocationProviderClient(ctx)

                val request = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    5000
                ).build()

                val callback = object : LocationCallback() {

                    override fun onLocationResult(result: LocationResult) {

                        val location = result.lastLocation ?: return

                        val newPoint = GeoPoint(location.latitude, location.longitude)

                        smoothPoints.add(newPoint)

// mantieni solo ultimi 5 punti
                        if (smoothPoints.size > 5) {
                            smoothPoints.removeAt(0)
                        }

// media semplice
                        val avgLat = smoothPoints.map { it.latitude }.average()
                        val avgLon = smoothPoints.map { it.longitude }.average()

                        val smoothPoint = GeoPoint(avgLat, avgLon)

                        currentPoint = smoothPoint
                        marker.position = smoothPoint
                        routePoints.add(smoothPoint)

                        if (lastLocation != null) {
                            distanceMeters += lastLocation!!.distanceTo(location)
                        }
                        lastLocation = location

                        // SAFE UI THREAD UPDATE
                        map.post {
                            polyline.setPoints(routePoints)
                            map.invalidate()
                        }
                    }
                }

                fusedClient.requestLocationUpdates(
                    request,
                    callback,
                    ctx.mainLooper
                )

                mapView = map
                map
            },
            update = { map ->
                map.invalidate()
            }
        )

        // INFO BOX
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Tempo: ${
                        "%02d:%02d:%02d".format(
                            elapsedSeconds / 3600,
                            (elapsedSeconds % 3600) / 60,
                            elapsedSeconds % 60
                        )
                    }"
                )

                Text("Distanza: %.2f km".format(distanceMeters / 1000f))
            }
        }

        // CENTRA MAPPA (manuale)
        FloatingActionButton(
            onClick = {
                currentPoint?.let {
                    mapView?.controller?.animateTo(it)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.MyLocation, null)
        }

        // STOP
        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Stop, null)
        }

        if (showDialog) {

            AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {

                    Button(onClick = {

                        scope.launch {

                            val avgSpeed =
                                if (elapsedSeconds > 0)
                                    (distanceMeters / 1000f) / (elapsedSeconds / 3600f)
                                else 0f

                            val routeId = withContext(Dispatchers.IO) {
                                db.routeDao().insertRoute(
                                    RouteEntity(
                                        title = routeTitle,
                                        date = System.currentTimeMillis(),
                                        durationSec = elapsedSeconds,
                                        distanceKm = distanceMeters / 1000f,
                                        averageSpeedKmh = avgSpeed
                                    )
                                )
                            }

                            withContext(Dispatchers.IO) {
                                db.routeDao().insertPoints(
                                    routePoints.map {
                                        com.fabiofiorini.traveltracker.data.RoutePointEntity(
                                            routeId = routeId,
                                            lat = it.latitude,
                                            lon = it.longitude,
                                            timestamp = System.currentTimeMillis()
                                        )
                                    }
                                )
                            }

                            onStop()
                        }
                    }) {
                        Text("Salva")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showDialog = false }) {
                        Text("Annulla")
                    }
                },
                title = { Text("Salva percorso") },
                text = {
                    OutlinedTextField(
                        value = routeTitle,
                        onValueChange = { routeTitle = it },
                        label = { Text("Titolo") }
                    )
                }
            )
        }
    }
}