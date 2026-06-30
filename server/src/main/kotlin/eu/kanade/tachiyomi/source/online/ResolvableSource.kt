package eu.kanade.tachiyomi.source.online

/*
 * Copyright © 2015 Javier Tomás
 * Copyright © 2024 Mihon Open Source Project
 * Copyright (C) 2026 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga

/**
 * A source that may handle opening an SManga or SChapter for a given URI.
 *
 * @since extensions-lib 1.5
 */
interface ResolvableSource : Source {

    /**
     * Returns what the given URI may open.
     * Returns [UriType.Unknown] if the source is not able to resolve the URI.
     *
     * @since extensions-lib 1.5
     */
    fun getUriType(uri: String): UriType

    /**
     * Called if [getUriType] is [UriType.Manga].
     * Returns the corresponding SManga, if possible.
     *
     * @since extensions-lib 1.5
     */
    suspend fun getManga(uri: String): SManga?

    /**
     * Called if [getUriType] is [UriType.Chapter].
     * Returns the corresponding SChapter, if possible.
     *
     * @since extensions-lib 1.5
     */
    suspend fun getChapter(uri: String): SChapter?
}

sealed interface UriType {
    data object Manga : UriType
    data object Chapter : UriType
    data object Unknown : UriType
}
