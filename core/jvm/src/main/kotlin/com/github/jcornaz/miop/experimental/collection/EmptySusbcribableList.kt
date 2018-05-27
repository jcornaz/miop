package com.github.jcornaz.miop.experimental.collection

import com.github.jcornaz.miop.experimental.emptyReceiveChannel
import com.github.jcornaz.miop.experimental.property.SubscribableValue

object EmptySusbcribableList : SubscribableList<Nothing> {
    override val size = SubscribableValue(0)
    override val isEmpty = SubscribableValue(true)

    override fun openListEventSubscription() = emptyReceiveChannel<ListEvent<Nothing>>()
}
