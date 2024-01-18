package eu.kanade.tachiyomi.data.backup

import io.javalin.websocket.WsContext
import io.javalin.websocket.WsMessageContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import mu.KotlinLogging
import suwayomi.tachidesk.manga.impl.update.Websocket

object ImporterSocket : Websocket<ImportStatus>() {
    private val logger = KotlinLogging.logger {}
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var job: Job? = null

    override fun notifyClient(ctx: WsContext, value: ImportStatus?) {
        logger.info { "[Import]updateWS notifyClient" }
        ctx.send(value ?: ProtoBackupImport.status.value)
    }

    override fun handleRequest(ctx: WsMessageContext) {
        logger.info { "[Import]updateWS onMessage ${ctx.message()} from ${ctx.sessionId}" }
        when (ctx.message()) {
            "STATUS" -> notifyClient(ctx, ProtoBackupImport.status.value)
            else -> ctx.send(
                """
                        |Invalid command.
                        |Supported commands are:
                        |    - STATUS
                        |       sends the current update status
                        |
                """.trimMargin()
            )
        }
    }

    override fun addClient(ctx: WsContext) {
        logger.info { "[Import]updateWS onConnect ${ctx.sessionId}" }
        super.addClient(ctx)
        if (job?.isActive != true) {
            job = start()
        }
    }

    override fun removeClient(ctx: WsContext) {
        logger.info { "[Import]updateWS onClose ${ctx.sessionId}" }
        super.removeClient(ctx)
        if (clients.isEmpty()) {
            job?.cancel()
            job = null
        }
    }

    fun start(): Job {
        return ProtoBackupImport.status
            .onEach {
                logger.info { "[Import]notify listener state=${it.state} message=${it.message}" }
                notifyAllClients(it)
            }
            .launchIn(scope)
    }
}
