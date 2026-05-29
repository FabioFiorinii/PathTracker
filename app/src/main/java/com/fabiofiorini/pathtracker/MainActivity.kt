package com.fabiofiorini.pathtracker

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.fabiofiorini.pathtracker.ui.map.MapScreen
import com.fabiofiorini.pathtracker.ui.history.HistoryScreen
import com.fabiofiorini.pathtracker.ui.history.RouteMapScreen
import com.fabiofiorini.pathtracker.ui.start.StartScreen
import com.fabiofiorini.pathtracker.ui.theme.PathTrackerTheme
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔧 Inizializza osmdroid (OBBLIGATORIO)
        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osm", MODE_PRIVATE)
        )

        // 🎨 UI Compose
        setContent {
            PathTrackerTheme {

                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "start"
                ) {

                    composable("start") {
                        StartScreen(
                            onStartTracking = {
                                navController.navigate("map")
                            },
                            onHistory = {
                                navController.navigate("history")
                            }
                        )
                    }

                    composable("map") {
                        MapScreen(
                            onStop = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable("history") {
                        HistoryScreen(
                            onBack = {
                                navController.popBackStack()
                            },
                            onRouteSelected = { routeId ->
                                navController.navigate("routeMap/$routeId")
                            }
                        )
                    }

                    composable(
                        "routeMap/{routeId}",
                        arguments = listOf(
                            navArgument("routeId") {
                                type = NavType.LongType
                            }
                        )
                    ) {

                        val routeId = it.arguments?.getLong("routeId") ?: 0L

                        RouteMapScreen(
                            routeId = routeId,
                            onClose = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}