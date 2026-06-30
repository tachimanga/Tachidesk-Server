package eu.kanade.tachiyomi.source.model

/*
 * Copyright (C) Contributors to the Suwayomi project
 * Copyright (C) 2026 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import kotlinx.serialization.json.JsonObject

class SMangaImpl : SManga {

    override lateinit var url: String

    override lateinit var title: String

    override var thumbnail_url: String? = null

    override var artist: String? = null

    override var author: String? = null

    override var status: Int = 0

    override var description: String? = null

    override var genre: String? = null

    override var update_strategy: UpdateStrategy = UpdateStrategy.ALWAYS_UPDATE

    override var initialized: Boolean = false

    override var memo: JsonObject = JsonObject(emptyMap())
}
