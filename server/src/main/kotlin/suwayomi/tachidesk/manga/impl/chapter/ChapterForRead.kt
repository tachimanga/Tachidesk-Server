package suwayomi.tachidesk.manga.impl.chapter

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import eu.kanade.tachiyomi.source.CatalogueSource
import eu.kanade.tachiyomi.source.SourceMeta
import eu.kanade.tachiyomi.source.local.LocalSource
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.sourceSupportDirect
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
import suwayomi.tachidesk.manga.impl.util.source.GetCatalogueSource
import suwayomi.tachidesk.manga.impl.util.source.GetCatalogueSource.getCatalogueSourceOrStub
import suwayomi.tachidesk.manga.impl.util.storage.ImageResponse
import suwayomi.tachidesk.manga.model.dataclass.ChapterDataClass
import suwayomi.tachidesk.manga.model.dataclass.buildImgDataClass
import suwayomi.tachidesk.manga.model.table.ChapterTable
import suwayomi.tachidesk.manga.model.table.MangaTable
import suwayomi.tachidesk.manga.model.table.PageTable
import suwayomi.tachidesk.manga.model.table.toDataClass
import java.io.File
import java.time.Instant

suspend fun getChapterReadReady(chapterIndex: Int, mangaId: Int): ChapterDataClass {
    val chapter = ChapterForRead(chapterIndex, mangaId)
    val data = chapter.asReadReady()
    chapter.updateLastReadAt()
    return data
}

private class ChapterForRead(
    private val chapterIndex: Int,
    private val mangaId: Int
) {
    suspend fun asReadReady(): ChapterDataClass {
        val downloaded = !isNotCompletelyDownloaded()
        if (downloaded) {
            return asDataClass()
        }

        // fetchPageList
        val mangaEntry = transaction { MangaTable.select { MangaTable.id eq mangaId }.first() }
        val source = getCatalogueSourceOrStub(mangaEntry[MangaTable.sourceReference])

        val pageListSrc = source.fetchPageList(
            SChapter.create().apply {
                url = chapterEntry[ChapterTable.url]
                name = chapterEntry[ChapterTable.name]
            }
        ).awaitSingle()
        val pageList = preprocessPageList(pageListSrc)

        val meta = GetCatalogueSource.getCatalogueSourceMeta(source)
        val support = supportDirect(source, meta, pageList)
        if (!support) {
            if (chapterEntry[ChapterTable.isDownloaded]) {
                markAsNotDownloaded()
            }
            updateDatabasePages(pageList)
            return asDataClass()
        }

        val chapter = asDataClass()
        chapter.pageData = pageList.associate {
            it.index to
                buildImgDataClass(url = it.imageUrl!!, headers = meta.headers)
        }
        chapter.pageCount = pageList.count()
        return chapter
    }

    private fun supportDirect(source: CatalogueSource, meta: SourceMeta, pageList: List<Page>): Boolean {
        if (source.id == LocalSource.ID) {
            return false
        }

        if (!sourceSupportDirect(meta)) {
            println(source.name + " not supportDirect")
            return false
        }

        val emptyUrl = pageList.any { it.imageUrl == null }
        if (emptyUrl) {
            println(source.name + " emptyUrl")
            return false
        }

        println(source.name + " support direct")
        return true
    }

    private fun asDataClass() = ChapterTable.toDataClass(chapterEntry)

    var chapterEntry: ResultRow = freshChapterEntry()

    private fun freshChapterEntry() = transaction {
        ChapterTable.select {
            (ChapterTable.sourceOrder eq chapterIndex) and (ChapterTable.manga eq mangaId)
        }.first()
    }

    private fun markAsNotDownloaded() {
        // chapter may be downloaded but if we are here, then images might be deleted and database data be false
        transaction {
            ChapterTable.update({ (ChapterTable.sourceOrder eq chapterIndex) and (ChapterTable.manga eq mangaId) }) {
                it[isDownloaded] = false
            }
        }
    }

    public fun updateLastReadAt() {
        // chapter may be downloaded but if we are here, then images might be deleted and database data be false
        transaction {
            ChapterTable.update({ (ChapterTable.sourceOrder eq chapterIndex) and (ChapterTable.manga eq mangaId) }) {
                it[lastReadAt] = Instant.now().epochSecond
            }
        }
    }

    private fun updateDatabasePages(pageList: List<Page>) {
        val chapterId = chapterEntry[ChapterTable.id].value
        val chapterIndex = chapterEntry[ChapterTable.sourceOrder]
        val mangaId = chapterEntry[ChapterTable.manga].value

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

        updatePageCount(pageList, mangaId, chapterIndex)

        // chapter was updated
        chapterEntry = freshChapterEntry()
    }

    private fun updatePageCount(
        pageList: List<Page>,
        mangaId: Int,
        chapterIndex: Int
    ) {
        val pageCount = pageList.count()

        transaction {
            ChapterTable.update({ (ChapterTable.manga eq mangaId) and (ChapterTable.sourceOrder eq chapterIndex) }) {
                it[ChapterTable.pageCount] = pageCount
            }
        }
    }

    private fun isNotCompletelyDownloaded(): Boolean {
        return !(
            chapterEntry[ChapterTable.isDownloaded] &&
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
