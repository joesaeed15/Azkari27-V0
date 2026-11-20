package com.azkari2027.app.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File
import java.io.FileOutputStream

class AzkariDatabaseHelper(private val context: Context) {

    private val dbName = "azkari_content.db"
    private val dbFile: File get() = context.getDatabasePath(dbName)
    @Volatile private var db: SQLiteDatabase? = null

    @Synchronized
    private fun getDatabase(): SQLiteDatabase {
        db?.takeIf { it.isOpen }?.let { return it }
        if (!dbFile.exists() || dbFile.length() == 0L) {
            dbFile.parentFile?.mkdirs()
            context.assets.open(dbName).use { input ->
                FileOutputStream(dbFile).use { output -> input.copyTo(output) }
            }
        }
        return SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READWRITE).also { db = it }
    }

    fun getAzkarCategories(languageId: Int): List<Category> {
        val sql = """
            SELECT c.CAT_ID, c.CAT_NAME, c.CAT_TYPE, c.CAT_IS_USER_DEFINED,
                   c.CAT_COUNT, c.CAT_TIME, c.LAN_ID, c.CAT_ICON,
                   COUNT(p.PRA_ID), COALESCE(SUM(p.PRA_REPETITION), 0)
            FROM CATEGORIES c
            LEFT JOIN PRAYERS p ON p.CAT_ID = c.CAT_ID AND p.LAN_ID = c.LAN_ID
            WHERE c.CAT_TYPE = 'A' AND c.LAN_ID = ?
            GROUP BY c.CAT_ID ORDER BY c.CAT_ID ASC
        """.trimIndent()
        return buildList {
            getDatabase().rawQuery(sql, arrayOf(languageId.toString())).use { cur ->
                while (cur.moveToNext()) {
                    add(
                        Category(
                            id = cur.getInt(0), name = cur.getString(1).orEmpty(),
                            type = cur.getString(2) ?: "A", isUserDefined = cur.getInt(3) == 1,
                            count = cur.getInt(4), timeSeconds = cur.getString(5)?.toLongOrNull() ?: 0L,
                            languageId = cur.getInt(6), icon = cur.getString(7).orEmpty(),
                            prayerCount = cur.getInt(8), totalRepetitions = cur.getInt(9)
                        )
                    )
                }
            }
        }
    }

    fun getTasbeehCategories(languageId: Int): List<Category> {
        val sql = """
            SELECT CAT_ID, CAT_NAME, CAT_TYPE, CAT_IS_USER_DEFINED,
                   CAT_COUNT, CAT_TIME, LAN_ID, CAT_ICON
            FROM CATEGORIES WHERE CAT_TYPE = 'T' AND LAN_ID = ? ORDER BY CAT_ID ASC
        """.trimIndent()
        return buildList {
            getDatabase().rawQuery(sql, arrayOf(languageId.toString())).use { cur ->
                while (cur.moveToNext()) {
                    add(
                        Category(
                            id = cur.getInt(0), name = cur.getString(1).orEmpty(),
                            type = "T", isUserDefined = cur.getInt(3) == 1,
                            count = cur.getInt(4), timeSeconds = cur.getString(5)?.toLongOrNull() ?: 0L,
                            languageId = cur.getInt(6), icon = cur.getString(7) ?: "masbaha_default"
                        )
                    )
                }
            }
        }
    }

    fun getAllLoggedCategories(languageId: Int): List<Category> {
        val sql = """
            SELECT CAT_ID, CAT_NAME, CAT_TYPE, CAT_IS_USER_DEFINED,
                   CAT_COUNT, CAT_TIME, LAN_ID, CAT_ICON
            FROM CATEGORIES WHERE LAN_ID = ? ORDER BY CAT_COUNT DESC, CAT_ID ASC
        """.trimIndent()
        return buildList {
            getDatabase().rawQuery(sql, arrayOf(languageId.toString())).use { cur ->
                while (cur.moveToNext()) {
                    add(
                        Category(
                            id = cur.getInt(0), name = cur.getString(1).orEmpty(),
                            type = cur.getString(2) ?: "A", isUserDefined = cur.getInt(3) == 1,
                            count = cur.getInt(4), timeSeconds = cur.getString(5)?.toLongOrNull() ?: 0L,
                            languageId = cur.getInt(6), icon = cur.getString(7).orEmpty()
                        )
                    )
                }
            }
        }
    }

    fun getCategoryById(categoryId: Int): Category? {
        val sql = """
            SELECT CAT_ID, CAT_NAME, CAT_TYPE, CAT_IS_USER_DEFINED,
                   CAT_COUNT, CAT_TIME, LAN_ID, CAT_ICON
            FROM CATEGORIES WHERE CAT_ID = ?
        """.trimIndent()
        getDatabase().rawQuery(sql, arrayOf(categoryId.toString())).use { cur ->
            if (cur.moveToFirst()) {
                return Category(
                    id = cur.getInt(0), name = cur.getString(1).orEmpty(),
                    type = cur.getString(2) ?: "A", isUserDefined = cur.getInt(3) == 1,
                    count = cur.getInt(4), timeSeconds = cur.getString(5)?.toLongOrNull() ?: 0L,
                    languageId = cur.getInt(6), icon = cur.getString(7).orEmpty()
                )
            }
        }
        return null
    }

    fun getPrayersForCategory(categoryId: Int, languageId: Int): List<Prayer> {
        val sql = """
            SELECT PRA_ID, CAT_ID, PRA_TEXT, PRA_PREFE, PRA_REPETITION, PRA_ORDER, LAN_ID, PRA_AUD
            FROM PRAYERS WHERE CAT_ID = ? AND LAN_ID = ?
            ORDER BY PRA_ORDER ASC, PRA_ID ASC
        """.trimIndent()
        return buildList {
            getDatabase().rawQuery(sql, arrayOf(categoryId.toString(), languageId.toString())).use { cur ->
                while (cur.moveToNext()) {
                    add(
                        Prayer(
                            id = cur.getInt(0), categoryId = cur.getInt(1),
                            text = cur.getString(2).orEmpty(), virtue = cur.getString(3).orEmpty(),
                            repetition = cur.getInt(4).coerceAtLeast(1), order = cur.getInt(5),
                            languageId = cur.getInt(6), audioName = cur.getString(7)?.trim().orEmpty()
                        )
                    )
                }
            }
        }
    }

    fun incrementCategoryCountAndTime(categoryId: Int, countDelta: Int, secondsDelta: Long) {
        val existing = getCategoryById(categoryId) ?: return
        val cv = ContentValues().apply {
            put("CAT_COUNT", (existing.count + countDelta).coerceAtLeast(0))
            put("CAT_TIME", (existing.timeSeconds + secondsDelta).coerceAtLeast(0L).toString())
        }
        getDatabase().update("CATEGORIES", cv, "CAT_ID = ?", arrayOf(categoryId.toString()))
    }

    fun addUserTasbeeh(name: String, languageId: Int): Long {
        val cv = ContentValues().apply {
            put("CAT_NAME", name.trim())
            put("CAT_TYPE", "T")
            put("CAT_IS_USER_DEFINED", 1)
            put("CAT_COUNT", 0)
            put("CAT_TIME", "0")
            put("LAN_ID", languageId)
            put("CAT_ICON", "masbaha_default")
        }
        return getDatabase().insert("CATEGORIES", null, cv)
    }

    fun deleteUserTasbeeh(categoryId: Int): Boolean =
        getDatabase().delete("CATEGORIES", "CAT_ID = ? AND CAT_IS_USER_DEFINED = 1", arrayOf(categoryId.toString())) > 0

    fun resetAllLogs(languageId: Int) {
        val cv = ContentValues().apply {
            put("CAT_COUNT", 0)
            put("CAT_TIME", "0")
        }
        getDatabase().update("CATEGORIES", cv, "LAN_ID = ?", arrayOf(languageId.toString()))
    }
}
