package eu.kanade.tachiyomi

/*
 * Copyright (C) Contributors to the Suwayomi project
 * Copyright (C) 2025 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import suwayomi.tachidesk.manga.impl.util.storage.ImageUtil

/**
 * Used by extensions.
 *
 * @since extension-lib 1.3
 */
object AppInfo {
    /** should be something like 74 */
    fun getVersionCode() = suwayomi.tachidesk.server.BuildConfig.REVISION.substring(1).toInt()

    /** should be something like "0.13.1" */
    fun getVersionName() = suwayomi.tachidesk.server.BuildConfig.VERSION.substring(1)

    /**
     * A list of supported image MIME types by the reader.
     * e.g. ["image/jpeg", "image/png", ...]
     *
     * @since extension-lib 1.5
     */
    fun getSupportedImageMimeTypes(): List<String> = ImageUtil.ImageType.entries.map { it.mime }
}
