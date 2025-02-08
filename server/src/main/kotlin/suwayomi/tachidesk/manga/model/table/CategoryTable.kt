package suwayomi.tachidesk.manga.model.table

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import app.tachimanga.cloud.enums.DataType
import app.tachimanga.cloud.model.dto.SyncCommitDTO
import app.tachimanga.cloud.model.dto.manga.CategoryDTO
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import suwayomi.tachidesk.manga.impl.Category
import suwayomi.tachidesk.manga.model.dataclass.CategoryDataClass

object CategoryTable : IntIdTable() {
    val name = varchar("name", 64)
    val order = integer("order").default(0)
    val isDefault = bool("is_default").default(false)
    val uuid = varchar("uuid", 128)
    val createAt = long("create_at").default(0)
    val updateAt = long("update_at").default(0)
    val isDelete = bool("is_delete").default(false)
    val dirty = bool("dirty").default(false)
    val commitId = long("commit_id").default(0)
}

fun CategoryTable.toDataClass(categoryEntry: ResultRow) = CategoryDataClass(
    categoryEntry[id].value,
    categoryEntry[order],
    categoryEntry[name],
    categoryEntry[isDefault],
)

fun CategoryTable.toSyncData(entry: ResultRow): SyncCommitDTO {
    val sync = SyncCommitDTO()
    sync.clientDataId = entry[id].value
    sync.dataType = DataType.Category.name
    val category = CategoryDTO()

    category.clientCreatedAt = entry[CategoryTable.createAt]
    category.clientUpdatedAt = entry[CategoryTable.updateAt]
    category.clientDeleted = entry[CategoryTable.isDelete]

    category.uuid = entry[CategoryTable.uuid]
    category.name = entry[CategoryTable.name]
    category.order = entry[CategoryTable.order]
    category.isDefault = entry[CategoryTable.isDefault]

    sync.category = category
    return sync
}
