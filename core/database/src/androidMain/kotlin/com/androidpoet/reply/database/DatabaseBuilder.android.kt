package com.androidpoet.reply.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun replyDatabaseBuilder(context: Context): RoomDatabase.Builder<ReplyDatabase> {
    val appContext = context.applicationContext
    return Room.databaseBuilder<ReplyDatabase>(appContext, appContext.getDatabasePath("reply.db").absolutePath)
}
