package suwayomi.tachidesk.manga.model.table

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import org.jetbrains.exposed.dao.id.IntIdTable

object UpdateRecordTable : IntIdTable() {
    val createAt = long("create_at").default(0)
    val updateAt = long("update_at").default(0)
    val finishAt = long("finish_at").default(0)

    // 0 MANUAL 1 APP_START 2 BG_TASK
    val type = integer("type").default(0)

    // 0 INIT 1 RUNNING 2 FAIL 3 SUCC
    val status = integer("status").default(0)
    val errCode = varchar("err_code", 32).nullable()
    val errMsg = varchar("err_msg", 256).nullable()

    // total num of jobs
    val totalCount = integer("total_count").default(0)
    val succCount = integer("succ_count").default(0)
    val failedCount = integer("failed_count").default(0)
    val skipCount = integer("skip_count").default(0)
    val newChapterCount = integer("new_chapter_count").default(0)
}
