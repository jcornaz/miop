package com.github.jcornaz.miop.experimental.collection

import com.github.jcornaz.miop.experimental.property.SubscribableValue
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.map

interface SubscribableCollection<out E> {
    val size: SubscribableValue<Int>
    val isEmpty: SubscribableValue<Boolean>

    fun openEventSubscription(): ReceiveChannel<CollectionEvent<E>>
}

interface SubscribableList<out E> : SubscribableCollection<E> {
    fun openListEventSubscription(): ReceiveChannel<ListEvent<E>>

    override fun openEventSubscription() = openListEventSubscription().map { event ->
        when (event) {
            is ListEvent.ElementInserted -> CollectionEvent.ElementAdded(event.element)
            is ListEvent.ElementsInserted -> CollectionEvent.ElementsAdded(event.elements)
            is ListEvent.ElementRemoved -> CollectionEvent.ElementRemoved(event.element)
            is ListEvent.ElementsRemoved -> CollectionEvent.ElementsRemoved(event.elements)
            ListEvent.Cleared -> CollectionEvent.Cleared
        }
    }
}

interface SubscribableSet<out E> : SubscribableCollection<E>

interface SubscribableMutableCollection<E> : SubscribableCollection<E> {
    suspend fun add(element: E): Boolean
    suspend fun addAll(elements: Collection<E>): Boolean

    suspend fun remove(element: E): Boolean
    suspend fun removeAll(elements: Collection<E>): Boolean

    suspend fun clear()
}

interface SubscribableMutableList<E> : SubscribableMutableCollection<E>, SubscribableList<E> {
    suspend fun add(index: Int, element: E)
    suspend fun addAll(index: Int, elements: Collection<E>)
    suspend fun removeAt(index: Int): E
}

interface SubscribableMutableSet<E> : SubscribableMutableCollection<E>, SubscribableSet<E>

