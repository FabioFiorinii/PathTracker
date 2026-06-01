package com.fabiofiorini.pathtracker.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.fabiofiorini.pathtracker.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fabiofiorini.pathtracker.data.RouteEntity
import com.fabiofiorini.pathtracker.data.RoutePointEntity
import com.fabiofiorini.pathtracker.ui.theme.Dark
import com.fabiofiorini.pathtracker.ui.theme.Orange
import com.fabiofiorini.pathtracker.ui.theme.Red
import com.fabiofiorini.pathtracker.ui.theme.White
import com.fabiofiorini.pathtracker.util.GpxExporter
import com.fabiofiorini.pathtracker.util.RouteStatsCalculator
import com.fabiofiorini.pathtracker.viewmodel.TrackingViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryRouteMapScreen(
    routeId: Long,
    onClose: () -> Unit
) {
    var routeDataPoints by remember {
        mutableStateOf<List<RoutePointEntity>>(emptyList())
    }

    var route by remember {
        mutableStateOf<RouteEntity?>(null)
    }

    val viewModel: TrackingViewModel = viewModel()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        routeDataPoints = viewModel.getPoints(routeId)
        route = viewModel.getRoute(routeId)
    }

    val geoPoints = routeDataPoints.map { GeoPoint(it.lat, it.lon) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_near_me),
                            contentDescription = null,
                            tint = Red
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Percorso",
                            color = Red,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Dark
                )
            )
        },
        containerColor = Dark
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val map = MapView(ctx)
                map.setTileSource(TileSourceFactory.MAPNIK)
                map.setMultiTouchControls(true)
                map.controller.setZoom(18.0)
                map
            },
            update = { map ->
                map.overlays.clear()
                if (routeDataPoints.size >= 2) {
                    val speeds = RouteStatsCalculator.calculateSpeeds(routeDataPoints)
                    val maxSpeed = speeds.maxOrNull() ?: 0f

                    val first = geoPoints.first()
                    val last = geoPoints.last()

                    for (i in 0 until geoPoints.size - 1) {
                        val ratio = if (maxSpeed > 0) (speeds[i] / maxSpeed).coerceIn(0f, 1f) else 0f
                        val segment = Polyline()
                        segment.outlinePaint.color = RouteStatsCalculator.speedColor(ratio)
                        segment.outlinePaint.strokeWidth = 8f
                        segment.setPoints(listOf(geoPoints[i], geoPoints[i + 1]))
                        map.overlays.add(segment)
                    }

                    val startMarker = Marker(map)
                    startMarker.position = first
                    startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    startMarker.title = "Partenza"
                    map.overlays.add(startMarker)

                    val endMarker = Marker(map)
                    endMarker.position = last
                    endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    endMarker.title = "Arrivo"
                    map.overlays.add(endMarker)

                    map.controller.setCenter(first)
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
                modifier = Modifier.padding(12.dp)
            ) {
                route?.let { r ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            r.title,
                            color = White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                val r = route ?: return@IconButton
                                val result = GpxExporter.export(
                                    context, r, routeDataPoints
                                )
                                val msg = if (result != null) {
                                    "GPX salvato"
                                } else {
                                    "Errore salvataggio GPX"
                                }
                                android.widget.Toast
                                    .makeText(context, msg, android.widget.Toast.LENGTH_SHORT)
                                    .show()
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_download),
                                contentDescription = "Scarica GPX",
                                tint = Red
                            )
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        SimpleDateFormat(
                            "dd/MM/yyyy HH:mm",
                            Locale.getDefault()
                        ).format(Date(r.date)),
                        color = White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "%.2f km".format(r.distanceKm),
                            color = Red,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(
                            "%02d:%02d:%02d".format(
                                r.durationSec / 3600,
                                (r.durationSec % 3600) / 60,
                                r.durationSec % 60
                            ),
                            color = White.copy(alpha = 0.7f)
                        )
                    }
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
}
