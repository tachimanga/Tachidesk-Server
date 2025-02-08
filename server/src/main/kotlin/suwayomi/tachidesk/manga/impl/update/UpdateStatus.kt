package suwayomi.tachidesk.manga.impl.update

import suwayomi.tachidesk.manga.model.dataclass.MangaDataClass

data class UpdateStatus(
    val statusMap: Map<JobStatus, List<MangaDataClass>> = emptyMap(),
    val running: Boolean = false,
    val numberOfJobs: Int = 0,
    val completeTimestamp: Int = 0,
) {

    constructor(jobs: Collection<UpdateJob>, running: Boolean) : this(
        statusMap = jobs.groupBy { it.status }
            .mapValues { entry ->
                entry.value.map { it.manga }
            },
        running = running,
        numberOfJobs = jobs.size,
        completeTimestamp = if (running) 0 else (System.currentTimeMillis() / 1000).toInt(),
    )
}
