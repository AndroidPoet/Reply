package com.androidpoet.reply.feature.compose

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.androidpoet.reply.data.Account
import com.androidpoet.reply.data.AccountStore
import com.androidpoet.reply.data.Email
import com.androidpoet.reply.data.EmailStore

@Stable
class ComposeDraft(
    initial: Email,
    val recipients: List<Account>,
    val senderOptions: List<Account>,
) {
    var subject by mutableStateOf(initial.subject)
    var body by mutableStateOf(initial.body)
    var sender by mutableStateOf(initial.sender)

    companion object {
        fun create(replyToId: Long, emailStore: EmailStore, accountStore: AccountStore): ComposeDraft {
            val email = if (replyToId < 0) emailStore.create() else emailStore.createReplyTo(replyToId)
            return ComposeDraft(
                initial = email,
                recipients = email.recipients.filterNot { accountStore.isUserAccount(it.uid) },
                senderOptions = accountStore.getAllUserAccounts(),
            )
        }
    }
}
