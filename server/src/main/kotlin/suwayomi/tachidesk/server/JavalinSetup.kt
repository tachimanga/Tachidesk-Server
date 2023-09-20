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
import org.kodein.di.DI
import org.kodein.di.conf.global
import org.kodein.di.instance
import suwayomi.tachidesk.global.GlobalAPI
import suwayomi.tachidesk.manga.MangaAPI
import suwayomi.tachidesk.server.util.Browser
import suwayomi.tachidesk.server.util.setupWebInterface
import java.io.IOException
import java.util.concurrent.CompletableFuture
import kotlin.concurrent.thread

object JavalinSetup {
    private val logger = KotlinLogging.logger {}

    private val applicationDirs by DI.global.instance<ApplicationDirs>()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var APP_INSTANCE: Javalin? = null

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

            config.enableCorsForAllOrigins()

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
            }
        )

        app.exception(NullPointerException::class.java) { e, ctx ->
            logger.error("NullPointerException while handling the request", e)
            ctx.status(500)
            ctx.result("Internal Server Error")
            ctx.header("x-err-msg", "Internal Server Error (NullPointerException)")
        }
        app.exception(NoSuchElementException::class.java) { e, ctx ->
            logger.error("NoSuchElementException while handling the request", e)
            ctx.status(500)
            ctx.result("Internal Server Error")
            ctx.header("x-err-msg", "Internal Server Error (NoSuchElementException)")
        }
        app.exception(IOException::class.java) { e, ctx ->
            logger.error("IOException while handling the request", e)
            ctx.status(500)
            ctx.result(e.message ?: "Internal Server Error")
            ctx.header("x-err-msg", e.message ?: "Internal Server Error (IOException)")
        }
        app.exception(IllegalArgumentException::class.java) { e, ctx ->
            logger.error("IllegalArgumentException while handling the request", e)
            ctx.status(400)
            ctx.result(e.message ?: "Bad Request")
            ctx.header("x-err-msg", e.message ?: "Bad Request")
        }
        app.exception(Exception::class.java) { e, ctx ->
            logger.error("Exception while handling the request", e)
            ctx.status(500)
            ctx.result(e.message ?: "Internal Server Error")
            ctx.header("x-err-msg", e.message ?: "Internal Server Error")
        }

        app.routes {
            path("api/v1/") {
                GlobalAPI.defineEndpoints()
                MangaAPI.defineEndpoints()
            }
        }

        app.before {
            val t = System.currentTimeMillis()
            it.attribute("ATTR_INVOKE_RT", t)
            println("Profiler: --> in " + it.req.requestURI + " " + it.req.method)
        }

        app.after {
            val t = it.attribute<Long>("ATTR_INVOKE_RT") ?: 0
            println("Profiler: <-- out " + it.req.requestURI + ", cost " + (System.currentTimeMillis() - t) + "ms")
        }
    }

    fun javalinStop() {
        println("javalinStop...")
        APP_INSTANCE?.stop()
        APP_INSTANCE = null
        println("javalinStop done")
    }

    object Auth {
        enum class Role : RouteRole { ANYONE, USER_READ, USER_WRITE }
    }
}
