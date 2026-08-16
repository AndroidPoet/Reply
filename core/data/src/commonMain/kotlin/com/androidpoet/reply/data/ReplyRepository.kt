package com.androidpoet.reply.data

import com.androidpoet.reply.data.local.BundledData
import com.androidpoet.reply.data.remote.AccountDto
import com.androidpoet.reply.data.remote.AccountsPayload
import com.androidpoet.reply.data.remote.EmailDto
import com.androidpoet.reply.data.remote.EmailsPayload
import com.androidpoet.reply.data.remote.ReplyApi
import com.androidpoet.reply.data.resources.Res
import com.androidpoet.reply.data.resources.allDrawableResources
import com.androidpoet.reply.data.resources.avatar_0
import com.androidpoet.reply.data.resources.paris_1
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi

enum class DataSource { NONE, BUNDLED, REMOTE }

@Inject
@SingleIn(AppScope::class)
class ReplyRepository(
    private val api: ReplyApi,
    private val accountStore: AccountStore,
    private val emailStore: EmailStore,
) {
    private val _source = MutableStateFlow(DataSource.NONE)
    val source: StateFlow<DataSource> = _source.asStateFlow()

    suspend fun load() {
        loadBundled()
        refresh()
    }

    suspend fun loadBundled() {
        if (_source.value != DataSource.NONE) return
        runCatching { publish(BundledData.accounts(), BundledData.emails()) }
            .onSuccess { _source.value = DataSource.BUNDLED }
    }

    suspend fun refresh() {
        runCatching { publish(api.accounts(), api.emails()) }
            .onSuccess { _source.value = DataSource.REMOTE }
    }

    private fun publish(accounts: AccountsPayload, emails: EmailsPayload) {
        val users = accounts.users.map { it.toAccount() }
        val contacts = accounts.contacts.map { it.toAccount() }
        val byId = (users + contacts).associateBy { it.id }
        accountStore.replace(users, contacts)
        emailStore.replace(
            emails = emails.emails.mapNotNull { it.toEmail(byId) },
            folders = emails.folders,
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
private fun drawable(name: String, fallback: DrawableResource): DrawableResource =
    Res.allDrawableResources[name] ?: fallback

private fun AccountDto.toAccount() = Account(
    id = id,
    uid = uid,
    firstName = firstName,
    lastName = lastName,
    email = email,
    altEmail = altEmail,
    avatar = drawable(avatar, Res.drawable.avatar_0),
    isCurrentAccount = isCurrentAccount,
)

private fun EmailDto.toEmail(accounts: Map<Long, Account>): Email? {
    val sender = accounts[senderId] ?: return null
    return Email(
        id = id,
        sender = sender,
        recipients = recipientIds.mapNotNull { accounts[it] },
        subject = subject,
        body = body,
        attachments = attachments.map { EmailAttachment(drawable(it.image, Res.drawable.paris_1), it.contentDesc) },
        isImportant = isImportant,
        isStarred = isStarred,
        mailbox = runCatching { Mailbox.valueOf(mailbox) }.getOrDefault(Mailbox.INBOX),
    )
}
