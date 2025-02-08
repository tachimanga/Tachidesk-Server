package suwayomi.tachidesk.manga.model.table

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import app.tachimanga.cloud.enums.DataType
import app.tachimanga.cloud.model.dto.SyncCommitDTO
import app.tachimanga.cloud.model.dto.manga.RepoDTO
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

    // seconds
    val createAt = long("create_at").default(0)

    // seconds
    val updateAt = long("update_at").default(0)
    val dirty = bool("dirty").default(false)
    val commitId = long("commit_id").default(0)
}

fun RepoTable.toDataClass(repoEntry: ResultRow) = RepoDataClass(
    repoEntry[id].value,
    repoEntry[type],
    repoEntry[name],
    repoEntry[metaUrl],
    repoEntry[baseUrl],
)

fun RepoTable.toSyncData(entry: ResultRow): SyncCommitDTO {
    val sync = SyncCommitDTO()
    sync.clientDataId = entry[id].value
    sync.dataType = DataType.Repo.name
    val repo = RepoDTO()

    repo.clientCreatedAt = entry[createAt] * 1000
    repo.clientUpdatedAt = entry[updateAt] * 1000
    repo.clientDeleted = entry[deleted]

    repo.type = entry[type]
    repo.name = entry[name]
    repo.metaUrl = entry[metaUrl]
    repo.baseUrl = entry[baseUrl]
    repo.homepage = entry[homepage]
    sync.repo = repo
    return sync
}
