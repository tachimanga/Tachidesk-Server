package suwayomi.tachidesk.manga.impl

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import eu.kanade.tachiyomi.util.chapter.ChapterRecognition
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SortOrder.ASC
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.jdbc.JdbcConnectionImpl
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.tachiyomi.Profiler
import suwayomi.tachidesk.manga.impl.Manga.getManga
import suwayomi.tachidesk.manga.impl.track.Track
import suwayomi.tachidesk.manga.impl.util.lang.awaitSingle
import suwayomi.tachidesk.manga.impl.util.source.GetCatalogueSource.getCatalogueSourceOrStub
import suwayomi.tachidesk.manga.model.dataclass.ChapterDataClass
import suwayomi.tachidesk.manga.model.dataclass.MangaChapterDataClass
import suwayomi.tachidesk.manga.model.dataclass.PaginatedList
import suwayomi.tachidesk.manga.model.dataclass.paginatedFrom
import suwayomi.tachidesk.manga.model.table.*
import suwayomi.tachidesk.manga.model.table.ChapterTable.scanlator
import suwayomi.tachidesk.server.database.MyBatchInsertStatement
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList

object Chapter {
    suspend fun delgetChapterList(mangaId: Int, onlineFetch: Boolean = false): List<ChapterDataClass> {
        transaction {
            val delte = ChapterTable.deleteWhere { ChapterTable.manga eq mangaId }.toString()
            // val delte = ChapterTable.deleteWhere(limit = 100) { ChapterTable.manga eq mangaId }.toString()
            println("delte " + delte)
        }
        return ArrayList()
    }

    /** get chapter list when showing a manga */
    suspend fun getChapterList(mangaId: Int, onlineFetch: Boolean = false): List<ChapterDataClass> {
        return if (onlineFetch) {
            Profiler.split("onlineFetch")
            getSourceChapters(mangaId)
        } else {
            Profiler.split("local")
            transaction {
                val chapterList = ChapterTable.select { ChapterTable.manga eq mangaId }
                    .orderBy(ChapterTable.sourceOrder to SortOrder.DESC)
                    .toList()
                Profiler.split("get chapterList")
                val chapterCount = chapterList.size
                val chapterIds = chapterList.map { it[ChapterTable.id] }
                val meta = getChaptersMetaMaps(chapterIds)
                Profiler.split("get meta")
                val list = chapterList.map {
                    ChapterTable.toDataClass(it, chapterCount, meta.getValue(it[ChapterTable.id]))
                }
                fixUploadDate(list)
                list
            }.ifEmpty {
                getSourceChapters(mangaId)
            }
        }
    }

