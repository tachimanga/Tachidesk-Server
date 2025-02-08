package suwayomi.tachidesk.cloud.model.table

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import org.jetbrains.exposed.dao.id.IntIdTable

object ChapterSyncTable : IntIdTable() {
    val url = varchar("url", 2048)
    val mangaId = integer("manga_id")
    val name = varchar("name", 512).nullable()

    val isRead = bool("read").default(false)
    val isBookmarked = bool("bookmark").default(false)
    val lastPageRead = integer("last_page_read").default(0)
    val lastReadAt = long("last_read_at").default(0)

    val createAt = long("create_at").default(0)
    val updateAt = long("update_at").default(0)
    val dirty = bool("dirty").default(false)
    val commitId = long("commit_id").default(0)
}
