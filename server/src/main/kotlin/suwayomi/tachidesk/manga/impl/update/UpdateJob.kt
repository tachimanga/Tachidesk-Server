package suwayomi.tachidesk.manga.impl.update

import suwayomi.tachidesk.manga.model.dataclass.MangaDataClass

enum class JobStatus {
    PENDING,
    RUNNING,
    COMPLETE,
    FAILED,
}

enum class JobErrorCode {
    UPDATE_FAILED,
    FILTERED_BY_EXCLUDE_CATEGORY,
    FILTERED_BY_UPDATE_STRATEGY,
    FILTERED_BY_MANGA_STATUS,
    FILTERED_BY_UNREAD,
    FILTERED_BY_NOT_STARTED,
}

data class UpdateJob(
    val manga: MangaDataClass,
    val status: JobStatus = JobStatus.PENDING,
    val failedInfo: FailedInfo? = null,
)

data class FailedInfo(
    val errorCode: JobErrorCode,
    val errorMessage: String? = null,
)
