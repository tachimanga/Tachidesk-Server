package suwayomi.tachidesk.server

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.core.security.RouteRole
import io.javalin.http.staticfiles.Location
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.future.future
import mu.KotlinLogging
import org.eclipse.jetty.server.Connector
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.MyStatisticsHandler
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.kodein.di.DI
import org.kodein.di.conf.global
import org.kodein.di.instance
import suwayomi.tachidesk.cloud.CloudAPI
import suwayomi.tachidesk.global.GlobalAPI
import suwayomi.tachidesk.manga.MangaAPI
import suwayomi.tachidesk.server.util.Browser
import suwayomi.tachidesk.server.util.setupWebInterface
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.concurrent.thread

object JavalinSetup {
    private val logger = KotlinLogging.logger {}

    private val applicationDirs by DI.global.instance<ApplicationDirs>()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var APP_INSTANCE: Javalin? = null
    private var STAT_HANDLER: MyStatisticsHandler? = null

    fun <T> future(block: suspend CoroutineScope.() -> T): CompletableFuture<T> {
        return scope.future(block = block)
    }

    fun javalinSetup() {
        val app = Javalin.create { config ->
            if (serverConfig.webUIEnabled) {
                setupWebInterface()

                logger.info { "Serving web static files for ${serverConfig.webUIFlavor}" }
                config.addStaticFiles(applicationDirs.webUIRoot, Location.EXTERNAL)
                config.addSinglePageRoot("/", applicationDirs.webUIRoot + "/index.html", Location.EXTERNAL)
            }

            STAT_HANDLER = MyStatisticsHandler()

            config.server {
                val pool = QueuedThreadPool(100, 8, 60_000).apply { name = "JettyServerThreadPool" }
                Server(pool).apply {
                    insertHandler(STAT_HANDLER)
                }
            }

            config.enableCorsForAllOrigins()

            config.addStaticFiles { staticFiles ->
                staticFiles.hostedPath = "/static"
                staticFiles.directory = "/static"
                staticFiles.location = Location.CLASSPATH
                staticFiles.headers = mapOf("cache-control" to "max-age=86400")
            }

            config.accessManager { handler, ctx, _ ->
                fun credentialsValid(): Boolean {
                    val (username, password) = ctx.basicAuthCredentials()
                    return username == serverConfig.basicAuthUsername && password == serverConfig.basicAuthPassword
                }

                if (serverConfig.basicAuthEnabled && !(ctx.basicAuthCredentialsExist() && credentialsValid())) {
                    ctx.header("WWW-Authenticate", "Basic")
                    ctx.status(401).json("Unauthorized")
                } else {
                    handler.handle(ctx)
                }
            }
        }.events { event ->
            event.serverStarted {
                if (serverConfig.initialOpenInBrowserEnabled) {
                    Browser.openInBrowser()
                }
            }
        }.start(serverConfig.ip, serverConfig.port)
        APP_INSTANCE = app

        // when JVM is prompted to shutdown, stop javalin gracefully
        Runtime.getRuntime().addShutdownHook(
            thread(start = false) {
                app.stop()
            },
        )

        app.exception(NullPointerException::class.java) { e, ctx ->
            logger.error("NullPointerException while handling the request", e)
            ctx.status(500)
            val msg = "Internal Server Error (NullPointerException)"
            ctx.result(msg)
            ctx.header("x-err-msg", msg)
        }
        app.exception(NoSuchElementException::class.java) { e, ctx ->
            logger.error("NoSuchElementException while handling the request", e)
            ctx.status(500)
            val msg = "Internal Server Error (NoSuchElementException)"
            ctx.result(msg)
            ctx.header("x-err-msg", msg)
        }
        app.exception(IOException::class.java) { e, ctx ->
            logger.error("IOException while handling the request", e)
            ctx.status(500)
            val msg = e.message?.take(100) ?: "Internal Server Error (IOException)"
            ctx.result(msg)
            ctx.header("x-err-msg", msg)
        }
        app.exception(IllegalArgumentException::class.java) { e, ctx ->
            logger.error("IllegalArgumentException while handling the request", e)
            ctx.status(400)
            val msg = e.message?.take(100) ?: "Bad Request"
            ctx.result(msg)
            ctx.header("x-err-msg", msg)
        }
        app.exception(Exception::class.java) { e, ctx ->
            logger.error("Exception while handling the request", e)
            ctx.status(500)
            val msg = e.message?.take(100) ?: "Internal Server Error"
            ctx.result(msg)
            ctx.header("x-err-msg", msg)
        }

        app.routes {
            path("api/v1/") {
                GlobalAPI.defineEndpoints()
                MangaAPI.defineEndpoints()
                CloudAPI.defineEndpoints()
            }
        }

        val dateFormat = SimpleDateFormat("HH:mm:ss.SSS")
        app.before {
            val t = System.currentTimeMillis()
            it.attribute("ATTR_INVOKE_RT", t)
            println("${dateFormat.format(Date())} Profiler: --> in " + it.req.requestURI + " " + it.req.method)
        }

        app.after {
            val t = it.attribute<Long>("ATTR_INVOKE_RT") ?: 0
            println(
                "Profiler: <-- out ${it.req.requestURI}, cost ${(System.currentTimeMillis() - t)}ms, " +
                    "code:${it.res.status}, type:${it.res.getHeader("Content-Type")}, res:${it.res}",
            )
        }
    }

    fun javalinStop() {
        logger.info("javalinStop...")
        APP_INSTANCE?.stop()
        APP_INSTANCE = null
        logger.info("javalinStop done")
    }

    private fun getConnector(): Connector? {
        val connectors = APP_INSTANCE?.jettyServer()?.server()?.connectors
        if (connectors?.isNotEmpty() == true) {
            return connectors[0]
        }
        return null
    }

    fun javalinStartSocket() {
        logger.info("startSocket...")
        STAT_HANDLER?.cancelShutdown()
        logger.info("startSocket cancel done")
        val connector = getConnector()
        if (connector == null) {
            logger.info("connector is null")
            return
        }
        connector.start()
        logger.info("startSocket done")
    }

    fun javalinWaitRequestDone() {
        logger.info("waitRequestDone...")
        if (STAT_HANDLER != null) {
            val future = STAT_HANDLER!!.shutdown()
            if (!future.isDone) {
                future.get()
            }
        }
        logger.info("waitRequestDone done")
    }

    fun javalinStopSocket() {
        logger.info("stopSocket...")
        val connector = getConnector()
        if (connector == null) {
            logger.info("connector is null")
            return
        }
        connector.stop()
        logger.info("stopSocket done")
    }

    object Auth {
        enum class Role : RouteRole { ANYONE, USER_READ, USER_WRITE }
    }
}
