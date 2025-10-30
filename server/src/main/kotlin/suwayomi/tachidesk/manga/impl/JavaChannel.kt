package suwayomi.tachidesk.manga.impl

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.kodein.di.DI
import org.kodein.di.conf.global
import org.kodein.di.instance
import suwayomi.tachidesk.manga.impl.download.DownloadManager
import suwayomi.tachidesk.manga.impl.update.IUpdater
import suwayomi.tachidesk.manga.impl.update.JobErrorCode
import suwayomi.tachidesk.manga.impl.update.JobStatus
import suwayomi.tachidesk.manga.impl.update.UpdateManager
import suwayomi.tachidesk.manga.model.dataclass.task.BgContinuedUpdateResultDataClass

object JavaChannel {
    private val updater by DI.global.instance<IUpdater>()
    private val logger = KotlinLogging.logger {}
    private val json by DI.global.instance<Json>()

    @OptIn(DelicateCoroutinesApi::class)
    private val dispatcher = newFixedThreadPoolContext(1, "JavaChannel")
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    fun call(topic: String, content: String): String {
        logger.info { "JavaChannel topic=$topic content: $content" }
        when (topic) {
            "UPDATE:START" -> {
                UpdateManager.backgroundUpdate()
                return ""
            }
            "UPDATE:PULL" -> {
                return fetchUpdateResult()
            }
            "UPDATE:PULL2" -> {
                return fetchUpdateResult2()
            }
            "UPDATE:STOP" -> {
                updater.reset(null)
                return ""
            }
            "DOWNLOAD:STOP" -> {
                scope.launch {
                    DownloadManager.stop()
                }
                return ""
            }
            "DOWNLOAD:PULL" -> {
                return fetchDownloadResult()
            }
            "PIP:PULL" -> {
                return PipStatus.getPipStatusString()
            }
            else -> return "NOT FOUND"
        }
    }

    private fun fetchUpdateResult(): String {
        return json.encodeToString(updater.fetchUpdateResult())
    }

    private fun fetchUpdateResult2(): String {
        val jobs = updater.fetchAllJobs()

        val skipCount = jobs.count { i -> i.status == JobStatus.FAILED && i.failedInfo?.errorCode != JobErrorCode.UPDATE_FAILED }
        val failedCount = jobs.count { i -> i.status == JobStatus.FAILED } - skipCount
        val finishCount = jobs.count { i -> i.status == JobStatus.COMPLETE }
        val totalCount = jobs.size - skipCount

        val running = jobs.any { i -> i.status == JobStatus.RUNNING || i.status == JobStatus.PENDING }
        val firstTitle = jobs.firstOrNull { i -> i.status == JobStatus.RUNNING }?.manga?.title

        val newChapterCount = if (!running) {
            jobs.filter { i -> i.status == JobStatus.COMPLETE && i.latestChapterInfo != null }
                .sumOf { i -> i.latestChapterInfo?.newChapterCount ?: 0 }
        } else {
            null
        }

        val result = BgContinuedUpdateResultDataClass(
            running = running,
            totalCount = totalCount,
            finishCount = finishCount + failedCount,
            failedCount = failedCount,
            skipCount = skipCount,
            firstRunningTitle = firstTitle,
            newChapterCount = newChapterCount,
        )
        return json.encodeToString(result)
    }

    private fun fetchDownloadResult(): String {
        val result = DownloadManager.getBgContinuedResult()
        return json.encodeToString(result)
    }
}
