package suwayomi.tachidesk.manga.controller

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import io.javalin.http.HttpCode
import suwayomi.tachidesk.manga.impl.PipStatus
import suwayomi.tachidesk.server.util.handler

object PipController {
    val ping = handler(
        behaviorOf = { ctx ->
            ctx.json(PipStatus.getPipStatus())
        },
        withResults = { httpCode(HttpCode.OK) }
    )
}
