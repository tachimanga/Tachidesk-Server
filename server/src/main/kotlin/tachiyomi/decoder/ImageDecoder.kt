package tachiyomi.decoder

/*
 * Copyright (C) 2024 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import java.io.InputStream

class ImageDecoder private constructor(
    private val bitmap: Bitmap,
    val width: Int,
    val height: Int,
) {
    // Mihon Preview r6595+
    fun decode(
        region: Rect = Rect(0, 0, width, height),
        sampleSize: Int = 1,
    ): Bitmap? {
        return bitmap
    }

    // Mihon Stable & forks
    fun decode(
        region: Rect = Rect(0, 0, width, height),
        rgb565: Boolean = true,
        sampleSize: Int = 1,
    ): Bitmap? {
        return bitmap
    }

    fun recycle() {
        // no-op
    }

    companion object {
        // Mihon Preview r6595+
        fun newInstance(
            stream: InputStream,
            cropBorders: Boolean = false,
            displayProfile: ByteArray? = null,
        ): ImageDecoder? {
            val bitmap = BitmapFactory.decodeStream(stream)
            return ImageDecoder(bitmap, bitmap.width, bitmap.height)
        }

        // Mihon Stable & forks
        fun newInstance(
            stream: InputStream,
            cropBorders: Boolean = false,
        ): ImageDecoder? {
            val bitmap = BitmapFactory.decodeStream(stream)
            return ImageDecoder(bitmap, bitmap.width, bitmap.height)
        }

        fun findType(bytes: ByteArray): ImageType? {
            return ImageType(Format.Jpeg, false)
        }
    }
}
