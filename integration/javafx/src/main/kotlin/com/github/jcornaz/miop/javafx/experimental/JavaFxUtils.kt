package com.github.jcornaz.miop.javafx.experimental

import javafx.application.Platform
import kotlinx.coroutines.CoroutineStart

internal fun javafxStart(): CoroutineStart =
    if (Platform.isFxApplicationThread()) CoroutineStart.UNDISPATCHED else CoroutineStart.DEFAULT
