package com.github.jcornaz.miop

import kotlinx.coroutines.CoroutineDispatcher

/**
 * [CoroutineDispatcher] dispatching executions on an IO thread pool.
 *
 * The backed thread pool will provide as much thread as necessary, and reuse them as much as possible.
 *
 * Not used thread will be destroyed after an arbitrary period of time.
 *
 * This dispatcher is better for IO operations as well as blocking operation.
 */
@Deprecated("Use Dispatchers.IO from JVM")
public expect val IoPool: CoroutineDispatcher
