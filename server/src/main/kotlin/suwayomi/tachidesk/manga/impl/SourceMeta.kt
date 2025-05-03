package suwayomi.tachidesk.manga.impl

import kotlinx.serialization.Serializable
import mu.KotlinLogging
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import suwayomi.tachidesk.manga.model.dataclass.SourceMetaDataClass
import suwayomi.tachidesk.manga.model.table.SourceMetaTable

object SourceMeta {
    private val logger = KotlinLogging.logger {}

    fun queryMeta(sourceId: Long, key: String): SourceMetaDataClass {
        val value = transaction {
            SourceMetaTable.slice(SourceMetaTable.value)
                .select { (SourceMetaTable.sourceId eq sourceId) and (SourceMetaTable.key eq key) and (SourceMetaTable.isDelete eq false) }
                .firstOrNull()
                ?.get(SourceMetaTable.value)
        }
        logger.info { "queryMeta sourceId:$sourceId key:$key value:$value" }
        return SourceMetaDataClass(sourceId, key, value)
    }

    fun queryMetas(sourceId: Long): Map<String, String> {
        val map = transaction {
            SourceMetaTable.slice(SourceMetaTable.value)
                .select { (SourceMetaTable.sourceId eq sourceId) and (SourceMetaTable.isDelete eq false) }
                .associate { it[SourceMetaTable.key] to it[SourceMetaTable.value] }
        }
        logger.info { "queryMeta sourceId:$sourceId map:$map" }
        return map
    }

    fun upsertMeta(sourceId: Long, key: String, value: String?) {
        val now = System.currentTimeMillis()
        transaction {
            SourceMetaTable.insert {
                it[SourceMetaTable.createAt] = now
                it[SourceMetaTable.updateAt] = now
                it[SourceMetaTable.isDelete] = value == null
                it[SourceMetaTable.dirty] = true
                it[SourceMetaTable.sourceId] = sourceId
                it[SourceMetaTable.key] = key
                it[SourceMetaTable.value] = value ?: ""
            }
        }
    }

    @Serializable
    data class SourceMetaQueryInput(
        val sourceId: Long,
        val key: String,
    )

    @Serializable
    data class SourceMetaUpdateInput(
        val sourceId: Long,
        val key: String,
        val value: String? = null,
    )
}
