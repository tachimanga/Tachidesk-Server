package suwayomi.tachidesk.manga.impl.util.source

import mu.KotlinLogging
import suwayomi.tachidesk.manga.impl.Setting

object SourceConfig {
    private val logger = KotlinLogging.logger {}

    private var FORCE_UA_MAP = mapOf<Long, String>()

    private var FORCE_EXT_UA = setOf<Long>()

    fun getForceUaBySourceId(sourceId: Long): String? {
        return FORCE_UA_MAP[sourceId]
    }

    fun isForceSourceUa(sourceId: Long): Boolean {
        return FORCE_EXT_UA.contains(sourceId)
    }

    fun updateSourceConfig(sourceConfigList: List<Setting.SourceConfigInfo>) {
        val sourceUaMap = mutableMapOf<Long, String>()
        val forceUaSet = mutableSetOf<Long>()
        for (sourceConfig in sourceConfigList) {
            if (sourceConfig.sourceId != null && sourceConfig.ua?.isNotEmpty() == true) {
                if (sourceConfig.ua == "0") {
                    forceUaSet.add(sourceConfig.sourceId)
                } else {
                    sourceUaMap[sourceConfig.sourceId] = sourceConfig.ua
                }
            }
        }
        FORCE_UA_MAP = sourceUaMap
        FORCE_EXT_UA = forceUaSet
        logger.info { "[Config]updateSourceConfig config:$sourceConfigList -> FORCE_UA_MAP:$FORCE_UA_MAP, FORCE_EXT_UA:$FORCE_EXT_UA" }
    }
}
