package suwayomi.tachidesk.manga.impl

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import eu.kanade.tachiyomi.source.local.LocalSource
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.online.HttpSource
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import suwayomi.tachidesk.manga.impl.download.FolderProvider2
import suwayomi.tachidesk.manga.impl.util.lang.awaitSingle
import suwayomi.tachidesk.manga.impl.util.source.GetCatalogueSource.getCatalogueSourceOrStub
import suwayomi.tachidesk.manga.impl.util.storage.ImageResponse.buildImageResponse
import suwayomi.tachidesk.manga.model.table.ChapterTable
import suwayomi.tachidesk.manga.model.table.MangaTable
import suwayomi.tachidesk.manga.model.table.PageTable
import java.io.InputStream

object Page {
    /**
     * A page might have a imageUrl ready from the get go, or we might need to
     * go an extra step and call fetchImageUrl to get it.
     */
    suspend fun getTrueImageUrl(page: Page, source: HttpSource): String {
        if (page.imageUrl == null) {
            page.imageUrl = source.fetchImageUrl(page).awaitSingle()
        }
        return page.imageUrl!!
    }

    suspend fun getPageImage(mangaId: Int, chapterIndex: Int, index: Int, progressFlow: ((StateFlow<Int>) -> Unit)? = null): Pair<InputStream, String> {
        val mangaEntry = transaction { MangaTable.select { MangaTable.id eq mangaId }.first() }
        val source = getCatalogueSourceOrStub(mangaEntry[MangaTable.sourceReference])
        val chapterEntry = transaction {
            ChapterTable.select {
                (ChapterTable.sourceOrder eq chapterIndex) and (ChapterTable.manga eq mangaId)
            }.first()
        }
        val chapterId = chapterEntry[ChapterTable.id].value

        if (chapterEntry[ChapterTable.isDownloaded] && chapterEntry[ChapterTable.pageCount] > 0) {
            val originalChapterId = chapterEntry[ChapterTable.originalChapterId]
            val pair = FolderProvider2(mangaId, chapterId, originalChapterId).getImage(index)
            if (pair != null) {
                return pair
            }
        }
        // TODO: why not select by index?
        val pageEntry =
            transaction {
                PageTable.select { (PageTable.chapter eq chapterId) }
                    .orderBy(PageTable.index to SortOrder.ASC)
                    .limit(1, index.toLong()).first()
            }
        val tachiyomiPage = Page(
            pageEntry[PageTable.index],
            pageEntry[PageTable.url],
            pageEntry[PageTable.imageUrl]
        )
        progressFlow?.invoke(tachiyomiPage.progress)

        // we treat Local source differently
        if (source.id == LocalSource.ID) {
            return LocalSource.getPageImage(chapterEntry[ChapterTable.url], tachiyomiPage.imageUrl, index)
        }

        source as HttpSource

        if (pageEntry[PageTable.imageUrl] == null) {
            val trueImageUrl = getTrueImageUrl(tachiyomiPage, source)
            transaction {
                PageTable.update({ (PageTable.chapter eq chapterId) and (PageTable.index eq index) }) {
                    it[imageUrl] = trueImageUrl
                }
            }
        }

        return buildImageResponse {
            source.fetchImage(tachiyomiPage).awaitSingle()
        }
    }

    /** converts 0 to "001" */
    fun getPageName(index: Int): String {
        return String.format("%03d", index + 1)
    }
}
