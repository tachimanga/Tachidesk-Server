package suwayomi.tachidesk.manga.model.table

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import app.tachimanga.cloud.enums.DataType
import app.tachimanga.cloud.model.dto.SyncCommitDTO
import app.tachimanga.cloud.model.dto.manga.HistoryDTO
import eu.kanade.tachiyomi.data.backup.models.BackupHistory
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow

object HistoryTable : IntIdTable() {
    val createAt = long("create_at").default(0)
    val updateAt = long("update_at").default(0)
    val isDelete = bool("is_delete").default(false)
    val mangaId = integer("manga_id")
    val lastChapterId = integer("last_chapter_id")

    // seconds
    val lastReadAt = long("last_read_at").default(0)

    // seconds
    val readDuration = integer("read_duration")

    val lastChapterName = varchar("last_chapter_name", 512).nullable()
    val dirty = bool("dirty").default(false)
    val commitId = long("commit_id").default(0)
}

fun HistoryTable.toSyncData(entry: ResultRow): SyncCommitDTO {
    val sync = SyncCommitDTO()
    sync.clientDataId = entry[id].value
    sync.dataType = DataType.History.name
    val history = HistoryDTO()

    history.clientCreatedAt = entry[HistoryTable.createAt]
    history.clientUpdatedAt = entry[HistoryTable.updateAt]
    history.clientDeleted = entry[HistoryTable.isDelete]

    history.lastReadAt = entry[HistoryTable.lastReadAt]
    history.mangaId = entry[HistoryTable.mangaId]
    history.chapterId = entry[HistoryTable.lastChapterId]
    history.readDuration = entry[HistoryTable.readDuration]

    sync.history = history
    return sync
}

fun HistoryTable.toBackupHistory(entry: ResultRow, chapterUrlMap: Map<Int, String>): BackupHistory? {
    val url = chapterUrlMap[entry[HistoryTable.lastChapterId]] ?: return null
    return BackupHistory(
        url = url,
        lastRead = entry[HistoryTable.lastReadAt] * 1000,
        readDuration = 0,
    )
}
