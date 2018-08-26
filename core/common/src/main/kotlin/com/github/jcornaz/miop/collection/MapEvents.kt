package com.github.jcornaz.miop.collection

import com.github.jcornaz.miop.transform
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlin.coroutines.coroutineContext

/**
 * Represent an event which happened in a [Map]
 */
@Suppress("unused")
public sealed class MapEvent<out K, out V>

/** A **new** entry has been added to the map. (there wasn't any value associated to [key] before) */
public data class MapEntryAdded<out K, out V>(val key: K, val value: V) : MapEvent<K, V>()

/** The value associated to [key] has been replaced by [newValue] */
public data class MapEntryUpdated<out K, out V>(val key: K, val newValue: V) : MapEvent<K, V>()

/** An entry has been removed from the map */
public data class MapEntryRemoved<out K, out V>(val key: K) : MapEvent<K, V>()

/** The map has been cleared */
public object MapCleared : MapEvent<Nothing, Nothing>()

/**
 * Apply the [event] to this map
 */
public operator fun <K, V> MutableMap<in K, in V>.plusAssign(event: MapEvent<K, V>) {
    when (event) {
        is MapEntryAdded -> put(event.key, event.value)
        is MapEntryUpdated -> put(event.key, event.newValue)
        is MapEntryRemoved -> remove(event.key)
        MapCleared -> clear()
    }
}

/**
 * Compute deltas between each received map and emits the corresponding events.
 */
public fun <K, V> ReceiveChannel<Map<K, V>>.toMapEvents(initialMap: Map<K, V> = emptyMap()): ReceiveChannel<MapEvent<K, V>> = transform { input, output ->
    val currentMap: MutableMap<K, V> = HashMap(initialMap)

    input.consumeEach { newMap ->
        handleNewMap(currentMap, newMap, coroutineContext[Job]).consumeEach { output.send(it) }
    }
}

private fun <K, V> handleNewMap(currentMap: MutableMap<K, V>, newMap: Map<K, V>, parent: Job?): ReceiveChannel<MapEvent<K, V>> = produce(parent = parent, capacity = Channel.UNLIMITED) {
    if (newMap.isEmpty() && currentMap.isNotEmpty()) {
        send(MapCleared)
        currentMap.clear()
        return@produce
    }

    val toAdd = HashMap(newMap)

    val iterator = currentMap.iterator()
    while (iterator.hasNext()) {
        val entry = iterator.next()
        if (entry.key !in newMap) {
            send(MapEntryRemoved(entry.key))
            iterator.remove()
        } else {
            @Suppress("UNCHECKED_CAST")
            val newValue = newMap[entry.key] as V

            if (entry.value != newValue) {
                send(MapEntryUpdated(entry.key, newValue))
                entry.setValue(newValue)
            }
            toAdd -= entry.key
        }
    }

    toAdd.forEach { (key, value) ->
        send(MapEntryAdded(key, value))
        currentMap[key] = value
    }
}
