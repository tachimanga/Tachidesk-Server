package eu.kanade.tachiyomi.data.backup.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
data class BackupHistory(
    @ProtoNumber(1) var url: String, // chapter.url
    @ProtoNumber(2) var lastRead: Long, // milliseconds
    @ProtoNumber(3) var readDuration: Long = 0
)
