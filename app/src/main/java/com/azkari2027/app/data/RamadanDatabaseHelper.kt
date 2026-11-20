package com.azkari2027.app.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File
import java.io.FileOutputStream

class RamadanDatabaseHelper(private val context: Context) {

    private val dbName = "RamadanTasks.db"
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

    fun getDays(): List<RamadanDay> = buildList {
        getDatabase().rawQuery("SELECT ID, NameAr, NameEn FROM Days ORDER BY ID ASC", null).use { cur ->
            while (cur.moveToNext()) {
                add(RamadanDay(cur.getInt(0), cur.getString(1)?.trim().orEmpty(), cur.getString(2)?.trim().orEmpty()))
            }
        }
    }

    fun getTaskTypes(): List<RamadanTaskType> = buildList {
        getDatabase().rawQuery("SELECT TypeID, Icon, TextAr, TextEn FROM TaskTypes ORDER BY TypeID ASC", null).use { cur ->
            while (cur.moveToNext()) {
                add(RamadanTaskType(cur.getInt(0), cur.getString(1).orEmpty(), cur.getString(2)?.trim().orEmpty(), cur.getString(3)?.trim().orEmpty()))
            }
        }
    }

    fun getTasksForDay(dayId: Int, typeId: Int? = null): List<RamadanTaskItem> {
        val sql = buildString {
            append(
                """
                SELECT t.TaskID, t.TypeID, t.Weight, t.TextAr, t.TextEn, t.OptionAr, t.OptionEn,
                       COALESCE(p.WeightAnswer, 0), COALESCE(p.CalculatedWeight, 0)
                FROM Tasks t
                LEFT JOIN DailyTasksProgress p ON p.TaskID = t.TaskID AND p.Day = ?
                """.trimIndent()
            )
            if (typeId != null) append(" WHERE t.TypeID = ? ")
            append(" ORDER BY t.TypeID ASC, t.TaskID ASC")
        }
        val args = if (typeId != null) arrayOf(dayId.toString(), typeId.toString()) else arrayOf(dayId.toString())
        return buildList {
            getDatabase().rawQuery(sql, args).use { cur ->
                while (cur.moveToNext()) {
                    val optArRaw = cur.getString(5)?.trim().orEmpty()
                    val optEnRaw = cur.getString(6)?.trim().orEmpty()
                    val optsAr = if (optArRaw.isEmpty()) emptyList() else optArRaw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    val optsEn = if (optEnRaw.isEmpty()) emptyList() else optEnRaw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    add(
                        RamadanTaskItem(
                            taskId = cur.getInt(0), typeId = cur.getInt(1), weight = cur.getInt(2),
                            textAr = cur.getString(3)?.trim().orEmpty(), textEn = cur.getString(4)?.trim().orEmpty(),
                            optionsAr = optsAr, optionsEn = optsEn,
                            weightAnswer = cur.getInt(7), calculatedWeight = cur.getDouble(8)
                        )
                    )
                }
            }
        }
    }

    fun updateTaskProgress(dayId: Int, task: RamadanTaskItem, answerIndex: Int) {
        val calculated: Double = if (task.isMultiOption) {
            val maxIdx = (task.optionsAr.size - 1).coerceAtLeast(1)
            (task.weight.toDouble() * answerIndex.coerceIn(0, maxIdx).toDouble()) / maxIdx.toDouble()
        } else {
            if (answerIndex > 0) task.weight.toDouble() else 0.0
        }
        val cv = ContentValues().apply {
            put("WeightAnswer", answerIndex)
            put("CalculatedWeight", calculated)
        }
        val updated = getDatabase().update(
            "DailyTasksProgress", cv, "Day = ? AND TaskID = ?",
            arrayOf(dayId.toString(), task.taskId.toString())
        )
        if (updated == 0) {
            cv.put("Day", dayId)
            cv.put("TaskID", task.taskId)
            getDatabase().insert("DailyTasksProgress", null, cv)
        }
    }

    fun getDayScoreSummary(dayId: Int): Pair<Double, Double> {
        var earned = 0.0
        var maxScore = 1.0
        getDatabase().rawQuery("SELECT SUM(Weight) FROM Tasks", null).use { cur ->
            if (cur.moveToFirst()) maxScore = cur.getDouble(0).coerceAtLeast(1.0)
        }
        getDatabase().rawQuery(
            "SELECT COALESCE(SUM(CalculatedWeight), 0) FROM DailyTasksProgress WHERE Day = ?",
            arrayOf(dayId.toString())
        ).use { cur ->
            if (cur.moveToFirst()) earned = cur.getDouble(0)
        }
        return earned to maxScore
    }

    fun resetAllProgress() {
        val cv = ContentValues().apply {
            put("WeightAnswer", 0)
            put("CalculatedWeight", 0)
        }
        getDatabase().update("DailyTasksProgress", cv, null, null)
    }
}
