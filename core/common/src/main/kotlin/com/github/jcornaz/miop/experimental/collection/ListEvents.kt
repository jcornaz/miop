package com.github.jcornaz.miop.experimental.collection

import com.github.jcornaz.miop.experimental.CommonPool
import com.github.jcornaz.miop.experimental.transform
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach

sealed class ListEvent<out E>

public data class ListElementAdded<out E>(val index: Int, val element: E) : ListEvent<E>()
public data class ListElementReplaced<out E>(val index: Int, val newElement: E) : ListEvent<E>()
public data class ListElementRemoved<out E>(val index: Int) : ListEvent<E>()
public data class ListTruncated<out E>(val newSize: Int) : ListEvent<E>()
public object ListCleared : ListEvent<Nothing>()

public operator fun <E> MutableList<in E>.plusAssign(event: ListEvent<E>) {
    when (event) {
        is ListElementAdded -> add(event.index, event.element)
        is ListElementReplaced -> set(event.index, event.newElement)
        is ListElementRemoved -> removeAt(event.index)
        is ListTruncated -> subList(event.newSize, size).clear()
        ListCleared -> clear()
    }
}

public fun <E> ReceiveChannel<List<E>>.toListEvents(initialList: List<E> = emptyList(), capacity: Int = 100): ReceiveChannel<ListEvent<E>> = transform(CommonPool, capacity) { input, output ->
    val currentList = initialList.toMutableList()

    input.consumeEach { newList ->

        if (newList.isEmpty() && currentList.isNotEmpty()) {
            output.send(ListCleared)
            currentList.clear()
            return@consumeEach
        }

        var index = 0

        while (index < newList.size && index < currentList.size) {
            val currentElement = currentList[index]
            val newElement = newList[index]
            if (currentElement != newElement) {
                when {
                    newList.size > index + 1 && currentElement == newList[index + 1] -> {
                        output.send(ListElementAdded(index, newElement))
                        currentList.add(index, newElement)
                    }
                    currentList.size > index + 1 && currentList[index + 1] == newElement -> {
                        output.send(ListElementRemoved(index))
                        currentList.removeAt(index)
                    }
                    else -> {
                        output.send(ListElementReplaced(index, newElement))
                        currentList[index] = newElement
                    }
                }
            }
            ++index
        }

        if (index < currentList.size) {
            output.send(ListTruncated(newList.size))
            currentList.subList(index, currentList.size).clear()
        } else if (index < newList.size) {
            val toAdd = newList.subList(index, newList.size)

            toAdd.forEachIndexed { i, elt ->
                output.send(ListElementAdded(i + index, elt))
            }

            currentList += toAdd
        }
    }
}