    private suspend fun getSourceChapters(mangaId: Int): List<ChapterDataClass> {
        val manga = getManga(mangaId)
        val source = getCatalogueSourceOrStub(manga.sourceId.toLong())
        Profiler.split("getManga")
        val sManga = SManga.create().apply {
            title = manga.title
            url = manga.url
        }

        val chapterList = source.fetchChapterList(sManga).awaitSingle()
        Profiler.split("after fetchChapterList")
        // Recognize number for new chapters.
        chapterList.forEach {
            (source as? HttpSource)?.prepareNewChapter(it, sManga)
            ChapterRecognition.parseChapterNumber(it, sManga)
        }
        Profiler.split("after prepareNewChapter")

        val chapterCount = chapterList.count()
        var now = Instant.now().epochSecond
        val dbChapterListUrlMap = transaction {
            ChapterTable.select { ChapterTable.manga eq mangaId }
                .associateBy { it[ChapterTable.url] }
        }
        Profiler.split("after getDbChapterList")

        val sourceChapterListUrlMap = chapterList.associateBy { it.url }
        val toDeleteChapterList = dbChapterListUrlMap.values
            .filter { sourceChapterListUrlMap[it[ChapterTable.url]] == null }
            .toList()

        val toDeleteChapterNameMap = toDeleteChapterList
            .associateBy { it[ChapterTable.name] }
        val toDeleteChapterNumMap = toDeleteChapterList
            .filter { it[ChapterTable.chapter_number] > -1 }
            .associateBy { it[ChapterTable.chapter_number] }
        var sourceChapterNameDuplicate = false
        var sourceChapterNumDuplicate = false
        if (toDeleteChapterList.isNotEmpty()) {
            sourceChapterNameDuplicate = chapterList
                .associateBy { it.name }
                .size != chapterList.size
            sourceChapterNumDuplicate = chapterList
                .filter { it.chapter_number > -1 }
                .associateBy { it.name }.size != chapterList.size
        }

        Profiler.split("after toDeleteChapterList")

        val chapterPairList = chapterList.reversed().mapIndexed { i, c -> i to c }
            .toList()
        val (insertList, updateList) = chapterPairList
            .partition { dbChapterListUrlMap[it.second.url] == null }

        val realUrlMap = chapterPairList.associate { p ->
            p.first to
                runCatching {
                    (source as? HttpSource)?.getChapterUrl(p.second)
                }.getOrNull()
        }
        Profiler.split("after getChapterUrl")

        transaction {
            if (insertList.isNotEmpty()) {
                if (false) {
                    ChapterTable.batchInsert(insertList) { pair ->
                        val index = pair.first
                        val fetchedChapter = pair.second

                        this[ChapterTable.url] = fetchedChapter.url
                        this[ChapterTable.name] = fetchedChapter.name
                        this[ChapterTable.date_upload] = fetchedChapter.date_upload
                        this[ChapterTable.chapter_number] = fetchedChapter.chapter_number
                        this[scanlator] = fetchedChapter.scanlator

                        this[ChapterTable.sourceOrder] = index + 1
                        this[ChapterTable.fetchedAt] = now++
                        this[ChapterTable.manga] = mangaId

                        this[ChapterTable.realUrl] = realUrlMap[index]
                    }
                } else {
                    val myBatchInsertStatement = MyBatchInsertStatement(ChapterTable)
                    insertList.forEach { pair ->
                        val index = pair.first
                        val fetchedChapter = pair.second

                        val my = myBatchInsertStatement

                        my.addBatch()

                        my[ChapterTable.url] = fetchedChapter.url
                        my[ChapterTable.name] = fetchedChapter.name
                        my[ChapterTable.date_upload] = fetchedChapter.date_upload
                        my[ChapterTable.chapter_number] = fetchedChapter.chapter_number
                        my[scanlator] = fetchedChapter.scanlator

                        my[ChapterTable.sourceOrder] = index + 1
                        my[ChapterTable.fetchedAt] = now++
                        my[ChapterTable.manga] = mangaId

                        my[ChapterTable.realUrl] = realUrlMap[index]

                        val toDeleteChapter = if (!sourceChapterNameDuplicate && toDeleteChapterNameMap[fetchedChapter.name] != null) {
                            toDeleteChapterNameMap[fetchedChapter.name]
                        } else if (!sourceChapterNumDuplicate && toDeleteChapterNumMap[fetchedChapter.chapter_number] != null) {
                            toDeleteChapterNumMap[fetchedChapter.chapter_number]
                        } else {
                            null
                        }
                        if (toDeleteChapter != null) {
                            my[ChapterTable.isDownloaded] = toDeleteChapter[ChapterTable.isDownloaded]
                            my[ChapterTable.isBookmarked] = toDeleteChapter[ChapterTable.isBookmarked]
                            my[ChapterTable.isRead] = toDeleteChapter[ChapterTable.isRead]
                        }
                    }

                    val sql = myBatchInsertStatement.prepareSQL(this)
                    val conn = (TransactionManager.current().connection as JdbcConnectionImpl).connection
                    val statement = conn.createStatement()
                    // println(sql)
                    statement.execute(sql)
                }
            }
        }
        Profiler.split("upsert chapterList")

        val toUpdateList = updateList
            .filter {
                decideChapterNeedUpdate(dbChapterListUrlMap, realUrlMap, it)
            }
            .toList()

        Profiler.split("calc toUpdateList")
        println("Profiler: updateList.size=${updateList.size}, toUpdateList.size=${toUpdateList.size}")
        transaction {
            if (toUpdateList.isNotEmpty()) {
                toUpdateList.forEach { pair ->
                    val index = pair.first
                    val fetchedChapter = pair.second
                    ChapterTable.update({ ChapterTable.manga eq mangaId and (ChapterTable.url eq fetchedChapter.url) }) {
                        it[name] = fetchedChapter.name
                        it[date_upload] = fetchedChapter.date_upload
                        it[chapter_number] = fetchedChapter.chapter_number
                        it[scanlator] = fetchedChapter.scanlator

                        it[sourceOrder] = index + 1
                        it[ChapterTable.manga] = mangaId
                        it[realUrl] = realUrlMap[index]
                    }
                }
            }
            Profiler.split("update chapterList")
            MangaTable.update({ MangaTable.id eq mangaId }) {
                it[MangaTable.chaptersLastFetchedAt] = Instant.now().epochSecond
            }
            Profiler.split("update MangaTable")
        }

        if (toDeleteChapterList.isNotEmpty()) {
            transaction {
                toDeleteChapterList.forEach { dbChapter ->
                    PageTable.deleteWhere { PageTable.chapter eq dbChapter[ChapterTable.id] }
                    ChapterTable.deleteWhere { ChapterTable.id eq dbChapter[ChapterTable.id] }
                }
            }
        }
        val dbChapterCount = transaction { ChapterTable.select { ChapterTable.manga eq mangaId }.count() }
        if (dbChapterCount > chapterCount) { // we got some clean up due
            val dbChapterList = transaction {
                ChapterTable.select { ChapterTable.manga eq mangaId }.orderBy(ChapterTable.url to ASC).toList()
            }
            dbChapterList.forEachIndexed { index, dbChapter ->
                if (
                    (index < dbChapterList.lastIndex && dbChapter[ChapterTable.url] == dbChapterList[index + 1][ChapterTable.url]) // is duplicate
                ) {
                    transaction {
                        PageTable.deleteWhere { PageTable.chapter eq dbChapter[ChapterTable.id] }
                        ChapterTable.deleteWhere { ChapterTable.id eq dbChapter[ChapterTable.id] }
                    }
                }
            }
        }

        Profiler.split("after clean old chapter")
        val dbChapterMap = transaction {
            ChapterTable.select { ChapterTable.manga eq mangaId }
                .associateBy({ it[ChapterTable.url] }, { it })
        }
        Profiler.split("get dbChapterMap")
        val chapterIds = chapterList.map { dbChapterMap.getValue(it.url)[ChapterTable.id] }
        val chapterMetas = getChaptersMetaMaps(chapterIds)
        Profiler.split("get getChaptersMetaMaps")

        val chapterDataList = chapterList.mapIndexed { index, it ->
            val dbChapter = dbChapterMap.getValue(it.url)
            ChapterDataClass(
                id = dbChapter[ChapterTable.id].value,
                url = it.url,
                name = it.name,
                uploadDate = it.date_upload,
                chapterNumber = it.chapter_number,
                scanlator = it.scanlator,
                mangaId = mangaId,

                read = dbChapter[ChapterTable.isRead],
                bookmarked = dbChapter[ChapterTable.isBookmarked],
                lastPageRead = dbChapter[ChapterTable.lastPageRead],
                lastReadAt = dbChapter[ChapterTable.lastReadAt],

                index = chapterCount - index,
                fetchedAt = dbChapter[ChapterTable.fetchedAt],
                realUrl = dbChapter[ChapterTable.realUrl],
                downloaded = dbChapter[ChapterTable.isDownloaded],

                pageCount = dbChapter[ChapterTable.pageCount],

                chapterCount = chapterList.size,
                meta = chapterMetas.getValue(dbChapter[ChapterTable.id])
            )
        }
        fixUploadDate(chapterDataList)
        return chapterDataList
    }

