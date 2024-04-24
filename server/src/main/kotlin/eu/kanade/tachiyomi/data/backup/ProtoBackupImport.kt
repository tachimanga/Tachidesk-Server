package eu.kanade.tachiyomi.data.backup

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import eu.kanade.tachiyomi.data.backup.models.Backup
import eu.kanade.tachiyomi.data.backup.models.BackupManga
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import eu.kanade.tachiyomi.util.BackupUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.protobuf.ProtoBuf
import mu.KotlinLogging
import okio.source
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.jdbc.JdbcConnectionImpl
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import suwayomi.tachidesk.manga.impl.Category
import suwayomi.tachidesk.manga.impl.CategoryManga
import suwayomi.tachidesk.manga.impl.extension.Extension
import suwayomi.tachidesk.manga.impl.extension.ExtensionsList
import suwayomi.tachidesk.manga.impl.track.Track
import suwayomi.tachidesk.manga.impl.track.tracker.model.toTrack
import suwayomi.tachidesk.manga.impl.util.source.GetCatalogueSource.getCatalogueSourceOrNull
import suwayomi.tachidesk.manga.model.table.ChapterTable
import suwayomi.tachidesk.manga.model.table.MangaTable
import suwayomi.tachidesk.server.database.MyBatchInsertStatement
import java.io.InputStream
import java.util.*
import java.util.concurrent.TimeUnit

data class ImportContext(
    val backup: Backup,
    val defaultRepoUrl: String,
    // backupCategory#order -> dbCategory#id
    val categoryMap: MutableMap<Long, Int> = mutableMapOf(),
    var codes: List<String> = emptyList()
)

object ProtoBackupImport {
    val parser = ProtoBuf
    private val logger = KotlinLogging.logger {}
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _status = MutableStateFlow(ImportStatus(ImportState.INIT.name, ""))
    val status = _status.asStateFlow()

    data class ImportResult(
        val dummy: String
    )

    private fun updateStatus(state: ImportState, message: String, codes: List<String> = emptyList()) {
        logger.info { "[Import]updateStatus, state:$state message=$message" }
        _status.value = ImportStatus(state.name, message, codes)
    }

    suspend fun performRestore(sourceStream: InputStream, defaultRepoUrl: String): ImportResult {
        try {
            if (_status.value.state == ImportState.RUNNING.name) {
                throw Exception("There is an ongoing import task.")
            }
            updateStatus(ImportState.RUNNING, "")
            return performRestore0(sourceStream, defaultRepoUrl)
        } catch (e: Throwable) {
            updateStatus(ImportState.FAIL, "Import failed")
            throw e
        }
    }

    private suspend fun performRestore0(sourceStream: InputStream, defaultRepoUrl: String): ImportResult {
        // decode
        logger.info { "[Import]decode start" }
        val backup = BackupUtil.decodeBackup(sourceStream)
        logger.info { "[Import]decode done" }

        // validate
        logger.info { "[Import]validate start" }
        validate(backup)
        logger.info { "[Import]validate done" }

        scope.launch {
            logger.info { "[Import]scope.launch..." }
            try {
                val context = ImportContext(backup, defaultRepoUrl)
                doRestore(context)
                updateStatus(ImportState.SUCCESS, "Import successful.", context.codes)
            } catch (e: Exception) {
                logger.error { "[Import]doRestore error:$e" }
                updateStatus(ImportState.FAIL, if (e.message != null) "Import failed: ${e.message}" else "Import failed")
            }
        }
        return ImportResult("")
    }

    private fun validate(backup: Backup) {
        if (backup.backupManga.isEmpty()) {
            throw Exception("Backup does not contain any manga.")
        }
    }

    private suspend fun doRestore(context: ImportContext) {
        restoreSources(context)
        restoreCategories(context)
        restoreManga(context)
    }

    private suspend fun restoreSources(context: ImportContext) {
        val sources = context.backup.backupSources.associate { it.sourceId to it.name }
        logger.info { "[Import]backupSources $sources" }

        updateStatus(ImportState.RUNNING, "Fetching extensions...")
        val pair = ExtensionsList.getExtensionListForImport(context.defaultRepoUrl)
        val foundExtensions = pair.second
        val dbExtensions = pair.first.associateBy { "${it.repoId}#${it.pkgName}}" }

        val todoList = foundExtensions
            .filter { it.sources.any { s -> sources.containsKey(s.id) } }
            .filter { dbExtensions["${it.repoId}#${it.pkgName}}"]?.installed == false }
            .toList()
        logger.info { "[Import]to install extensions ${todoList.map { it.pkgName }}" }

        todoList.forEach {
            updateStatus(ImportState.RUNNING, "Installing ${it.name}...")
            val extension = dbExtensions["${it.repoId}#${it.pkgName}}"]
            Extension.installExtension(extension!!.extensionId)
        }

        context.codes = todoList.map { it.lang }.toList()
        logger.info { "[Import]install extensions done, code:${context.codes}" }
    }

    private fun restoreCategories(context: ImportContext) {
        updateStatus(ImportState.RUNNING, "Importing categories...")

        val backupCategories = context.backup.backupCategories
        logger.info { "[Import]backupCategories $backupCategories" }
        val dbCategories = Category.getCategoryList().associateBy { it.name }
        logger.info { "[Import]dbCategories $dbCategories" }

        // Iterate over them and create missing categories
        backupCategories.forEach { category ->
            val dbCategory = dbCategories[category.name]
            if (dbCategory == null) {
                context.categoryMap[category.order] = Category.createCategory(category.name)
            } else {
                context.categoryMap[category.order] = dbCategory.id
            }
        }

        logger.info { "[Import]categoryMap ${context.categoryMap}" }
    }

