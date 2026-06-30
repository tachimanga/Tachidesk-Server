package suwayomi.tachidesk.manga.impl.track.tracker.model

/*
 * Copyright (C) 2023 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import java.io.Serializable

interface Track : Serializable {

    var id: Long?

    var manga_id: Long

    var sync_id: Int

    var media_id: Long

    var library_id: Long?

    var title: String

    var last_chapter_read: Float

    var total_chapters: Int

    var score: Float

    var status: Int

    var started_reading_date: Long

    var finished_reading_date: Long

    var started_reading_date_str: String?

    var finished_reading_date_str: String?

    var tracking_url: String

    fun copyPersonalFrom(other: Track) {
        last_chapter_read = other.last_chapter_read
        score = other.score
        status = other.status
        started_reading_date = other.started_reading_date
        finished_reading_date = other.finished_reading_date
    }

    companion object {
        fun create(serviceId: Long): Track = TrackImpl().apply {
            sync_id = serviceId.toInt()
        }
    }
}
