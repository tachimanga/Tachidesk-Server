package suwayomi.tachidesk.manga.impl.update

import eu.kanade.tachiyomi.source.model.UpdateStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.conf.global
import org.kodein.di.instance
import suwayomi.tachidesk.global.impl.GlobalMeta
import suwayomi.tachidesk.manga.impl.Category
import suwayomi.tachidesk.manga.impl.CategoryManga
import suwayomi.tachidesk.manga.impl.util.lang.isNotEmpty
import suwayomi.tachidesk.manga.model.dataclass.MangaDataClass
import suwayomi.tachidesk.manga.model.table.*

object UpdateManager {
    private val json by DI.global.instance<Json>()
    private val logger = KotlinLogging.logger {}
    private val updater by DI.global.instance<IUpdater>()

    fun updateAll() {
        logger.info { "[UPDATE]updateAll" }
        val task = UpdateTask(startAt = System.currentTimeMillis(), type = TaskType.MANUAL)
        addMangaToUpdater(task = task) {
            var mangaList = transaction {
                MangaTable
                    .select { (MangaTable.inLibrary eq true) }
                    .toList()
            }.map { MangaTable.toDataClass(it) }

            fillChapterInfo(mangaList)

            logger.info { "[UPDATE]updateAll mangaList before size=${mangaList.size}" }
            mangaList = filteredByCategories(mangaList)
            mangaList = filteredByConditions(mangaList)
            logger.info { "[UPDATE]updateAll mangaList after size=${mangaList.size}" }
            mangaList
        }
    }

    fun updateByCategories(categories: List<Int>) {
        logger.info { "[UPDATE]updateByCategories ids=$categories" }
        val task = UpdateTask(startAt = System.currentTimeMillis(), type = TaskType.MANUAL)
        addMangaToUpdater(task = task) {
            var mangaList = categories
                .flatMap { CategoryManga.getMangaListByCategory(it) }
                .distinctBy { it.id }

            fillChapterInfo(mangaList)

            logger.info { "[UPDATE]updateAll mangaList before size=${mangaList.size}" }
            mangaList = filteredByConditions(mangaList)
            logger.info { "[UPDATE]updateAll mangaList after size=${mangaList.size}" }
            mangaList
        }
    }

    fun backgroundUpdate() {
        logger.info { "[UPDATE]backgroundUpdate" }
        val task = UpdateTask(startAt = System.currentTimeMillis(), type = TaskType.BG_TASK)
        addMangaToUpdater(task = task) {
            var mangaList = transaction {
                MangaTable
                    .select { (MangaTable.inLibrary eq true) }
                    .toList()
            }.map { MangaTable.toDataClass(it) }

            fillChapterInfo(mangaList)

            logger.info { "[UPDATE]backgroundUpdate mangaList before size=${mangaList.size}" }
            mangaList = filteredByCategories(mangaList)
            mangaList = filteredByConditions(mangaList)
            logger.info { "[UPDATE]backgroundUpdate mangaList after size=${mangaList.size}" }
            mangaList
        }
    }

    fun retryByCodes(errorCodes: List<JobErrorCode>) {
        logger.info { "[UPDATE]retryByCodes errorCodes=$errorCodes" }
        addMangaToUpdater(reset = false) {
            val jobs = updater.fetchAllJobs()
            val mangaList = jobs
                .filter { it.status == JobStatus.FAILED && errorCodes.contains(it.failedInfo?.errorCode) }
                .map { it.manga }
            logger.info { "[UPDATE]retryByCodes mangaList size=${mangaList.size}" }
            mangaList
        }
    }

    fun retrySkipped() {
        logger.info { "[UPDATE]retrySkipped" }
        addMangaToUpdater(reset = false) {
            val jobs = updater.fetchAllJobs()
            var mangaList = jobs
                .filter { it.status == JobStatus.FAILED && it.failedInfo?.errorCode != JobErrorCode.UPDATE_FAILED }
                .map { it.manga }
            logger.info { "[UPDATE]retrySkipped mangaList before size=${mangaList.size}" }
            mangaList = filteredByCategories(mangaList)
            mangaList = filteredByConditions(mangaList)
            logger.info { "[UPDATE]retrySkipped mangaList after size=${mangaList.size}" }
            mangaList
        }
    }

    private fun addMangaToUpdater(reset: Boolean = true, task: UpdateTask? = null, fetch: () -> List<MangaDataClass>) {
        if (reset) {
            updater.reset(task)
        }
        updater.updateStatus(true)
        try {
            val mangaList = fetch()
            if (mangaList.isNotEmpty()) {
                updater.addMangaListToQueue(mangaList)
            } else {
                updater.updateStatus(null)
            }
        } catch (e: Exception) {
            updater.updateStatus(null)
            throw e
        }
    }

