package com.github.jcornaz.miop.experimental.collection

import com.github.jcornaz.miop.experimental.receiveChannelOf
import com.github.jcornaz.miop.internal.test.runTest
import kotlinx.coroutines.experimental.channels.toList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SetEventTest {

    @Test
    fun testSetEventScenario() = runTest {
        val set = mutableSetOf(1, 2)

        val events = receiveChannelOf(
            setOf(1, 3),
            emptySet(),
            setOf(2, 3, 4),
            setOf(2, 4)
        )
            .toSetEvents(set)
            .toList()

        assertTrue(SetCleared in events)
        assertTrue(events.any { it is SetElementAdded })
        assertTrue(events.any { it is SetElementRemoved })

        events.forEach {
            println(it)
            set += it
        }

        assertEquals(setOf(2, 4), set)
    }

    @Test
    fun shouldEmitClearEvent() = runTest {
        val initialSet = setOf(1, 2)

        val events = receiveChannelOf(emptySet<Int>())
            .toSetEvents(initialSet)
            .toList()

        assertEquals(listOf(SetCleared), events)
    }

    @Test
    fun shouldEmitAddEvents() = runTest {
        val initialSet = setOf(1)

        val events = receiveChannelOf(setOf(1, 2))
            .toSetEvents(initialSet)
            .toList()

        assertEquals(listOf(SetElementAdded(2)), events)
    }

    @Test
    fun shouldEmitRemoveEvents() = runTest {
        val initialSet = setOf(1, 2)

        val events = receiveChannelOf(setOf(1))
            .toSetEvents(initialSet)
            .toList()

        assertEquals(listOf(SetElementRemoved(2)), events)
    }

    @Test
    fun shouldNotEmitEventsForIdenticalSets() = runTest {
        assertTrue(receiveChannelOf(emptySet<Int>()).toSetEvents().toList().isEmpty())
        assertTrue(receiveChannelOf(setOf(1, 2), setOf(1, 2)).toSetEvents(setOf(1, 2)).toList().isEmpty())
    }
}
