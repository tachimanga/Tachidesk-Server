package suwayomi.tachidesk.manga.impl.update

/*
 * Copyright (C) Contributors to the Suwayomi project
 * Copyright (C) 2023 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import kotlinx.coroutines.flow.StateFlow
import suwayomi.tachidesk.manga.model.dataclass.MangaDataClass
import suwayomi.tachidesk.manga.model.dataclass.update.BgUpdateResultDataClass

interface IUpdater {
    fun addMangaToQueue(manga: MangaDataClass)
    fun addMangaListToQueue(mangaList: List<MangaDataClass>)
    fun addMangaToTracker(job: UpdateJob)

    val status: StateFlow<UpdateStatus>
    fun reset(task: UpdateTask?)
    fun updateStatus(running: Boolean?)
    fun getQueueStatus(): Pair<Int, Int>
    fun fetchAllJobs(): List<UpdateJob>
    fun fetchUpdateResult(): BgUpdateResultDataClass
}