    private fun fixUploadDate(list: List<ChapterDataClass>) {
        val rightNow = Date().time
        var maxSeenUploadDate = 0L

        list.forEach {
            if (it.uploadDate == 0L) {
                it.uploadDate = if (maxSeenUploadDate == 0L) rightNow else maxSeenUploadDate
            } else {
                maxSeenUploadDate = Math.max(maxSeenUploadDate, it.uploadDate)
            }
        }
    }

    private fun decideChapterNeedUpdate(
        dbChapterMap: Map<String, ResultRow>,
        realChapterUrlMap: Map<Int, String?>,
        pair: Pair<Int, SChapter>
    ): Boolean {
        val index = pair.first
        val fetchedChapter = pair.second
        val dbChapter = dbChapterMap[fetchedChapter.url] ?: return false
        val realUrl = realChapterUrlMap[index]

        if (dbChapter[ChapterTable.name] == fetchedChapter.name &&
            dbChapter[ChapterTable.date_upload] == fetchedChapter.date_upload &&
            dbChapter[ChapterTable.chapter_number] == fetchedChapter.chapter_number &&
            dbChapter[ChapterTable.scanlator] == fetchedChapter.scanlator &&
            dbChapter[ChapterTable.sourceOrder] == index + 1 &&
            dbChapter[ChapterTable.realUrl] == realUrl
        ) {
            return false
        }
        return true
    }

