package com.github.jcornaz.miop.javafx

import com.github.jcornaz.miop.collection.*
import javafx.beans.property.Property
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.javafx.JavaFx

/**
 * Start a new job in the JavaFx thread which update the [target] with each elements received
 *
 * The result or the scope shall be cancelled in order to stop the channel
 */
@ObsoleteCoroutinesApi
@UseExperimental(ExperimentalCoroutinesApi::class)
public fun <T> CoroutineScope.launchFxUpdater(target: Property<in T>, source: ReceiveChannel<T>): Job =
    launch(Dispatchers.JavaFx, javafxStart()) { source.consumeEach { target.value = it } }

/**
 * Start a new job in the JavaFx thread which update the [target] with each elements received
 *
 * The result or the [parent] shall be cancelled in order to cancel the channel
 */
@Deprecated(DEPRECATION_MESSAGE)
@UseExperimental(ObsoleteCoroutinesApi::class)
public fun <T> ReceiveChannel<T>.launchFxUpdater(target: Property<in T>, parent: Job?): Job {
    val scope = if (parent == null) GlobalScope else CoroutineScope(parent)

    return scope.launchFxUpdater(target, this)
}

/**
 * Start a new job in the JavaFx thread which update the [target] with each elements received
 *
 * The result or the shall be cancelled in order to cancel the channel
 */
@Deprecated(DEPRECATION_MESSAGE, ReplaceWith("GlobalScope.launchFxUpdater(target, this)", "kotlinx.coroutines.GlobalScope"))
@UseExperimental(ObsoleteCoroutinesApi::class)
public fun <T> ReceiveChannel<T>.launchFxUpdater(target: Property<in T>): Job =
    GlobalScope.launchFxUpdater(target, this)


/**
 * Start a new job in the JavaFx thread which update the [target] with each new list received
 *
 * The result or the scope shall be cancelled in order to cancel the channel
 */
@ObsoleteCoroutinesApi
@UseExperimental(ExperimentalCoroutinesApi::class)
public fun <E> CoroutineScope.launchFxListUpdater(target: MutableList<in E>, source: ReceiveChannel<List<E>>): Job = launch(Dispatchers.JavaFx, javafxStart()) {
    source.consumeEach { newList ->
        when {
            target.isEmpty() -> target.addAll(newList)
            newList.isEmpty() -> target.clear()
            else -> target.set(newList)
        }
    }
}

private fun <E> MutableList<in E>.set(newList: List<E>) {
    val sourceIterator = newList.iterator()
    val targetIterator = listIterator()
    var index = 0

    while (sourceIterator.hasNext() && targetIterator.hasNext()) {
        val sourceElement = sourceIterator.next()

        if (sourceElement !== targetIterator.next()) {
            targetIterator.set(sourceElement)
        }

        ++index
    }

    when {
        targetIterator.hasNext() -> subList(index, size).clear()
        sourceIterator.hasNext() -> sourceIterator.forEachRemaining { add(it) }
    }
}

/**
 * Start a new job in the JavaFx thread which update the [target] for each new key-set received
 *
 * The target list will receive items created with [createItem] for each keys of emitted by [source]
 *
 * [disposeItem] is called for each item which have to be removed from [target]
 *
 * The result or the scope shall be cancelled in order to cancel the channel
 */
@Deprecated("Use launchFxCollectionUpdater instead", ReplaceWith("launchFxCollectionUpdater(target, source, disposeItem, createItem)"))
@UseExperimental(ExperimentalCollectionEvent::class, ExperimentalCoroutinesApi::class, ObsoleteCoroutinesApi::class)
public fun <K, V> CoroutineScope.launchFxListUpdater(target: MutableList<in V>, source: ReceiveChannel<Set<K>>, disposeItem: (V) -> Unit, createItem: (K) -> V): Job =
    launchFxCollectionUpdater(target, source, disposeItem, createItem)

/**
 * Start a new job in the JavaFx thread which update the [target] with each new list received
 *
 * The result or the [parent] shall be cancelled in order to cancel the channel
 */
@Deprecated(DEPRECATION_MESSAGE)
@UseExperimental(ObsoleteCoroutinesApi::class)
public fun <E> ReceiveChannel<List<E>>.launchFxListUpdater(target: MutableList<in E>, parent: Job?): Job {
    val scope = if (parent == null) GlobalScope else CoroutineScope(parent)

    return scope.launchFxListUpdater(target, this)
}

/**
 * Start a new job in the JavaFx thread which update the [target] with each new list received
 *
 * The result shall be cancelled in order to cancel the channel
 */
