package suwayomi.tachidesk.manga.impl

import com.jichao.tachiyomi.Profiler
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.transactions.transaction
import suwayomi.tachidesk.manga.model.dataclass.MangaDataClass
import suwayomi.tachidesk.manga.model.table.*

/**
 * @author mc
 */
object History {
    fun getHistoryMangaList(): List<MangaDataClass> {
        val lastChapterList = transaction {
            ChapterTable
                .select { (ChapterTable.lastReadAt greater 0) }
                .orderBy(ChapterTable.lastReadAt to SortOrder.DESC)
                .limit(1000)
                .map { ChapterTable.toDataClass(it) }
        }
        val lastChapterMap = lastChapterList.groupBy { it.mangaId }
            .mapValues { it.value.maxByOrNull { item -> item.lastReadAt } }
        val mangaIds = lastChapterMap.keys.toList()
        Profiler.split("mangaIds done")

        val mangaList = transaction {
            // Fetch data from the MangaTable and join with the CategoryMangaTable, if a category is specified
            MangaTable
                .select { (MangaTable.id inList mangaIds) }
                .map {
                    // Map the data from the result row to the MangaDataClass
                    val dataClass = MangaTable.toDataClass(it)
                    dataClass.lastReadAt = lastChapterMap[dataClass.id]?.lastReadAt
                    dataClass.lastChapterRead = lastChapterMap[dataClass.id]
                    dataClass
                }
        }
        Profiler.split("mangaList done")
        return mangaList.sortedByDescending { it.lastReadAt }
    }
}
