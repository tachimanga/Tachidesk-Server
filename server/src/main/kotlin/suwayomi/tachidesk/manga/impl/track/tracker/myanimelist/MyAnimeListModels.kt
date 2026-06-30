package suwayomi.tachidesk.manga.impl.track.tracker.myanimelist

/*
 * Copyright (C) 2023 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import kotlinx.serialization.Serializable
import suwayomi.tachidesk.manga.impl.track.tracker.model.Track

@Serializable
data class OAuth(
    val refresh_token: String,
    val access_token: String,
    val token_type: String,
    val created_at: Long = System.currentTimeMillis(),
    val expires_in: Long,
)

fun OAuth.isExpired() = System.currentTimeMillis() > created_at + (expires_in * 1000)

fun Track.toMyAnimeListStatus() = when (status) {
    MyAnimeList.READING -> "reading"
    MyAnimeList.COMPLETED -> "completed"
    MyAnimeList.ON_HOLD -> "on_hold"
    MyAnimeList.DROPPED -> "dropped"
    MyAnimeList.PLAN_TO_READ -> "plan_to_read"
    MyAnimeList.REREADING -> "reading"
    else -> null
}

fun getStatus(status: String?) = when (status) {
    "reading" -> MyAnimeList.READING
    "completed" -> MyAnimeList.COMPLETED
    "on_hold" -> MyAnimeList.ON_HOLD
    "dropped" -> MyAnimeList.DROPPED
    "plan_to_read" -> MyAnimeList.PLAN_TO_READ
    else -> MyAnimeList.READING
}
