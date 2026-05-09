package com.fabiofiorini.traveltracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.navigation.compose.*
import com.fabiofiorini.traveltracker.ui.map.MapScreen
import com.fabiofiorini.traveltracker.ui.start.StartScreen
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {

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
                                // per ora non fa nulla
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
                }
            }
        }
    }
}
