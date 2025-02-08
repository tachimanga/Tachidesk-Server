package eu.kanade.tachiyomi.data.backup.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
data class BackupTracking(
    // in 1.x some of these values have different types or names
    // syncId is called siteId in 1,x
    @ProtoNumber(1) var syncId: Int,
    // LibraryId is not null in 1.x
    @ProtoNumber(2) var libraryId: Long,
    // trackingUrl is called mediaUrl in 1.x
    @ProtoNumber(4) var trackingUrl: String = "",
    @ProtoNumber(5) var title: String = "",
    // lastChapterRead is called last read, and it has been changed to a float in 1.x
    @ProtoNumber(6) var lastChapterRead: Float = 0F,
    @ProtoNumber(7) var totalChapters: Int = 0,
    @ProtoNumber(8) var score: Float = 0F,
    @ProtoNumber(9) var status: Int = 0,
    // startedReadingDate is called startReadTime in 1.x
    @ProtoNumber(10) var startedReadingDate: Long = 0,
    // finishedReadingDate is called endReadTime in 1.x
    @ProtoNumber(11) var finishedReadingDate: Long = 0,
    @ProtoNumber(100) var mediaId: Long = 0,
)
