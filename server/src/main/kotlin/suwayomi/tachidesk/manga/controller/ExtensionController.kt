package suwayomi.tachidesk.manga.controller

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import io.javalin.http.HttpCode
import mu.KotlinLogging
import org.tachiyomi.Profiler
import suwayomi.tachidesk.manga.impl.extension.Extension
import suwayomi.tachidesk.manga.impl.extension.ExtensionsList
import suwayomi.tachidesk.manga.model.dataclass.ExtensionDataClass
import suwayomi.tachidesk.server.JavalinSetup.future
import suwayomi.tachidesk.server.util.handler
import suwayomi.tachidesk.server.util.pathParam
import suwayomi.tachidesk.server.util.queryParam
import suwayomi.tachidesk.server.util.withOperation

object ExtensionController {
    private val logger = KotlinLogging.logger {}

    /** list all extensions */
    val list = handler(
        queryParam<String>("repoUrl", ""),
        documentWith = {
            withOperation {
                summary("Extension list")
                description("List all extensions")
            }
        },
        behaviorOf = { ctx, repoUrl ->
            ctx.future(
                future {
                    Profiler.start()
                    val r = ExtensionsList.getExtensionList(repoUrl)
                    Profiler.all()
                    r
                }
            )
        },
        withResults = {
            json<Array<ExtensionDataClass>>(HttpCode.OK)
        }
    )

    /** install extension identified with "pkgName" */
    val install = handler(
        pathParam<Int>("extensionId"),
        documentWith = {
            withOperation {
                summary("Extension install")
                description("install extension identified with \"pkgName\"")
            }
        },
        behaviorOf = { ctx, extensionId ->
            ctx.future(
                future {
                    Extension.installExtension(extensionId)
                }
            )
        },
        withResults = {
            httpCode(HttpCode.CREATED)
            httpCode(HttpCode.FOUND)
            httpCode(HttpCode.INTERNAL_SERVER_ERROR)
        }
    )

    /** install the uploaded apk file */
    val installFile = handler(
        documentWith = {
            withOperation {
                summary("Extension install apk")
                description("Install the uploaded apk file")
            }
//            uploadedFile("file") {
//                it.description("Extension apk")
//                it.required(true)
//            }
        },
        behaviorOf = { ctx ->
            val uploadedFile = ctx.uploadedFile("file")!!
            logger.debug { "Uploaded extension file name: " + uploadedFile.filename }

            ctx.future(
                future {
                    Extension.installExternalExtension(uploadedFile.content, uploadedFile.filename)
                }
            )
        },
        withResults = {
            httpCode(HttpCode.CREATED)
            httpCode(HttpCode.FOUND)
            httpCode(HttpCode.INTERNAL_SERVER_ERROR)
        }
    )

    /** update extension identified with "pkgName" */
    val update = handler(
        pathParam<Int>("extensionId"),
        documentWith = {
            withOperation {
                summary("Extension update")
                description("Update extension identified with \"pkgName\"")
            }
        },
        behaviorOf = { ctx, extensionId ->
            ctx.future(
                future {
                    Extension.updateExtension(extensionId)
                }
            )
        },
        withResults = {
            httpCode(HttpCode.CREATED)
            httpCode(HttpCode.FOUND)
            httpCode(HttpCode.NOT_FOUND)
            httpCode(HttpCode.INTERNAL_SERVER_ERROR)
        }
    )

    /** uninstall extension identified with "pkgName" */
    val uninstall = handler(
        pathParam<Int>("extensionId"),
        documentWith = {
            withOperation {
                summary("Extension uninstall")
                description("Uninstall extension identified with \"pkgName\"")
            }
        },
        behaviorOf = { ctx, extensionId ->
            Extension.uninstallExtensionById(extensionId, removePref = true)
            ctx.status(200)
        },
        withResults = {
            httpCode(HttpCode.CREATED)
            httpCode(HttpCode.FOUND)
            httpCode(HttpCode.NOT_FOUND)
            httpCode(HttpCode.INTERNAL_SERVER_ERROR)
        }
    )

    /** icon for extension named `apkName` */
    val icon = handler(
        pathParam<String>("apkName"),
        documentWith = {
            withOperation {
                summary("Extension icon")
                description("Icon for extension named `apkName`")
            }
        },
        behaviorOf = { ctx, apkName ->
            ctx.future(
                future { Extension.getExtensionIcon(apkName) }
                    .thenApply {
                        ctx.header("content-type", it.second)
                        it.first
                    }
            )
        },
        withResults = {
            image(HttpCode.OK)
            httpCode(HttpCode.NOT_FOUND)
        }
    )
}
