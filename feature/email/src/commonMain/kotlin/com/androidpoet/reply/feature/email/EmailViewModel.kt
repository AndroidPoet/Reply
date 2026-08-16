package com.androidpoet.reply.feature.email

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidpoet.reply.data.Email
import com.androidpoet.reply.data.EmailStore
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@AssistedInject
class EmailViewModel(
    @Assisted val emailId: Long,
    private val emailStore: EmailStore,
) : ViewModel() {
    val email: StateFlow<Email?> = emailStore.emails
        .map { list -> list.firstOrNull { it.id == emailId } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emailStore.get(emailId))

    fun toggleStar() = emailStore.toggleStar(emailId)

    fun delete() = emailStore.delete(emailId)

    @AssistedFactory
    fun interface Factory {
        fun create(emailId: Long): EmailViewModel
    }
}
