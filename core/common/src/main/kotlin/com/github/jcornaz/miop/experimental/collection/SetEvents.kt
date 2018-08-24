package com.github.jcornaz.miop.experimental.collection

import com.github.jcornaz.miop.experimental.CommonPool
import com.github.jcornaz.miop.experimental.transform
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach


public sealed class SetEvent<out E>

public class SetElementAdded<out E>(val element: E) : SetEvent<E>()
public class SetElementRemoved<out E>(val element: E) : SetEvent<E>()
public object SetCleared : SetEvent<Nothing>()

public operator fun <E> MutableSet<in E>.plusAssign(event: SetEvent<E>) {
    when (event) {
        is SetElementAdded -> add(event.element)
        is SetElementRemoved -> remove(event.element)
        SetCleared -> clear()
    }
}

public fun <E> ReceiveChannel<Set<E>>.toSetEvents(initialSet: Set<E>, capacity: Int = 100): ReceiveChannel<SetEvent<E>> = transform(CommonPool, capacity) { input, output ->

    val currentSet = initialSet.toHashSet()

    input.consumeEach { newSet ->
        if (newSet.isEmpty() && currentSet.isNotEmpty()) {
            output.send(SetCleared)
            currentSet.clear()
            return@consumeEach
        }

        val toAdd = newSet.toHashSet()
        val iterator = currentSet.iterator()
        while (iterator.hasNext()) {
            val element = iterator.next()
            if (element !in newSet) {
                output.send(SetElementRemoved(element))
                iterator.remove()
            } else {
                toAdd -= element
            }
        }

        toAdd.forEach { output.send(SetElementAdded(it)) }
    }
}
