package suwayomi.tachidesk.manga.impl.download

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample
import mu.KotlinLogging
import org.kodein.di.DI
import org.kodein.di.conf.global
import org.kodein.di.instance
import suwayomi.tachidesk.manga.impl.Page
import suwayomi.tachidesk.manga.impl.download.model.DownloadChapter
import suwayomi.tachidesk.manga.impl.util.getChapterDownloadPath
import suwayomi.tachidesk.server.ApplicationDirs
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class FolderProvider2(val mangaId: Int, val chapterId: Int, private val originalChapterId: Int?) {
    private val realChapterId
        get() = originalChapterId ?: chapterId

    private val logger = KotlinLogging.logger {}

    private val applicationDirs by DI.global.instance<ApplicationDirs>()

    private fun getChapterDownloadPath2(mangaId: Int, chapterId: Int): String {
        val hash = "00$mangaId".takeLast(2)
        return "${applicationDirs.mangaDownloadsRoot2}/$hash/$mangaId/$chapterId"
    }

    fun getImage(index: Int): Pair<InputStream, String>? {
        val file = getImageFile(index)
        if (file != null) {
            return Pair(FileInputStream(file).buffered(), "image/jpeg")
        }
        return null
    }

    fun getImageFile(index: Int): File? {
        val v2 = getImageV2(index)
        if (v2 != null) {
            logger.info { "[DOWNLOAD] getImage hit v2" }
            return v2
        }
        val v1 = getImageV1(index)
        if (v1 != null) {
            logger.info { "[DOWNLOAD] getImage hit v1" }
            return v1
        }
        return null
    }

    private fun getImageV2(index: Int): File? {
        val chapterDir = getChapterDownloadPath2(mangaId, realChapterId)
        val path = "$chapterDir/$index"
        val file = File(path)
        val exist = file.exists()
        logger.info { "[DOWNLOAD] getImageV2 path:$path chapterId=$chapterId exist:$exist" }
        if (!exist) {
            return null
        }
        return file
    }

    private fun getImageV1(index: Int): File? {
        val chapterDir = getChapterDownloadPath(mangaId, chapterId)
        val folder = File(chapterDir)
        if (!folder.exists()) {
            return null
        }
        folder.mkdirs()
        val file = folder.listFiles()?.sortedBy { it.name }?.getOrNull(index)
        logger.info { "[DOWNLOAD] getImageV1 path:$chapterDir exist:${file != null}" }
        return file
    }

    @OptIn(FlowPreview::class)
    suspend fun download(
        download: DownloadChapter,
        scope: CoroutineScope,
        step: suspend (DownloadChapter?, Boolean) -> Unit
    ): Boolean {
        val pageCount = download.chapter.pageCount
        val chapterDir = getChapterDownloadPath2(mangaId, realChapterId)
        logger.info { "[DOWNLOAD] download chapterDir $chapterDir chapterId=$chapterId" }
        val folder = File(chapterDir)
        folder.mkdirs()

        for (pageNum in 0 until pageCount) {
            var pageProgressJob: Job? = null
            val filePath = "$chapterDir/$pageNum"
            val file = File(filePath)
            if (file.exists()) continue
            try {
                Page.getPageImage(
                    mangaId = download.mangaId,
                    chapterIndex = download.chapterIndex,
                    index = pageNum
                ) { flow ->
                    pageProgressJob = flow
                        .sample(100)
                        .distinctUntilChanged()
                        .onEach {
                            download.progress = (pageNum.toFloat() + (it.toFloat() * 0.01f)) / pageCount
                            step(null, false) // don't throw on canceled download here since we can't do anything
                        }
                        .launchIn(scope)
                }.first.use { image ->
                    image.use { input -> file.outputStream().use { output -> input.copyTo(output) } }
                }
            } finally {
                // always cancel the page progress job even if it throws an exception to avoid memory leaks
                pageProgressJob?.cancel()
            }
            // TODO: retry on error with 2,4,8 seconds of wait
            download.progress = ((pageNum + 1).toFloat()) / pageCount
            step(download, false)
        }
        return true
    }

    fun delete(): Boolean {
        // v1
        try {
            FolderProvider(mangaId, chapterId).delete()
        } catch (e: Exception) {
            logger.error("FolderProvider delete error", e)
        }

        // v2
        val chapterDir = getChapterDownloadPath2(mangaId, realChapterId)
        logger.info { "[DOWNLOAD] download delete $chapterDir chapterId=$chapterId" }
        return File(chapterDir).deleteRecursively()
    }
}
