package com.fabiofiorini.pathtracker.repository

import com.fabiofiorini.pathtracker.data.RouteDao
import com.fabiofiorini.pathtracker.data.RouteEntity
import com.fabiofiorini.pathtracker.data.RoutePointEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackingRepositoryTest {

    private fun createFakeDao(): RouteDao {
        val savedRoutes = mutableListOf<RouteEntity>()
        val savedPoints = mutableListOf<RoutePointEntity>()
        var nextRouteId = 1L
        val routesFlow = MutableStateFlow(listOf<RouteEntity>())

        return object : RouteDao {
            override suspend fun insertRoute(route: RouteEntity): Long {
                val id = nextRouteId++
                val saved = route.copy(id = id)
                savedRoutes.add(saved)
                routesFlow.value = savedRoutes.toList()
                return id
            }

            override suspend fun insertPoints(points: List<RoutePointEntity>) {
                savedPoints.addAll(points)
            }

            override fun getAllRoutes(): Flow<List<RouteEntity>> = routesFlow

            override suspend fun getRoutePoints(routeId: Long): List<RoutePointEntity> =
                savedPoints.filter { it.routeId == routeId }

            override suspend fun deleteRouteById(routeId: Long) {
                savedRoutes.removeAll { it.id == routeId }
                routesFlow.value = savedRoutes.toList()
            }

            override suspend fun deletePointsByRoute(routeId: Long) {
                savedPoints.removeAll { it.routeId == routeId }
            }

            override suspend fun getPointsForRoute(routeId: Long): List<RoutePointEntity> =
                savedPoints.filter { it.routeId == routeId }

            override suspend fun getRouteById(routeId: Long): RouteEntity? =
                savedRoutes.find { it.id == routeId }

            override fun getRouteCount(): Flow<Int> =
                MutableStateFlow(savedRoutes.size)

            override fun getTotalDistanceKm(): Flow<Float> =
                MutableStateFlow(savedRoutes.sumOf { it.distanceKm.toDouble() }.toFloat())

            override fun getTotalDurationSec(): Flow<Long> =
                MutableStateFlow(savedRoutes.sumOf { it.durationSec })
        }
    }

    @Test
    fun `save and retrieve routes`() = runTest {
        val dao = createFakeDao()
        val repo = TrackingRepository(dao)

        val route = RouteEntity(
            title = "Test percorso",
            distanceKm = 5.2f,
            durationSec = 1800L,
            averageSpeedKmh = 10.4f,
            date = 1000L
        )
        val id = repo.saveRoute(route)

        assertTrue(id > 0)
        val saved = repo.getAllRoutes().first()
        assertEquals(1, saved.size)
        assertEquals("Test percorso", saved.first().title)
    }

    @Test
    fun `save and retrieve points for a route`() = runTest {
        val dao = createFakeDao()
        val repo = TrackingRepository(dao)

        val routeId = repo.saveRoute(RouteEntity(
            title = "R", distanceKm = 1f, durationSec = 100L, averageSpeedKmh = 36f, date = 2000L
        ))

        val testPoints = listOf(
            RoutePointEntity(routeId = routeId, orderIndex = 0, lat = 45.0, lon = 9.0, timestampSec = 100),
            RoutePointEntity(routeId = routeId, orderIndex = 1, lat = 45.1, lon = 9.1, timestampSec = 200)
        )
        repo.savePoints(testPoints)

        val retrieved = repo.getPoints(routeId)
        assertEquals(2, retrieved.size)
    }

    @Test
    fun `delete route also removes its points`() = runTest {
        val dao = createFakeDao()
        val repo = TrackingRepository(dao)

        val routeId = repo.saveRoute(RouteEntity(
            title = "Da cancellare", distanceKm = 1f, durationSec = 100L, averageSpeedKmh = 10f, date = 3000L
        ))
        repo.savePoints(listOf(
            RoutePointEntity(routeId = routeId, orderIndex = 0, lat = 45.0, lon = 9.0, timestampSec = 100)
        ))
        assertEquals(1, repo.getPoints(routeId).size)

        repo.deletePoints(routeId)
        assertTrue(repo.getPoints(routeId).isEmpty())
    }
}
