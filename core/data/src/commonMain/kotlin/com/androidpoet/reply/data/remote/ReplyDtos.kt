package com.androidpoet.reply.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class AccountsPayload(
    val users: List<AccountDto>,
    val contacts: List<AccountDto>,
)

@Serializable
data class AccountDto(
    val id: Long,
    val uid: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val altEmail: String,
    val avatar: String,
    val isCurrentAccount: Boolean = false,
)

@Serializable
data class EmailsPayload(
    val folders: List<String>,
    val emails: List<EmailDto>,
)

@Serializable
data class EmailDto(
    val id: Long,
    val senderId: Long,
    val recipientIds: List<Long>,
    val subject: String,
    val body: String,
    val attachments: List<AttachmentDto> = emptyList(),
    val isImportant: Boolean = false,
    val isStarred: Boolean = false,
    val mailbox: String = "INBOX",
)

@Serializable
data class AttachmentDto(
    val image: String,
    val contentDesc: String,
)
