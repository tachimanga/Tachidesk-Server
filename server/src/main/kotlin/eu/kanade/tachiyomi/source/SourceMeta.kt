package eu.kanade.tachiyomi.source

import java.util.Collections

/**
 * mc
 */
fun sourceSupportDirect(meta: SourceMeta?): Boolean {
    if (SourceSetting.ENABLE_FLUTTER_DIRECT == false) { // 仅false时, 返回false
        return false
    }
    return meta != null && meta.simpleClient && meta.simpleRequest
}

data class SourceMeta(
    var simpleClient: Boolean = false,
    var simpleRequest: Boolean = false,
    var headers: Map<String, String> = Collections.emptyMap()
)

class SourceSetting {
    companion object {
        var ENABLE_FLUTTER_DIRECT: Boolean? = null
    }
}
