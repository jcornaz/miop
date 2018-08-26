package com.github.jcornaz.miop.experimental.collection

import com.github.jcornaz.miop.experimental.transform
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach

@Suppress("unused")
public sealed class MapEvent<out K, out V>

public data class MapEntryAdded<out K, out V>(val key: K, val value: V) : MapEvent<K, V>()
public data class MapEntryUpdated<out K, out V>(val key: K, val newValue: V) : MapEvent<K, V>()
public data class MapEntryRemoved<out K, out V>(val key: K) : MapEvent<K, V>()
public object MapCleared : MapEvent<Nothing, Nothing>()

public operator fun <K, V> MutableMap<in K, in V>.plusAssign(event: MapEvent<K, V>) {
    when (event) {
        is MapEntryAdded -> put(event.key, event.value)
        is MapEntryUpdated -> put(event.key, event.newValue)
        is MapEntryRemoved -> remove(event.key)
        MapCleared -> clear()
    }
}

public fun <K, V> ReceiveChannel<Map<K, V>>.toMapEvents(initialMap: Map<K, V> = emptyMap()): ReceiveChannel<MapEvent<K, V>> = transform(DefaultDispatcher) { input, output ->
    val currentMap = initialMap.toMutableMap()

    input.consumeEach { newMap ->

        if (newMap.isEmpty() && currentMap.isNotEmpty()) {
            output.send(MapCleared)
            currentMap.clear()
            return@consumeEach
        }

        val toAdd = newMap.toMutableMap()

        val iterator = currentMap.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.key !in newMap) {
                output.send(MapEntryRemoved(entry.key))
                iterator.remove()
            } else {
                @Suppress("UNCHECKED_CAST")
                val newValue = newMap[entry.key] as V

                if (entry.value != newValue) {
                    output.send(MapEntryUpdated(entry.key, newValue))
                    entry.setValue(newValue)
                }
                toAdd -= entry.key
            }
        }

        toAdd.forEach { (key, value) ->
            output.send(MapEntryAdded(key, value))
            currentMap[key] = value
        }
    }
}
