package com.fabiofiorini.traveltracker.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.fabiofiorini.traveltracker.R
import com.fabiofiorini.traveltracker.tracking.TrackingManager
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class TrackingService : Service() {

    private lateinit var fusedClient: FusedLocationProviderClient

    private var lastLocation: Location? = null

    private val smoothPoints = mutableListOf<GeoPoint>()

    private val timerScope = CoroutineScope(Dispatchers.Default)

    private var timerJob: Job? = null

    private lateinit var callback: LocationCallback

    private val trackingManager: TrackingManager
        get() = TrackingManager.current
            ?: error("TrackingManager non inizializzato")

    override fun onCreate() {
        super.onCreate()

        fusedClient =
            LocationServices.getFusedLocationProviderClient(this)

        createNotificationChannel()

        startForeground(1, createNotification())

        startTimer()

        startTracking()
    }

    private fun startTimer() {

        timerJob?.cancel()

        timerJob = timerScope.launch {

            while (true) {
                delay(1000)

                if (trackingManager.isTracking.value) {
                    trackingManager.elapsedSeconds.longValue++
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startTracking() {

        trackingManager.isTracking.value = true

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000
        ).build()

        callback = object : LocationCallback() {

            override fun onLocationResult(result: LocationResult) {

                val location = result.lastLocation ?: return

                val newPoint = GeoPoint(
                    location.latitude,
                    location.longitude
                )

                smoothPoints.add(newPoint)

                if (smoothPoints.size > 5) {
                    smoothPoints.removeAt(0)
                }

                val avgLat = smoothPoints.map { it.latitude }.average()
                val avgLon = smoothPoints.map { it.longitude }.average()
                val smoothPoint = GeoPoint(avgLat, avgLon)

                val previousPoint = trackingManager.points.lastOrNull()

                if (previousPoint != null) {

                    val results = FloatArray(1)

                    Location.distanceBetween(
                        previousPoint.latitude,
                        previousPoint.longitude,
                        smoothPoint.latitude,
                        smoothPoint.longitude,
                        results
                    )

                    val distance = results[0]

                    if (distance < 3f) {
                        return
                    }
                }

                trackingManager.points.add(smoothPoint)
                trackingManager.timestamps.add(System.currentTimeMillis())

                if (lastLocation != null) {
                    trackingManager.distanceMeters.floatValue +=
                        lastLocation!!.distanceTo(location)
                }

                lastLocation = location
            }
        }
        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedClient.requestLocationUpdates(
            request,
            callback,
            mainLooper
        )
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                "tracking_channel",
                "Tracking",
                NotificationManager.IMPORTANCE_LOW
            )

            val manager =
                getSystemService(NotificationManager::class.java)

            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {

        return NotificationCompat.Builder(this, "tracking_channel")
            .setContentTitle("TravelTracker")
            .setContentText("Tracking percorso attivo")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()

        fusedClient.removeLocationUpdates(callback)

        timerJob?.cancel()

        trackingManager.isTracking.value = false
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
