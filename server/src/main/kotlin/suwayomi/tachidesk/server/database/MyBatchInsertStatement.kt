package suwayomi.tachidesk.server.database

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.BatchInsertStatement

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

open class MyBatchInsertStatement(
    table: Table,
    ignore: Boolean = false,
    shouldReturnGeneratedValues: Boolean = true,
) : BatchInsertStatement(table, ignore, shouldReturnGeneratedValues) {

    override fun prepareSQL(transaction: Transaction): String {
        val builder = QueryBuilder(false)

        builder.append("VALUES")
        for ((i, values) in arguments!!.withIndex()) {
            with(builder) {
                values.appendTo(prefix = "(", postfix = ")") { (col, value) ->
                    if (value is String) {
                        registerArgument(col, escapeNul(value))
                    } else {
                        registerArgument(col, value)
                    }
                }
            }
            if (i != arguments!!.size - 1) {
                builder.append(",\n")
            }
        }

        val columnsToInsert = arguments!!.first().map { it.first }
        val columnsExpr = columnsToInsert.takeIf { it.isNotEmpty() }?.joinToString(prefix = "(", postfix = ")") { transaction.identity(it) } ?: ""

        val valuesExpr = builder.toString()

        return "INSERT INTO ${transaction.identity(table)} $columnsExpr $valuesExpr"
    }

    private fun escapeNul(s: String): String {
        if (s.indexOf('\u0000') == -1) {
            return s
        }
        return s.filter { it != '\u0000' }
    }
}
