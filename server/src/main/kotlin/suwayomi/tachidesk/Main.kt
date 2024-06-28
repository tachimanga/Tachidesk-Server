package suwayomi.tachidesk

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import suwayomi.tachidesk.manga.impl.PipStatus
import suwayomi.tachidesk.server.JavalinSetup.javalinSetup
import suwayomi.tachidesk.server.JavalinSetup.javalinStartSocket
import suwayomi.tachidesk.server.JavalinSetup.javalinStop
import suwayomi.tachidesk.server.JavalinSetup.javalinStopSocket
import suwayomi.tachidesk.server.JavalinSetup.javalinWaitRequestDone
import suwayomi.tachidesk.server.applicationSetup
import suwayomi.tachidesk.server.envSetup

fun main() {
    envSetup()
    applicationSetup()
    javalinSetup()
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
