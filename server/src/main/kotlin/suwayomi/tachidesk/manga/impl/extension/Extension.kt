package suwayomi.tachidesk.manga.impl.extension

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import android.net.Uri
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.NetworkHelper
import eu.kanade.tachiyomi.network.awaitSuccess
import eu.kanade.tachiyomi.source.CatalogueSource
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.SourceFactory
import eu.kanade.tachiyomi.source.sourceSupportDirect
import mu.KotlinLogging
import okhttp3.Request
import okio.buffer
import okio.sink
import okio.source
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.conf.global
import org.kodein.di.instance
import suwayomi.tachidesk.cloud.impl.Sync
import suwayomi.tachidesk.manga.impl.extension.github.ExtensionGithubApi
import suwayomi.tachidesk.manga.impl.util.PackageTools
import suwayomi.tachidesk.manga.impl.util.PackageTools.EXTENSION_FEATURE
import suwayomi.tachidesk.manga.impl.util.PackageTools.LIB_VERSION_MAX
import suwayomi.tachidesk.manga.impl.util.PackageTools.LIB_VERSION_MIN
import suwayomi.tachidesk.manga.impl.util.PackageTools.METADATA_NSFW
import suwayomi.tachidesk.manga.impl.util.PackageTools.METADATA_SOURCE_CLASS
import suwayomi.tachidesk.manga.impl.util.PackageTools.METADATA_SOURCE_FACTORY
import suwayomi.tachidesk.manga.impl.util.PackageTools.dex2jar
import suwayomi.tachidesk.manga.impl.util.PackageTools.getPackageInfo
import suwayomi.tachidesk.manga.impl.util.PackageTools.loadExtensionSources
import suwayomi.tachidesk.manga.impl.util.source.GetCatalogueSource
import suwayomi.tachidesk.manga.impl.util.storage.ImageResponse
import suwayomi.tachidesk.manga.model.table.ExtensionTable
import suwayomi.tachidesk.manga.model.table.RepoTable
import suwayomi.tachidesk.manga.model.table.SourceTable
import suwayomi.tachidesk.manga.model.table.toDataClass
import suwayomi.tachidesk.server.ApplicationDirs
import uy.kohesive.injekt.injectLazy
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object Extension {
    private val logger = KotlinLogging.logger {}
    private val applicationDirs by DI.global.instance<ApplicationDirs>()
    private val installLock = ConcurrentHashMap<Int, Long>()

    suspend fun installExtension(extensionId: Int, markDirty: Boolean = true): Int {
        logger.debug("Installing $extensionId")
        val now = System.currentTimeMillis()
        val prev = installLock.putIfAbsent(extensionId, now)
        if (prev != null && now - prev < 10 * 1000) {
            logger.debug("Installing $extensionId, hit lock")
            return 302
        }
        val extensionRecord = transaction {
            ExtensionTable.select { ExtensionTable.id eq extensionId }.first()
        }
        val extApkName = extensionRecord[ExtensionTable.apkName]
        if (extApkName == "tachiyomi-all.komga-v1.4.47.apk") {
            return preInstallExtension(extApkName)
        }
        if (extensionRecord[ExtensionTable.isInstalled]) {
            return 302 // extension was already installed
        }
        val repoRecord = transaction { RepoTable.select { RepoTable.id eq extensionRecord[ExtensionTable.repoId] }.first() }
        val repo = RepoTable.toDataClass(repoRecord)
        return installAPK(extensionId, markDirty = markDirty) {
            val apkURL = ExtensionGithubApi.getApkUrl(repo, extApkName)
            val apkName = Uri.parse(apkURL).lastPathSegment!!
            val apkSavePath = "${applicationDirs.extensionsRoot}/$apkName"
            // download apk file
            downloadAPKFile(apkURL, apkSavePath)

            apkSavePath
        }
    }

    suspend fun installExternalExtension(inputStream: InputStream, apkName: String): Int {
        return installAPK(null) {
            val savePath = "${applicationDirs.extensionsRoot}/$apkName"
            logger.debug { "Saving apk at $apkName" }
            // download apk file
            val downloadedFile = File(savePath)
            downloadedFile.sink().buffer().use { sink ->
                inputStream.source().use { source ->
                    sink.writeAll(source)
                    sink.flush()
                }
            }
            savePath
        }
    }

    private fun preCopyExtension(apkName: String): String {
        val apkSavePath = "${applicationDirs.extensionsRoot}/$apkName"
        try {
            val apkFile = File(apkSavePath)
            if (!apkFile.exists()) {
                Extension::class.java.getResourceAsStream("/extension/$apkName").use { input ->
                    apkFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Exception while copying apk", e)
        }
        return apkSavePath
    }

    fun preCopyExtensionIcon(apkName: String) {
        try {
            val iconFile = File("${applicationDirs.extensionsRoot}/icon/$apkName.png")
            if (!iconFile.exists()) {
                Extension::class.java.getResourceAsStream("/extension/icon/$apkName.png").use { input ->
                    iconFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Exception while copying icon", e)
        }
    }

    // apkName tachiyomi-all.komga-v1.4.47.apk
    // pkgName eu.kanade.tachiyomi.extension.all.komga
    private suspend fun preInstallExtension(apkName: String): Int {
        logger.debug("preInstallExtension $apkName")
        return installAPK(null, markDirty = false) {
            // copy apk
            preCopyExtension(apkName)
        }
    }

    suspend fun installAPK(extensionId: Int?, markDirty: Boolean = true, fetcher: suspend () -> String): Int {
        val apkFilePath = fetcher()
        val apkName = File(apkFilePath).name

        val fileNameWithoutType = apkName.substringBefore(".apk")

        val dirPathWithoutType = "${applicationDirs.extensionsRoot}/$fileNameWithoutType"
        val jarFilePath = "$dirPathWithoutType.jar"
        val dexFilePath = "$dirPathWithoutType.dex"

        val packageInfo = getPackageInfo(apkFilePath)
        val pkgName = packageInfo.packageName

        uninstallExtension(pkgName)

        if (true) {
            if (!packageInfo.reqFeatures.orEmpty().any { it.name == EXTENSION_FEATURE }) {
                throw Exception("This apk is not a Tachiyomi extension")
            }

            // Validate lib version
            val libVersion = packageInfo.versionName.substringBeforeLast('.').toDouble()
            if (libVersion < LIB_VERSION_MIN || libVersion > LIB_VERSION_MAX) {
                throw Exception(
                    "Lib version is $libVersion, while only versions " +
                        "$LIB_VERSION_MIN to $LIB_VERSION_MAX are allowed",
                )
            }

            // TODO: allow trusting keys
//            val signatureHash = getSignatureHash(packageInfo)

//            if (signatureHash == null) {
//                throw Exception("Package $pkgName isn't signed")
//            } else if (signatureHash !in trustedSignatures) {
//                throw Exception("This apk is not a signed with the official tachiyomi signature")
//            }

            val isNsfw = packageInfo.applicationInfo.metaData.getString(METADATA_NSFW) == "1"

            val className =
                packageInfo.packageName + packageInfo.applicationInfo.metaData.getString(METADATA_SOURCE_CLASS)

            val pkgFactory = packageInfo.applicationInfo.metaData.getString(METADATA_SOURCE_FACTORY)

            logger.debug("Main class for extension is $className")

            dex2jar(apkFilePath, jarFilePath, fileNameWithoutType)
            extractAssetsFromApk(apkFilePath, jarFilePath)

            // clean up
            File(apkFilePath).delete()
            File(dexFilePath).delete()

            // collect sources from the extension
            val extensionMainClassInstance = loadExtensionSources(jarFilePath, className)
            val sources: List<CatalogueSource> = when (extensionMainClassInstance) {
                is Source -> listOf(extensionMainClassInstance)
                is SourceFactory -> extensionMainClassInstance.createSources()
                else -> throw RuntimeException("Unknown source class type! ${extensionMainClassInstance.javaClass}")
            }.map { it as CatalogueSource }

            val directMap = sources.associate {
                it.id to sourceSupportDirect(GetCatalogueSource.getCatalogueSourceMeta(it))
            }

            val langs = sources.map { it.lang }.toSet()
            val extensionLang = when (langs.size) {
                0 -> ""
                1 -> langs.first()
                else -> "all"
            }

            val extensionName = packageInfo.applicationInfo.nonLocalizedLabel.toString().substringAfter("Tachiyomi: ")

            val now = System.currentTimeMillis()
            var setNeedSync = false
            // update extension info
            transaction {
                val dbExtensionId = if (extensionId != null) {
                    extensionId
                } else {
                    val record = ExtensionTable.select { (ExtensionTable.pkgName eq pkgName) and (ExtensionTable.repoId eq 0) }.firstOrNull()
                    if (record != null) {
                        record[ExtensionTable.id].value
                    } else {
                        // local extension
                        ExtensionTable.insertAndGetId {
                            it[this.apkName] = apkName
                            it[name] = extensionName
                            it[this.pkgName] = packageInfo.packageName
                            it[versionName] = packageInfo.versionName
                            it[versionCode] = packageInfo.versionCode
                            it[lang] = extensionLang
                            it[this.isNsfw] = isNsfw
                            it[this.pkgFactory] = pkgFactory
                        }.value
                    }
                }

                ExtensionTable.update({ ExtensionTable.id eq dbExtensionId }) {
                    it[this.apkName] = apkName
                    it[this.isInstalled] = true
                    it[this.classFQName] = className
                    it[this.pkgFactory] = pkgFactory
                    it[ExtensionTable.updateAt] = now
                    if (markDirty) {
                        it[ExtensionTable.dirty] = true
                        setNeedSync = true
                    }
                }

                sources.forEach { httpSource ->
                    SourceTable.deleteWhere { SourceTable.id eq httpSource.id }
                    SourceTable.insert {
                        it[id] = httpSource.id
                        it[name] = httpSource.name
                        it[lang] = httpSource.lang
                        it[extension] = dbExtensionId
                        it[SourceTable.isNsfw] = isNsfw
                        it[SourceTable.isDirect] = directMap[httpSource.id]
                    }
                    logger.debug { "Installed source ${httpSource.name} (${httpSource.lang}) with id:${httpSource.id}" }
                }
            }
            if (setNeedSync) {
                Sync.setNeedsSync()
            }
            return 201 // we installed successfully
        } else {
            return 302 // extension was already installed
        }
    }

    private fun extractAssetsFromApk(apkPath: String, jarPath: String) {
        val apkFile = File(apkPath)
        val jarFile = File(jarPath)

        val assetsFolder = File("${apkFile.parent}/${apkFile.nameWithoutExtension}_assets")
        assetsFolder.mkdir()
        ZipInputStream(apkFile.inputStream()).use { zipInputStream ->
            var zipEntry = zipInputStream.nextEntry
            while (zipEntry != null) {
                if (zipEntry.name.startsWith("assets/") && !zipEntry.isDirectory) {
                    val assetFile = File(assetsFolder, zipEntry.name)
                    assetFile.parentFile.mkdirs()
                    FileOutputStream(assetFile).use { outputStream ->
                        zipInputStream.copyTo(outputStream)
                    }
                }
                zipEntry = zipInputStream.nextEntry
            }
        }

        val tempJarFile = File("${jarFile.parent}/${jarFile.nameWithoutExtension}_temp.jar")
        ZipInputStream(jarFile.inputStream()).use { jarZipInputStream ->
            ZipOutputStream(FileOutputStream(tempJarFile)).use { jarZipOutputStream ->
                var zipEntry = jarZipInputStream.nextEntry
                while (zipEntry != null) {
                    if (!zipEntry.name.startsWith("META-INF/")) {
                        jarZipOutputStream.putNextEntry(ZipEntry(zipEntry.name))
                        jarZipInputStream.copyTo(jarZipOutputStream)
                    }
                    zipEntry = jarZipInputStream.nextEntry
                }
                assetsFolder.walkTopDown().forEach { file ->
                    if (file.isFile) {
                        jarZipOutputStream.putNextEntry(ZipEntry(file.relativeTo(assetsFolder).toString().replace("\\", "/")))
                        file.inputStream().use { inputStream ->
                            inputStream.copyTo(jarZipOutputStream)
                        }
                        jarZipOutputStream.closeEntry()
                    }
                }
            }
        }

        jarFile.delete()
        tempJarFile.renameTo(jarFile)

        assetsFolder.deleteRecursively()
    }

    private val network: NetworkHelper by injectLazy()

    private suspend fun downloadAPKFile(url: String, savePath: String) {
        val request = Request.Builder().url(url).build()
        val response = network.client.newCall(request).awaitSuccess()

        val downloadedFile = File(savePath)
        downloadedFile.sink().buffer().use { sink ->
            response.body.source().use { source ->
                sink.writeAll(source)
                sink.flush()
            }
        }
    }

    fun uninstallExtension(pkgName: String) {
        logger.debug("Uninstalling pkgName:$pkgName")
        val existsExtList = transaction {
            ExtensionTable.select { (ExtensionTable.pkgName eq pkgName) and (ExtensionTable.isInstalled eq true) }
                .toList()
        }
        existsExtList.forEach {
            uninstallExtensionById(it[ExtensionTable.id].value)
        }
    }

    fun uninstallExtensionById(extensionId: Int, removePref: Boolean = false, markDirty: Boolean = true) {
        logger.debug("Uninstalling $extensionId")

        var setNeedSync = false

        val extensionRecord = transaction { ExtensionTable.select { ExtensionTable.id eq extensionId }.first() }
        logger.debug("Uninstalling ${extensionRecord[ExtensionTable.pkgName]}")
        val fileNameWithoutType = extensionRecord[ExtensionTable.apkName].substringBefore(".apk")
        val jarPath = "${applicationDirs.extensionsRoot}/$fileNameWithoutType.jar"
        val sources = transaction {
            val sources = SourceTable.select { SourceTable.extension eq extensionId }.map { it[SourceTable.id].value }

            SourceTable.deleteWhere { SourceTable.extension eq extensionId }

            if (extensionRecord[ExtensionTable.isObsolete]) {
                ExtensionTable.deleteWhere { ExtensionTable.id eq extensionId }
            } else {
                ExtensionTable.update({ ExtensionTable.id eq extensionId }) {
                    it[isInstalled] = false
                    it[ExtensionTable.updateAt] = System.currentTimeMillis()
                    if (markDirty) {
                        it[ExtensionTable.dirty] = true
                        setNeedSync = true
                    }
                }
            }

            sources
        }

        if (File(jarPath).exists()) {
            // free up the file descriptor if exists
            PackageTools.jarLoaderMap.remove(jarPath)?.close()

            // clear all loaded sources
            sources.forEach {
                GetCatalogueSource.unregisterCatalogueSource(it)
                GetCatalogueSource.unregisterCatalogueSourceExt(it)
                if (removePref) {
                    suwayomi.tachidesk.manga.impl.Source.removeSourcePref(it)
                }
            }

            File(jarPath).delete()
        }

        if (setNeedSync) {
            Sync.setNeedsSync()
        }
    }

    suspend fun updateExtension(extensionId: Int): Int {
        val targetExtension = ExtensionsList.updateMap.remove(extensionId) ?: return 200
        uninstallExtensionById(extensionId)
        transaction {
            ExtensionTable.update({ ExtensionTable.id eq extensionId }) {
                it[name] = targetExtension.name
                it[versionName] = targetExtension.versionName
                it[versionCode] = targetExtension.versionCode
                it[lang] = targetExtension.lang
                it[isNsfw] = targetExtension.isNsfw
                it[apkName] = targetExtension.apkName
                it[iconUrl] = targetExtension.iconUrl
                it[hasUpdate] = false
            }
        }
        return installExtension(extensionId)
    }

    suspend fun getExtensionIcon(apkName: String): Pair<InputStream, String> {
        val iconUrl = if (apkName == "localSource") {
            ""
        } else {
            transaction { ExtensionTable.select { ExtensionTable.apkName eq apkName }.first() }[ExtensionTable.iconUrl]
        }

        // for local source icon
        val cacheSaveDir = "${applicationDirs.extensionsRoot}/icon"
        val cachedFile = ImageResponse.findFileNameStartingWith(cacheSaveDir, apkName)
        if (cachedFile != null) {
            return Pair(
                ImageResponse.pathToInputStream(cachedFile),
                "image/jpeg",
            )
        }
        return ImageResponse.buildImageResponse {
            network.client.newCall(
                GET(iconUrl),
            ).awaitSuccess()
        }
    }

    fun getExtensionIconUrl(apkName: String, iconUrl: String): String {
        if (apkName == "localSource") {
            return "/api/v1/extension/icon/$apkName"
        }
        return iconUrl
    }
}
