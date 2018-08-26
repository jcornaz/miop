package com.github.jcornaz.miop.experimental.collection

import com.github.jcornaz.miop.experimental.receiveChannelOf
import com.github.jcornaz.miop.internal.test.runTest
import kotlinx.coroutines.experimental.channels.toList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MapEventTest {

    @Test
    fun testMapEventScenario() = runTest {

        val map = mutableMapOf(
            1 to "one",
            2 to "two"
        )

        val events = receiveChannelOf(
            mapOf(1 to "un", 3 to "trois"),
            mapOf(2 to "two"),
            emptyMap(),
            mapOf(1 to "two", 2 to "one", 3 to "zero")
        )
            .toMapEvents(map)
            .toList()

        assertTrue(MapCleared in events)
        assertTrue(events.any { it is MapEntryAdded })
        assertTrue(events.any { it is MapEntryUpdated })
        assertTrue(events.any { it is MapEntryRemoved })

        events.forEach { map += it }

        assertEquals(mapOf(1 to "two", 2 to "one", 3 to "zero"), map)
    }

    @Test
    fun shouldEmitClearEvent() = runTest {
        val initialMap = mapOf(1 to "one", 2 to "two")

        val events = receiveChannelOf(emptyMap<Int, String>())
            .toMapEvents(initialMap)
            .toList()

        assertEquals(listOf(MapCleared), events)
    }

    @Test
    fun shouldEmitAddEvents() = runTest {
        val initialMap = mapOf(1 to "one")

        val events = receiveChannelOf(mapOf(1 to "one", 2 to "two"))
            .toMapEvents(initialMap)
            .toList()

        assertEquals(listOf(MapEntryAdded(2, "two")), events)
    }

    @Test
    fun shouldEmitRemoveEvents() = runTest {
        val initialMap = mapOf(1 to "one", 2 to "two")

        val events = receiveChannelOf(mapOf(1 to "one"))
            .toMapEvents(initialMap)
            .toList()

        assertEquals(listOf(MapEntryRemoved(2)), events)
    }

    @Test
    fun shouldEmitUpdateEvents() = runTest {
        val initialMap = mapOf(1 to "one", 2 to "two")

        val events = receiveChannelOf(mapOf(1 to "one", 2 to "deux"))
            .toMapEvents(initialMap)
            .toList()

        assertEquals(listOf(MapEntryUpdated(2, "deux")), events)
    }

    @Test
    fun shouldNotEmitEventsForIdenticalMaps() = runTest {
        assertTrue(receiveChannelOf(emptyMap<Int, String>()).toMapEvents().toList().isEmpty())
        assertTrue(receiveChannelOf(mapOf(1 to "one"), mapOf(1 to "one")).toMapEvents(mapOf(1 to "one")).toList().isEmpty())
    }
}
