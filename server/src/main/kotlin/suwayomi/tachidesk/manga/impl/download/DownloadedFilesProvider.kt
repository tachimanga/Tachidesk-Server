package suwayomi.tachidesk.manga.impl.download

/*
 * Copyright (C) Contributors to the Suwayomi project
 * Copyright (C) 2025 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import kotlinx.coroutines.CoroutineScope
import suwayomi.tachidesk.manga.impl.download.model.DownloadChapter
import java.io.InputStream

/*
* Base class for downloaded chapter files provider, example: Folder, Archive
* */
abstract class DownloadedFilesProvider(val mangaId: Int, val chapterId: Int) {
    abstract fun getImage(index: Int): Pair<InputStream, String>

    abstract suspend fun download(
        download: DownloadChapter,
        scope: CoroutineScope,
        step: suspend (DownloadChapter?, Boolean) -> Unit,
    ): Boolean

    abstract fun delete(): Boolean
}
