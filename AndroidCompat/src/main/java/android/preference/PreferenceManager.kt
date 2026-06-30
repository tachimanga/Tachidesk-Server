package android.preference

/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) Contributors to the Suwayomi project
 * Copyright (C) 2025 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import android.content.Context

/**
 * Created by nulldev on 3/26/17.
 */

class PreferenceManager {
    companion object {
        @JvmStatic
        fun getDefaultSharedPreferences(context: Context) =
            context.getSharedPreferences(
                context.applicationInfo.packageName,
                Context.MODE_PRIVATE,
            )!!
    }
}
