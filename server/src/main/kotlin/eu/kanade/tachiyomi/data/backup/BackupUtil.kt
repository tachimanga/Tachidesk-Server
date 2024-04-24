package eu.kanade.tachiyomi.util

import eu.kanade.tachiyomi.data.backup.ProtoBackupImport
import eu.kanade.tachiyomi.data.backup.models.Backup
import eu.kanade.tachiyomi.data.backup.models.BackupSerializer
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

        return ProtoBackupImport.parser.decodeFromByteArray(BackupSerializer, backupString)
    }
}
