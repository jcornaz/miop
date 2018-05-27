package com.github.jcornaz.miop.experimental.collection

import com.github.jcornaz.miop.experimental.emptyReceiveChannel
import com.github.jcornaz.miop.experimental.property.SubscribableValue
import com.github.jcornaz.miop.experimental.receiveChannelOf
import kotlinx.coroutines.experimental.channels.ReceiveChannel

class ImmutableSubscribableList<E>(private val elements: List<E>) : SubscribableList<E> {
    override val size = SubscribableValue(elements.size)
    override val isEmpty = SubscribableValue(elements.isEmpty())

    override fun openListEventSubscription(): ReceiveChannel<ListEvent<E>> {
        if (elements.isEmpty()) return emptyReceiveChannel()

        val event = if (elements.size == 1)
            ListEvent.ElementInserted(0, elements.single())
        else
            ListEvent.ElementsInserted(0, elements)

        return receiveChannelOf(event)
    }
}
