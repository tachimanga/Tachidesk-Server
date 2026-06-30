package eu.kanade.tachiyomi.util

/*
 * Copyright (C) 2024 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import eu.kanade.tachiyomi.data.backup.ProtoBackupImport
import eu.kanade.tachiyomi.data.backup.models.Backup
import kotlinx.serialization.ExperimentalSerializationApi
import okio.buffer
import okio.gzip
import okio.source
import java.io.InputStream

object BackupUtil {
    /**
     * Decode a potentially-gzipped backup.
     */
    @OptIn(ExperimentalSerializationApi::class)
    fun decodeBackup(sourceStream: InputStream): Backup {
        val backupStringSource = sourceStream.source().buffer()

        val peeked = backupStringSource.peek()
        peeked.require(2)
        val id1id2 = peeked.readShort()
        val backupString = if (id1id2.toInt() == 0x1f8b) { // 0x1f8b is gzip magic bytes
            backupStringSource.gzip().buffer()
        } else {
            backupStringSource
        }.use { it.readByteArray() }

        return ProtoBackupImport.parser.decodeFromByteArray(Backup.serializer(), backupString)
    }
}
