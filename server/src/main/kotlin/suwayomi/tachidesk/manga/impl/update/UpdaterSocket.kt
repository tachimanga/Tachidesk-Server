package suwayomi.tachidesk.manga.impl.update

import io.javalin.websocket.WsContext
import io.javalin.websocket.WsMessageContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import mu.KotlinLogging
import org.kodein.di.DI
import org.kodein.di.conf.global
import org.kodein.di.instance

object UpdaterSocket : Websocket<UpdateStatus>() {
    private val logger = KotlinLogging.logger {}
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val updater by DI.global.instance<IUpdater>()
    private var job: Job? = null

    override fun notifyClient(ctx: WsContext, value: UpdateStatus?) {
        logger.info { "categoryUpdateWS notifyClient" }
        ctx.send(value ?: updater.status.value)
    }

    override fun handleRequest(ctx: WsMessageContext) {
        logger.info { "categoryUpdateWS onMessage ${ctx.message()} from ${ctx.sessionId}" }
        when (ctx.message()) {
            "STATUS" -> notifyClient(ctx, updater.status.value)
            else -> ctx.send(
                """
                        |Invalid command.
                        |Supported commands are:
                        |    - STATUS
                        |       sends the current update status
                        |
                """.trimMargin(),
            )
        }
    }

    override fun addClient(ctx: WsContext) {
        logger.info { "categoryUpdateWS onConnect ${ctx.sessionId}" }
        super.addClient(ctx)
        if (job?.isActive != true) {
            job = start()
        }
    }

    override fun removeClient(ctx: WsContext) {
        logger.info { "categoryUpdateWS onClose ${ctx.sessionId}" }
        super.removeClient(ctx)
        if (clients.isEmpty()) {
            job?.cancel()
            job = null
        }
    }

    fun start(): Job {
        return updater.status
            .onEach {
                println("notify updater numberOfJobs:${it.numberOfJobs} running:${it.running}")
                notifyAllClients(it)
            }
            .launchIn(scope)
    }
}
