package com.androidpoet.reply.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

@Database(
    entities = [AccountEntity::class, EmailEntity::class, FolderEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
@ConstructedBy(ReplyDatabaseConstructor::class)
abstract class ReplyDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun emailDao(): EmailDao
    abstract fun folderDao(): FolderDao
}

@Suppress("KotlinNoActualForExpect")
expect object ReplyDatabaseConstructor : RoomDatabaseConstructor<ReplyDatabase> {
    override fun initialize(): ReplyDatabase
}

fun RoomDatabase.Builder<ReplyDatabase>.buildReplyDatabase(): ReplyDatabase =
    setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
