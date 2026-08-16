package com.androidpoet.reply.data

import androidx.compose.runtime.Immutable
@Immutable
data class Email(
    val id: Long,
    val sender: Account,
    val recipients: List<Account> = emptyList(),
    val subject: String = "",
    val body: String = "",
    val attachments: List<EmailAttachment> = emptyList(),
    val isImportant: Boolean = false,
    val isStarred: Boolean = false,
    val mailbox: Mailbox = Mailbox.INBOX,
) {
    val senderPreview: String get() = "${sender.fullName} - 4 hrs ago"
    val hasBody: Boolean get() = body.isNotBlank()
    val hasAttachments: Boolean get() = attachments.isNotEmpty()
    val recipientsPreview: String
        get() = recipients.map { it.firstName }.fold("") { acc, name -> "$name, $acc" }
}
