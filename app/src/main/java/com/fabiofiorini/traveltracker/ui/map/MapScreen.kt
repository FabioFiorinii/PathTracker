package com.fabiofiorini.traveltracker.ui.map

import android.content.Intent
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
import com.fabiofiorini.traveltracker.tracking.TrackingManager
import com.fabiofiorini.traveltracker.service.TrackingService
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
fun MapScreen(
    onStop: () -> Unit
) {

    val context = LocalContext.current

    val routePoints = TrackingManager.points

    val elapsedSeconds =
        TrackingManager.elapsedSeconds.longValue

    val distanceMeters =
        TrackingManager.distanceMeters.floatValue

    var mapView by remember {
        mutableStateOf<MapView?>(null)
    }

    var marker by remember {
        mutableStateOf<Marker?>(null)
    }

    var polyline by remember {
        mutableStateOf<Polyline?>(null)
    }

    LaunchedEffect(Unit) {

        Configuration.getInstance().userAgentValue =
            context.packageName

        val intent = Intent(
            context,
            TrackingService::class.java
        )

        context.startForegroundService(intent)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->

                val map = MapView(ctx)

                map.onResume()

                map.setTileSource(TileSourceFactory.MAPNIK)

                map.setMultiTouchControls(true)

                map.controller.setZoom(18.0)

                val m = Marker(map)
                map.overlays.add(m)

                val p = Polyline()
                p.color = android.graphics.Color.RED
                p.width = 8f
                map.overlays.add(p)

                marker = m
                polyline = p
                mapView = map

                map
            },
            update = { map ->

                val lastPoint = routePoints.lastOrNull()

                if (lastPoint != null) {

                    marker?.position = lastPoint

                    polyline?.setPoints(routePoints)

                    map.invalidate()
                }
            }
        )

        Card(
            modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    "Tempo: ${
                        "%02d:%02d:%02d".format(
                            elapsedSeconds / 3600,
                            (elapsedSeconds % 3600) / 60,
                            elapsedSeconds % 60
                        )
                    }"
                )

                Text(
                    "Distanza: %.2f km"
                        .format(distanceMeters / 1000f)
                )
            }
        }

        FloatingActionButton(
            onClick = {

                val point = routePoints.lastOrNull()

                if (point != null) {
                    mapView?.controller?.animateTo(point)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.MyLocation, null)
        }

        FloatingActionButton(
            onClick = {
                val intent = Intent(
                    context,
                    TrackingService::class.java
                )

                context.stopService(intent)

                onStop()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
            ) {
            Icon(Icons.Default.Stop, null)
        }
    }
}