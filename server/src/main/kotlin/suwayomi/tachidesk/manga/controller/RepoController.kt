package suwayomi.tachidesk.manga.controller

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import io.javalin.http.HttpCode
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.kodein.di.DI
import org.kodein.di.conf.global
import org.kodein.di.instance
import suwayomi.tachidesk.manga.impl.Repo
import suwayomi.tachidesk.manga.model.dataclass.CategoryDataClass
import suwayomi.tachidesk.server.JavalinSetup.future
import suwayomi.tachidesk.server.util.handler
import suwayomi.tachidesk.server.util.pathParam

object RepoController {
    private val json by DI.global.instance<Json>()
    private val logger = KotlinLogging.logger {}

    val repoList = handler(
        behaviorOf = { ctx ->
            ctx.json(Repo.repoList())
        },
        withResults = {
            json<Array<CategoryDataClass>>(HttpCode.OK)
        },
    )

    val checkRepo = handler(
        behaviorOf = { ctx ->
            val input = json.decodeFromString<Repo.RepoCreate>(ctx.body())
            logger.info("checkRepo $input")
            ctx.future(future { Repo.checkRepo(input.repoName, input.metaUrl) })
        },
        withResults = {
            httpCode(HttpCode.OK)
        },
    )

    val createRepo = handler(
        behaviorOf = { ctx ->
            val input = json.decodeFromString<Repo.RepoCreate>(ctx.body())
            logger.info("createRepo $input")
            ctx.json(Repo.createRepo(input.repoName, input.metaUrl))
        },
        withResults = {
            httpCode(HttpCode.OK)
        },
    )

    val updateByMetaUrl = handler(
        behaviorOf = { ctx ->
            val input = json.decodeFromString<Repo.RepoUpdateByMetaUrl>(ctx.body())
            logger.info("updateByMetaUrl $input")
            ctx.json(Repo.updateRepoByMetaUrl(input.metaUrl, input.targetMetaUrl))
        },
        withResults = {
            httpCode(HttpCode.OK)
        },
    )

    val removeRepo = handler(
        pathParam<Int>("repoId"),
        documentWith = {
        },
        behaviorOf = { ctx, repoId ->
            logger.info("removeRepo $repoId")
            Repo.removeRepo(repoId)
            ctx.status(200)
        },
        withResults = {
            httpCode(HttpCode.OK)
        },
    )
}
