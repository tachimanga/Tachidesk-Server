package suwayomi.tachidesk.kotlin

import android.os.Looper
import kotlinx.coroutines.*
import java.lang.Runnable
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.CoroutineContext

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

@OptIn(InternalCoroutinesApi::class)
open class JvmMainDispatcher private constructor() : MainCoroutineDispatcher(), Delay {
    private val mainThreadExecutor = Looper.MAIN_EXECUTOR

    private val mainThread = AtomicReference<Thread>()

    init {
        initializeMainThread()
    }

    private fun initializeMainThread() {
        mainThreadExecutor.submit {
            mainThread.set(Thread.currentThread())
        }
    }

    override val immediate: MainCoroutineDispatcher
        get() = this

    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return mainThread.get() != Thread.currentThread()
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        mainThreadExecutor.execute(block)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>) {
        val disposable = mainThreadExecutor.schedule({
            with(continuation) {
                resumeUndispatched(Unit)
            }
        }, timeMillis, TimeUnit.MILLISECONDS)

        continuation.invokeOnCancellation {
            disposable.cancel(false)
        }
    }

    override fun invokeOnTimeout(timeMillis: Long, block: Runnable, context: CoroutineContext): DisposableHandle {
        val future = mainThreadExecutor.schedule(block, timeMillis, TimeUnit.MILLISECONDS)
        return DisposableHandle { future.cancel(false) }
    }

    override fun toString(): String {
        return "JvmMainDispatcher"
    }

    internal object Dispatcher : JvmMainDispatcher()
}
