package com.github.jcornaz.miop.experimental.collection

sealed class CollectionEvent<out E> {
    class ElementAdded<out E>(val element: E) : CollectionEvent<E>()
    class ElementsAdded<out E>(val elements: Collection<E>) : CollectionEvent<E>()
    class ElementRemoved<out E>(val elements: E) : CollectionEvent<E>()
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
