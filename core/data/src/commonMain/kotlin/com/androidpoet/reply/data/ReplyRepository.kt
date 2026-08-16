package com.androidpoet.reply.data

import com.androidpoet.reply.data.local.BundledData
import com.androidpoet.reply.data.remote.AccountDto
import com.androidpoet.reply.data.remote.AccountsPayload
import com.androidpoet.reply.data.remote.EmailDto
import com.androidpoet.reply.data.remote.EmailsPayload
import com.androidpoet.reply.data.remote.ReplyApi
import com.androidpoet.reply.data.resources.Res
import com.androidpoet.reply.data.resources.allDrawableResources
import com.github.panpf.sketch.fetch.newComposeResourceUri
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jetbrains.compose.resources.ExperimentalResourceApi

private const val IMAGE_BASE_URL =
    "https://raw.githubusercontent.com/AndroidPoet/Reply/main/core/data/src/commonMain/composeResources/drawable"

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
        runCatching { publish(BundledData.accounts(), BundledData.emails(), remote = false) }
            .onSuccess { _source.value = DataSource.BUNDLED }
    }

    suspend fun refresh() {
        runCatching { publish(api.accounts(), api.emails(), remote = true) }
            .onSuccess { _source.value = DataSource.REMOTE }
    }

    private fun publish(accounts: AccountsPayload, emails: EmailsPayload, remote: Boolean) {
        val image: (String) -> ReplyImage = { name ->
            ReplyImage(
                uri = if (remote) remoteImageUri(name) else bundledImageUri(name),
                fallback = Res.allDrawableResources[name.substringBeforeLast('.')],
            )
        }
        val users = accounts.users.map { it.toAccount(image) }
        val contacts = accounts.contacts.map { it.toAccount(image) }
        val byId = (users + contacts).associateBy { it.id }
        accountStore.replace(users, contacts)
        emailStore.replace(
            emails = emails.emails.mapNotNull { it.toEmail(byId, image) },
            folders = emails.folders,
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
private fun bundledImageUri(fileName: String): String = newComposeResourceUri(Res.getUri("drawable/$fileName"))

private fun remoteImageUri(fileName: String): String = "$IMAGE_BASE_URL/$fileName"

private fun AccountDto.toAccount(image: (String) -> ReplyImage) = Account(
    id = id,
    uid = uid,
    firstName = firstName,
    lastName = lastName,
    email = email,
    altEmail = altEmail,
    avatar = image(avatar),
    isCurrentAccount = isCurrentAccount,
)

private fun EmailDto.toEmail(accounts: Map<Long, Account>, image: (String) -> ReplyImage): Email? {
    val sender = accounts[senderId] ?: return null
    return Email(
        id = id,
        sender = sender,
        recipients = recipientIds.mapNotNull { accounts[it] },
        subject = subject,
        body = body,
        attachments = attachments.map { EmailAttachment(image(it.image), it.contentDesc) },
        isImportant = isImportant,
        isStarred = isStarred,
        mailbox = runCatching { Mailbox.valueOf(mailbox) }.getOrDefault(Mailbox.INBOX),
    )
}
