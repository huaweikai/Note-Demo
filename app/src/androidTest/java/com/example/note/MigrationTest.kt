package com.example.note

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.note.db.MIGRATION_1_2
import com.example.note.db.NotesDatabase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val TEST_DB = "migration-test"

    @get: Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        NotesDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate1To2() {
        val dateTime = SimpleDateFormat(
            "yyyy/M/dd HH:mm:ss",
            Locale.CHINA
        ).format(System.currentTimeMillis())
        helper.createDatabase(TEST_DB, 1).apply {

            execSQL("INSERT INTO Notes (id, title, sub_title, date_time, note_text, img_path, web_link, color) VALUES ('1', '123', '345', '$dateTime', '111', '4321', '321', '3213')")
            close()
        }
        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)
        db.query("select * from Notes")
    }

}