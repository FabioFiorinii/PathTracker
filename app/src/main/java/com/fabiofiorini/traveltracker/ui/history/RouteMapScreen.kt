package com.fabiofiorini.traveltracker.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fabiofiorini.traveltracker.data.RouteEntity
import com.fabiofiorini.traveltracker.ui.theme.Red
import com.fabiofiorini.traveltracker.ui.theme.White
import com.fabiofiorini.traveltracker.viewmodel.TrackingViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RouteMapScreen(
    routeId: Long,
    onClose: () -> Unit
) {
    var points by remember {
        mutableStateOf<List<GeoPoint>>(emptyList())
    }

    var route by remember {
        mutableStateOf<RouteEntity?>(null)
    }

    val viewModel: TrackingViewModel = viewModel()

    LaunchedEffect(Unit) {
        val routePoints = viewModel.getPoints(routeId)
        points = routePoints.map {
            GeoPoint(it.lat, it.lon)
        }
        route = viewModel.getRoute(routeId)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val map = MapView(ctx)
                map.setTileSource(TileSourceFactory.MAPNIK)
                map.setMultiTouchControls(true)
                map.controller.setZoom(16.0)

                if (points.isNotEmpty()) {
                    val polyline = Polyline()
                    polyline.color = android.graphics.Color.RED
                    polyline.width = 8f
                    polyline.setPoints(points)
                    map.overlays.add(polyline)

                    val startMarker = Marker(map)
                    startMarker.position = points.first()
                    startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    val startIcon = Marker(map)
                    startIcon.icon = ctx.getDrawable(
                        android.R.drawable.ic_menu_mylocation
                    )
                    if (startIcon.icon != null) {
                        startMarker.icon = startIcon.icon
                    }
                    startMarker.title = "Partenza"
                    map.overlays.add(startMarker)

                    val endMarker = Marker(map)
                    endMarker.position = points.last()
                    endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    endMarker.icon = ctx.getDrawable(
                        android.R.drawable.ic_menu_compass
                    )
                    endMarker.title = "Arrivo"
                    map.overlays.add(endMarker)

                    map.controller.setCenter(points.first())
                }
                map
            },
            update = { map ->
                map.overlays.clear()
                if (points.isNotEmpty()) {
                    val polyline = Polyline()
                    polyline.color = android.graphics.Color.RED
                    polyline.width = 8f
                    polyline.setPoints(points)
                    map.overlays.add(polyline)

                    val startMarker = Marker(map)
                    startMarker.position = points.first()
                    startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    startMarker.title = "Partenza"
                    map.overlays.add(startMarker)

                    val endMarker = Marker(map)
                    endMarker.position = points.last()
                    endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    endMarker.title = "Arrivo"
                    map.overlays.add(endMarker)

                    map.controller.setCenter(points.first())
                }
                map.invalidate()
            }
        )

        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2A2A2A)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                route?.let { r ->
                    Text(
                        r.title,
                        color = White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        SimpleDateFormat(
                            "dd/MM/yyyy HH:mm",
                            Locale.getDefault()
                        ).format(Date(r.date)),
                        color = White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Distanza: %.2f km".format(r.distanceKm),
                        color = White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Durata: %02d:%02d:%02d".format(
                            r.durationSec / 3600,
                            (r.durationSec % 3600) / 60,
                            r.durationSec % 60
                        ),
                        color = White.copy(alpha = 0.7f)
                    )
                    Text(
                        "Vel. media: %.2f km/h".format(r.averageSpeedKmh),
                        color = White.copy(alpha = 0.7f)
                    )
                } ?: run {
                    Text(
                        "Caricamento...",
                        color = White.copy(alpha = 0.5f)
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = {
                onClose()
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Red,
            contentColor = White
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null
            )
        }
    }
}
