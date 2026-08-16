package com.androidpoet.reply.data

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

@Inject
@SingleIn(AppScope::class)
class EmailStore(private val accounts: AccountStore) {
    private var nextId = 1_000L
    private val _emails = MutableStateFlow<List<Email>>(emptyList())
    private val _folders = MutableStateFlow<List<String>>(emptyList())

    val emails: StateFlow<List<Email>> = _emails.asStateFlow()
    val folders: StateFlow<List<String>> = _folders.asStateFlow()

    fun replace(emails: List<Email>, folders: List<String>) {
        _emails.value = emails
        _folders.value = folders
    }

    fun getEmails(mailbox: Mailbox): Flow<List<Email>> = emails.map { it.inMailbox(mailbox) }

    fun snapshot(mailbox: Mailbox): List<Email> = _emails.value.inMailbox(mailbox)

    private fun List<Email>.inMailbox(mailbox: Mailbox): List<Email> = when (mailbox) {
        Mailbox.STARRED -> filter { it.isStarred }
        else -> filter { it.mailbox == mailbox }
    }

    fun get(id: Long): Email? = _emails.value.firstOrNull { it.id == id }

    fun create(): Email = Email(nextId++, requireNotNull(accounts.getDefaultUserAccount()) { "No accounts loaded" })

    fun createReplyTo(replyToId: Long): Email {
        val replyTo = get(replyToId) ?: return create()
        return Email(
            id = nextId++,
            sender = replyTo.recipients.firstOrNull() ?: requireNotNull(accounts.getDefaultUserAccount()) { "No accounts loaded" },
            recipients = listOf(replyTo.sender) + replyTo.recipients,
            subject = replyTo.subject,
            isStarred = replyTo.isStarred,
            isImportant = replyTo.isImportant,
        )
    }

    fun delete(id: Long) = update(id) { copy(mailbox = Mailbox.TRASH) }

    fun toggleStar(id: Long) = update(id) { copy(isStarred = !isStarred) }

    fun setStarred(id: Long, starred: Boolean) = update(id) { copy(isStarred = starred) }

    fun update(id: Long, with: Email.() -> Email) {
        _emails.value = _emails.value.map { if (it.id == id) it.with() else it }
    }

}
