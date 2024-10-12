package suwayomi.tachidesk.manga.impl

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.online.HttpSource
import eu.kanade.tachiyomi.util.chapter.ChapterRecognition
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SortOrder.ASC
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.jdbc.JdbcConnectionImpl
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.tachiyomi.Profiler
import suwayomi.tachidesk.manga.impl.download.FolderProvider2
import suwayomi.tachidesk.manga.impl.track.Track
import suwayomi.tachidesk.manga.impl.util.lang.awaitSingle
import suwayomi.tachidesk.manga.impl.util.source.GetCatalogueSource.getCatalogueSourceOrStub
import suwayomi.tachidesk.manga.model.dataclass.*
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
            val chapterList = transaction {
                ChapterTable.select { ChapterTable.manga eq mangaId }
                    .orderBy(ChapterTable.sourceOrder to SortOrder.DESC)
                    .toList()
            }
            Profiler.split("get chapterList")
            val list = chapterList.map {
                ChapterTable.toDataClass(it)
            }
            fixUploadDate(list)
            list.ifEmpty {
                getSourceChapters(mangaId)
            }
        }
    }

    private suspend fun getSourceChapters(mangaId: Int): List<ChapterDataClass> {
        // val manga = getManga(mangaId)
        val mangaEntry = transaction { MangaTable.select { MangaTable.id eq mangaId }.first() }
        val source = getCatalogueSourceOrStub(mangaEntry[MangaTable.sourceReference])
        Profiler.split("getManga")
        // tachiyomi: state.source.getMangaDetails(state.manga.toSManga())
        val sManga = MangaTable.toSManga(mangaEntry)
        val rawChapterList = source.fetchChapterList(sManga).awaitSingle()
        val chapterList = rawChapterList
            .distinctBy { it.url }
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
            .groupBy { it[ChapterTable.name] }
        val toDeleteChapterNumMap = toDeleteChapterList
            .filter { it[ChapterTable.chapter_number] > -1 }
            .groupBy { it[ChapterTable.chapter_number] }

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
                        my[ChapterTable.name] = fetchedChapter.name.take(512)
                        my[ChapterTable.date_upload] = fetchedChapter.date_upload
                        my[ChapterTable.chapter_number] = fetchedChapter.chapter_number
                        my[scanlator] = fetchedChapter.scanlator?.take(128)

                        my[ChapterTable.sourceOrder] = index + 1
                        my[ChapterTable.fetchedAt] = now++
                        my[ChapterTable.manga] = mangaId

                        my[ChapterTable.realUrl] = realUrlMap[index]

                        val sameNameChapterList = toDeleteChapterNameMap[fetchedChapter.name]
                        val sameNumChapterList = toDeleteChapterNumMap[fetchedChapter.chapter_number]
                        /*
                        val toDeleteChapter = if (sameNameChapterList?.size == 1) {
                            sameNameChapterList[0]
                        } else if (sameNumChapterList?.size == 1) {
                            sameNumChapterList[0]
                        } else {
                            null
                        }*/
                        val toDeleteChapter = findSameChapter(sameNameChapterList) ?: findSameChapter(sameNumChapterList)

                        if (toDeleteChapter != null) {
                            my[ChapterTable.isDownloaded] = toDeleteChapter[ChapterTable.isDownloaded]
                            my[ChapterTable.pageCount] = toDeleteChapter[ChapterTable.pageCount]
                            my[ChapterTable.isBookmarked] = toDeleteChapter[ChapterTable.isBookmarked]
                            my[ChapterTable.isRead] = toDeleteChapter[ChapterTable.isRead]
                            my[ChapterTable.fetchedAt] = toDeleteChapter[ChapterTable.fetchedAt]
                            my[ChapterTable.lastPageRead] = toDeleteChapter[ChapterTable.lastPageRead]
                            my[ChapterTable.lastReadAt] = toDeleteChapter[ChapterTable.lastReadAt]
                            my[ChapterTable.originalChapterId] = toDeleteChapter[ChapterTable.originalChapterId] ?: toDeleteChapter[ChapterTable.id].value
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
                        it[name] = fetchedChapter.name.take(512)
                        it[date_upload] = fetchedChapter.date_upload
                        it[chapter_number] = fetchedChapter.chapter_number
                        it[scanlator] = fetchedChapter.scanlator?.take(128)

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

        if (toDeleteChapterList.isNotEmpty() && chapterList.isNotEmpty()) {
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

                pageCount = dbChapter[ChapterTable.pageCount]
            )
        }
        fixUploadDate(chapterDataList)
        return chapterDataList
    }

    private fun findSameChapter(chapterList: List<ResultRow>?): ResultRow? {
        if (chapterList.isNullOrEmpty()) {
            return null
        }
        if (chapterList.size == 1) {
            return chapterList[0]
        }
        val chapter = chapterList.firstOrNull {
            it[ChapterTable.isDownloaded] || it[ChapterTable.isBookmarked] || it[ChapterTable.isRead]
        }
        return chapter ?: chapterList[0]
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

    fun modifyChapter2(input: ChapterModifyInput) {
        if (input.mangaId == null || input.chapterId == null) {
            return
        }
        transaction {
            if (input.read != null || input.bookmarked != null || input.lastPageRead != null) {
                ChapterTable.update({ (ChapterTable.id eq input.chapterId) }) {
                    if (input.read != null) {
                        it[ChapterTable.isRead] = input.read
                    }
                    if (input.bookmarked != null) {
                        it[ChapterTable.isBookmarked] = input.bookmarked
                    }
                    if (input.lastPageRead != null) {
                        it[ChapterTable.lastPageRead] = input.lastPageRead
                        it[ChapterTable.lastReadAt] = Instant.now().epochSecond
                    }
                }
            }
            if (input.markPrevRead == true) {
                val chapter = ChapterTable.select { ChapterTable.id eq input.chapterId }.first()
                ChapterTable.update({ (ChapterTable.manga eq input.mangaId) and (ChapterTable.sourceOrder less chapter[ChapterTable.sourceOrder]) }) {
                    it[ChapterTable.isRead] = true
                }
            }
        }
        if (input.lastPageRead != null && input.incognito != true) {
            History.upsertHistory(input.mangaId, input.chapterId)
        }
        if (input.read == true || input.markPrevRead == true) {
            Track.asyncTrackChapter(input.mangaId)
        }
    }

    fun chapterBatchQuery(input: ChapterBatchQueryInput): List<ChapterDataClass> {
        if (input.chapterIds.isNullOrEmpty()) {
            return listOf()
        }
        val chapterList = transaction {
            ChapterTable
                .select { (ChapterTable.id inList input.chapterIds) }
                .map { ChapterTable.toDataClass(it) }
        }
        return chapterList
    }

    @Serializable
    data class ChapterModifyInput(
        val mangaId: Int? = null,
        val chapterId: Int? = null,
        val read: Boolean? = null,
        val bookmarked: Boolean? = null,
        val markPrevRead: Boolean? = null,
        val lastPageRead: Int? = null,
        val incognito: Boolean? = null
    )

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

    @Serializable
    data class ChapterBatchQueryInput(
        val chapterIds: List<Int>? = null
    )

    fun modifyChapters(input: MangaChapterBatchEditInput, mangaId: Int? = null) {
        // Make sure change is defined
        if (input.change == null) return
        val (isRead, isBookmarked, lastPageRead, delete) = input.change

        // Handle deleting separately
        if (delete == true) {
            deleteChapters(input, mangaId)
            return
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
            val chapter =
                ChapterTable.select { (ChapterTable.manga eq mangaId) and (ChapterTable.sourceOrder eq chapterIndex) }
                    .first()
            val chapterId = chapter[ChapterTable.id].value
            val originalChapterId = chapter[ChapterTable.originalChapterId]
            FolderProvider2(mangaId, chapterId, originalChapterId).delete()

            ChapterTable.update({ (ChapterTable.id eq chapterId) }) {
                it[isDownloaded] = false
            }
        }
    }

    fun deleteChapters(input: MangaChapterBatchEditInput, mangaId: Int? = null) {
        if (input.chapterIds?.isNotEmpty() == true) {
            val chapterIds = input.chapterIds

            transaction {
                ChapterTable.slice(ChapterTable.manga, ChapterTable.id, ChapterTable.originalChapterId)
                    .select { ChapterTable.id inList chapterIds }
                    .forEach { row ->
                        val chapterMangaId = row[ChapterTable.manga].value
                        val chapterId = row[ChapterTable.id].value
                        val originalChapterId = row[ChapterTable.originalChapterId]
                        FolderProvider2(chapterMangaId, chapterId, originalChapterId).delete()
                    }

                ChapterTable.update({ ChapterTable.id inList chapterIds }) {
                    it[isDownloaded] = false
                }
            }
        } else if (input.chapterIndexes?.isNotEmpty() == true && mangaId != null) {
            transaction {
                val chapterIds = ChapterTable.slice(ChapterTable.manga, ChapterTable.id, ChapterTable.originalChapterId)
                    .select { (ChapterTable.sourceOrder inList input.chapterIndexes) and (ChapterTable.manga eq mangaId) }
                    .map { row ->
                        val chapterId = row[ChapterTable.id].value
                        val originalChapterId = row[ChapterTable.originalChapterId]
                        FolderProvider2(mangaId, chapterId, originalChapterId).delete()
                        chapterId
                    }

                ChapterTable.update({ ChapterTable.id inList chapterIds }) {
                    it[isDownloaded] = false
                }
            }
        }
    }

    fun getRecentChapters(pageNum: Int): PaginatedList<MangaChapterDataClass> {
        var fetchAt = 0L
        var list: List<MangaDataClass>? = null
        for (i in 0..2) {
            fetchAt = System.currentTimeMillis() / 1000 - 86400 * (3 + i * 2)
            list = getRecentMangaList(fetchAt)
            if (list.isNotEmpty()) {
                break
            }
        }
        val mangaList = list!!
        val minFetchedAt = fetchAt

        val mangaMap = mangaList.associateBy { it.id }

        val counter = mutableMapOf<Int, Int>()
        val chapterRows = transaction {
            ChapterTable
                .select { (ChapterTable.manga inList mangaMap.keys) and (ChapterTable.fetchedAt greater minFetchedAt) }
                .orderBy(ChapterTable.fetchedAt to SortOrder.DESC)
                .limit(3000)
                .toList()
        }

        val chapters = ArrayList<ResultRow>(chapterRows.size)
        chapterRows.forEach {
            val cnt = counter.getOrDefault(it[ChapterTable.manga].value, 0)
            if (cnt < 10) {
                counter[it[ChapterTable.manga].value] = cnt + 1
                chapters.add(it)
            }
        }

        val paginatedList = paginatedFrom(pageNum, paginationFactor = 100) {
            chapters
        }
        return PaginatedList(
            paginatedList.page.map {
                MangaChapterDataClass(
                    mangaMap[it[ChapterTable.manga].value]!!,
                    ChapterTable.toDataClass(it)
                )
            },
            paginatedList.hasNextPage
        )
    }

    private fun getRecentMangaList(minFetchedAt: Long): List<MangaDataClass> {
        val mangaList = transaction {
            MangaTable
                .select {
                    (MangaTable.inLibrary eq true) and
                        (MangaTable.chaptersLastFetchedAt greater minFetchedAt) and
                        (MangaTable.chaptersLastFetchedAt greater MangaTable.inLibraryAt)
                }
                .map {
                    MangaTable.toDataClass(it)
                }
        }
        return mangaList
    }

    fun getChapterRealUrl(mangaId: Int, chapterIndex: Int): ChapterDataClass {
        val chapterEntry = transaction {
            ChapterTable.select {
                (ChapterTable.sourceOrder eq chapterIndex) and (ChapterTable.manga eq mangaId)
            }.first()
        }

        val chapterData = ChapterTable.toDataClass(chapterEntry)
        if (chapterData.realUrl?.isNotEmpty() == true) {
            return chapterData
        }

        val mangaEntry = transaction { MangaTable.select { MangaTable.id eq mangaId }.first() }
        val source = getCatalogueSourceOrStub(mangaEntry[MangaTable.sourceReference])

        return if (source is HttpSource) {
            val sChapter = ChapterTable.toSChapter(chapterEntry)
            val realUrl = source.getChapterUrl(sChapter)
            return chapterData.copy(realUrl = realUrl)
        } else {
            chapterData
        }
    }
}
