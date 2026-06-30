package eu.kanade.tachiyomi.network

/*
 * Copyright (C) 2026 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import okhttp3.Response

/**
 * Exception that handles HTTP codes considered not successful by OkHttp.
 * Use it to have a standardized error message in the app across the extensions.
 *
 * @see Response.isSuccessful
 * @since tachiyomix 1.6
 * @param code [Int] the HTTP status code
 */
class HttpException(val code: Int) : IllegalStateException("HTTP error $code")
