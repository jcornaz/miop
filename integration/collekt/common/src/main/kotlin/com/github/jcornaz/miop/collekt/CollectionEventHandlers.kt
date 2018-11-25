package com.github.jcornaz.miop.collekt

import com.github.jcornaz.collekt.api.PersistentMap
import com.github.jcornaz.collekt.api.PersistentSet
import com.github.jcornaz.miop.collection.*

/**
 * Returns a new [PersistentMap] having the [event] applied.
 *
 * This is essentially a *reducing* function ([PersistentMap], [MapEvent]) -> [PersistentMap]
 */
@ExperimentalCollectionEvent
public operator fun <K, V> PersistentMap<K, V>.plus(event: MapEvent<K, V>): PersistentMap<K, V> = when (event) {
    is MapEntryAdded -> plus(event.key, event.value)
    is MapEntryUpdated -> plus(event.key, event.newValue)
    is MapEntryRemoved -> minus(event.key)
    MapCleared -> empty()
}

/**
 * Returns a new [PersistentSet] having the [event] applied.
 *
 * This is essentially a *reducing* function ([PersistentSet], [MapEvent]) -> [PersistentSet]
 */
@ExperimentalCollectionEvent
public operator fun <E> PersistentSet<E>.plus(event: SetEvent<E>): PersistentSet<E> = when (event) {
    is SetElementAdded -> plus(event.element)
    is SetElementRemoved -> minus(event.element)
    SetCleared -> empty()
}
