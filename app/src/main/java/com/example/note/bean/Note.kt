package com.example.note.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.note.fragment.NoteBottomSheetFragment
import com.example.note.util.dateString
import kotlin.random.Random

@Entity(tableName = "Notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "title")
    val title: String = "",

    @ColumnInfo(name = "sub_title")
    val subTitle: String = "",

    @ColumnInfo(name = "note_text")
    val noteText: String = "",

    @ColumnInfo(name = "img_path")
    val imgPath: String? = null,

    @ColumnInfo(name = "web_link")
    val webLink: String? = null,

    @ColumnInfo(name = "color")
    val color: String = NoteBottomSheetFragment.colorList[Random.nextInt(
        0,
        NoteBottomSheetFragment.colorList.size - 1
    )],
    @ColumnInfo(name = "update_time")
    val updateTime: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "date_time")
    val dateTime: String = updateTime.dateString,
)