    fun modifyChapter(
        mangaId: Int,
        chapterIndex: Int,
        isRead: Boolean?,
        isBookmarked: Boolean?,
        markPrevRead: Boolean?,
        lastPageRead: Int?
    ) {
        transaction {
            if (listOf(isRead, isBookmarked, lastPageRead).any { it != null }) {
                ChapterTable.update({ (ChapterTable.manga eq mangaId) and (ChapterTable.sourceOrder eq chapterIndex) }) { update ->
                    isRead?.also {
                        update[ChapterTable.isRead] = it
                    }
                    isBookmarked?.also {
                        update[ChapterTable.isBookmarked] = it
                    }
                    lastPageRead?.also {
                        update[ChapterTable.lastPageRead] = it
                        update[ChapterTable.lastReadAt] = Instant.now().epochSecond
                    }
                }
            }
            if (markPrevRead == true) {
                ChapterTable.update({ (ChapterTable.manga eq mangaId) and (ChapterTable.sourceOrder less chapterIndex) }) {
                    it[ChapterTable.isRead] = markPrevRead
                }
            }
        }

        if (isRead == true || markPrevRead == true) {
            Track.asyncTrackChapter(mangaId)
        }
    }

    @Serializable
    data class ChapterChange(
        val isRead: Boolean? = null,
        val isBookmarked: Boolean? = null,
        val lastPageRead: Int? = null,
        val delete: Boolean? = null
    )

    @Serializable
    data class MangaChapterBatchEditInput(
        val chapterIds: List<Int>? = null,
        val chapterIndexes: List<Int>? = null,
        val change: ChapterChange?
    )

    @Serializable
    data class ChapterBatchEditInput(
        val chapterIds: List<Int>? = null,
        val change: ChapterChange?
    )

    fun modifyChapters(input: MangaChapterBatchEditInput, mangaId: Int? = null) {
        // Make sure change is defined
        if (input.change == null) return
        val (isRead, isBookmarked, lastPageRead, delete) = input.change

        // Handle deleting separately
        if (delete == true) {
            deleteChapters(input, mangaId)
        }

        // return early if there are no other changes
        if (listOfNotNull(isRead, isBookmarked, lastPageRead).isEmpty()) return

        // Make sure some filter is defined
        val condition = when {
            mangaId != null ->
                // mangaId is not null, scope query under manga
                when {
                    input.chapterIds != null ->
                        Op.build { (ChapterTable.manga eq mangaId) and (ChapterTable.id inList input.chapterIds) }
                    input.chapterIndexes != null ->
                        Op.build { (ChapterTable.manga eq mangaId) and (ChapterTable.sourceOrder inList input.chapterIndexes) }
                    else -> null
                }
            else -> {
                // mangaId is null, only chapterIndexes is valid for this case
                when {
                    input.chapterIds != null ->
                        Op.build { (ChapterTable.id inList input.chapterIds) }
                    else -> null
                }
            }
        } ?: return

        transaction {
            val now = Instant.now().epochSecond
            ChapterTable.update({ condition }) { update ->
                isRead?.also {
                    update[ChapterTable.isRead] = it
                }
                isBookmarked?.also {
                    update[ChapterTable.isBookmarked] = it
                }
                lastPageRead?.also {
                    update[ChapterTable.lastPageRead] = it
                    update[ChapterTable.lastReadAt] = now
                }
            }
        }

        if (isRead == true) {
            val mangaIds = transaction {
                ChapterTable.select { condition }
                    .map { it[ChapterTable.manga].value }
                    .distinct()
            }
            mangaIds.forEach { Track.asyncTrackChapter(it) }
        }
    }

