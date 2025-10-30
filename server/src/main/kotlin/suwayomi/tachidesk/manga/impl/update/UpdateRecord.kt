package suwayomi.tachidesk.manga.impl.update

import mu.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.conf.global
import org.kodein.di.instance
import suwayomi.tachidesk.manga.model.dataclass.update.UpdateRecordDataClass
import suwayomi.tachidesk.manga.model.table.UpdateRecordTable

object UpdateRecord {
    private val logger = KotlinLogging.logger {}
    private val updater by DI.global.instance<IUpdater>()

    fun queryUpdateRecords(type: TaskType?): List<UpdateRecordDataClass> {
        val records = transaction {
            val select = if (type != null) {
                UpdateRecordTable.select { UpdateRecordTable.type eq type.code }
            } else {
                UpdateRecordTable.selectAll()
            }
            select.orderBy(UpdateRecordTable.id to SortOrder.DESC)
                .limit(500)
                .toList()
        }
        return records.map {
            UpdateRecordDataClass(
                id = it[UpdateRecordTable.id].value,
                createAt = it[UpdateRecordTable.createAt],
                updateAt = it[UpdateRecordTable.updateAt],
                finishAt = it[UpdateRecordTable.finishAt],
                type = it[UpdateRecordTable.type],
                status = it[UpdateRecordTable.status],
                errCode = it[UpdateRecordTable.errCode],
                errMsg = it[UpdateRecordTable.errMsg],
                totalCount = it[UpdateRecordTable.totalCount],
                succCount = it[UpdateRecordTable.succCount],
                failedCount = it[UpdateRecordTable.failedCount],
                skipCount = it[UpdateRecordTable.skipCount],
                newChapterCount = it[UpdateRecordTable.newChapterCount],
            )
        }
    }

    fun createTaskRecord(task: UpdateTask?) {
        try {
            createTaskRecord0(task)
        } catch (e: Exception) {
            logger.error(e) { "createTaskRecord error" }
        }
    }

    private fun createTaskRecord0(task: UpdateTask?) {
        if (task == null) return

        // save new task
        val now = System.currentTimeMillis()
        transaction {
            val id = UpdateRecordTable.insertAndGetId { it ->
                it[UpdateRecordTable.createAt] = now
                it[UpdateRecordTable.updateAt] = now
                it[UpdateRecordTable.type] = task.type.code
                it[UpdateRecordTable.status] = TaskStatus.INIT.code
            }
            task.recordId = id.value
        }

        // keep latest 500 records. (all type)
        val maxRecord = 500
        if ((task.recordId ?: 0) > maxRecord) {
            transaction {
                UpdateRecordTable.deleteWhere { UpdateRecordTable.id lessEq (task.recordId!! - maxRecord) }
            }
        }
    }

    fun updateTaskRecord(task: UpdateTask?, status: TaskStatus, code: TaskErrorCode? = null) {
        try {
            updateTaskRecord0(task, status, code)
        } catch (e: Exception) {
            logger.error(e) { "updateTaskRecord error" }
        }
    }

    private fun updateTaskRecord0(task: UpdateTask?, status: TaskStatus, errorCode: TaskErrorCode?) {
        val recordId = task?.recordId ?: return
        val now = System.currentTimeMillis()
        val result = updater.fetchUpdateResult()
        transaction {
            UpdateRecordTable.update({ UpdateRecordTable.id eq recordId }) { it ->
                it[UpdateRecordTable.updateAt] = now
                if (status == TaskStatus.SUCC) {
                    it[UpdateRecordTable.finishAt] = now
                }
                it[UpdateRecordTable.status] = status.code
                if (errorCode != null) {
                    it[UpdateRecordTable.errCode] = errorCode.name
                }
                it[UpdateRecordTable.totalCount] = result.totalCount
                it[UpdateRecordTable.succCount] = result.finishCount
                it[UpdateRecordTable.failedCount] = result.failedCount
                it[UpdateRecordTable.skipCount] = result.skipCount
                it[UpdateRecordTable.newChapterCount] = result.mangaChapterList?.sumOf { i -> i.newChapterCount } ?: 0
            }
        }
    }
}
