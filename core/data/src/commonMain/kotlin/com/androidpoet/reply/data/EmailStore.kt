package com.androidpoet.reply.data

import com.androidpoet.reply.database.EmailDao
import com.androidpoet.reply.database.EmailEntity
import com.androidpoet.reply.database.FolderDao
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Inject
@SingleIn(AppScope::class)
class EmailStore(
    private val emailDao: EmailDao,
    folderDao: FolderDao,
    private val accounts: AccountStore,
    private val imageResolver: ImageResolver,
    private val scope: CoroutineScope,
) {
    private var nextId = 1_000L

    val emails: StateFlow<List<Email>> = combine(
        emailDao.observeAll(),
        accounts.allAccounts,
        imageResolver.source,
    ) { entities, accountList, _ ->
        val byId = accountList.associateBy { it.id }
        entities.mapNotNull { it.toEmail(byId) }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val folders: StateFlow<List<String>> = folderDao.observeAll()
        .map { list -> list.map { it.name } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun getEmails(mailbox: Mailbox): Flow<List<Email>> = emails.map { it.inMailbox(mailbox) }

    fun snapshot(mailbox: Mailbox): List<Email> = emails.value.inMailbox(mailbox)

    private fun List<Email>.inMailbox(mailbox: Mailbox): List<Email> = when (mailbox) {
        Mailbox.STARRED -> filter { it.isStarred }
        else -> filter { it.mailbox == mailbox }
    }

    fun get(id: Long): Email? = emails.value.firstOrNull { it.id == id }

    fun create(): Email = Email(nextId++, defaultAccount())

    fun createReplyTo(replyToId: Long): Email {
        val replyTo = get(replyToId) ?: return create()
        return Email(
            id = nextId++,
            sender = replyTo.recipients.firstOrNull() ?: defaultAccount(),
            recipients = listOf(replyTo.sender) + replyTo.recipients,
            subject = replyTo.subject,
            isStarred = replyTo.isStarred,
            isImportant = replyTo.isImportant,
        )
    }

    fun delete(id: Long) {
        scope.launch { emailDao.setMailbox(id, Mailbox.TRASH.name) }
    }

    fun toggleStar(id: Long) {
        val current = get(id) ?: return
        setStarred(id, !current.isStarred)
    }

    fun setStarred(id: Long, starred: Boolean) {
        scope.launch { emailDao.setStarred(id, starred) }
    }

    private fun defaultAccount(): Account = requireNotNull(accounts.getDefaultUserAccount()) { "No accounts loaded" }

    private fun EmailEntity.toEmail(accounts: Map<Long, Account>): Email? {
        val sender = accounts[senderId] ?: return null
        return Email(
            id = id,
            sender = sender,
            recipients = recipientIds.mapNotNull { accounts[it] },
            subject = subject,
            body = body,
            attachments = attachments.map { EmailAttachment(imageResolver.image(it.image), it.contentDesc) },
            isImportant = isImportant,
            isStarred = isStarred,
            mailbox = runCatching { Mailbox.valueOf(mailbox) }.getOrDefault(Mailbox.INBOX),
        )
    }
}
