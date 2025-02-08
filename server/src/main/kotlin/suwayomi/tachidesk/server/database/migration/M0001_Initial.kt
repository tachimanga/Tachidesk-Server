package suwayomi.tachidesk.server.database.migration

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import de.neonew.exposed.migrations.helpers.AddTableMigration
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.model.UpdateStrategy
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

/** initial migration, create all tables */
@Suppress("ClassName", "unused")
class M0001_Initial : AddTableMigration() {
    private class ExtensionTable : IntIdTable() {
        init {
            varchar("apk_name", 1024)

            // default is the local source icon from tachiyomi
            varchar("icon_url", 2048)
                .default("https://raw.githubusercontent.com/tachiyomiorg/tachiyomi/64ba127e7d43b1d7e6d58a6f5c9b2bd5fe0543f7/app/src/main/res/mipmap-xxxhdpi/ic_local_source.webp")

            varchar("name", 128)
            varchar("pkg_name", 128)
            varchar("version_name", 16)
            integer("version_code")
            varchar("lang", 32)
            bool("is_nsfw")

            bool("is_installed").default(false)
            bool("has_update").default(false)
            bool("is_obsolete").default(false)

            varchar("class_name", 1024).default("") // fully qualified name
        }
    }

    private class SourceTable(extensionTable: ExtensionTable) : IdTable<Long>() {
        override val id = long("id").entityId()
        init {
            varchar("name", 128)
            varchar("lang", 32)
            reference("extension", extensionTable)
            bool("is_nsfw").default(false)
        }
    }

    private class MangaTable : IntIdTable() {
        init {
            varchar("url", 2048)
            varchar("title", 512)
            bool("initialized").default(false)

            varchar("artist", 512).nullable()
            varchar("author", 512).nullable()
            varchar("description", Integer.MAX_VALUE).nullable()
            varchar("genre", Integer.MAX_VALUE).nullable()

            integer("status").default(SManga.UNKNOWN)
            varchar("thumbnail_url", 2048).nullable()
            long("thumbnail_url_last_fetched").default(0)

            bool("in_library").default(false)
            bool("default_category").default(true)
            long("in_library_at").default(0)

            // the [source] field name is used by some ancestor of IntIdTable
            long("source")

            /** the real url of a manga used for the "open in WebView" feature */
            varchar("real_url", 2048).nullable()

            long("last_fetched_at").default(0)
            long("chapters_last_fetched_at").default(0)

            varchar("update_strategy", 256).default(UpdateStrategy.ALWAYS_UPDATE.name)
        }
    }

    private class ChapterTable(mangaTable: MangaTable) : IntIdTable() {
        init {
            varchar("url", 2048)
            varchar("name", 512)
            long("date_upload").default(0)
            float("chapter_number").default(-1f)
            varchar("scanlator", 128).nullable()

            bool("read").default(false)
            bool("bookmark").default(false)
            integer("last_page_read").default(0)
            long("last_read_at").default(0)
            long("fetched_at").default(0)

            integer("source_order")

            /** the real url of a chapter used for the "open in WebView" feature */
            varchar("real_url", 2048).nullable()

            bool("is_downloaded").default(false)

            integer("page_count").default(-1)

            reference("manga", mangaTable)
        }
    }

    private class PageTable(chapterTable: ChapterTable) : IntIdTable() {
        init {
            integer("index")
            varchar("url", 2048)
            varchar("imageUrl", 2048).nullable()

            reference("chapter", chapterTable)
        }
    }

    private class CategoryTable : IntIdTable() {
        init {
            varchar("name", 64)
            integer("order").default(0)
            bool("is_default").default(false)
        }
    }

    private class CategoryMangaTable(categoryTable: CategoryTable, mangaTable: MangaTable) : IntIdTable() {
        init {
            reference("category", categoryTable)
            reference("manga", mangaTable)
        }
    }

    private class CategoryMetaTable(categoryTable: CategoryTable) : IntIdTable() {
        init {
            varchar("key", 256)
            varchar("value", 4096)
            reference("category_ref", categoryTable, ReferenceOption.CASCADE)
        }
    }

    private class ChapterMetaTable(chapterTable: ChapterTable) : IntIdTable() {
        init {
            varchar("key", 256)
            varchar("value", 4096)
            reference("chapter_ref", chapterTable, ReferenceOption.CASCADE)
        }
    }

    private class MangaMetaTable(mangaTable: MangaTable) : IntIdTable() {
        init {
            varchar("key", 256)
            varchar("value", 4096)
            reference("manga_ref", mangaTable, ReferenceOption.CASCADE)
        }
    }

    override val tables: Array<Table>
        get() {
            val extensionTable = ExtensionTable()
            val sourceTable = SourceTable(extensionTable)
            val mangaTable = MangaTable()
            val chapterTable = ChapterTable(mangaTable)
            val pageTable = PageTable(chapterTable)
            val categoryTable = CategoryTable()
            val categoryMangaTable = CategoryMangaTable(categoryTable, mangaTable)
            val categoryMetaTable = CategoryMetaTable(categoryTable)
            val chapterMetaTable = ChapterMetaTable(chapterTable)
            val mangaMetaTable = MangaMetaTable(mangaTable)

            return arrayOf(
                extensionTable,
                sourceTable,
                mangaTable,
                chapterTable,
                pageTable,
                categoryTable,
                categoryMangaTable,
                categoryMetaTable,
                chapterMetaTable,
                mangaMetaTable,
            )
        }
}
