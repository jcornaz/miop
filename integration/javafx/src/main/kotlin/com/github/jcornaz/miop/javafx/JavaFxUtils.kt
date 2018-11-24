package com.github.jcornaz.miop.javafx

import javafx.application.Platform
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
internal fun javafxStart(): CoroutineStart =
    if (Platform.isFxApplicationThread()) CoroutineStart.UNDISPATCHED else CoroutineStart.ATOMIC
