package com.fabiofiorini.pathtracker.ui.map

import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.fabiofiorini.pathtracker.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fabiofiorini.pathtracker.service.TrackingService
import com.fabiofiorini.pathtracker.ui.theme.Dark
import com.fabiofiorini.pathtracker.ui.theme.Orange
import com.fabiofiorini.pathtracker.ui.theme.Red
import com.fabiofiorini.pathtracker.ui.theme.White
import com.fabiofiorini.pathtracker.viewmodel.TrackingViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MapScreen(
    onStop: () -> Unit
) {
    val context = LocalContext.current

    var mapView by remember {
        mutableStateOf<MapView?>(null)
    }

    var showDialog by remember { mutableStateOf(false) }
    var routeTitle by remember { mutableStateOf("") }

    var marker by remember {
        mutableStateOf<Marker?>(null)
    }

    var polyline by remember {
        mutableStateOf<Polyline?>(null)
    }

    var mapCentered by remember { mutableStateOf(false) }

    val viewModel: TrackingViewModel = viewModel()

    val routePoints = viewModel.routePoints

    val elapsedSeconds = viewModel.elapsedSeconds.longValue

    val distanceMeters = viewModel.distanceMeters.floatValue

    val steps = viewModel.trackingManager.steps.intValue

    LaunchedEffect(true) {
        Configuration.getInstance().userAgentValue =
            context.packageName

        val intent = Intent(
            context,
            TrackingService::class.java
        )

        context.startForegroundService(intent)
    }

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
                            "Registra il percorso",
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
                map.onResume()
                map.setTileSource(TileSourceFactory.MAPNIK)
                map.setMultiTouchControls(true)
                map.controller.setZoom(19.0)

                val m = Marker(map)
                m.setIcon(ContextCompat.getDrawable(ctx, R.drawable.ic_marker_current))
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
                    if (!mapCentered) {
                        map.controller.setCenter(lastPoint)
                        mapCentered = true
                    }
                    marker?.position = lastPoint
                    if (polyline?.getActualPoints()?.size != routePoints.size) {
                        polyline?.setPoints(routePoints)
                    }
                    map.invalidate()
                }
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_schedule),
                            contentDescription = null,
                            tint = Red,
                            modifier = Modifier.size(18.dp)
                        )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "%02d:%02d:%02d".format(
                            elapsedSeconds / 3600,
                            (elapsedSeconds % 3600) / 60,
                            elapsedSeconds % 60
                        ),
                        color = White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_map),
                        contentDescription = null,
                        tint = Orange,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "%.2f km".format(distanceMeters / 1000f),
                        color = White.copy(alpha = 0.8f)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_footsteps),
                        contentDescription = null,
                        tint = Orange,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "$steps",
                        color = White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = {
                val point = routePoints.lastOrNull()
                if (point != null) {
                    mapView?.controller?.setCenter(point)
                    mapView?.controller?.setZoom(19.0)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Orange,
            contentColor = White
        ) {
            Icon(painterResource(R.drawable.ic_my_location), "Centra posizione")
        }

        BackHandler {
            showDialog = true
        }

        FloatingActionButton(
            onClick = {
                showDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            containerColor = Red,
            contentColor = White
        ) {
            Icon(painterResource(R.drawable.ic_save), contentDescription = null)
        }

        if (showDialog) {
            val canSave = routeTitle.isNotBlank()
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                },
                containerColor = Color(0xFF2A2A2A),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { showDialog = false }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_back),
                                contentDescription = "Indietro",
                                tint = White
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("Salva percorso", color = Red)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = routeTitle,
                            onValueChange = {
                                routeTitle = it
                            },
                            label = {
                                Text("Titolo *")
                            },
                            isError = routeTitle.isBlank(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Red,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedLabelColor = Red,
                                unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                                cursorColor = Red
                            )
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(context, TrackingService::class.java)
                                    context.stopService(intent)
                                    viewModel.exitWithoutSaving()
                                    showDialog = false
                                    onStop()
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Red),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Esci senza salvare")
                            }
                            Button(
                                onClick = {
                                    val intent = Intent(context, TrackingService::class.java)
                                    context.stopService(intent)
                                    viewModel.saveCurrentRoute(routeTitle)
                                    showDialog = false
                                    onStop()
                                },
                                enabled = canSave,
                                colors = ButtonDefaults.buttonColors(containerColor = Red),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Salva", color = White)
                            }
                        }
                    }
                },
                confirmButton = {}
            )
        }
    }
    }
}
