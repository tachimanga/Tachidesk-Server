package eu.kanade.tachiyomi.util

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import android.util.Base64
import java.security.SecureRandom

object PkceUtil {

    private const val PKCE_BASE64_ENCODE_SETTINGS = Base64.NO_WRAP or Base64.NO_PADDING or Base64.URL_SAFE

    fun generateCodeVerifier(): String {
        val codeVerifier = ByteArray(50)
        SecureRandom().nextBytes(codeVerifier)
        return Base64.encodeToString(codeVerifier, PKCE_BASE64_ENCODE_SETTINGS)
    }
}
