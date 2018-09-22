package com.github.jcornaz.miop.collection

import com.github.jcornaz.miop.transform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce

/**
 * Represent an event which happened in a [Map]
 */
@Suppress("unused")
@ExperimentalCollectionEvent
public sealed class MapEvent<out K, out V>

/** A **new** entry has been added to the map. (there wasn't any value associated to [key] before) */
@ExperimentalCollectionEvent
public data class MapEntryAdded<out K, out V>(val key: K, val value: V) : MapEvent<K, V>()

/** The value associated to [key] has been replaced by [newValue] */
@ExperimentalCollectionEvent
public data class MapEntryUpdated<out K, out V>(val key: K, val newValue: V) : MapEvent<K, V>()

/** An entry has been removed from the map */
@ExperimentalCollectionEvent
public data class MapEntryRemoved<out K, out V>(val key: K) : MapEvent<K, V>()

/** The map has been cleared */
@ExperimentalCollectionEvent
public object MapCleared : MapEvent<Nothing, Nothing>()

/**
 * Apply the [event] to this map
 */
@ExperimentalCollectionEvent
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
@ExperimentalCollectionEvent
public fun <K, V> ReceiveChannel<Map<out K, V>>.toMapEvents(initialMap: Map<out K, V> = emptyMap()): ReceiveChannel<MapEvent<K, V>> = transform { input, output ->
    val currentMap: MutableMap<K, V> = HashMap(initialMap)

    input.consumeEach { newMap ->
        handleNewMap(currentMap, newMap).consumeEach { output.send(it) }
    }
}

@ExperimentalCollectionEvent
private fun <K, V> CoroutineScope.handleNewMap(currentMap: MutableMap<K, V>, newMap: Map<out K, V>): ReceiveChannel<MapEvent<K, V>> = produce(Dispatchers.Default, Channel.UNLIMITED) {
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
