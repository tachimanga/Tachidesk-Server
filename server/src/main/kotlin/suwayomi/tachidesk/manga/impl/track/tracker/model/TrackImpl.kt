package suwayomi.tachidesk.manga.impl.track.tracker.model

/*
 * Copyright (C) 2023 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

class TrackImpl : Track {

    override var id: Long? = null

    override var manga_id: Long = 0

    override var sync_id: Int = 0

    override var media_id: Long = 0

    override var library_id: Long? = null

    override lateinit var title: String

    override var last_chapter_read: Float = 0F

    override var total_chapters: Int = 0

    override var score: Float = 0f

    override var status: Int = 0

    override var started_reading_date: Long = 0

    override var finished_reading_date: Long = 0

    override var started_reading_date_str: String? = null

    override var finished_reading_date_str: String? = null

    override var tracking_url: String = ""
}
