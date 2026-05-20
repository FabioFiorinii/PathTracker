package com.fabiofiorini.traveltracker.ui.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fabiofiorini.traveltracker.viewmodel.TrackingViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline

@Composable
fun RouteMapScreen(
    routeId: Long,
    onClose: () -> Unit
) {

    var points by remember {
        mutableStateOf<List<GeoPoint>>(emptyList())
    }

    val viewModel: TrackingViewModel = viewModel()

    LaunchedEffect(Unit) {

        val routePoints = viewModel.getPoints(routeId)

        points = routePoints.map {
            GeoPoint(it.lat, it.lon)
        }
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
                    polyline.setPoints(points)

                    map.overlays.add(polyline)

                    map.controller.setCenter(points.first())
                }

                map
            },
            update = { map ->

                map.overlays.clear()

                if (points.isNotEmpty()) {

                    val polyline = Polyline()
                    polyline.setPoints(points)

                    map.overlays.add(polyline)

                    map.controller.setCenter(points.first())
                }

                map.invalidate()
            }
        )

        FloatingActionButton(
            onClick = {
                onClose()
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null
            )
        }
    }
}