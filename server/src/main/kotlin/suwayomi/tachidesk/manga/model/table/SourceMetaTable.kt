package suwayomi.tachidesk.manga.model.table

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import org.jetbrains.exposed.dao.id.IntIdTable

object SourceMetaTable : IntIdTable() {
    val createAt = long("create_at").default(0)
    val updateAt = long("update_at").default(0)
    val isDelete = bool("is_delete").default(false)
    val dirty = bool("dirty").default(false)
    val commitId = long("commit_id").default(0)

    val sourceId = long("source_id")
    val key = varchar("key", 256)
    val value = varchar("value", 4096)
}
