package suwayomi.tachidesk.manga.impl.util

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.conf.global
import org.kodein.di.instance
import org.tachiyomi.NativeChannel

object LogEvent {
    private val json by DI.global.instance<Json>()

    fun log(event: String, params: Map<String, String>? = null) {
        val payload = EventContent(event = event, params = params)
        NativeChannel.call("LOG_EVENT", json.encodeToString(payload))
    }
}

@Serializable
data class EventContent(
    val event: String,
    val params: Map<String, String>?,
)