    private fun restoreManga(context: ImportContext) {
        val total = context.backup.backupManga.size
        context.backup.backupManga.forEachIndexed { index, manga ->
            updateStatus(ImportState.RUNNING, "Importing(${index + 1}/$total) ${manga.title}...")
            try {
                restoreMangaData(manga, context.categoryMap)
            } catch (e: Exception) {
                logger.error { "[Import]restoreMangaData error:$e" }
            }
        }
    }

    private fun restoreMangaData(
        manga: BackupManga,
        categoryMapping: Map<Long, Int>
    ) {
        logger.info { "[Import]import manga..., title: ${manga.title}" }
        val source = getCatalogueSourceOrNull(manga.source)
        transaction {
            val dbManga =
                MangaTable.select { (MangaTable.url eq manga.url) and (MangaTable.sourceReference eq manga.source) }
                    .firstOrNull()
            val mangaId =
                if (dbManga != null) {
                    val id = dbManga[MangaTable.id].value
                    val mangaInLibrary = dbManga[MangaTable.inLibrary]
                    logger.info { "[Import]exist manga, inLibrary:$mangaInLibrary. title: ${manga.title}" }
                    if (!mangaInLibrary) {
                        transaction {
                            MangaTable.update({ MangaTable.id eq id }) {
                                it[inLibrary] = manga.favorite
                                it[inLibraryAt] = TimeUnit.MILLISECONDS.toSeconds(manga.dateAdded)
                            }
                        }
                    }

                    val chapters = manga.chapters.filter { it.read || it.bookmark }
                    if (chapters.isNotEmpty()) {
                        val dbChapters = ChapterTable.select { ChapterTable.manga eq id }
                            .associateBy { it[ChapterTable.url] }
                        chapters.forEach { chapter ->
                            val dbChapter = dbChapters[chapter.url]
                            if (dbChapter != null) {
                                ChapterTable.update({ (ChapterTable.id eq dbChapter[ChapterTable.id]) }) {
                                    it[isRead] = chapter.read || dbChapter[isRead]
                                    it[isBookmarked] = chapter.bookmark || dbChapter[isBookmarked]
                                }
                            }
                        }
                    }
                    id
                } else {
                    // insert manga to database
                    val id = MangaTable.insertAndGetId {
                        it[url] = manga.url
                        it[title] = manga.title.take(512)

                        it[artist] = manga.artist?.take(512)
                        it[author] = manga.author?.take(512)
                        it[description] = manga.description
                        it[genre] = manga.genre.joinToString()

                        it[status] = manga.status
                        it[thumbnail_url] = manga.thumbnailUrl
                        it[updateStrategy] = manga.updateStrategy.name

                        it[sourceReference] = manga.source

                        it[initialized] = true

                        it[inLibrary] = manga.favorite

                        it[inLibraryAt] = TimeUnit.MILLISECONDS.toSeconds(manga.dateAdded)

                        val sManga = SManga.create().apply {
                            title = manga.title
                            url = manga.url
                        }
                        it[realUrl] = runCatching {
                            (source as? HttpSource)?.getMangaUrl(sManga)
                        }.getOrNull()
                    }.value

                    // insert chapter data
                    if (manga.chapters.isNotEmpty()) {
                        val historyMap = manga.history.associateBy { it.url }
                        val chaptersLength = manga.chapters.size
                        val myBatchInsertStatement = MyBatchInsertStatement(ChapterTable)
                        manga.chapters.forEach { chapter ->
                            val my = myBatchInsertStatement

                            my.addBatch()

                            my[ChapterTable.url] = chapter.url
                            my[ChapterTable.name] = chapter.name.take(512)
                            my[ChapterTable.date_upload] = chapter.dateUpload
                            my[ChapterTable.chapter_number] = chapter.chapterNumber
                            my[ChapterTable.scanlator] = chapter.scanlator?.take(128)
                            my[ChapterTable.sourceOrder] = chaptersLength - chapter.sourceOrder.toInt()
                            my[ChapterTable.manga] = id

                            my[ChapterTable.isRead] = chapter.read
                            my[ChapterTable.lastPageRead] = chapter.lastPageRead.toInt()
                            my[ChapterTable.isBookmarked] = chapter.bookmark

                            my[ChapterTable.fetchedAt] = TimeUnit.MILLISECONDS.toSeconds(chapter.dateFetch)

                            val history = historyMap[chapter.url]
                            if (history != null) {
                                my[ChapterTable.lastReadAt] = TimeUnit.MILLISECONDS.toSeconds(history.lastRead)
                            }
                        }
                        val sql = myBatchInsertStatement.prepareSQL(this)
                        val conn = (TransactionManager.current().connection as JdbcConnectionImpl).connection
                        val statement = conn.createStatement()
                        statement.execute(sql)
                    }

                    id
                }

            manga.categories.forEach {
                val categoryId = categoryMapping[it]
                if (categoryId != null) {
                    CategoryManga.addMangaToCategory(mangaId, categoryId)
                }
            }

            manga.tracking.forEach {
                Track.upsertTrackRecord(it.toTrack(mangaId.toLong()))
            }
        }
        logger.info { "[Import]import manga done, title: ${manga.title}" }
    }
}
