package com.example.note.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.note.bean.Note

@Database(
    entities = [Note::class],
    version = 2,
    exportSchema = true
)
abstract class NotesDatabase : RoomDatabase() {
    abstract val noteDao: NoteDao
}

val MIGRATION_1_2 = object :Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 新增update_time字段为Long类型
        database.execSQL("ALTER TABLE Notes ADD COLUMN update_time INTEGER NOT NULL DEFAULT 0")
        // 对于数据库中的 yyyy/M/dd HH:mm:ss strftime函数无法格式化，所以以当前时间作为处理
        database.execSQL("update Notes set update_time = strftime('%s', 'now')")
    }
}