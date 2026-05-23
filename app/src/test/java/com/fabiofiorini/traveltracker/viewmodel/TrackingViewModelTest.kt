package com.fabiofiorini.traveltracker.viewmodel

import android.app.Application
import com.fabiofiorini.traveltracker.data.RouteEntity
import com.fabiofiorini.traveltracker.repository.TrackingRepository
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

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { repo.getAllRoutes() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `saveCurrentRoute persists route and points then resets`() = runTest(testDispatcher) {
        val routeSlot = slot<RouteEntity>()
        coEvery { repo.saveRoute(capture(routeSlot)) } returns 1L
        coEvery { repo.savePoints(any()) } returns Unit

        val viewModel = TrackingViewModel(app, repo)
        val tm = viewModel.trackingManager
        tm.points.add(GeoPoint(45.0, 9.0))
        tm.elapsedSeconds.longValue = 600L
        tm.distanceMeters.floatValue = 3000f

        viewModel.saveCurrentRoute("Gita al lago")
        testDispatcher.scheduler.advanceUntilIdle()

        val captured = routeSlot.captured
        assert(captured.title == "Gita al lago")
        assert(captured.distanceKm == 3f)
        assert(captured.durationSec == 600L)

        coVerify { repo.saveRoute(any()) }
        coVerify { repo.savePoints(any()) }

        assert(tm.points.isEmpty())
        assert(tm.elapsedSeconds.longValue == 0L)
    }

    @Test
    fun `deleteRoute calls dao delete methods`() = runTest(testDispatcher) {
        val viewModel = TrackingViewModel(app, repo)
        val route = RouteEntity(id = 5, title = "X", distanceKm = 1f, durationSec = 100L, averageSpeedKmh = 10f, date = 1000L)

        viewModel.deleteRoute(route)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repo.deletePoints(5L) }
        coVerify { repo.deleteRoute(route) }
    }
}
