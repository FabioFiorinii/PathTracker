package com.fabiofiorini.traveltracker.viewmodel

import android.app.Application
import androidx.room.RoomDatabase
import com.fabiofiorini.traveltracker.data.AppDatabase
import com.fabiofiorini.traveltracker.data.DatabaseProvider
import com.fabiofiorini.traveltracker.data.RouteDao
import com.fabiofiorini.traveltracker.data.RouteEntity
import com.fabiofiorini.traveltracker.tracking.TrackingManager
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.verify
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

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        TrackingManager.reset()
    }

    @Test
    fun `saveCurrentRoute persists route and points then resets`() = runTest(testDispatcher) {
        val app = mockk<Application>(relaxed = true)
        val dao = mockk<RouteDao>(relaxed = true)
        val db = mockk<AppDatabase>()

        mockkObject(DatabaseProvider)
        every { DatabaseProvider.getDatabase(app) } returns db
        every { db.routeDao() } returns dao
        every { dao.getAllRoutes() } returns flowOf(emptyList())

        val routeSlot = slot<RouteEntity>()
        every { dao.insertRoute(capture(routeSlot)) } returns 1L
        every { dao.insertPoints(any()) } just Runs

        TrackingManager.points.add(GeoPoint(45.0, 9.0))
        TrackingManager.elapsedSeconds.longValue = 600L
        TrackingManager.distanceMeters.floatValue = 3000f

        val viewModel = TrackingViewModel(app)
        viewModel.saveCurrentRoute("Gita al lago")

        val captured = routeSlot.captured
        assert(captured.title == "Gita al lago")
        assert(captured.distanceKm == 3f)
        assert(captured.durationSec == 600L)

        verify { dao.insertRoute(any()) }
        verify { dao.insertPoints(any()) }

        assert(TrackingManager.points.isEmpty())
        assert(TrackingManager.elapsedSeconds.longValue == 0L)
    }

    @Test
    fun `deleteRoute calls dao delete methods`() = runTest(testDispatcher) {
        val app = mockk<Application>(relaxed = true)
        val dao = mockk<RouteDao>(relaxed = true)
        val db = mockk<AppDatabase>()

        mockkObject(DatabaseProvider)
        every { DatabaseProvider.getDatabase(app) } returns db
        every { db.routeDao() } returns dao
        every { dao.getAllRoutes() } returns flowOf(emptyList())

        val viewModel = TrackingViewModel(app)
        val route = RouteEntity(id = 5, title = "X", distanceKm = 1f, durationSec = 100L, averageSpeedKmh = 10f, date = 1000L)

        viewModel.deleteRoute(route)

        verify { dao.deletePointsByRoute(5L) }
        verify { dao.deleteRoute(route) }
    }
}
