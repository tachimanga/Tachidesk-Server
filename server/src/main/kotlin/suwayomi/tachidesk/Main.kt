package suwayomi.tachidesk

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import suwayomi.tachidesk.manga.impl.JavaChannel
import suwayomi.tachidesk.manga.impl.PipStatus
import suwayomi.tachidesk.manga.impl.util.LogEvent
import suwayomi.tachidesk.server.JavalinSetup.javalinSetup
import suwayomi.tachidesk.server.JavalinSetup.javalinStartSocket
import suwayomi.tachidesk.server.JavalinSetup.javalinStop
import suwayomi.tachidesk.server.JavalinSetup.javalinStopSocket
import suwayomi.tachidesk.server.JavalinSetup.javalinWaitRequestDone
import suwayomi.tachidesk.server.applicationSetup
import suwayomi.tachidesk.server.applicationSetupExtra
import suwayomi.tachidesk.server.envSetup

fun main() {
    try {
        main0()
    } catch (e: Throwable) {
        LogEvent.log("SERVER:BOOT:FAILED", mapOf("error" to (e.message ?: "")))
        throw e
    }
}

fun main0() {
    val t = System.currentTimeMillis()
    envSetup()
    applicationSetup()
    javalinSetup()
    applicationSetupExtra()
    println("[BOOT]Main.kt cost:${System.currentTimeMillis() - t}ms")
}

fun startServer() {
    javalinSetup()
}

fun stopServer() {
    javalinStop()
}

fun startSocket() {
    javalinStartSocket()
}

fun waitRequestDone() {
    javalinWaitRequestDone()
}

fun stopSocket() {
    javalinStopSocket()
}

fun getStatus(): String {
    return PipStatus.getPipStatusString()
}

fun javaChannel(tag: String, content: String): String {
    return JavaChannel.call(tag, content)
}
