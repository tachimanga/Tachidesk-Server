package suwayomi.tachidesk.manga.impl.update

import kotlinx.coroutines.flow.StateFlow
import suwayomi.tachidesk.manga.model.dataclass.MangaDataClass
import suwayomi.tachidesk.manga.model.dataclass.update.BgUpdateResultDataClass

interface IUpdater {
    fun addMangaToQueue(manga: MangaDataClass)
    fun addMangaListToQueue(mangaList: List<MangaDataClass>)
    fun addMangaToTracker(job: UpdateJob)

    val status: StateFlow<UpdateStatus>
    fun reset(task: UpdateTask?)
    fun updateStatus(running: Boolean?)
    fun getQueueStatus(): Pair<Int, Int>
    fun fetchAllJobs(): List<UpdateJob>
    fun fetchUpdateResult(): BgUpdateResultDataClass
}
