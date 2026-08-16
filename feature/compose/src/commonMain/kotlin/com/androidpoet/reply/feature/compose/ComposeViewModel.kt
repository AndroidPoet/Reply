package com.androidpoet.reply.feature.compose

import androidx.lifecycle.ViewModel
import com.androidpoet.reply.data.AccountStore
import com.androidpoet.reply.data.EmailStore
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

@AssistedInject
class ComposeViewModel(
    @Assisted replyToId: Long,
    emailStore: EmailStore,
    accountStore: AccountStore,
) : ViewModel() {
    val draft: ComposeDraft = ComposeDraft.create(replyToId, emailStore, accountStore)

    @AssistedFactory
    fun interface Factory {
        fun create(replyToId: Long): ComposeViewModel
    }
}
