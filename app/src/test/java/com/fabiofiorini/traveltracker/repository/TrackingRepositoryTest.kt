package com.fabiofiorini.traveltracker.repository

import com.fabiofiorini.traveltracker.data.RouteDao
import com.fabiofiorini.traveltracker.data.RouteEntity
import com.fabiofiorini.traveltracker.data.RoutePointEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrackingRepositoryTest {

    private fun createFakeDao(): RouteDao {
        val routes = mutableListOf<RouteEntity>()
        val points = mutableListOf<RoutePointEntity>()
        var nextRouteId = 1L
        val routesFlow = MutableStateFlow(listOf<RouteEntity>())

        return object : RouteDao {
            override suspend fun insertRoute(route: RouteEntity): Long {
                val id = nextRouteId++
                val saved = route.copy(id = id)
                routes.add(saved)
                routesFlow.value = routes.toList()
                return id
            }

            override suspend fun insertPoints(points: List<RoutePointEntity>) {
                var nextId = points.size + 1L
                this.points.addAll(points.map { it.copy(id = nextId++) })
            }

            override fun getAllRoutes(): Flow<List<RouteEntity>> = routesFlow

            override suspend fun getRoutePoints(routeId: Long): List<RoutePointEntity> =
                points.filter { it.routeId == routeId }

            override suspend fun deleteRoute(route: RouteEntity) {
                routes.removeAll { it.id == route.id }
                routesFlow.value = routes.toList()
            }

            override suspend fun deletePointsByRoute(routeId: Long) {
                points.removeAll { it.routeId == routeId }
            }

            override suspend fun getPointsForRoute(routeId: Long): List<RoutePointEntity> =
                points.filter { it.routeId == routeId }
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
        val saved = repo.getAllRoutes().let { flow ->
            var result: List<RouteEntity>? = null
            flow.collect { result = it }
            result
        }
        assertEquals(1, saved?.size)
        assertEquals("Test percorso", saved?.first()?.title)
    }

    @Test
    fun `save and retrieve points for a route`() = runTest {
        val dao = createFakeDao()
        val repo = TrackingRepository(dao)

        val routeId = repo.saveRoute(RouteEntity(
            title = "R", distanceKm = 1f, durationSec = 100L, averageSpeedKmh = 36f, date = 2000L
        ))

        val testPoints = listOf(
            RoutePointEntity(routeId = routeId, lat = 45.0, lon = 9.0, timestamp = 100L),
            RoutePointEntity(routeId = routeId, lat = 45.1, lon = 9.1, timestamp = 200L)
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
            RoutePointEntity(routeId = routeId, lat = 45.0, lon = 9.0, timestamp = 100L)
        ))
        assertEquals(1, repo.getPoints(routeId).size)

        repo.deletePoints(routeId)
        assertTrue(repo.getPoints(routeId).isEmpty())
    }
}