    fun getChaptersMetaMaps(chapterIds: List<EntityID<Int>>): Map<EntityID<Int>, Map<String, String>> {
        return transaction {
            ChapterMetaTable.select { ChapterMetaTable.ref inList chapterIds }
                .groupBy { it[ChapterMetaTable.ref] }
                .mapValues { it.value.associate { it[ChapterMetaTable.key] to it[ChapterMetaTable.value] } }
                .withDefault { emptyMap<String, String>() }
        }
    }

    fun getChapterMetaMap(chapter: EntityID<Int>): Map<String, String> {
        return transaction {
            ChapterMetaTable.select { ChapterMetaTable.ref eq chapter }
                .associate { it[ChapterMetaTable.key] to it[ChapterMetaTable.value] }
        }
    }

    fun modifyChapterMeta(mangaId: Int, chapterIndex: Int, key: String, value: String) {
        transaction {
            val chapterId =
                ChapterTable.select { (ChapterTable.manga eq mangaId) and (ChapterTable.sourceOrder eq chapterIndex) }
                    .first()[ChapterTable.id].value
            val meta =
                ChapterMetaTable.select { (ChapterMetaTable.ref eq chapterId) and (ChapterMetaTable.key eq key) }
                    .firstOrNull()

            if (meta == null) {
                ChapterMetaTable.insert {
                    it[ChapterMetaTable.key] = key
                    it[ChapterMetaTable.value] = value
                    it[ChapterMetaTable.ref] = chapterId
                }
            } else {
                ChapterMetaTable.update({ (ChapterMetaTable.ref eq chapterId) and (ChapterMetaTable.key eq key) }) {
                    it[ChapterMetaTable.value] = value
                }
            }
        }
    }

    fun deleteChapter(mangaId: Int, chapterIndex: Int) {
        transaction {
            val chapterId =
                ChapterTable.select { (ChapterTable.manga eq mangaId) and (ChapterTable.sourceOrder eq chapterIndex) }
                    .first()[ChapterTable.id].value

            ChapterDownloadHelper.delete(mangaId, chapterId)

            ChapterTable.update({ (ChapterTable.manga eq mangaId) and (ChapterTable.sourceOrder eq chapterIndex) }) {
                it[isDownloaded] = false
            }
        }
    }

    private fun deleteChapters(input: MangaChapterBatchEditInput, mangaId: Int? = null) {
        if (input.chapterIds != null) {
            val chapterIds = input.chapterIds

            transaction {
                ChapterTable.slice(ChapterTable.manga, ChapterTable.id)
                    .select { ChapterTable.id inList chapterIds }
                    .forEach { row ->
                        val chapterMangaId = row[ChapterTable.manga].value
                        val chapterId = row[ChapterTable.id].value
                        ChapterDownloadHelper.delete(chapterMangaId, chapterId)
                    }

                ChapterTable.update({ ChapterTable.id inList chapterIds }) {
                    it[isDownloaded] = false
                }
            }
        } else if (input.chapterIndexes != null && mangaId != null) {
            transaction {
                val chapterIds = ChapterTable.slice(ChapterTable.manga, ChapterTable.id)
                    .select { (ChapterTable.sourceOrder inList input.chapterIndexes) and (ChapterTable.manga eq mangaId) }
                    .map { row ->
                        val chapterId = row[ChapterTable.id].value
                        ChapterDownloadHelper.delete(mangaId, chapterId)

                        chapterId
                    }

                ChapterTable.update({ ChapterTable.id inList chapterIds }) {
                    it[isDownloaded] = false
                }
            }
        }
    }

    fun getRecentChapters(pageNum: Int): PaginatedList<MangaChapterDataClass> {
        return paginatedFrom(pageNum) {
            transaction {
                (ChapterTable innerJoin MangaTable)
                    .select { (MangaTable.inLibrary eq true) and (ChapterTable.fetchedAt greater MangaTable.inLibraryAt) }
                    .orderBy(ChapterTable.fetchedAt to SortOrder.DESC)
                    .map {
                        MangaChapterDataClass(
                            MangaTable.toDataClass(it),
                            ChapterTable.toDataClass(it)
                        )
                    }
            }
        }
    }
}
