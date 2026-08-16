package com.androidpoet.reply.data

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Inject
@SingleIn(AppScope::class)
class AccountStore {
    private val _userAccounts = MutableStateFlow<List<Account>>(emptyList())
    val userAccounts: StateFlow<List<Account>> = _userAccounts.asStateFlow()

    private var contacts: List<Account> = emptyList()

    fun replace(users: List<Account>, contacts: List<Account>) {
        val current = _userAccounts.value.firstOrNull { it.isCurrentAccount }?.id
        _userAccounts.value = if (current != null && users.any { it.id == current }) {
            users.map { it.copy(isCurrentAccount = it.id == current) }
        } else {
            users
        }
        this.contacts = contacts
    }

    fun getDefaultUserAccount(): Account? = _userAccounts.value.firstOrNull { it.isCurrentAccount } ?: _userAccounts.value.firstOrNull()

    fun getAllUserAccounts(): List<Account> = _userAccounts.value

    fun isUserAccount(uid: Long): Boolean = _userAccounts.value.any { it.uid == uid }

    fun setCurrentUserAccount(accountId: Long): Boolean {
        val current = _userAccounts.value
        val updated = current.map { it.copy(isCurrentAccount = it.id == accountId) }
        if (updated == current) return false
        _userAccounts.value = updated
        return true
    }

    fun getContactAccountById(accountId: Long): Account? = contacts.firstOrNull { it.id == accountId }
}
