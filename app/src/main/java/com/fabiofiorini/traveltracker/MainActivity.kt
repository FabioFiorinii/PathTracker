package com.fabiofiorini.traveltracker

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.fabiofiorini.traveltracker.ui.map.MapScreen
import com.fabiofiorini.traveltracker.ui.history.HistoryScreen
import com.fabiofiorini.traveltracker.ui.history.RouteMapScreen
import com.fabiofiorini.traveltracker.ui.start.StartScreen
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
            MaterialTheme {

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
                            navController = navController
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