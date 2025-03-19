package suwayomi.tachidesk.manga.impl

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import kotlinx.serialization.Serializable
import mu.KotlinLogging
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import suwayomi.tachidesk.cloud.impl.Sync
import suwayomi.tachidesk.manga.impl.download.FolderProvider2
import suwayomi.tachidesk.manga.impl.track.Track
import suwayomi.tachidesk.manga.model.table.CategoryMangaTable
import suwayomi.tachidesk.manga.model.table.ChapterTable
import suwayomi.tachidesk.manga.model.table.MangaTable

object MangaBatch {
    private val logger = KotlinLogging.logger {}

    fun batchUpdate(input: MangaBatchInput) {
        logger.info { "batchUpdate $input" }
        if (input.changes == null) {
            return
        }
        batchUpdateChapterRead(changes = input.changes)
        batchUpdateChapterUnread(changes = input.changes)
        batchUpdateCategory(changes = input.changes)
        batchRemoveFromLibrary(changes = input.changes)
        batchRemoveDownloads(changes = input.changes)
    }

    private fun batchUpdateChapterRead(changes: List<MangaChange>) {
        val mangaIds = changes
            .filter { it.chapterRead == true }
            .mapNotNull { it.mangaId }
            .distinct()
            .toList()
        if (mangaIds.isEmpty()) {
            return
        }

        val effectedMangaIds = transaction {
            ChapterTable.slice(ChapterTable.manga)
                .select { (ChapterTable.manga inList mangaIds) and (ChapterTable.isRead eq false) }
                .withDistinct(true)
                .map { it[ChapterTable.manga].value }
        }

        val now = System.currentTimeMillis()
        transaction {
            ChapterTable.update({ (ChapterTable.manga inList mangaIds) and (ChapterTable.isRead eq false) }) {
                it[ChapterTable.isRead] = true
                it[ChapterTable.updateAt] = now
                it[ChapterTable.dirty] = true
            }
        }

        if (effectedMangaIds.isNotEmpty()) {
            effectedMangaIds.forEach { Track.asyncTrackChapter(it) }
            Manga.batchMarkDirtyIfCommitIdZero(effectedMangaIds)
        }

        Sync.setNeedsSync()
    }

    private fun batchUpdateChapterUnread(changes: List<MangaChange>) {
        val mangaIds = changes
            .filter { it.chapterRead == false }
            .mapNotNull { it.mangaId }
            .distinct()
            .toList()
        if (mangaIds.isEmpty()) {
            return
        }

        val now = System.currentTimeMillis()
        transaction {
            ChapterTable.update({ (ChapterTable.manga inList mangaIds) and (ChapterTable.isRead eq true) }) {
                it[ChapterTable.isRead] = false
                it[ChapterTable.updateAt] = now
                it[ChapterTable.dirty] = true
            }
        }
        Manga.batchMarkDirtyIfCommitIdZero(mangaIds)
        Sync.setNeedsSync()
    }

    private fun batchUpdateCategory(changes: List<MangaChange>) {
        val pairs = changes
            .filter { it.mangaId != null && it.categoryIds != null }
            .map { it.mangaId!! to it.categoryIds!! }
            .toList()
        if (pairs.isEmpty()) {
            return
        }
        CategoryManga.batchUpdateCategory(pairs)
    }

    private fun batchRemoveFromLibrary(changes: List<MangaChange>) {
        val mangaIds = changes
            .filter { it.removeFromLibrary == true }
            .mapNotNull { it.mangaId }
            .distinct()
            .toList()
        if (mangaIds.isEmpty()) {
            return
        }

        val now = System.currentTimeMillis()
        transaction {
            MangaTable.update({ (MangaTable.id inList mangaIds) and (MangaTable.inLibrary eq true) }) {
                it[MangaTable.inLibrary] = false
                it[MangaTable.updateAt] = now
                it[MangaTable.dirty] = true
            }
            CategoryMangaTable.deleteWhere { (CategoryMangaTable.manga inList mangaIds) }
        }
        Sync.setNeedsSync()
    }

    private fun batchRemoveDownloads(changes: List<MangaChange>) {
        val mangaIds = changes
            .filter { it.removeDownloads == true }
            .mapNotNull { it.mangaId }
            .distinct()
            .toList()
        if (mangaIds.isEmpty()) {
            return
        }
        for (mangaId in mangaIds) {
            FolderProvider2(mangaId, 0, 0).deleteAll()
        }
        transaction {
            ChapterTable.update({ (ChapterTable.manga inList mangaIds) and (ChapterTable.isDownloaded eq true) }) {
                it[ChapterTable.isDownloaded] = false
            }
        }
    }

    @Serializable
    data class MangaBatchInput(
        val changes: List<MangaChange>? = null,
    )

    @Serializable
    data class MangaChange(
        val mangaId: Int? = null,
        val removeFromLibrary: Boolean? = null,
        val removeDownloads: Boolean? = null,
        val categoryIds: List<Int>? = null,
        val chapterRead: Boolean? = null,
    )
}
