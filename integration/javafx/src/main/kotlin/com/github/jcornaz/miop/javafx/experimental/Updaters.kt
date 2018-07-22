package com.github.jcornaz.miop.javafx.experimental

import com.github.jcornaz.miop.experimental.distinctUntilChanged
import com.github.jcornaz.miop.experimental.launchConsumeEach
import javafx.beans.property.Property
import javafx.collections.ObservableList
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.javafx.JavaFx

fun <T> ReceiveChannel<T>.launchUpdater(property: Property<in T>, parent: Job? = null): Job =
    distinctUntilChanged().launchConsumeEach(JavaFx, parent = parent) { property.value = it }

fun <E> ReceiveChannel<List<E>>.launchUpdater(observableList: ObservableList<in E>, parent: Job? = null): Job =
    launchConsumeEach(JavaFx, parent = parent) { newList ->
        when {
            observableList.isEmpty() -> observableList.addAll(newList)
            newList.isEmpty() -> observableList.clear()
            else -> {
                val sourceIterator = newList.iterator()
                val targetIterator = observableList.listIterator()
                var index = 0

                while (sourceIterator.hasNext() && targetIterator.hasNext()) {
                    val sourceElement = sourceIterator.next()

                    if (sourceElement != targetIterator.next()) {
                        targetIterator.set(sourceElement)
                    }

                    ++index
                }

                if (targetIterator.hasNext()) {
                    observableList.remove(index, observableList.size)
                } else if (sourceIterator.hasNext()) {
                    sourceIterator.forEachRemaining { observableList.add(it) }
                }
            }
        }
    }