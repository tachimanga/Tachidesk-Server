package suwayomi.tachidesk.manga.model.table

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import app.tachimanga.cloud.enums.DataType
import app.tachimanga.cloud.model.dto.SyncCommitDTO
import app.tachimanga.cloud.model.dto.manga.TrackRecordDTO
import eu.kanade.tachiyomi.data.backup.models.BackupTracking
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow

object TrackRecordTable : IntIdTable() {
    val mangaId = integer("manga_id")
    val syncId = integer("sync_id")
    val remoteId = integer("remote_id")
    val libraryId = integer("library_id").nullable()
    val title = varchar("title", 512)
    val lastChapterRead = double("last_chapter_read")
    val totalChapters = integer("total_chapters")
    val status = integer("status")
    val score = double("score")
    val remoteUrl = varchar("remote_url", 512)
    val startDate = long("start_date")
    val finishDate = long("finish_date")
    val createAt = long("create_at").default(0)
    val updateAt = long("update_at").default(0)
    val isDelete = bool("is_delete").default(false)
    val dirty = bool("dirty").default(false)
    val commitId = long("commit_id").default(0)
}

fun TrackRecordTable.toSyncData(entry: ResultRow): SyncCommitDTO {
    val sync = SyncCommitDTO()
    sync.clientDataId = entry[id].value
    sync.dataType = DataType.TrackRecord.name
    val record = TrackRecordDTO()

    record.clientCreatedAt = entry[TrackRecordTable.createAt]
    record.clientUpdatedAt = entry[TrackRecordTable.updateAt]
    record.clientDeleted = entry[TrackRecordTable.isDelete]

    record.syncId = entry[TrackRecordTable.syncId].toLong()
    record.remoteId = entry[TrackRecordTable.remoteId].toLong()
    record.libraryId = entry[TrackRecordTable.libraryId]?.toLong()
    record.title = entry[TrackRecordTable.title]
    record.lastChapterRead = entry[TrackRecordTable.lastChapterRead].toFloat()
    record.totalChapters = entry[TrackRecordTable.totalChapters]
    record.status = entry[TrackRecordTable.status]
    record.score = entry[TrackRecordTable.score].toFloat()
    record.remoteUrl = entry[TrackRecordTable.remoteUrl]
    record.startDate = entry[TrackRecordTable.startDate]
    record.finishDate = entry[TrackRecordTable.finishDate]
    record.mangaId = entry[TrackRecordTable.mangaId]

    sync.trackRecord = record
    return sync
}

fun TrackRecordTable.toBackupTracking(entry: ResultRow) =
    BackupTracking(
        syncId = entry[TrackRecordTable.syncId],
        mediaId = entry[TrackRecordTable.remoteId].toLong(),
        libraryId = entry[TrackRecordTable.libraryId]?.toLong() ?: 0,
        title = entry[TrackRecordTable.title],
        lastChapterRead = entry[TrackRecordTable.lastChapterRead].toFloat(),
        totalChapters = entry[TrackRecordTable.totalChapters],
        score = entry[TrackRecordTable.score].toFloat(),
        status = entry[TrackRecordTable.status],
        startedReadingDate = entry[TrackRecordTable.startDate],
        finishedReadingDate = entry[TrackRecordTable.finishDate],
        trackingUrl = entry[TrackRecordTable.remoteUrl],
    )
