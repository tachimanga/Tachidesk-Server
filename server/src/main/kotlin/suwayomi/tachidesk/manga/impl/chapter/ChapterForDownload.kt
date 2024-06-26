package suwayomi.tachidesk.manga.impl.chapter

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import eu.kanade.tachiyomi.source.model.Page
import okhttp3.internal.trimSubstring
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import suwayomi.tachidesk.manga.impl.Page.getPageName
import suwayomi.tachidesk.manga.impl.util.getChapterCbzPath
import suwayomi.tachidesk.manga.impl.util.getChapterDownloadPath
import suwayomi.tachidesk.manga.impl.util.lang.awaitSingle
import suwayomi.tachidesk.manga.impl.util.source.GetCatalogueSource.getCatalogueSourceOrStub
import suwayomi.tachidesk.manga.impl.util.storage.ImageResponse
import suwayomi.tachidesk.manga.model.dataclass.ChapterDataClass
import suwayomi.tachidesk.manga.model.table.*
import java.io.File
import java.time.Instant

suspend fun getChapterDownloadReady(chapterIndex: Int, mangaId: Int): ChapterDataClass {
    val chapter = ChapterForDownload(chapterIndex, mangaId)

    return chapter.asDownloadReady()
}

fun preprocessPageList(pageList: List<Page>): List<Page> {
    val list = pageList.filter {
        it.imageUrl != ""
    }
    list.forEach {
        it.imageUrl = it.imageUrl?.trimSubstring()
        if (it.imageUrl?.startsWith("://") == true) {
            it.imageUrl = "https${it.imageUrl}"
        } else if (it.imageUrl?.startsWith("//") == true) {
            it.imageUrl = "https:${it.imageUrl}"
        }
    }
    // Tachiyomi: Don't trust sources and use our own indexing
    return list.mapIndexed { index, page ->
        Page(index, page.url, page.imageUrl)
    }
}

private class ChapterForDownload(
    private val chapterIndex: Int,
    private val mangaId: Int
) {
    suspend fun asDownloadReady(): ChapterDataClass {
        if (isNotCompletelyDownloaded()) {
            markAsNotDownloaded()

            val pageListSrc = fetchPageList()
            val pageList = preprocessPageList(pageListSrc)
            updateDatabasePages(pageList)
        }

        return asDataClass()
    }

    private fun asDataClass() = ChapterTable.toDataClass(chapterEntry)

    var chapterEntry: ResultRow = freshChapterEntry()

    private fun freshChapterEntry() = transaction {
        ChapterTable.select {
            (ChapterTable.sourceOrder eq chapterIndex) and (ChapterTable.manga eq mangaId)
        }.first()
    }

    private suspend fun fetchPageList(): List<Page> {
        val mangaEntry = transaction { MangaTable.select { MangaTable.id eq mangaId }.first() }
        val source = getCatalogueSourceOrStub(mangaEntry[MangaTable.sourceReference])

        // tachyomi: val pages = download.source.getPageList(download.chapter.toSChapter())
        val sChapter = ChapterTable.toSChapter(chapterEntry)
        return source.fetchPageList(sChapter).awaitSingle()
    }

    private fun markAsNotDownloaded() {
        val chapterId = chapterEntry[ChapterTable.id].value
        // chapter may be downloaded but if we are here, then images might be deleted and database data be false
        transaction {
            ChapterTable.update({ (ChapterTable.id eq chapterId) }) {
                it[isDownloaded] = false
            }
            MangaTable.update({ MangaTable.id eq mangaId }) {
                it[lastDownloadAt] = Instant.now().epochSecond
            }
        }
    }

    private fun updateDatabasePages(pageList: List<Page>) {
        val chapterId = chapterEntry[ChapterTable.id].value

        transaction {
            pageList.forEach { page ->
                val pageEntry = transaction {
                    PageTable.select { (PageTable.chapter eq chapterId) and (PageTable.index eq page.index) }
                        .firstOrNull()
                }
                if (pageEntry == null) {
                    PageTable.insert {
                        it[index] = page.index
                        it[url] = page.url
                        it[imageUrl] = page.imageUrl
                        it[chapter] = chapterId
                    }
                } else {
                    PageTable.update({ (PageTable.chapter eq chapterId) and (PageTable.index eq page.index) }) {
                        it[url] = page.url
                        it[imageUrl] = page.imageUrl
                    }
                }
            }
        }

        updatePageCount(pageList, chapterId)

        // chapter was updated
        chapterEntry = freshChapterEntry()
    }

    private fun updatePageCount(
        pageList: List<Page>,
        chapterId: Int
    ) {
        val pageCount = pageList.count()

        transaction {
            ChapterTable.update({ (ChapterTable.id eq chapterId) }) {
                it[ChapterTable.pageCount] = pageCount
            }
        }
    }

    private fun isNotCompletelyDownloaded(): Boolean {
        return !(
            chapterEntry[ChapterTable.isDownloaded] &&
                chapterEntry[ChapterTable.pageCount] > 0 &&
                (firstPageExists() || File(getChapterCbzPath(mangaId, chapterEntry[ChapterTable.id].value)).exists())
            )
    }

    private fun firstPageExists(): Boolean {
        val chapterId = chapterEntry[ChapterTable.id].value

        val chapterDir = getChapterDownloadPath(mangaId, chapterId)

        println(chapterDir)
        println(getPageName(0))

        return ImageResponse.findFileNameStartingWith(
            chapterDir,
            getPageName(0)
        ) != null
    }
}
