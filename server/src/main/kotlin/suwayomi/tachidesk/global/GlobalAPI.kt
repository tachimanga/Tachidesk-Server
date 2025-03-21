package suwayomi.tachidesk.global

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import io.javalin.apibuilder.ApiBuilder.*
import suwayomi.tachidesk.global.controller.GlobalMetaController
import suwayomi.tachidesk.global.controller.SettingsController

object GlobalAPI {
    fun defineEndpoints() {
        path("meta") {
            get("", GlobalMetaController.getMeta)
            patch("", GlobalMetaController.modifyMeta)
        }
        path("settings") {
            get("about", SettingsController.about)
            post("uploadSettings", SettingsController.uploadSettings)
            get("clearCookies", SettingsController.clearCookies)
            get("uploadUserAgent", SettingsController.uploadUserAgent)
            get("systemInfo", SettingsController.systemInfo)
        }
    }
}
