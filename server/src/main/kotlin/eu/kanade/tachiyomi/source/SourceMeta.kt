package eu.kanade.tachiyomi.source

import java.util.Collections

/**
 * mc
 */
fun sourceSupportDirect(meta: SourceMeta?): Boolean {
    return meta != null && meta.simpleClient && meta.simpleRequest
}

data class SourceMeta(
    var simpleClient: Boolean = false,
    var simpleRequest: Boolean = false,
    var headers: Map<String, String> = Collections.emptyMap()
)
