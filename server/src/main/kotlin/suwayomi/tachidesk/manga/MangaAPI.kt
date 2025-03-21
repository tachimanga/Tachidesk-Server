package suwayomi.tachidesk.manga

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import io.javalin.apibuilder.ApiBuilder.delete
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.ApiBuilder.patch
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.apibuilder.ApiBuilder.post
import io.javalin.apibuilder.ApiBuilder.ws
import suwayomi.tachidesk.manga.controller.*

object MangaAPI {
    fun defineEndpoints() {
        path("extension") {
            get("list", ExtensionController.list)

            get("install/{extensionId}", ExtensionController.install)
            post("install", ExtensionController.installFile)
            get("update/{extensionId}", ExtensionController.update)
            get("uninstall/{extensionId}", ExtensionController.uninstall)

            get("icon/{apkName}", ExtensionController.icon)
        }

        path("source") {
            get("list", SourceController.list)
            get("listForSearch", SourceController.listForSearch)
            get("{sourceId}", SourceController.retrieve)

            get("{sourceId}/popular/{pageNum}", SourceController.popular)
            get("{sourceId}/latest/{pageNum}", SourceController.latest)

            get("{sourceId}/preferences", SourceController.getPreferences)
            post("{sourceId}/preferences", SourceController.setPreference)

            get("{sourceId}/filters", SourceController.getFilters)
            post("{sourceId}/filters", SourceController.setFilters)

            get("{sourceId}/search", SourceController.searchSingle)
            post("{sourceId}/quick-search", SourceController.quickSearchSingle)
        }

        path("manga") {
            get("{mangaId}", MangaController.retrieve)
            get("{mangaId}/full", MangaController.retrieveFull)
            get("{mangaId}/thumbnail", MangaController.thumbnail)
            get("{mangaId}/realUrl", MangaController.mangaRealUrl)

            get("{mangaId}/category", MangaController.categoryList)
            post("{mangaId}/updateCategory", MangaController.updateCategory)
            get("{mangaId}/library", MangaController.addToLibrary)
            delete("{mangaId}/library", MangaController.removeFromLibrary)

            patch("{mangaId}/meta", MangaController.meta)

            get("{mangaId}/chapters", MangaController.chapterList)
            get("{mangaId}/delchapters", MangaController.delchapterList)
            post("{mangaId}/chapter/batch", MangaController.chapterBatch)
            get("{mangaId}/chapter/{chapterIndex}", MangaController.chapterRetrieve)
            post("chapter/modify", MangaController.chapterModify2)

            delete("{mangaId}/chapter/{chapterIndex}", MangaController.chapterDelete)
            get("{mangaId}/chapter/{chapterIndex}/realUrl", MangaController.chapterRealUrl)

            patch("{mangaId}/chapter/{chapterIndex}/meta", MangaController.chapterMeta)
            get("{mangaId}/chapter/{chapterIndex}/page/{index}", MangaController.pageRetrieve)

            post("install", MangaController.installFile)
            delete("removeLocal", MangaController.removeLocalManga)

            post("batchUpdate", MangaController.batchUpdate)
        }

        path("history") {
            get("list", HistoryController.list)
            delete("batch", HistoryController.batchDelete)
            delete("clear", HistoryController.clear)
        }

        path("chapter") {
            post("batch", MangaController.anyChapterBatch)
            post("batchQuery", MangaController.chapterBatchQuery)
        }

        path("category") {
            get("", CategoryController.categoryList)
            post("", CategoryController.categoryCreate)

            // The order here is important {categoryId} needs to be applied last
            // or throws a NumberFormatException
            patch("reorder", CategoryController.categoryReorder)

            get("{categoryId}", CategoryController.categoryMangas)
            patch("{categoryId}", CategoryController.categoryModify)
            delete("{categoryId}", CategoryController.categoryDelete)

            post("{categoryId}/meta", CategoryController.meta)
        }

        path("downloads") {
            ws("", DownloadController::downloadsWS)

            get("start", DownloadController.start)
            get("stop", DownloadController.stop)
            get("clear", DownloadController.clear)
            post("updateSetting", DownloadController.updateSetting)
        }

        path("download") {
            get("{mangaId}/chapter/{chapterIndex}", DownloadController.queueChapter)
            delete("{mangaId}/chapter/{chapterIndex}", DownloadController.unqueueChapter)
            patch("{mangaId}/chapter/{chapterIndex}/reorder/{to}", DownloadController.reorderChapter)
            post("batch", DownloadController.queueChapters)
            delete("batch", DownloadController.unqueueChapters)
        }

        path("downloaded") {
            get("list", DownloadController.getDownloadedMangaList)
            delete("batch", DownloadController.deleteDownloadedManga)
        }

        path("update") {
            get("recentChapters/{pageNum}", UpdateController.recentChapters)
            post("fetch2", UpdateController.categoryUpdate2)
            post("reset", UpdateController.reset)
            get("summary", UpdateController.updateSummary)
            ws("", UpdateController::categoryUpdateWS)
        }

        path("track") {
            get("list", TrackController.list)
            post("login", TrackController.login)
            post("logout", TrackController.logout)
            post("search", TrackController.search)
            post("bind", TrackController.bind)
            post("update", TrackController.update)
        }

        path("proto") {
            post("import", ProtoBackupController.protobufImportFile)
            ws("importWs", ProtoBackupController::importerWS)
            post("export", ProtoBackupController.protobufExport)
        }

        path("repo") {
            get("list", RepoController.repoList)
            post("check", RepoController.checkRepo)
            post("create", RepoController.createRepo)
            delete("remove/{repoId}", RepoController.removeRepo)
            post("updateByMetaUrl", RepoController.updateByMetaUrl)
        }

        path("migrate") {
            get("info", MigrateController.info)
            get("sourceList", MigrateController.sourceList)
            get("mangaList", MigrateController.mangaList)
            post("migrate", MigrateController.migrate)
        }

        path("pip") {
            get("ping", PipController.ping)
        }

        path("stats") {
            get("readTime", StatsController.readTime)
        }
    }
}
