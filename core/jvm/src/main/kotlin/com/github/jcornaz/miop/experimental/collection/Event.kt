package com.github.jcornaz.miop.experimental.collection

sealed class CollectionEvent<out E> {
    class ElementAdded<out E>(val element: E) : CollectionEvent<E>()
    class ElementsAdded<out E>(val elements: Collection<E>) : CollectionEvent<E>()
    class ElementRemoved<out E>(val element: E) : CollectionEvent<E>()
    class ElementsRemoved<out E>(val elements: Collection<E>) : CollectionEvent<E>()
    object Cleared : CollectionEvent<Nothing>()
}

sealed class ListEvent<out E> {
    class ElementInserted<out E>(val index: Int, val element: E) : ListEvent<E>()
    class ElementsInserted<out E>(val index: Int, val elements: List<E>) : ListEvent<E>()
    class ElementRemoved<out E>(val index: Int, val element: E) : ListEvent<E>()
    class ElementsRemoved<out E>(val elements: List<E>) : ListEvent<E>()
    object Cleared : ListEvent<Nothing>()
}

fun <E> CollectionEvent<E>.applyTo(collection: MutableCollection<in E>): Unit = when (this) {
    is CollectionEvent.ElementAdded -> collection.add(element).unit
    is CollectionEvent.ElementsAdded -> collection.addAll(elements).unit
    is CollectionEvent.ElementRemoved -> collection.remove(element).unit
    is CollectionEvent.ElementsRemoved -> collection.removeAll(elements).unit
    CollectionEvent.Cleared -> collection.clear()
}

fun <E> ListEvent<E>.applyTo(list: MutableList<in E>): Unit = when (this) {
    is ListEvent.ElementInserted -> list.add(index, element)
    is ListEvent.ElementsInserted -> list.addAll(index, elements).unit
    is ListEvent.ElementRemoved -> list.removeAt(index).unit
    is ListEvent.ElementsRemoved -> list.removeAll(elements).unit
    ListEvent.Cleared -> list.clear()
}

private val Any?.unit: Unit get() = Unit