@Deprecated(DEPRECATION_MESSAGE, ReplaceWith("GlobalScope.launchFxListUpdater(target, this)", "kotlinx.coroutines.GlobalScope"))
@UseExperimental(ObsoleteCoroutinesApi::class)
public fun <E> ReceiveChannel<List<E>>.launchFxListUpdater(target: MutableList<in E>): Job =
    GlobalScope.launchFxListUpdater(target, this)

/**
 * Start a new job in the JavaFx thread which update the [target] with each new map received
 *
 * The result or the scope shall be cancelled in order to cancel the channel
 */
@ObsoleteCoroutinesApi
@UseExperimental(ExperimentalCollectionEvent::class, ExperimentalCoroutinesApi::class)
public fun <K, V> CoroutineScope.launchFxMapUpdater(target: MutableMap<in K, in V>, source: ReceiveChannel<Map<out K, V>>): Job =
    launch(Dispatchers.JavaFx, javafxStart()) {
        val initialMap = source.receiveOrNull() ?: return@launch

        target.clear()
        target.putAll(initialMap)

        source.toMapEvents(initialMap).consumeEach { target += it }
    }

/**
 * Start a new job in the JavaFx thread which update the [target] with each new map received
 *
 * The result or the [parent] shall be cancelled in order to cancel the channel
 */
@Deprecated(DEPRECATION_MESSAGE)
@UseExperimental(ObsoleteCoroutinesApi::class)
public fun <K, V> ReceiveChannel<Map<out K, V>>.launchFxMapUpdater(target: MutableMap<in K, in V>, parent: Job?): Job {
    val scope = if (parent == null) GlobalScope else CoroutineScope(parent)

    return scope.launchFxMapUpdater(target, this)
}

/**
 * Start a new job in the JavaFx thread which update the [target] with each new map received
 *
 * The result shall be cancelled in order to cancel the channel
 */
@Deprecated(DEPRECATION_MESSAGE, ReplaceWith("GlobalScope.launchFxMapUpdater(target, this)", "kotlinx.coroutines.GlobalScope"))
@UseExperimental(ObsoleteCoroutinesApi::class)
public fun <K, V> ReceiveChannel<Map<out K, V>>.launchFxMapUpdater(target: MutableMap<in K, in V>): Job =
    GlobalScope.launchFxMapUpdater(target, this)


/**
 * Start a new job in the JavaFx thread which update the [target] for each new key-set received
 *
 * The target list will receive items created with [createItem] for each keys of emitted by [source]
 *
 * [disposeItem] is called for each item which have to be removed from [target]
 *
 * The result or the scope shall be cancelled in order to cancel the channel
 */
@ObsoleteCoroutinesApi
@UseExperimental(ExperimentalCollectionEvent::class, ExperimentalCoroutinesApi::class)
public fun <K, V> CoroutineScope.launchFxCollectionUpdater(
    target: MutableCollection<in V>,
    source: ReceiveChannel<Set<K>>,
    disposeItem: (V) -> Unit,
    createItem: (K) -> V
): Job = launch(Dispatchers.JavaFx, javafxStart()) {

    val initialKeySet = source.receiveOrNull() ?: return@launch
    val entryMap: MutableMap<K, V> = HashMap(initialKeySet.size)

    try {
        initialKeySet.forEach { entryMap[it] = createItem(it) }

        target.clear()
        target.addAll(entryMap.values)

        source.toSetEvents(entryMap.keys).consumeEach { event ->
            when (event) {
                is SetElementAdded -> {
                    val item = createItem(event.element)
                    entryMap[event.element] = item
                    target.add(item)
                }
                is SetElementRemoved -> {
                    val item = entryMap.remove(event.element) ?: return@consumeEach
                    disposeItem(item)
                    target.remove(item)
                }
                SetCleared -> {
                    entryMap.values.forEach(disposeItem)
                    entryMap.clear()
                    target.clear()
                }
            }
        }
    } finally {
        entryMap.values.forEach(disposeItem)
    }
}

/**
 * Start a job in the JavaFx thread which keeps up-to-date the [target] collection.
 * Order of elements is ignored. Only consider the elements and their occurrence count.
 *
 * The result or the scope shall be cancelled in order to cancel the channel
 */
@ObsoleteCoroutinesApi
@UseExperimental(ExperimentalCoroutinesApi::class)
public fun <E> CoroutineScope.launchFxCollectionUpdater(target: MutableCollection<in E>, source: ReceiveChannel<Collection<E>>): Job = launch(Dispatchers.JavaFx, javafxStart()) {
    var currentElementCounts: Map<in E, Int> = target.countElements()

    source.consumeEach { newCollection ->
        val newElementCounts = target.addNewElements(newCollection, currentElementCounts)

        target.removeExcess(currentElementCounts, newElementCounts)

        currentElementCounts = newElementCounts
    }
}

