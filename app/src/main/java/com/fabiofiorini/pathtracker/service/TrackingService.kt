package com.fabiofiorini.pathtracker.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.fabiofiorini.pathtracker.R
import com.fabiofiorini.pathtracker.tracking.TrackingManager
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class TrackingService : Service() {

    companion object {
        private const val SMOOTHING_BUFFER = 5
        private const val JITTER_FILTER_M = 3f
        private const val NOTIFICATION_ID = 1
        private const val LOCATION_INTERVAL_MS = 3000L
        private const val MIN_DISTANCE_M = 5f
        private const val TIMER_DELAY_ACTIVE_MS = 1000L
        private const val TIMER_DELAY_IDLE_MS = 5000L
    }

    private lateinit var fusedClient: FusedLocationProviderClient

    private lateinit var sensorManager: SensorManager

    private var stepSensor: Sensor? = null

    private var lastLocation: Location? = null

    private val smoothPoints = mutableListOf<GeoPoint>()

    private val timerScope = CoroutineScope(Dispatchers.Main)

    private var timerJob: Job? = null

    private lateinit var callback: LocationCallback

    private val trackingManager: TrackingManager?
        get() = TrackingManager.current

    private val stepListener = object : SensorEventListener {
        private var baselineSet = false
        private var baseline = 0

        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type != Sensor.TYPE_STEP_COUNTER) return
            val value = event.values[0].toInt()
            if (!baselineSet) {
                baseline = value
                baselineSet = true
            }
            trackingManager?.steps?.intValue = value - baseline
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    override fun onCreate() {
        super.onCreate()

        fusedClient =
            LocationServices.getFusedLocationProviderClient(this)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        createNotificationChannel()

        startForeground(NOTIFICATION_ID, createNotification())

        val tm = trackingManager
        if (tm == null) {
            stopSelf()
            return
        }

        startTimer(tm)
        startTracking(tm)

        stepSensor?.let {
            sensorManager.registerListener(
                stepListener, it, SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    private fun startTimer(tm: TrackingManager) {

        timerJob?.cancel()

        timerJob = timerScope.launch {

            while (true) {
                if (tm.isTracking.value) {
                    delay(TIMER_DELAY_ACTIVE_MS)
                    tm.elapsedSeconds.longValue++
                } else {
                    delay(TIMER_DELAY_IDLE_MS)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startTracking(tm: TrackingManager) {

        tm.isTracking.value = true

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            LOCATION_INTERVAL_MS
        )
            .setMinUpdateDistanceMeters(MIN_DISTANCE_M)
            .build()

        callback = object : LocationCallback() {

            override fun onLocationResult(result: LocationResult) {

                val location = result.lastLocation ?: return

                val newPoint = GeoPoint(
                    location.latitude,
                    location.longitude
                )

                smoothPoints.add(newPoint)

                if (smoothPoints.size > SMOOTHING_BUFFER) {
                    smoothPoints.removeAt(0)
                }

                val avgLat = smoothPoints.map { it.latitude }.average()
                val avgLon = smoothPoints.map { it.longitude }.average()
                val smoothPoint = GeoPoint(avgLat, avgLon)

                    val previousPoint = tm.points.lastOrNull()
                var segmentDistance = 0f
                if (previousPoint != null) {
                    val results = FloatArray(1)
                    Location.distanceBetween(
                        previousPoint.latitude,
                        previousPoint.longitude,
                        smoothPoint.latitude,
                        smoothPoint.longitude,
                        results
                    )
                    segmentDistance = results[0]
                    if (segmentDistance < JITTER_FILTER_M) {
                        return
                    }
                }

                tm.points.add(smoothPoint)
                tm.timestamps.add(System.currentTimeMillis())
                tm.distanceMeters.floatValue += segmentDistance
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
            .setContentTitle("PathTracker")
            .setContentText("Tracking percorso attivo")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()

        sensorManager.unregisterListener(stepListener)

        fusedClient.removeLocationUpdates(callback)

        timerJob?.cancel()

        trackingManager?.isTracking?.value = false
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
