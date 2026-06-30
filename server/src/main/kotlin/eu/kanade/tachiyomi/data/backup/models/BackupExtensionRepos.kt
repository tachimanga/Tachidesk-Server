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
class BackupExtensionRepos(
    // https://raw.githubusercontent.com/xyz/extensions/repo
    @ProtoNumber(1) var baseUrl: String,
    // xyz
    @ProtoNumber(2) var name: String,
    @ProtoNumber(3) var shortName: String?,
    @ProtoNumber(4) var website: String,
    @ProtoNumber(5) var signingKeyFingerprint: String,
)
