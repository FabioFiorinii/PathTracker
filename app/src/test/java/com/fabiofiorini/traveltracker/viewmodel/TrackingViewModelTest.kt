package com.fabiofiorini.traveltracker.viewmodel

import android.app.Application
import com.fabiofiorini.traveltracker.data.AppDatabase
import com.fabiofiorini.traveltracker.data.DatabaseProvider
import com.fabiofiorini.traveltracker.data.RouteDao
import com.fabiofiorini.traveltracker.data.RouteEntity
import com.fabiofiorini.traveltracker.tracking.TrackingManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.osmdroid.util.GeoPoint

class TrackingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val app = mockk<Application>(relaxed = true)
    private val dao = mockk<RouteDao>(relaxed = true)
    private val db = mockk<AppDatabase>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockkObject(DatabaseProvider)
        every { DatabaseProvider.getDatabase(app) } returns db
        every { db.routeDao() } returns dao
        coEvery { dao.getAllRoutes() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        TrackingManager.reset()
        unmockkAll()
    }

    @Test
    fun `saveCurrentRoute persists route and points then resets`() = runTest(testDispatcher) {
        val routeSlot = slot<RouteEntity>()
        coEvery { dao.insertRoute(capture(routeSlot)) } returns 1L
        coEvery { dao.insertPoints(any()) } returns Unit

        TrackingManager.points.add(GeoPoint(45.0, 9.0))
        TrackingManager.elapsedSeconds.longValue = 600L
        TrackingManager.distanceMeters.floatValue = 3000f

        val viewModel = TrackingViewModel(app)
        viewModel.saveCurrentRoute("Gita al lago")
        testDispatcher.scheduler.advanceUntilIdle()

        val captured = routeSlot.captured
        assert(captured.title == "Gita al lago")
        assert(captured.distanceKm == 3f)
        assert(captured.durationSec == 600L)

        coVerify { dao.insertRoute(any()) }
        coVerify { dao.insertPoints(any()) }

        assert(TrackingManager.points.isEmpty())
        assert(TrackingManager.elapsedSeconds.longValue == 0L)
    }

    @Test
    fun `deleteRoute calls dao delete methods`() = runTest(testDispatcher) {
        val viewModel = TrackingViewModel(app)
        val route = RouteEntity(id = 5, title = "X", distanceKm = 1f, durationSec = 100L, averageSpeedKmh = 10f, date = 1000L)

        viewModel.deleteRoute(route)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { dao.deletePointsByRoute(5L) }
        coVerify { dao.deleteRoute(route) }
    }
}
