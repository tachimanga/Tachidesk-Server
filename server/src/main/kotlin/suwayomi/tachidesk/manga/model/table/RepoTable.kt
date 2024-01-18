package suwayomi.tachidesk.manga.model.table

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import suwayomi.tachidesk.manga.model.dataclass.RepoDataClass

object RepoTable : IntIdTable() {
    val type = integer("type").default(0)
    val name = varchar("name", 1024)
    val metaUrl = varchar("meta_url", 2048)
    val baseUrl = varchar("base_url", 2048)
    val homepage = varchar("homepage", 2048).nullable()
    val deleted = bool("deleted").default(false)
    val createAt = long("create_at").default(0)
    val updateAt = long("update_at").default(0)
}

fun RepoTable.toDataClass(repoEntry: ResultRow) = RepoDataClass(
    repoEntry[id].value,
    repoEntry[type],
    repoEntry[name],
    repoEntry[metaUrl],
    repoEntry[baseUrl]
)
