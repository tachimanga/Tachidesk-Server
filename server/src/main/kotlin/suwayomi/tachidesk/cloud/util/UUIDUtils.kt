package suwayomi.tachidesk.cloud.util

import java.util.*

object UUIDUtils {
    fun generateUUID(): String {
        return UUID.randomUUID().toString().replace("{", "").replace("}", "").replace("-", "")
    }
}
