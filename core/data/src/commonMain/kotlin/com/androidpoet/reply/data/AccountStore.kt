package com.androidpoet.reply.data

import com.androidpoet.reply.database.AccountDao
import com.androidpoet.reply.database.AccountEntity
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Inject
@SingleIn(AppScope::class)
class AccountStore(
    private val dao: AccountDao,
    private val imageResolver: ImageResolver,
    private val scope: CoroutineScope,
) {
    val allAccounts: StateFlow<List<Account>> = combine(dao.observeAll(), imageResolver.source) { entities, _ ->
        entities.map { it.toAccount() }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val userAccounts: StateFlow<List<Account>> = allAccounts
        .map { list -> list.filter { it.isUser } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun getDefaultUserAccount(): Account? =
        userAccounts.value.firstOrNull { it.isCurrentAccount } ?: userAccounts.value.firstOrNull()

    fun getAllUserAccounts(): List<Account> = userAccounts.value

    fun isUserAccount(uid: Long): Boolean = userAccounts.value.any { it.uid == uid }

    fun setCurrentUserAccount(accountId: Long) {
        scope.launch { dao.setCurrent(accountId) }
    }

    private fun AccountEntity.toAccount() = Account(
        id = id,
        uid = uid,
        firstName = firstName,
        lastName = lastName,
        email = email,
        altEmail = altEmail,
        avatar = imageResolver.image(avatar),
        isCurrentAccount = isCurrent,
        isUser = isUser,
    )
}