    private fun filteredByCategories(mangaList: List<MangaDataClass>): List<MangaDataClass> {
        if (mangaList.isEmpty()) {
            return mangaList
        }

        val excludeCategoryIds = transaction {
            CategoryMetaTable
                .slice(CategoryMetaTable.ref)
                .select { (CategoryMetaTable.key eq CategoryMetaKey.flutter_update_exclude.name) and (CategoryMetaTable.value eq "true") }
                .map { it[CategoryMetaTable.ref].value }
                .toList()
        }
        logger.info { "[UPDATE]excludeCategoryIds=$excludeCategoryIds" }

        if (excludeCategoryIds.isEmpty()) {
            return mangaList
        }

        val excludeDefaultCategory = excludeCategoryIds.contains(0)
        val dest = mutableListOf<MangaDataClass>()
        for (subList in mangaList.chunked(200)) {
            val mangaIds = subList.map { it.id }
            val excludeMangaIds = transaction {
                CategoryMangaTable
                    .slice(CategoryMangaTable.manga)
                    .select { (CategoryMangaTable.category inList excludeCategoryIds) and (CategoryMangaTable.manga inList mangaIds) }
                    .map { it[CategoryMangaTable.manga].value }
                    .toMutableSet()
            }
            if (excludeDefaultCategory) {
                val defaultCategoryMangaIds = transaction {
                    MangaTable
                        .slice(MangaTable.id)
                        .select { (MangaTable.id inList mangaIds) and (MangaTable.defaultCategory eq true) }
                        .map { it[MangaTable.id].value }
                        .toList()
                }
                logger.info { "[UPDATE]defaultCategoryMangaIds=$defaultCategoryMangaIds" }
                if (defaultCategoryMangaIds.isNotEmpty()) {
                    excludeMangaIds.addAll(defaultCategoryMangaIds)
                }
            }
            logger.info { "[UPDATE]excludeMangaIds=$excludeMangaIds" }
            for (manga in subList) {
                if (!excludeMangaIds.contains(manga.id)) {
                    dest.add(manga)
                } else {
                    updater.addMangaToTracker(
                        UpdateJob(
                            manga,
                            status = JobStatus.FAILED,
                            failedInfo = FailedInfo(errorCode = JobErrorCode.FILTERED_BY_EXCLUDE_CATEGORY),
                        ),
                    )
                }
            }
        }
        logger.info { "[UPDATE]filteredByCategories skip=${mangaList.size - dest.size}" }
        return dest
    }

    private fun filteredByConditions(list: List<MangaDataClass>): List<MangaDataClass> {
        val settings = fetchUpdateConditionSetting()
        logger.info { "[UPDATE]settings $settings" }
        var mangaList = list
        if (settings.filteredByUpdateStrategy != false) {
            mangaList = filteredByUpdateStrategy(list)
        }
        if (settings.filteredByMangaStatus != false) {
            mangaList = filteredByMangaStatus(mangaList)
        }
        if (settings.filteredByMangaUnread != false) {
            mangaList = filteredByMangaUnread(mangaList)
        }
        if (settings.filteredByMangaNotStart != false) {
            mangaList = filteredByMangaNotStart(mangaList)
        }
        return mangaList
    }

    private fun filteredByUpdateStrategy(mangaList: List<MangaDataClass>): List<MangaDataClass> {
        val dest = mutableListOf<MangaDataClass>()
        for (manga in mangaList) {
            if (manga.updateStrategy == UpdateStrategy.ALWAYS_UPDATE) {
                dest.add(manga)
            } else {
                updater.addMangaToTracker(
                    UpdateJob(
                        manga,
                        status = JobStatus.FAILED,
                        failedInfo = FailedInfo(errorCode = JobErrorCode.FILTERED_BY_UPDATE_STRATEGY),
                    ),
                )
            }
        }
        logger.info { "[UPDATE]filteredByUpdateStrategy skip=${mangaList.size - dest.size}" }
        return dest
    }

    private fun filteredByMangaStatus(mangaList: List<MangaDataClass>): List<MangaDataClass> {
        val dest = mutableListOf<MangaDataClass>()
        for (manga in mangaList) {
            if (manga.status != MangaStatus.COMPLETED.name) {
                dest.add(manga)
            } else {
                updater.addMangaToTracker(
                    UpdateJob(
                        manga,
                        status = JobStatus.FAILED,
                        failedInfo = FailedInfo(errorCode = JobErrorCode.FILTERED_BY_MANGA_STATUS),
                    ),
                )
            }
        }
        logger.info { "[UPDATE]filteredByMangaStatus skip=${mangaList.size - dest.size}" }
        return dest
    }

    private fun filteredByMangaUnread(mangaList: List<MangaDataClass>): List<MangaDataClass> {
        val dest = mutableListOf<MangaDataClass>()
        for (manga in mangaList) {
            if (manga.unreadCount == 0L || manga.unreadCount == null) {
                dest.add(manga)
            } else {
                updater.addMangaToTracker(
                    UpdateJob(
                        manga,
                        status = JobStatus.FAILED,
                        failedInfo = FailedInfo(errorCode = JobErrorCode.FILTERED_BY_UNREAD),
                    ),
                )
            }
        }
        logger.info { "[UPDATE]filteredByMangaUnread skip=${mangaList.size - dest.size}" }
        return dest
    }

