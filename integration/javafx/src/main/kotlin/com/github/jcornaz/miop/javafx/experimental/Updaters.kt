package com.github.jcornaz.miop.javafx.experimental

import com.github.jcornaz.collekt.toPersistentMap
import com.github.jcornaz.miop.experimental.distinctUntilChanged
import com.github.jcornaz.miop.experimental.launchConsumeEach
import javafx.beans.property.Property
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.channels.consumes
import kotlinx.coroutines.experimental.javafx.JavaFx
import kotlinx.coroutines.experimental.launch
import java.util.*

/**
 * Start a new job in the JavaFx thread which update the [target] with each elements received
 *
 * The result or the [parent] shall be cancelled in order to cancel the channel
 */
public fun <T> ReceiveChannel<T>.launchFxUpdater(target: Property<in T>, parent: Job? = null): Job =
    distinctUntilChanged().launchConsumeEach(JavaFx, javafxStart(), parent) { target.value = it }

/**
 * Start a new job in the JavaFx thread which update the [target] with each new list received
 *
 * The result or the [parent] shall be cancelled in order to cancel the channel
 */
public fun <E> ReceiveChannel<List<E>>.launchFxListUpdater(target: MutableList<in E>, parent: Job? = null): Job =
    launchConsumeEach(JavaFx, javafxStart(), parent) { newList ->
        when {
            target.isEmpty() -> target.addAll(newList)
            newList.isEmpty() -> target.clear()
            else -> {
                val sourceIterator = newList.iterator()
                val targetIterator = target.listIterator()
                var index = 0

                while (sourceIterator.hasNext() && targetIterator.hasNext()) {
                    val sourceElement = sourceIterator.next()

                    if (sourceElement !== targetIterator.next()) {
                        targetIterator.set(sourceElement)
                    }

                    ++index
                }

                if (targetIterator.hasNext()) {
                    target.subList(index, target.size).clear()
                } else if (sourceIterator.hasNext()) {
                    sourceIterator.forEachRemaining { target.add(it) }
                }
            }
        }
    }

/**
 * Start a new job in the JavaFx thread which update the [target] with each new set received
 *
 * The result or the [parent] shall be cancelled in order to cancel the channel
 */
public fun <E> ReceiveChannel<Set<E>>.launchFxSetUpdater(target: MutableSet<in E>, parent: Job? = null): Job =
    launchFxCollectionUpdater(target, parent)

/**
 * Start a new job in the JavaFx thread which update the [target] with each new map received
 *
 * The result or the [parent] shall be cancelled in order to cancel the channel
 */
public fun <K, V> ReceiveChannel<Map<K, V>>.launchFxMapUpdater(target: MutableMap<K, in V>, parent: Job? = null): Job =
    launchConsumeEach(JavaFx, javafxStart(), parent) { newMap ->
        val iterator = target.iterator()
        var toAdd = newMap.toPersistentMap()

        while(iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.key !in newMap) {
                iterator.remove()
            } else {
                if (entry.value != newMap[entry.key]) {
                    entry.setValue(newMap[entry.key] as V)
                }
                toAdd -= entry.key
            }
        }

        target.putAll(toAdd)
    }

/**
 * Start a job in the JavaFx thread which keeps up-to-date the [target] collection.
 * Order of elements is ignored. Only consider the elements and their occurrence count.
 *
 * The result or the [parent] shall be cancelled in order to cancel the channel
 */
public fun <E> ReceiveChannel<Collection<E>>.launchFxCollectionUpdater(target: MutableCollection<E>, parent: Job? = null): Job =
    launch(JavaFx, javafxStart(), parent, consumes()) {

        val currentElementCounts = HashMap<E, Int>()

        target.forEach { element ->
            currentElementCounts[element] = currentElementCounts[element]?.let { it + 1 } ?: 1
        }

        consumeEach { newCollection ->
            val newElementCounts = HashMap<E, Int>()

            newCollection.forEach { element ->
                val newCount = newElementCounts[element]?.let { it + 1 } ?: 1
                newElementCounts[element] = newCount

                if (newCount > currentElementCounts[element] ?: 0) {
                    target.add(element)
                    currentElementCounts[element] = currentElementCounts[element]?.let { it + 1 } ?: 1
                }
            }

            val iterator = currentElementCounts.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()

                val newCount = newElementCounts[entry.key]?.takeUnless { it <= 0 }

                if (entry.value > (newCount ?: 0)) {

                    if (target is Set<*>) {
                        if (newCount == null) target.remove(entry.key)
                    } else {
                        repeat(entry.value - (newCount ?: 0)) {
                            target.remove(entry.key)
                        }
                    }

                    if (newCount == null) {
                        iterator.remove()
                    } else {
                        entry.setValue(newCount)
                    }
                }
            }
        }
    }
