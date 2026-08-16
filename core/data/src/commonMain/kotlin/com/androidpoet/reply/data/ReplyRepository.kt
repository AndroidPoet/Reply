package com.androidpoet.reply.data

import com.androidpoet.reply.data.local.BundledData
import com.androidpoet.reply.data.remote.AccountDto
import com.androidpoet.reply.data.remote.AccountsPayload
import com.androidpoet.reply.data.remote.EmailDto
import com.androidpoet.reply.data.remote.EmailsPayload
import com.androidpoet.reply.data.remote.ReplyApi
import com.androidpoet.reply.database.AccountEntity
import com.androidpoet.reply.database.AttachmentEmbedded
import com.androidpoet.reply.database.EmailEntity
import com.androidpoet.reply.database.FolderEntity
import com.androidpoet.reply.database.ReplyDatabase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Inject
@SingleIn(AppScope::class)
class ReplyRepository(
    private val api: ReplyApi,
    private val database: ReplyDatabase,
    private val imageResolver: ImageResolver,
    private val emailStore: EmailStore,
    private val settings: SettingsRepository,
) {
    val source: StateFlow<DataSource> get() = imageResolver.source

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    suspend fun load() {
        loadBundled()
        refresh()
    }

    suspend fun loadBundled() {
        if (imageResolver.source.value != DataSource.NONE) return
        if (database.emailDao().count() == 0) {
            runCatching { persist(BundledData.accounts(), BundledData.emails()) }
        }
        imageResolver.setSource(DataSource.BUNDLED)
        emailStore.emails.first { it.isNotEmpty() }
    }

    suspend fun refresh() {
        if (_syncStatus.value == SyncStatus.Syncing) return
        _syncStatus.value = SyncStatus.Syncing
        runCatching { persist(api.accounts(), api.emails()) }
            .onSuccess {
                val now = Clock.System.now().toEpochMilliseconds()
                imageResolver.setSource(DataSource.REMOTE)
                settings.setLastSync(now)
                _syncStatus.value = SyncStatus.Synced(now)
            }
            .onFailure { error ->
                _syncStatus.value = SyncStatus.Failed(
                    message = error.message ?: error::class.simpleName.orEmpty(),
                    lastSyncEpochMillis = settings.lastSyncEpochMillis.first(),
                )
            }
    }

    private suspend fun persist(accounts: AccountsPayload, emails: EmailsPayload) {
        database.accountDao().insertIgnore(
            accounts.users.map { it.toEntity(isUser = true) } + accounts.contacts.map { it.toEntity(isUser = false) },
        )
        database.emailDao().insertIgnore(emails.emails.mapIndexed { index, dto -> dto.toEntity(index) })
        database.folderDao().replaceAll(emails.folders.mapIndexed { index, name -> FolderEntity(name, index) })
    }
}

private fun AccountDto.toEntity(isUser: Boolean) = AccountEntity(
    id = id,
    uid = uid,
    firstName = firstName,
    lastName = lastName,
    email = email,
    altEmail = altEmail,
    avatar = avatar,
    isUser = isUser,
    isCurrent = isUser && isCurrentAccount,
)

private fun EmailDto.toEntity(position: Int) = EmailEntity(
    id = id,
    senderId = senderId,
    recipientIds = recipientIds,
    subject = subject,
    body = body,
    attachments = attachments.map { AttachmentEmbedded(it.image, it.contentDesc) },
    isImportant = isImportant,
    isStarred = isStarred,
    mailbox = mailbox,
    position = position,
)
