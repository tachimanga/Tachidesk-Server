package suwayomi.tachidesk.manga.impl.util

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.conf.global
import org.kodein.di.instance
import suwayomi.tachidesk.manga.impl.util.source.GetCatalogueSource
import suwayomi.tachidesk.manga.impl.util.storage.SafePath
import suwayomi.tachidesk.manga.model.table.ChapterTable
import suwayomi.tachidesk.manga.model.table.MangaTable
import suwayomi.tachidesk.server.ApplicationDirs

private val applicationDirs by DI.global.instance<ApplicationDirs>()

private fun getMangaDir(mangaId: Int): String {
    val mangaEntry = getMangaEntry(mangaId)
    val source = GetCatalogueSource.getCatalogueSourceOrStub(mangaEntry[MangaTable.sourceReference])

    val sourceDir = SafePath.buildValidFilename(source.toString())
    val mangaDir = SafePath.buildValidFilename(mangaEntry[MangaTable.title])
    return "$sourceDir/$mangaDir"
}

private fun getChapterDir(mangaId: Int, chapterId: Int): String {
    val chapterEntry = transaction { ChapterTable.select { ChapterTable.id eq chapterId }.first() }

    val chapterDir = SafePath.buildValidFilename(
        when {
            chapterEntry[ChapterTable.scanlator] != null -> "${chapterEntry[ChapterTable.scanlator]}_${chapterEntry[ChapterTable.name]}"
            else -> chapterEntry[ChapterTable.name]
        },
    )

    return getMangaDir(mangaId) + "/$chapterDir"
}

fun getChapterDownloadPath(mangaId: Int, chapterId: Int): String {
    return applicationDirs.mangaDownloadsRoot + "/" + getChapterDir(mangaId, chapterId)
}

fun getMangaDownloadPath(mangaId: Int): String {
    return applicationDirs.mangaDownloadsRoot + "/" + getMangaDir(mangaId)
}

fun getChapterCbzPath(mangaId: Int, chapterId: Int): String {
    return getChapterDownloadPath(mangaId, chapterId) + ".cbz"
}

private fun getMangaEntry(mangaId: Int): ResultRow {
    return transaction { MangaTable.select { MangaTable.id eq mangaId }.first() }
}
