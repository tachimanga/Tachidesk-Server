package suwayomi.tachidesk.cloud.util

/*
 * Copyright (C) 2024 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import java.util.*

object UUIDUtils {
    fun generateUUID(): String {
        return UUID.randomUUID().toString().replace("{", "").replace("}", "").replace("-", "")
    }
}
