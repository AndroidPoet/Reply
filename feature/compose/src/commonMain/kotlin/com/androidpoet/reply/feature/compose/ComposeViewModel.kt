package com.androidpoet.reply.feature.compose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.androidpoet.reply.data.Account
import com.androidpoet.reply.data.AccountStore
import com.androidpoet.reply.data.Email
import com.androidpoet.reply.data.EmailStore
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

/**
 * Draft state for `ComposeFragment`. [replyToId] < 0 starts a blank email; otherwise the draft is
 * pre-filled as a reply to that email.
 */
@AssistedInject
class ComposeViewModel(
    @Assisted val replyToId: Long,
    emailStore: EmailStore,
    private val accountStore: AccountStore,
) : ViewModel() {

    private val draft: Email = if (replyToId < 0) emailStore.create() else emailStore.createReplyTo(replyToId)

    var subject by mutableStateOf(draft.subject)
    var body by mutableStateOf(draft.body)
    var sender by mutableStateOf(draft.sender)

    /** Recipients that are not one of the current user's own accounts. */
    val recipients: List<Account> = draft.recipients.filterNot { accountStore.isUserAccount(it.uid) }

    /** All identities the user can send from (the sender spinner's entries). */
    val senderOptions: List<Account> get() = accountStore.getAllUserAccounts()

    @AssistedFactory
    fun interface Factory {
        fun create(replyToId: Long): ComposeViewModel
    }
}
