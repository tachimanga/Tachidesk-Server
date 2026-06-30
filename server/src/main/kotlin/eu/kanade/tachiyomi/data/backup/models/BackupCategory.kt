package eu.kanade.tachiyomi.data.backup.models

/*
 * Copyright © 2015 Javier Tomás
 * Copyright © 2024 Mihon Open Source Project
 * Copyright (C) 2023 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
class BackupCategory(
    @ProtoNumber(1) var name: String,
    @ProtoNumber(2) var order: Long = 0,
    // @ProtoNumber(3) val updateInterval: Int = 0, 1.x value not used in 0.x
    @ProtoNumber(100) var flags: Long = 0,
) {
    override fun toString(): String {
        return "BackupCategory(name='$name', order=$order)"
    }
}
