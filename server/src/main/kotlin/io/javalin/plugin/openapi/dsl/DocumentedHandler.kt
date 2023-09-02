package io.javalin.plugin.openapi.dsl

import io.javalin.http.Context
import io.javalin.http.Handler

open class DocumentedHandler(
    val documentation: OpenApiDocumentation,
    private val handler: Handler
) : Handler {
    override fun handle(ctx: Context) = handler.handle(ctx)
}

/** Creates a documented Handler with a lambda */
fun documented(documentation: OpenApiDocumentation, handle: (ctx: Context) -> Unit) = DocumentedHandler(
    documentation,
    Handler { ctx -> handle(ctx) }
)

/** Creates a documented Handler */
fun documented(documentation: OpenApiDocumentation, handler: Handler) = DocumentedHandler(
    documentation,
    handler
)
