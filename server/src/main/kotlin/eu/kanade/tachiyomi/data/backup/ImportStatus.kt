package eu.kanade.tachiyomi.data.backup

enum class ImportState {
    INIT,
    RUNNING,
    SUCCESS,
    FAIL,
}
data class ImportStatus(
    val state: String,
    val message: String,
    // extension language codes
    val codes: List<String> = emptyList(),
)
