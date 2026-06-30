package eu.kanade.tachiyomi.source.local.loader

/*
 * Copyright (C) Contributors to the Suwayomi project
 * Copyright (C) 2023 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import eu.kanade.tachiyomi.util.lang.compareToCaseInsensitiveNaturalOrder
import suwayomi.tachidesk.manga.impl.util.storage.ImageUtil
import java.io.File
import java.util.zip.ZipFile

class ZipPageLoader(file: File) : PageLoader {
    /**
     * The zip file to load pages from.
     */
    private val zip = ZipFile(file)

    /**
     * Returns an observable containing the pages found on this zip archive ordered with a natural
     * comparator.
     */
    override fun getPages(): List<ReaderPage> {
        return zip.entries().toList()
            .filter {
                !it.isDirectory &&
                    !it.name.endsWith(".DS_Store") &&
                    !it.name.startsWith("__MACOSX/") &&
                    ImageUtil.isImage(it.name) { zip.getInputStream(it) }
            }
            .sortedWith { f1, f2 -> f1.name.compareToCaseInsensitiveNaturalOrder(f2.name) }
            .mapIndexed { i, entry ->
                val streamFn = { zip.getInputStream(entry) }
                ReaderPage(i).apply {
                    stream = streamFn
                }
            }
    }
}
