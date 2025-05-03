package suwayomi.tachidesk.manga.impl

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import eu.kanade.tachiyomi.source.online.HttpSource
import kotlinx.serialization.Serializable
import mu.KotlinLogging
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import suwayomi.tachidesk.manga.impl.util.source.GetCatalogueSource
import suwayomi.tachidesk.manga.model.table.*

object Browse {
    private val logger = KotlinLogging.logger {}

    fun fetchUrl(input: UrlFetchInput): UrlFetchOutput {
        logger.info { "fetchUrl $input" }
        val output = when {
            input.type == UrlFetchType.source.name && input.sourceId != null ->
                fetchSourceUrl(input.sourceId)
            input.type == UrlFetchType.manga.name && input.mangaId != null ->
                fetchMangaUrl(input.mangaId)
            input.type == UrlFetchType.chapter.name && input.chapterId != null ->
                fetchChapterUrlById(input.chapterId)
            input.type == UrlFetchType.chapter.name && input.mangaId != null && input.chapterIndex != null ->
                fetchChapterUrlByIndex(input.mangaId, input.chapterIndex)
            else -> UrlFetchOutput()
        }
        logger.info { "fetchUrl $input, output:$output" }
        return output
    }

    private fun fetchSourceUrl(sourceId: Long): UrlFetchOutput {
        val source = Source.getSource(sourceId)
        val userAgent = fetchUserAgent(sourceId)
        return UrlFetchOutput(url = source?.baseUrl, userAgent = userAgent)
    }

    private fun fetchMangaUrl(mangaId: Int): UrlFetchOutput {
        val mangaEntry = transaction { MangaTable.select { MangaTable.id eq mangaId }.firstOrNull() } ?: return UrlFetchOutput()
        val fallback = UrlFetchOutput(url = mangaEntry[MangaTable.realUrl], userAgent = HttpSource.DEFAULT_USER_AGENT)
        val source = GetCatalogueSource.getCatalogueSourceOrNull(mangaEntry[MangaTable.sourceReference]) as? HttpSource ?: return fallback
        val realUrl = runCatching {
            source.getMangaUrl(MangaTable.toSManga(mangaEntry))
        }.getOrNull()
        if (realUrl != null && realUrl != mangaEntry[MangaTable.realUrl]) {
            transaction {
                MangaTable.update({ (MangaTable.id eq mangaId) }) {
                    it[MangaTable.realUrl] = realUrl
                }
            }
        }
        val userAgent = fetchUserAgent(source.id)
        return UrlFetchOutput(url = realUrl ?: mangaEntry[MangaTable.realUrl], userAgent = userAgent)
    }

    private fun fetchChapterUrl(chapterEntry: ResultRow): UrlFetchOutput {
        val fallback = UrlFetchOutput(url = chapterEntry[ChapterTable.realUrl], userAgent = HttpSource.DEFAULT_USER_AGENT)
        val mangaEntry = transaction { MangaTable.select { MangaTable.id eq chapterEntry[ChapterTable.manga].value }.firstOrNull() } ?: return fallback
        val source = GetCatalogueSource.getCatalogueSourceOrNull(mangaEntry[MangaTable.sourceReference]) as? HttpSource ?: return fallback
        val realUrl = runCatching {
            source.getChapterUrl(ChapterTable.toSChapter(chapterEntry))
        }.getOrNull()
        if (realUrl != null && realUrl != chapterEntry[ChapterTable.realUrl]) {
            transaction {
                ChapterTable.update({ (ChapterTable.id eq chapterEntry[ChapterTable.id].value) }) {
                    it[ChapterTable.realUrl] = realUrl
                }
            }
        }
        val userAgent = fetchUserAgent(source.id)
        return UrlFetchOutput(url = realUrl ?: chapterEntry[ChapterTable.realUrl], userAgent = userAgent)
    }

    private fun fetchChapterUrlById(chapterId: Int): UrlFetchOutput {
        val chapterEntry = transaction {
            ChapterTable.select { ChapterTable.id eq chapterId }.firstOrNull()
        } ?: return UrlFetchOutput()
        return fetchChapterUrl(chapterEntry)
    }

    private fun fetchChapterUrlByIndex(mangaId: Int, chapterIndex: Int): UrlFetchOutput {
        val chapterEntry = transaction {
            ChapterTable.select { (ChapterTable.sourceOrder eq chapterIndex) and (ChapterTable.manga eq mangaId) }.firstOrNull()
        } ?: return UrlFetchOutput()
        return fetchChapterUrl(chapterEntry)
    }

    private fun fetchUserAgent(sourceId: Long): String {
        val latestUserAgent = GetCatalogueSource.queryLatestUserAgent(sourceId)
        logger.info { "[UA]fetchUserAgent latestUserAgent $latestUserAgent" }
        if (latestUserAgent != null) {
            return latestUserAgent
        }
        val source = GetCatalogueSource.getCatalogueSourceOrNull(sourceId)
        if (source is HttpSource) {
            val headersUserAgent = source.headers["User-Agent"]
            logger.info { "[UA]fetchUserAgent headersUserAgent $headersUserAgent" }
            if (headersUserAgent != null) {
                return headersUserAgent
            }
        }
        logger.info { "[UA]fetchUserAgent default ${HttpSource.DEFAULT_USER_AGENT}" }
        return HttpSource.DEFAULT_USER_AGENT
    }

    @Serializable
    data class UrlFetchInput(
        val type: String? = null,
        val sourceId: Long? = null,
        val mangaId: Int? = null,
        val chapterId: Int? = null,
        val chapterIndex: Int? = null,
    )

    @Serializable
    data class UrlFetchOutput(
        val url: String? = null,
        val userAgent: String? = null,
    )

    enum class UrlFetchType {
        source,
        manga,
        chapter,
    }
}
