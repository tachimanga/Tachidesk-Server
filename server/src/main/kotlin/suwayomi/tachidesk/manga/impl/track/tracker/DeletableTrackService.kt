package suwayomi.tachidesk.manga.impl.track.tracker

/*
 * Copyright (C) 2023 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import suwayomi.tachidesk.manga.impl.track.tracker.model.Track

/**
 * For track services api that support deleting a manga entry for a user's list
 */
interface DeletableTrackService {

    suspend fun delete(track: Track): Track
}
