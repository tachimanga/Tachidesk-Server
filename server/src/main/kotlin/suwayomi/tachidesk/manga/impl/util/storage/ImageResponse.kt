package suwayomi.tachidesk.manga.impl.util.storage

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import okhttp3.Response
import okhttp3.internal.closeQuietly
import org.tachiyomi.Profiler
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

object ImageResponse {
    fun pathToInputStream(path: String): InputStream {
        return FileInputStream(path).buffered()
    }

    /** find file with name when file extension is not known */
    fun findFileNameStartingWith(directoryPath: String, fileName: String): String? {
        val target = "$fileName."
        File(directoryPath).listFiles().orEmpty().forEach { file ->
            if (file.name.startsWith(target)) {
                return "$directoryPath/${file.name}"
            }
        }
        return null
    }

    /** Save image safely */
    fun saveImage(filePath: String, image: InputStream): Pair<String, String> {
        val tmpSavePath = "$filePath.tmp"
        val tmpSaveFile = File(tmpSavePath)

        image.use { input -> tmpSaveFile.outputStream().use { output -> input.copyTo(output) } }
        Profiler.split("save file")

        // find image type
        val imageType = ImageUtil.findImageType { tmpSaveFile.inputStream() }?.mime
            ?: "image/jpeg"

        val actualSavePath = "$filePath.${imageType.substringAfter("/")}"

        tmpSaveFile.renameTo(File(actualSavePath))
        return Pair(actualSavePath, imageType)
    }

    suspend fun buildImageResponse(fetcher: suspend () -> Response): Pair<InputStream, String> {
        Profiler.split("before get img")
        val response = fetcher()
        Profiler.split("get img")
        if (response.code == 200) {
            return response.body.byteStream() to (response.headers["content-type"] ?: "image/jpeg")
        } else {
            response.closeQuietly()
            throw Exception("request error! ${response.code}")
        }
    }

    fun clearFastCachedImage(saveDir: String, fileName: String) {
        val file = File(saveDir, fileName)
        if (file.exists()) {
            file.delete()
            println("clearFastCachedImage $fileName deleted successfully.")
        } else {
            println("clearFastCachedImage $fileName does not exist.")
        }
    }
}
