package suwayomi.tachidesk.manga.impl.update

import suwayomi.tachidesk.manga.model.dataclass.MangaDataClass

enum class TaskType(val code: Int) {
    MANUAL(0),
    APP_START(1),
    BG_TASK(2),
    ;

    companion object {
        fun valueOf(type: Int?): TaskType? {
            return entries.find { it.code == type }
        }
    }
}

enum class TaskStatus(val code: Int) {
    INIT(0),
    RUNNING(1),
    FAIL(2),
    SUCC(3),
}

enum class TaskErrorCode {
    SYSTEM_ERROR,
    CANCEL,
}

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

data class UpdateTask(
    val startAt: Long,
    val type: TaskType,
    var recordId: Int? = null,
)

data class UpdateJob(
    val manga: MangaDataClass,
    val status: JobStatus = JobStatus.PENDING,
    val latestChapterInfo: LatestChapterInfo? = null,
    val failedInfo: FailedInfo? = null,
)

data class LatestChapterInfo(
    val newChapterCount: Int? = null,
    val latestChapterId: Int? = null,
    val latestChapterName: String? = null,
)

data class FailedInfo(
    val errorCode: JobErrorCode,
    val errorMessage: String? = null,
)
