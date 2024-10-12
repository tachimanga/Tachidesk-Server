package eu.kanade.tachiyomi.source.local.loader

import com.github.junrar.Archive
import com.github.junrar.exception.RarException
import com.github.junrar.rarfile.FileHeader
import eu.kanade.tachiyomi.util.lang.compareToCaseInsensitiveNaturalOrder
import suwayomi.tachidesk.manga.impl.util.storage.ImageUtil
import java.io.*
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.math.min

/**
 * Loader used to load a chapter from a .rar or .cbr file.
 */
class RarPageLoader(file: File) : PageLoader {

    /**
     * The rar archive to load pages from.
     */
    private val archive = Archive(file)

    private val executor = Executors.newSingleThreadExecutor()

    /**
     * Returns an observable containing the pages found on this rar archive ordered with a natural
     * comparator.
     */
    override fun getPages(): List<ReaderPage> {
        if (archive.isPasswordProtected) throw RuntimeException("Encrypted RAR archives are not supported")
        if (archive.mainHeader.isSolid) throw RuntimeException("Solid RAR archives are not supported")
        if (archive.mainHeader.isMultiVolume) throw RuntimeException("Multi-Volume RAR archives are not supported")
        return archive.fileHeaders
            .filter { !it.isDirectory && ImageUtil.isImage(it.fileName) { archive.getInputStream(it) } }
            .sortedWith { f1, f2 -> f1.fileName.compareToCaseInsensitiveNaturalOrder(f2.fileName) }
            .mapIndexed { i, header ->
                ReaderPage(i).apply {
                    stream = { getInputStream(archive, header) }
                }
            }
    }

    private fun getInputStream(rar: Archive, hd: FileHeader): InputStream {
        // If the file is empty, return an empty InputStream
        // This saves adding a task on the executor that will effectively do nothing
        if (hd.fullUnpackSize <= 0) {
            return EmptyInputStream()
        }

        // Small optimization to prevent the creation of large buffers for very small files
        // Never allocate more than needed, but ensure the buffer will be at least 1-byte long
        val bufferSize = max(min(hd.fullUnpackSize.toDouble(), 32 * 1024.0), 1.0).toInt()
        val input = PipedInputStream(bufferSize)
        val out = PipedOutputStream(input)

        // Data will be available in another InputStream, connected to the OutputStream
        // Delegates execution to the cached executor service.
        executor.submit {
            try {
                rar.extractFile(hd, out)
            } catch (ignored: RarException) {
            } finally {
                try {
                    out.close()
                } catch (ignored: IOException) {
                }
            }
        }
        return input
    }

    private class EmptyInputStream : InputStream() {
        override fun available(): Int {
            return 0
        }

        override fun read(): Int {
            return -1
        }
    }
}
