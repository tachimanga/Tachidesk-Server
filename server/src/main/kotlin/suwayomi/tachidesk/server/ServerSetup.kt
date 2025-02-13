package suwayomi.tachidesk.server

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import eu.kanade.tachiyomi.App
import eu.kanade.tachiyomi.source.local.LocalSource
import io.javalin.plugin.json.JavalinJackson
import io.javalin.plugin.json.JsonMapper
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.conf.global
import org.kodein.di.singleton
import suwayomi.tachidesk.cloud.impl.Sync
import suwayomi.tachidesk.manga.impl.UserData
import suwayomi.tachidesk.manga.impl.extension.Extension
import suwayomi.tachidesk.manga.impl.update.IUpdater
import suwayomi.tachidesk.manga.impl.update.Updater
import suwayomi.tachidesk.server.database.databaseUp
import suwayomi.tachidesk.server.database.databaseUpFast
import tachiyomi.core.util.lang.launchIO
import xyz.nulldev.androidcompat.AndroidCompat
import xyz.nulldev.androidcompat.AndroidCompatInitializer
import xyz.nulldev.ts.config.ApplicationRootDir
import xyz.nulldev.ts.config.ConfigKodeinModule
import xyz.nulldev.ts.config.GlobalConfigManager
import java.io.File
import java.security.Security
import java.util.*

private val logger = KotlinLogging.logger {}

class ApplicationDirs(
    val dataRoot: String = ApplicationRootDir,
    val tempRoot: String = "${System.getProperty("java.io.tmpdir")}/Tachidesk",
) {
    val extensionsRoot = "$dataRoot/extensions"
    val thumbnailsRoot = "$dataRoot/thumbnails"
    val coversRoot = "$dataRoot/covers"
    val customCoversRoot = "$dataRoot/custom_covers"
    val mangaDownloadsRoot = serverConfig.downloadsPath.ifBlank { "$dataRoot/downloads" }
    val mangaDownloadsRoot2 = "$dataRoot/downloads2"
    val localMangaRoot = "${System.getProperty("user.home")}/Documents/local"
    val prefsRoot = "${System.getProperty("user.home")}/Library/Preferences"
    val webUIRoot = "$dataRoot/webUI"
    val tempProtoBackups = "$tempRoot/proto_backups"
}

val serverConfig: ServerConfig by lazy { GlobalConfigManager.module() }

val androidCompat by lazy { AndroidCompat() }

fun envSetup() {
    System.setProperty("java.net.useSystemProxies", "true")
    System.setProperty("suwayomi.tachidesk.config.server.debugLogsEnabled", "true")
    // println("prev os.name is " + System.getProperty("os.name")) //--> prev os.name is Mac OS X
    System.setProperty("os.name", "Mac OS X")
}

fun applicationSetup() {
    val skipInit = System.getProperty("app.tachimanga.skipInit") == "1"
    val cleanDb = System.getProperty("app.tachimanga.cleanDb") == "1"
    val vacuum = System.getProperty("app.tachimanga.vacuum") == "1"
    logger.info("Running Tachidesk ${BuildConfig.VERSION} revision ${BuildConfig.REVISION}")
    logger.info("param skipInit=$skipInit, cleanDb=$cleanDb, vacuum=$vacuum")

    // register Tachidesk's config which is dubbed "ServerConfig"
    GlobalConfigManager.registerModule(
        ServerConfig.register(GlobalConfigManager.config),
    )

    // Application dirs
    val applicationDirs = ApplicationDirs()

    DI.global.addImport(
        DI.Module("Server") {
            bind<ApplicationDirs>() with singleton { applicationDirs }
            bind<IUpdater>() with singleton { Updater() }
            bind<JsonMapper>() with singleton { JavalinJackson() }
            bind<Json>() with singleton { Json { ignoreUnknownKeys = true } }
        },
    )

    logger.debug("Data Root directory is set to: ${applicationDirs.dataRoot}")

    val localDirExist =
        File(applicationDirs.localMangaRoot).exists()

    // make dirs we need
    listOf(
        applicationDirs.dataRoot,
        applicationDirs.extensionsRoot,
        applicationDirs.extensionsRoot + "/icon",
        applicationDirs.thumbnailsRoot,
        applicationDirs.customCoversRoot,
        applicationDirs.coversRoot,
        applicationDirs.mangaDownloadsRoot,
        applicationDirs.mangaDownloadsRoot2,
        applicationDirs.tempProtoBackups,
        applicationDirs.localMangaRoot,
    ).forEach {
        File(it).mkdirs()
    }

    // Make sure only one instance of the app is running
    // handleAppMutex()

    // Load config API
    DI.global.addImport(ConfigKodeinModule().create())
    // Load Android compatibility dependencies
    AndroidCompatInitializer().init()
    // start app
    androidCompat.startApp(App())

    // copy local source icon
    try {
        val localSourceIconFile = File("${applicationDirs.extensionsRoot}/icon/localSource.png")
        if (!localSourceIconFile.exists()) {
            JavalinSetup::class.java.getResourceAsStream("/icon/localSource.png").use { input ->
                localSourceIconFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    } catch (e: Exception) {
        logger.error("Exception while copying Local source's icon", e)
    }
    Extension.preCopyExtensionIcon("tachiyomi-all.komga-v1.4.47.apk")

    if (!localDirExist) {
        copyDemoManga(applicationDirs)
    }

    // fixes #119 , ref: https://github.com/Suwayomi/Tachidesk-Server/issues/119#issuecomment-894681292 , source Id calculation depends on String.lowercase()
    Locale.setDefault(Locale.ENGLISH)

    if (skipInit) {
        databaseUpFast()
        LocalSource.registerFast()
    } else {
        databaseUp()
        LocalSource.register()
    }

    if (vacuum) {
        UserData.vacuum()
    }

    // Disable jetty's logging
    System.setProperty("org.eclipse.jetty.util.log.announce", "false")
    System.setProperty("org.eclipse.jetty.util.log.class", "org.eclipse.jetty.util.log.StdErrLog")
    System.setProperty("org.eclipse.jetty.LEVEL", "OFF")

    // socks proxy settings
    if (serverConfig.socksProxyEnabled) {
        System.getProperties()["socksProxyHost"] = serverConfig.socksProxyHost
        System.getProperties()["socksProxyPort"] = serverConfig.socksProxyPort
        logger.info("Socks Proxy is enabled to ${serverConfig.socksProxyHost}:${serverConfig.socksProxyPort}")
    }

    Sync.setup()

//    // AES/CBC/PKCS7Padding Cypher provider for zh.copymanga
//    Security.addProvider(BouncyCastleProvider())
}

fun applicationSetupExtra() {
    // AES/CBC/PKCS7Padding Cypher provider for zh.copymanga.
    launchIO {
        Security.addProvider(BouncyCastleProvider())

        val cleanDb = System.getProperty("app.tachimanga.cleanDb") == "1"
        if (cleanDb) {
            UserData.cleanDb()
        }
    }
}

fun copyDemoManga(applicationDirs: ApplicationDirs) {
    try {
        val list = listOf(
            "Top to bottom",
            "Left to right",
            "Right to left",
        )
        for (n in list) {
            val file = File("${applicationDirs.localMangaRoot}/$n/$n.zip")
            if (!file.exists()) {
                File("${applicationDirs.localMangaRoot}/$n").mkdirs()
                JavalinSetup::class.java.getResourceAsStream("/demo/$n.zip").use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    } catch (e: Exception) {
        logger.error("Exception while copying demo manga", e)
    }
}
