package com.androidpoet.reply.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: Long,
    val uid: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val altEmail: String,
    val avatar: String,
    val isUser: Boolean,
    val isCurrent: Boolean,
)

@Entity(tableName = "emails")
data class EmailEntity(
    @PrimaryKey val id: Long,
    val senderId: Long,
    val recipientIds: List<Long>,
    val subject: String,
    val body: String,
    val attachments: List<AttachmentEmbedded>,
    val isImportant: Boolean,
    val isStarred: Boolean,
    val mailbox: String,
    val position: Int,
)

data class AttachmentEmbedded(val image: String, val contentDesc: String)

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey val name: String,
    val position: Int,
)
