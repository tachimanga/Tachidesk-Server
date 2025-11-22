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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
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

        val pageEntry = transaction {
            PageTable.select { (PageTable.chapter eq chapterId) and (PageTable.index eq index) }.first()
        }
        val tachiyomiPage = Page(
            pageEntry[PageTable.index],
            pageEntry[PageTable.url],
            pageEntry[PageTable.imageUrl],
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
            try {
                source.getImage(tachiyomiPage)
            } catch (e: IllegalArgumentException) {
                // imageUrl = "" HttpUrl.kt:1366 throw IllegalArgumentException("Expected URL scheme 'http' or 'https' but no scheme was found for $truncated",)
                if (e.message != "Expected URL scheme 'http' or 'https' but no scheme was found for ") {
                    throw e
                }
                // return 1x1.png
                createEmptyImageResponse(e)
            }
        }
    }

    /** converts 0 to "001" */
    fun getPageName(index: Int): String {
        return String.format("%03d", index + 1)
    }

    private fun createEmptyImageResponse(e: Throwable) =
        Page::class.java.getResourceAsStream("/icon/1x1.png")
            ?.use { stream ->
                Response.Builder()
                    .request(Request.Builder().url("https://example.com/1x1.png").build())
                    .protocol(Protocol.HTTP_1_1)
                    .message("OK")
                    .code(200)
                    .body(stream.readBytes().toResponseBody("image/png".toMediaType()))
                    .build()
            } ?: throw e
}
