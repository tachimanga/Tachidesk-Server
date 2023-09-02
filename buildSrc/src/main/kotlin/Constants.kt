import java.io.BufferedReader

/*
 * Copyright (C) Contributors to the Suwayomi project
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

const val MainClass = "suwayomi.tachidesk.MainKt"

// should be bumped with each stable release
val tachideskVersion = System.getenv("ProductVersion") ?: "v0.7.0"

val webUIRevisionTag = System.getenv("WebUIRevision") ?: "r983"

// counts commits on the master branch
val tachideskRevision = "r0"

