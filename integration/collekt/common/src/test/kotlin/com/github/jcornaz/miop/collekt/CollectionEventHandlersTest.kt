package com.github.jcornaz.miop.collekt

import com.github.jcornaz.collekt.emptyPersistentMap
import com.github.jcornaz.miop.collection.MapCleared
import com.github.jcornaz.miop.collection.MapEntryAdded
import com.github.jcornaz.miop.collection.MapEntryRemoved
import com.github.jcornaz.miop.collection.MapEntryUpdated
import kotlin.test.Test
import kotlin.test.assertEquals

class CollectionEventHandlersTest {

    @Test
    fun test() {
        var map = emptyPersistentMap<Int, String>()
        assertEquals(emptyMap<Int, String>(), map)

        map += MapEntryAdded(1, "Hello")
        assertEquals(mapOf(1 to "Hello"), map)

        map += MapEntryAdded(2, "World")
        assertEquals(mapOf(1 to "Hello", 2 to "World"), map)

        map += MapEntryRemoved(2)
        assertEquals(mapOf(1 to "Hello"), map)

        map += MapEntryAdded(2, "Kotlin")
        assertEquals(mapOf(1 to "Hello", 2 to "Kotlin"), map)

        map += MapEntryUpdated(2, "persistent data structure")
        assertEquals(mapOf(1 to "Hello", 2 to "persistent data structure"), map)

        map += MapCleared
        assertEquals(emptyMap<Int, String>(), map)

        map += MapEntryAdded(1, "test")
        assertEquals(mapOf(1 to "test"), map)
    }
}