private fun <E> Collection<E>.countElements(): Map<E, Int> {
    val counts = HashMap<E, Int>()

    forEach { element -> counts[element] = counts[element]?.let { it + 1 } ?: 1 }

    return counts
}

private fun <E> MutableCollection<in E>.addNewElements(newCollection: Collection<E>, currentElementCounts: Map<in E, Int>): Map<in E, Int> {
    val newElementCounts = HashMap<E, Int>()

    newCollection.forEach { element ->
        val newCount = newElementCounts[element]?.let { it + 1 } ?: 1
        newElementCounts[element] = newCount

        if (newCount > currentElementCounts[element] ?: 0) {
            add(element)
        }
    }

    return newElementCounts
}

private fun <E> MutableCollection<in E>.removeExcess(currentElementCounts: Map<in E, Int>, newElementCounts: Map<in E, Int>) {
    currentElementCounts.forEach { element, oldCount ->
        val newCount = (newElementCounts[element] ?: 0)
        when {
            newCount == 0 && this is Set<*> -> remove(element)
            newCount == 0 -> repeat(oldCount) { remove(element) }
            newCount < oldCount && this !is Set<*> -> repeat(oldCount - newCount) { remove(element) }
        }
    }
}

/**
 * Start a job in the JavaFx thread which keeps up-to-date the [target] collection.
 * Order of elements is ignored. Only consider the elements and their occurrence count.
 *
 * The result or the [parent] shall be cancelled in order to cancel the channel
 */
@Deprecated(DEPRECATION_MESSAGE)
@UseExperimental(ObsoleteCoroutinesApi::class)
public fun <E> ReceiveChannel<Collection<E>>.launchFxCollectionUpdater(target: MutableCollection<in E>, parent: Job?): Job {
    val scope = if (parent == null) GlobalScope else CoroutineScope(parent)

    return scope.launchFxCollectionUpdater(target, this)
}

/**
 * Start a job in the JavaFx thread which keeps up-to-date the [target] collection.
 * Order of elements is ignored. Only consider the elements and their occurrence count.
 *
 * The result shall be cancelled in order to cancel the channel
 */
@Deprecated(DEPRECATION_MESSAGE, ReplaceWith("GlobalScope.launchFxCollectionUpdater(target, this)", "kotlinx.coroutines.GlobalScope"))
@UseExperimental(ObsoleteCoroutinesApi::class)
public fun <E> ReceiveChannel<Collection<E>>.launchFxCollectionUpdater(target: MutableCollection<in E>): Job =
    GlobalScope.launchFxCollectionUpdater(target, this)

/**
 * Start a new job in the JavaFx thread which update the [target] with each new set received
 *
 * The result or the scope shall be cancelled in order to cancel the channel
 */
@ObsoleteCoroutinesApi
@UseExperimental(ExperimentalCollectionEvent::class, ExperimentalCoroutinesApi::class)
public fun <E> CoroutineScope.launchFxSetUpdater(target: MutableSet<in E>, source: ReceiveChannel<Set<E>>): Job =
    launch(Dispatchers.JavaFx, javafxStart()) {
        val initialSet = source.receive()

        target.clear()
        target.addAll(initialSet)

        source.toSetEvents(initialSet).consumeEach { target += it }
    }

/**
 * Start a new job in the JavaFx thread which update the [target] with each new set received
 *
 * The result or the [parent] shall be cancelled in order to cancel the channel
 */
@Deprecated(DEPRECATION_MESSAGE)
@UseExperimental(ObsoleteCoroutinesApi::class)
public fun <E> ReceiveChannel<Set<E>>.launchFxSetUpdater(target: MutableSet<in E>, parent: Job?): Job {
    val scope = if (parent == null) GlobalScope else CoroutineScope(parent)

    return scope.launchFxSetUpdater(target, this)
}

/**
 * Start a new job in the JavaFx thread which update the [target] with each new set received
 *
 * The result shall be cancelled in order to cancel the channel
 */
@UseExperimental(ObsoleteCoroutinesApi::class)
@Deprecated(DEPRECATION_MESSAGE, ReplaceWith("GlobalScope.launchFxSetUpdater(target, this)", "kotlinx.coroutines.GlobalScope"))
public fun <E> ReceiveChannel<Set<E>>.launchFxSetUpdater(target: MutableSet<in E>): Job =
    GlobalScope.launchFxSetUpdater(target, this)

private const val DEPRECATION_MESSAGE = "launch the updater from a CoroutineScope"
