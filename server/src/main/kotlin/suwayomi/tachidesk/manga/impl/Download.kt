package suwayomi.tachidesk.manga.impl

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.transactions.transaction
import suwayomi.tachidesk.manga.model.dataclass.MangaDataClass
import suwayomi.tachidesk.manga.model.table.*

object Download {
    fun getDownloadedMangaList(): List<MangaDataClass> {
        val mangaList = transaction {
            MangaTable
                .select { (MangaTable.lastDownloadAt greater 0) }
                .orderBy(MangaTable.lastDownloadAt to SortOrder.DESC)
                .limit(300)
                .map {
                    MangaTable.toDataClass(it)
                }
        }
        return mangaList
    }

    fun deleteDownloadedManga(input: BatchInput) {
        if (input.mangaIds.isNullOrEmpty()) {
            return
        }
        transaction {
            MangaTable.update({ MangaTable.id inList input.mangaIds }) {
                it[lastDownloadAt] = 0
            }
        }
        val chapterIds = transaction {
            ChapterTable
                .slice(ChapterTable.id)
                .select { (ChapterTable.manga inList input.mangaIds) and (ChapterTable.isDownloaded eq true) }
                .map {
                    it[ChapterTable.id].value
                }
        }
        if (chapterIds.isEmpty()) {
            return
        }
        val param = Chapter.MangaChapterBatchEditInput(chapterIds = chapterIds, change = null)
        Chapter.deleteChapters(param)
    }

    @Serializable
    data class BatchInput(
        val mangaIds: List<Int>? = null
    )
}
