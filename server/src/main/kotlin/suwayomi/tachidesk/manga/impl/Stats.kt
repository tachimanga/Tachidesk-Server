package suwayomi.tachidesk.manga.impl

import mu.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.transactions.transaction
import suwayomi.tachidesk.manga.model.dataclass.ReadTimeDataClass
import suwayomi.tachidesk.manga.model.table.*
import java.text.SimpleDateFormat
import java.util.*

object Stats {
    private val logger = KotlinLogging.logger {}

    fun getReadTimeStats(): ReadTimeDataClass {
        val totalReadDuration = transaction {
            HistoryTable
                .slice(HistoryTable.readDuration.sum())
                .select { HistoryTable.isDelete eq false }
                .map { it[HistoryTable.readDuration.sum()] }
                .firstOrNull() ?: 0
        }

        val historyList = transaction {
            HistoryTable.slice(HistoryTable.mangaId, HistoryTable.readDuration)
                .select { (HistoryTable.isDelete eq false) and (HistoryTable.readDuration greater 60) }
                .orderBy(HistoryTable.readDuration to SortOrder.DESC)
                .limit(200)
                .toList()
        }

        val mangaIds = historyList.map { it[HistoryTable.mangaId] }.toList()
        val mangaList = transaction {
            MangaTable
                .select { (MangaTable.id inList mangaIds) }
                .map { MangaTable.toDataClass(it) }
        }
        val mangaMap = mangaList.associateBy { it.id }

        val list = historyList.mapNotNull {
            val manga = mangaMap[it[HistoryTable.mangaId]]
            if (manga != null) {
                manga.readDuration = it[HistoryTable.readDuration]
            }
            manga
        }
        return ReadTimeDataClass(totalReadDuration, list)
    }

    fun upsertStats(mangaId: Int, readDuration: Int) {
        val currentDate = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val currentDateAsInt = currentDate.toInt()
        val now = System.currentTimeMillis()

        transaction {
            val stats =
                StatsTable.slice(StatsTable.id)
                    .select { (StatsTable.mangaId eq mangaId) and (StatsTable.day eq currentDateAsInt) }
                    .firstOrNull()
            if (stats != null) {
                StatsTable.update({ StatsTable.id eq stats[StatsTable.id] }) {
                    it[StatsTable.updateAt] = now
                    it[StatsTable.isDelete] = false
                    it[StatsTable.dirty] = true
                    it[StatsTable.readDuration] = StatsTable.readDuration + readDuration
                }
            } else {
                StatsTable.insert {
                    it[StatsTable.createAt] = now
                    it[StatsTable.updateAt] = now
                    it[StatsTable.isDelete] = false
                    it[StatsTable.dirty] = true
                    it[StatsTable.day] = currentDateAsInt
                    it[StatsTable.mangaId] = mangaId
                    it[StatsTable.readDuration] = readDuration
                }
            }
        }
    }
}
