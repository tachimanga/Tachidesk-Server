package suwayomi.tachidesk.manga.impl.util.source

import mu.KotlinLogging
import suwayomi.tachidesk.manga.impl.Setting

object SourceConfig {
    private val logger = KotlinLogging.logger {}

    private var FORCE_UA_MAP = mapOf(
        6551136894818591762L to "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/118.0",
        5234610795363016972L to "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/118.0"
    )

    private var FORCE_EXT_UA = setOf(
        8061953015808280611L
    )

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
