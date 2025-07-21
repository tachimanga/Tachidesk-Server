package suwayomi.tachidesk.manga.impl.util

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import mu.KotlinLogging
import org.kodein.di.DI
import org.kodein.di.conf.global
import org.kodein.di.instance
import suwayomi.tachidesk.server.ApplicationDirs

private val applicationDirs by DI.global.instance<ApplicationDirs>()
private val logger = KotlinLogging.logger {}

fun getChapterDownloadPath2(mangaId: Int, chapterId: Int): String {
    val hash = "00$mangaId".takeLast(2)
    return "${applicationDirs.mangaDownloadsRoot2}/$hash/$mangaId/$chapterId"
}

fun getMangaDownloadPath2(mangaId: Int): String {
    val hash = "00$mangaId".takeLast(2)
    return "${applicationDirs.mangaDownloadsRoot2}/$hash/$mangaId"
}
