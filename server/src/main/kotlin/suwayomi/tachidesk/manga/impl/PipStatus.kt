package suwayomi.tachidesk.manga.impl

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.conf.global
import org.kodein.di.instance
import suwayomi.tachidesk.manga.impl.download.DownloadManager
import suwayomi.tachidesk.manga.impl.update.IUpdater
import suwayomi.tachidesk.manga.model.dataclass.PipStatusDataClass
import uy.kohesive.injekt.api.get

object PipStatus {
    private val updater by DI.global.instance<IUpdater>()
    private val json by DI.global.instance<Json>()

    fun getPipStatus(): PipStatusDataClass {
        val downloadStatus = DownloadManager.getQueueStatus()
        val updateStatus = updater.getQueueStatus()

        return PipStatusDataClass(
            downloadFinishCount = downloadStatus.first,
            downloadTotalCount = downloadStatus.second,
            updateFinishCount = updateStatus.first,
            updateTotalCount = updateStatus.second,
            working = !(downloadStatus.first == downloadStatus.second && updateStatus.first == updateStatus.second)
        )
    }

    fun getPipStatusString(): String {
        return json.encodeToString(getPipStatus())
    }
}
