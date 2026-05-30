package com.fabiofiorini.pathtracker.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RouteDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: RouteDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.routeDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndGetRoutes() = runBlocking {
        val route = RouteEntity(
            title = "Prova", distanceKm = 10f, durationSec = 3600L, averageSpeedKmh = 10f, date = 1000L
        )
        val id = dao.insertRoute(route)

        val routes = dao.getAllRoutes().first()
        assertEquals(1, routes.size)
        assertEquals(id, routes.first().id)
        assertEquals("Prova", routes.first().title)
    }

    @Test
    fun insertAndGetRoutePoints() = runBlocking {
        val routeId = dao.insertRoute(
            RouteEntity(title = "R", distanceKm = 1f, durationSec = 100L, averageSpeedKmh = 36f, date = 2000L)
        )
        dao.insertPoints(
            listOf(
                RoutePointEntity(routeId = routeId, orderIndex = 0, lat = 45.0, lon = 9.0, timestampSec = 100),
                RoutePointEntity(routeId = routeId, orderIndex = 1, lat = 45.1, lon = 9.1, timestampSec = 200)
            )
        )

        val points = dao.getPointsForRoute(routeId)
        assertEquals(2, points.size)
        assertEquals(45.0, points[0].lat, 0.001)
    }

    @Test
    fun deleteRouteRemovesPoints() = runBlocking {
        val routeId = dao.insertRoute(
            RouteEntity(title = "R", distanceKm = 1f, durationSec = 100L, averageSpeedKmh = 36f, date = 3000L)
        )
        dao.insertPoints(
            listOf(RoutePointEntity(routeId = routeId, orderIndex = 0, lat = 45.0, lon = 9.0, timestampSec = 100))
        )

        dao.deletePointsByRoute(routeId)
        assertTrue(dao.getPointsForRoute(routeId).isEmpty())

        val route = dao.getAllRoutes().first().first()
        dao.deleteRouteById(route.id)
        assertTrue(dao.getAllRoutes().first().isEmpty())
    }
}
