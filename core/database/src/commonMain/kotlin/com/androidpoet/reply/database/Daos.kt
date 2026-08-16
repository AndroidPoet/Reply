package com.androidpoet.reply.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY id")
    fun observeAll(): Flow<List<AccountEntity>>

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(accounts: List<AccountEntity>)

    @Query("UPDATE accounts SET isCurrent = (id = :id) WHERE isUser = 1")
    suspend fun setCurrent(id: Long)
}

@Dao
interface EmailDao {
    @Query("SELECT * FROM emails ORDER BY position")
    fun observeAll(): Flow<List<EmailEntity>>

    @Query("SELECT COUNT(*) FROM emails")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(emails: List<EmailEntity>)

    @Query("UPDATE emails SET isStarred = :starred WHERE id = :id")
    suspend fun setStarred(id: Long, starred: Boolean)

    @Query("UPDATE emails SET mailbox = :mailbox WHERE id = :id")
    suspend fun setMailbox(id: Long, mailbox: String)
}

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders ORDER BY position")
    fun observeAll(): Flow<List<FolderEntity>>

    @Query("DELETE FROM folders")
    suspend fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(folders: List<FolderEntity>)

    @Transaction
    suspend fun replaceAll(folders: List<FolderEntity>) {
        clear()
        insertAll(folders)
    }
}
