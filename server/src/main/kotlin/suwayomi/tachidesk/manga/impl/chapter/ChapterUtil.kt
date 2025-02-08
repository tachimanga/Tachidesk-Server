package suwayomi.tachidesk.manga.impl.chapter

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import eu.kanade.tachiyomi.source.model.Page
import mu.KotlinLogging
import okhttp3.internal.trimSubstring
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.statements.jdbc.JdbcConnectionImpl
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import suwayomi.tachidesk.manga.impl.download.FolderProvider2
import suwayomi.tachidesk.manga.model.table.ChapterTable
import suwayomi.tachidesk.manga.model.table.PageTable
import suwayomi.tachidesk.server.database.MyBatchInsertStatement

object ChapterUtil {
    private val logger = KotlinLogging.logger {}

    fun updateDatabasePages(chapterId: Int, pageList: List<Page>) {
        if (pageList.isEmpty()) {
            logger.warn { "updateDatabasePages pageList is empty" }
            return
        }
        transaction {
            PageTable.deleteWhere { PageTable.chapter eq chapterId }

            val myBatchInsertStatement = MyBatchInsertStatement(PageTable)
            pageList.forEach { page ->
                val my = myBatchInsertStatement
                my.addBatch()

                my[PageTable.index] = page.index
                my[PageTable.url] = page.url
                my[PageTable.imageUrl] = page.imageUrl
                my[PageTable.chapter] = chapterId
            }

            val sql = myBatchInsertStatement.prepareSQL(this)
            val conn = (TransactionManager.current().connection as JdbcConnectionImpl).connection
            val statement = conn.createStatement()
            statement.execute(sql)
        }
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

    fun isCompletelyDownloaded(mangaId: Int, chapterEntry: ResultRow): Boolean {
        val isDownloaded = chapterEntry[ChapterTable.isDownloaded]
        val pageCount = chapterEntry[ChapterTable.pageCount]
        val ret = (isDownloaded && pageCount > 0 && firstPageExists(mangaId, chapterEntry))
        logger.info { "[DOWNLOAD]isCompletelyDownloaded=$ret isDownloaded=$isDownloaded pageCount=$pageCount" }
        return ret
    }

    private fun firstPageExists(mangaId: Int, chapterEntry: ResultRow): Boolean {
        val chapterId = chapterEntry[ChapterTable.id].value
        val originalChapterId = chapterEntry[ChapterTable.originalChapterId]
        val imageFile = FolderProvider2(mangaId, chapterId, originalChapterId).getImageFile(0)
        val ret = imageFile != null
        logger.info { "[DOWNLOAD]firstPageExists=$ret" }
        return ret
    }
}
