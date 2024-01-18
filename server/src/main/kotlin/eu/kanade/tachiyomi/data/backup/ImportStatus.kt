package eu.kanade.tachiyomi.data.backup

import mu.KotlinLogging

val logger = KotlinLogging.logger {}

enum class ImportState {
    INIT,
    RUNNING,
    SUCCESS,
    FAIL
}
data class ImportStatus(
    val state: String,
    val message: String,
    val codes: List<String> = emptyList()
)
