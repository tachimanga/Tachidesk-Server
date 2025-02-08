package suwayomi.tachidesk.manga.model.table

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import app.tachimanga.cloud.enums.DataType
import app.tachimanga.cloud.model.dto.SyncCommitDTO
import app.tachimanga.cloud.model.dto.manga.ChapterDTO
import eu.kanade.tachiyomi.data.backup.models.BackupChapter
import eu.kanade.tachiyomi.source.model.SChapter
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import suwayomi.tachidesk.manga.model.dataclass.ChapterDataClass

object ChapterTable : IntIdTable() {
    val url = varchar("url", 2048)
    val name = varchar("name", 512)

    val date_upload = long("date_upload").default(0)
    val chapter_number = float("chapter_number").default(-1f)
    val scanlator = varchar("scanlator", 128).nullable()

    val isRead = bool("read").default(false)
    val isBookmarked = bool("bookmark").default(false)
    val lastPageRead = integer("last_page_read").default(0)

    val lastReadAt = long("last_read_at").default(0)

    val fetchedAt = long("fetched_at").default(0)

    val sourceOrder = integer("source_order")

    /** the real url of a chapter used for the "open in WebView" feature */
    val realUrl = varchar("real_url", 2048).nullable()

    val isDownloaded = bool("is_downloaded").default(false)

    val pageCount = integer("page_count").default(-1)

    val manga = reference("manga", MangaTable)

    val originalChapterId = integer("original_chapter_id").nullable()

    val createAt = long("create_at").default(0)
    val updateAt = long("update_at").default(0)
    val dirty = bool("dirty").default(false)
    val commitId = long("commit_id").default(0)
}

fun ChapterTable.toDataClass(chapterEntry: ResultRow) =
    ChapterDataClass(
        id = chapterEntry[id].value,
        url = chapterEntry[url],
        name = chapterEntry[name],
        uploadDate = chapterEntry[date_upload],
        chapterNumber = chapterEntry[chapter_number],
        scanlator = chapterEntry[scanlator],
        mangaId = chapterEntry[manga].value,
        read = chapterEntry[isRead],
        bookmarked = chapterEntry[isBookmarked],
        lastPageRead = chapterEntry[lastPageRead],
        lastReadAt = chapterEntry[lastReadAt],
        index = chapterEntry[sourceOrder],
        fetchedAt = chapterEntry[fetchedAt],
        realUrl = chapterEntry[realUrl],
        downloaded = chapterEntry[isDownloaded],
        pageCount = chapterEntry[pageCount],
        originalChapterId = chapterEntry[originalChapterId],
    )

// tachyomi: val pages = download.source.getPageList(download.chapter.toSChapter())
fun ChapterTable.toSChapter(chapterEntry: ResultRow) = SChapter.create().apply {
    url = chapterEntry[ChapterTable.url]
    name = chapterEntry[ChapterTable.name]
    date_upload = chapterEntry[ChapterTable.date_upload]
    chapter_number = chapterEntry[ChapterTable.chapter_number]
    scanlator = chapterEntry[ChapterTable.scanlator]
}

fun ChapterTable.toSyncData(entry: ResultRow): SyncCommitDTO {
    val sync = SyncCommitDTO()
    sync.clientDataId = entry[id].value
    sync.dataType = DataType.Chapter.name
    val chapter = ChapterDTO()

    chapter.clientCreatedAt = entry[ChapterTable.createAt]
    chapter.clientUpdatedAt = entry[ChapterTable.updateAt]
    chapter.clientDeleted = false

    chapter.url = entry[ChapterTable.url]
    chapter.name = entry[ChapterTable.name]
    chapter.dateUpload = entry[ChapterTable.date_upload]
    chapter.scanlator = entry[ChapterTable.scanlator]
    chapter.read = entry[ChapterTable.isRead]
    chapter.bookmark = entry[ChapterTable.isBookmarked]
    chapter.lastPageRead = entry[ChapterTable.lastPageRead]
    chapter.lastReadAt = entry[ChapterTable.lastReadAt]
    chapter.realUrl = entry[ChapterTable.realUrl]
    chapter.mangaId = entry[ChapterTable.manga].value

    sync.chapter = chapter
    return sync
}

fun ChapterTable.toBackupChapter(entry: ResultRow, totalChapter: Int) =
    BackupChapter(
        url = entry[ChapterTable.url],
        name = entry[ChapterTable.name],
        scanlator = entry[ChapterTable.scanlator],
        read = entry[ChapterTable.isRead],
        bookmark = entry[ChapterTable.isBookmarked],
        lastPageRead = entry[ChapterTable.lastPageRead].toLong(),
        dateFetch = entry[ChapterTable.fetchedAt] * 1000,
        dateUpload = entry[ChapterTable.date_upload],
        chapterNumber = entry[ChapterTable.chapter_number],
        sourceOrder = totalChapter - entry[ChapterTable.sourceOrder].toLong(),
    )
