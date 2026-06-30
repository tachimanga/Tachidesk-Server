package io.javalin.plugin.openapi.dsl

/*
 * Copyright (C) 2023 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import io.javalin.http.Context
import io.javalin.http.Handler

open class DocumentedHandler(
    val documentation: OpenApiDocumentation,
    private val handler: Handler,
) : Handler {
    override fun handle(ctx: Context) = handler.handle(ctx)
}

/** Creates a documented Handler with a lambda */
fun documented(documentation: OpenApiDocumentation, handle: (ctx: Context) -> Unit) = DocumentedHandler(
    documentation,
    Handler { ctx -> handle(ctx) },
)

/** Creates a documented Handler */
fun documented(documentation: OpenApiDocumentation, handler: Handler) = DocumentedHandler(
    documentation,
    handler,
)
