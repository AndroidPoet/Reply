package com.androidpoet.reply.database

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

fun replyDatabaseBuilder(): RoomDatabase.Builder<ReplyDatabase> {
    val dir = File(System.getProperty("user.home"), ".reply").apply { mkdirs() }
    return Room.databaseBuilder<ReplyDatabase>(File(dir, "reply.db").absolutePath)
}

fun inMemoryReplyDatabaseBuilder(): RoomDatabase.Builder<ReplyDatabase> = Room.inMemoryDatabaseBuilder<ReplyDatabase>()
