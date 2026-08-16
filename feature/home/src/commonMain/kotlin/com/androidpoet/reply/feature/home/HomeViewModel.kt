package com.androidpoet.reply.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidpoet.reply.data.Email
import com.androidpoet.reply.data.EmailStore
import com.androidpoet.reply.data.Mailbox
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@AssistedInject
class HomeViewModel(
    @Assisted val mailbox: Mailbox,
    private val emailStore: EmailStore,
) : ViewModel() {
    val emails: StateFlow<List<Email>> = emailStore
        .getEmails(mailbox)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emailStore.snapshot(mailbox))

    fun setStarred(email: Email, starred: Boolean) = emailStore.setStarred(email.id, starred)


    @AssistedFactory
    fun interface Factory {
        fun create(mailbox: Mailbox): HomeViewModel
    }
}
