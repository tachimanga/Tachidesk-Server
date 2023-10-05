package eu.kanade.tachiyomi.source

import java.util.Collections

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

fun sourceSupportDirect(meta: SourceMeta?): Boolean {
    if (SourceSetting.ENABLE_FLUTTER_DIRECT == false) {
        return false
    }
    return meta != null && meta.simpleClient && meta.simpleRequest
}

data class SourceMeta(
    var simpleClient: Boolean = false,
    var simpleRequest: Boolean = false,
    var headers: Map<String, String> = Collections.emptyMap()
)

class SourceSetting {
    companion object {
        var ENABLE_FLUTTER_DIRECT: Boolean? = null
    }
}