    private fun filteredByMangaNotStart(mangaList: List<MangaDataClass>): List<MangaDataClass> {
        val dest = mutableListOf<MangaDataClass>()
        for (manga in mangaList) {
            val total = manga.chapterCount ?: 0L
            val unread = manga.unreadCountRaw ?: 0L
            if (total > 0 && total == unread) {
                updater.addMangaToTracker(
                    UpdateJob(
                        manga,
                        status = JobStatus.FAILED,
                        failedInfo = FailedInfo(errorCode = JobErrorCode.FILTERED_BY_NOT_STARTED),
                    ),
                )
            } else {
                dest.add(manga)
            }
        }
        logger.info { "[UPDATE]filteredByMangaNotStart skip=${mangaList.size - dest.size}" }
        return dest
    }

    private fun fillChapterInfo(mangaList: List<MangaDataClass>) {
        val chunkedLists = mangaList.chunked(100)
        chunkedLists.forEach {
            fillChapterInfo0(it)
        }
    }

    private fun fillChapterInfo0(mangaList: List<MangaDataClass>) {
        val mangaIds = mangaList.map { it.id }

        val unreadCountMap = CategoryManga.unreadCountMapWithScanlator(mangaIds)

        val unreadCount = Expression.build {
            val caseExpr = case()
                .When(ChapterTable.isRead eq booleanLiteral(false), intLiteral(1))
                .Else(intLiteral(0))
            Sum(caseExpr, IntegerColumnType())
        }

        val chapterCount = ChapterTable.id.count()

        val mangaMap = mangaList.associateBy { it.id }
        transaction {
            ChapterTable.slice(
                ChapterTable.manga,
                unreadCount,
                chapterCount,
            )
                .select { ChapterTable.manga inList mangaIds }
                .groupBy(ChapterTable.manga)
                .forEach {
                    val mangaId = it[ChapterTable.manga].value
                    val dataClass = mangaMap[mangaId]
                    if (dataClass != null) {
                        dataClass.unreadCount = unreadCountMap?.get(mangaId) ?: it[unreadCount]?.toLong()
                        dataClass.unreadCountRaw = it[unreadCount]?.toLong()
                        dataClass.chapterCount = it[chapterCount]
                    }
                }
        }
    }

    private fun fetchUpdateConditionSetting(): UpdateConditionSetting {
        val value = GlobalMeta.getValue(SettingKey.UpdateRestrictions.name)
        if (value?.isNotEmpty() == true) {
            return json.decodeFromString<UpdateConditionSetting>(value)
        }
        return UpdateConditionSetting()
    }

    fun migrateMigrateSelectedCategoriesIfNeeded(selectedCategories: List<String>) {
        val flag = transaction {
            SettingTable.select { SettingTable.key eq SettingKey.MigrateSelectedCategories.name }.count()
        }
        logger.info { "[UPDATE]migrateMigrateSelectedCategoriesIfNeeded flag=$flag selectedCategories:$selectedCategories" }
        if (flag > 0) {
            return
        }
        try {
            doMigrateMigrateSelectedCategories(selectedCategories)
        } catch (e: Throwable) {
            logger.error(e) { "[UPDATE]migrateMigrateSelectedCategoriesIfNeeded error" }
        }
        transaction {
            val now = System.currentTimeMillis()
            SettingTable.insert {
                it[SettingTable.key] = SettingKey.MigrateSelectedCategories.name
                it[SettingTable.value] = "1"
                it[SettingTable.createAt] = now
                it[SettingTable.updateAt] = now
            }
        }
    }

    private fun doMigrateMigrateSelectedCategories(selectedCategories: List<String>) {
        val selectedIds = selectedCategories
            .mapNotNull { it.toIntOrNull() }
            .toSet()
        if (selectedIds.isEmpty()) {
            return
        }
        val categoryList = Category.getCategoryList()
        val excludeList = categoryList.filter { !selectedIds.contains(it.id) }
        logger.info { "[UPDATE] category size=${categoryList.size}, exclude size=${excludeList.size}" }
        if (excludeList.size > categoryList.size * 0.3) {
            logger.info { "[UPDATE] skip init exclude category" }
            return
        }
        excludeList.forEach { Category.modifyMeta(it.id, CategoryMetaKey.flutter_update_exclude.name, "true") }
    }

    @Serializable
    data class UpdateConditionSetting(
        val filteredByUpdateStrategy: Boolean? = null,
        val filteredByMangaStatus: Boolean? = null,
        val filteredByMangaUnread: Boolean? = null,
        val filteredByMangaNotStart: Boolean? = null,
    )
}
