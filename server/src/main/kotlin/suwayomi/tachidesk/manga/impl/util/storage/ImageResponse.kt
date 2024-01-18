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
    private fun pathToInputStream(path: String): InputStream {
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

    /**
     * Get a cached image response
     *
     * Note: The caller should also call [clearCachedImage] when appropriate
     *
     * @param cacheSavePath where to save the cached image. Caller should decide to use perma cache or temp cache (OS temp dir)
     * @param fileName what the saved cache file should be named
     */
    suspend fun getCachedImageResponse(saveDir: String, fileName: String, fetcher: suspend () -> Response): Pair<InputStream, String> {
        File(saveDir).mkdirs()
        val cachedFile = findFileNameStartingWith(saveDir, fileName)
        val filePath = "$saveDir/$fileName"
        if (cachedFile != null) {
            val fileType = cachedFile.substringAfter("$filePath.")
            return Pair(
                pathToInputStream(cachedFile),
                "image/$fileType"
            )
        }
        Profiler.split("before get img")
        val response = fetcher()
        Profiler.split("get img")
        if (response.code == 200) {
            if (response.headers["x-native-cost"] != null && response.headers["content-type"] != null) {
                return response.body!!.byteStream() to response.headers["content-type"]!!
            }
            val (actualSavePath, imageType) = saveImage(filePath, response.body!!.byteStream())
            Profiler.split("save img")
            return pathToInputStream(actualSavePath) to imageType
        } else {
            response.closeQuietly()
            throw Exception("request error! ${response.code}")
        }
    }

    /**
     */
    suspend fun getFastCachedImageResponse(saveDir: String, fileName: String, fetcher: suspend () -> Response): Pair<InputStream, String> {
        File(saveDir).mkdirs()
        val filePath = "$saveDir/$fileName"
        val cachedFile = File(filePath)
        if (cachedFile.exists()) {
            println("[Cache]file exist, $filePath")
            return Pair(
                pathToInputStream(filePath),
                "image/jpeg"
            )
        }
        Profiler.split("[Cache]before get img")
        val response = fetcher()
        Profiler.split("[Cache]get img")
        if (response.isSuccessful) {
            val (actualSavePath, imageType) = saveImageV2(filePath, response.body.byteStream())
            return pathToInputStream(actualSavePath) to imageType
        } else {
            response.closeQuietly()
            throw Exception("request error! ${response.code}")
        }
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

    /** Save image safely */
    fun saveImageV2(filePath: String, image: InputStream): Pair<String, String> {
        val tmpSaveFile = File(filePath)
        image.use { input -> tmpSaveFile.outputStream().use { output -> input.copyTo(output) } }
        Profiler.split("save file")
        return Pair(filePath, "image/jpeg")
    }

    fun clearCachedImage(saveDir: String, fileName: String) {
        val cachedFile = findFileNameStartingWith(saveDir, fileName)
        cachedFile?.also {
            File(it).delete()
        }
    }
}
