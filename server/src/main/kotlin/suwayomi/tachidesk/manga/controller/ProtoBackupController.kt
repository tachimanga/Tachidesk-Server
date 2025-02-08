package suwayomi.tachidesk.manga.controller

import eu.kanade.tachiyomi.data.backup.BackupFlags
import eu.kanade.tachiyomi.data.backup.ImporterSocket
import eu.kanade.tachiyomi.data.backup.ProtoBackupExport
import eu.kanade.tachiyomi.data.backup.ProtoBackupImport
import io.javalin.http.HttpCode
import io.javalin.websocket.WsConfig
import suwayomi.tachidesk.server.JavalinSetup.future
import suwayomi.tachidesk.server.util.formParam
import suwayomi.tachidesk.server.util.handler

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

object ProtoBackupController {

    /** expects a Tachiyomi protobuf backup as a file upload, the file must be named "backup.proto.gz" */
    val protobufImportFile = handler(
        formParam<String>("defaultRepoUrl"),
        documentWith = {
        },
        behaviorOf = { ctx, defaultRepoUrl ->
            ctx.future(
                future {
                    ProtoBackupImport.performRestore(ctx.uploadedFile("backup.proto.gz")!!.content, defaultRepoUrl)
                },
            )
        },
        withResults = {
            httpCode(HttpCode.OK)
        },
    )

    fun importerWS(ws: WsConfig) {
        ws.onConnect { ctx ->
            ImporterSocket.addClient(ctx)
        }
        ws.onMessage { ctx ->
            ImporterSocket.handleRequest(ctx)
        }
        ws.onClose { ctx ->
            ImporterSocket.removeClient(ctx)
        }
    }

    val protobufExport = handler(
        behaviorOf = { ctx ->
            ctx.future(
                future {
                    val flags = BackupFlags(
                        includeManga = true,
                        includeCategories = true,
                        includeChapters = true,
                        includeTracking = true,
                        includeHistory = true,
                    )
                    val path = ProtoBackupExport.createBackup(flags)
                    mapOf("path" to path)
                },
            )
        },
        withResults = {
            httpCode(HttpCode.OK)
        },
    )
}
