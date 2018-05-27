package com.github.jcornaz.miop.experimental.collection

import com.github.jcornaz.miop.experimental.property.SubscribableValue
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.channels.*
import kotlinx.coroutines.experimental.sync.Mutex
import kotlinx.coroutines.experimental.sync.withLock
import kotlinx.coroutines.experimental.withContext

public class SubscribableArrayList<E>(
        elements: Collection<E> = emptyList(),
        initialCapacity: Int = elements.size,
        broadcastCapacity: Int = 1
) : SubscribableMutableList<E> {

    private val broadcast = BroadcastChannel<ListEvent<E>>(broadcastCapacity)
    private val list = ArrayList<E>(initialCapacity).apply { addAll(elements) }
    private val mutex = Mutex()

    override val size = SubscribableValue {
        produce(Unconfined, Channel.CONFLATED) {
            var (size, sub) = mutex.withLock { list.size to broadcast.openSubscription() }

            sub.consume {
                if (isEmpty) send(size)

                for (event in this) {

                    @Suppress("UNUSED_VARIABLE")
                    val safeWhen: Any = when (event) { // `val safeWhen: Any = ` is there to make the compiler check if all event types are handled
                        is ListEvent.ElementInserted -> ++size
                        is ListEvent.ElementsInserted -> size += event.elements.size
                        is ListEvent.ElementRemoved -> --size
                        is ListEvent.ElementsRemoved -> size -= event.elements.size
                        ListEvent.Cleared -> size = 0
                    }

                    if (isEmpty) send(size)
                }
            }
        }
    }

    override val isEmpty = SubscribableValue {
        size.openSubscription().map { it == 0 }
    }

    override fun openListEventSubscription(): ReceiveChannel<ListEvent<E>> = broadcast.openSubscription()

    override suspend fun add(element: E): Boolean = mutex.withLock {
        val event = ListEvent.ElementInserted(list.size, element)
        list.add(element)
        broadcast.send(event)
        return@withLock true
    }

    override suspend fun add(index: Int, element: E) {
        list.add(index, element)
        broadcast.send(ListEvent.ElementInserted(index, element))
    }

    override suspend fun addAll(elements: Collection<E>): Boolean = mutex.withLock {
        val event = ListEvent.ElementsInserted(list.size, elements.toList())
        list.addAll(event.elements)
        broadcast.send(event)
        return@withLock true
    }

    override suspend fun addAll(index: Int, elements: Collection<E>) = mutex.withLock {
        val event = ListEvent.ElementsInserted(index, elements.toList())
        list.addAll(event.elements)
        broadcast.send(event)
    }

    override suspend fun remove(element: E): Boolean = mutex.withLock {
        val index = list.indexOf(element).takeIf { it >= 0 } ?: return@withLock false
        val event = ListEvent.ElementRemoved(index, element)
        list.removeAt(index)
        broadcast.send(event)
        return@withLock true
    }

    override suspend fun removeAt(index: Int) = mutex.withLock {
        list.removeAt(index).also {
            broadcast.send(ListEvent.ElementRemoved(index, it))
        }
    }

    override suspend fun removeAll(elements: Collection<E>): Boolean {
        if (elements.isEmpty()) return false
        if (elements.size == 1) return remove(elements.single())

        return mutex.withLock {
            val removedElements = mutableListOf<E>()

            // CPU intensive as each remove may cause an array copy
            withContext(CommonPool) {
                elements.forEach { element ->
                    if (list.remove(element)) removedElements.add(element)
                }
            }

            if (removedElements.isNotEmpty()) {
                broadcast.send(ListEvent.ElementsRemoved(removedElements))
                true
            } else false
        }
    }

    override suspend fun clear() = mutex.withLock {
        if (list.isEmpty()) return@withLock

        list.clear()
        broadcast.send(ListEvent.Cleared)
    }
}

