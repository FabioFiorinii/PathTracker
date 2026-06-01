package com.fabiofiorini.pathtracker.viewmodel

import android.app.Application
import com.fabiofiorini.pathtracker.data.RouteEntity
import com.fabiofiorini.pathtracker.data.RoutePointEntity
import com.fabiofiorini.pathtracker.repository.TrackingRepository
import com.fabiofiorini.pathtracker.tracking.TrackingManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.osmdroid.util.GeoPoint

@OptIn(ExperimentalCoroutinesApi::class)
class TrackingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val app = mockk<Application>(relaxed = true)
    private val repo = mockk<TrackingRepository>(relaxed = true)
    private val tm = TrackingManager()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { repo.getAllRoutes() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        tm.reset()
        unmockkAll()
    }

    @Test
    fun `saveCurrentRoute persists route and points then resets`() = runTest(testDispatcher) {
        val routeSlot = slot<RouteEntity>()
        coEvery { repo.saveRouteWithPoints(capture(routeSlot), any()) } answers {
            secondArg<(Long) -> List<RoutePointEntity>>().invoke(1L)
            1L
        }

        val viewModel = TrackingViewModel(app, repo, tm)
        tm.points.add(GeoPoint(45.0, 9.0))
        tm.timestamps.add(100L)
        tm.elapsedSeconds.longValue = 600L
        tm.distanceMeters.floatValue = 3000f

        viewModel.saveCurrentRoute("Gita al lago")
        testDispatcher.scheduler.advanceUntilIdle()

        val captured = routeSlot.captured
        assert(captured.title == "Gita al lago")
        assert(captured.distanceKm == 3f)
        assert(captured.durationSec == 600L)

        coVerify { repo.saveRouteWithPoints(any(), any()) }

        assert(tm.points.isEmpty())
        assert(tm.elapsedSeconds.longValue == 0L)
    }

    @Test
    fun `deleteRoute calls dao delete methods`() = runTest(testDispatcher) {
        val viewModel = TrackingViewModel(app, repo, tm)
        val route = RouteEntity(id = 5, title = "X", distanceKm = 1f, durationSec = 100L, averageSpeedKmh = 10f, date = 1000L)

        viewModel.deleteRoute(route)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repo.deletePoints(5L) }
        coVerify { repo.deleteRouteById(5L) }
    }
}
