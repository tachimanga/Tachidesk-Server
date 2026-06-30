package suwayomi.tachidesk.manga.model.dataclass

/*
 * Copyright (C) 2023 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

fun buildImgDataClass(url: String, headers: Map<String, String>?): ImgDataClass {
    return ImgDataClass(url = url, headers = headers)
}

data class ImgDataClass(
    val url: String,
    val method: String? = "GET",
    val headers: Map<String, String>?,
)
