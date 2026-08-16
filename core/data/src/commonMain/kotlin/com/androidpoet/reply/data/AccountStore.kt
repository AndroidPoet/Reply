package com.androidpoet.reply.data

import com.androidpoet.reply.data.resources.Res
import com.androidpoet.reply.data.resources.avatar_0
import com.androidpoet.reply.data.resources.avatar_1
import com.androidpoet.reply.data.resources.avatar_10
import com.androidpoet.reply.data.resources.avatar_2
import com.androidpoet.reply.data.resources.avatar_3
import com.androidpoet.reply.data.resources.avatar_4
import com.androidpoet.reply.data.resources.avatar_5
import com.androidpoet.reply.data.resources.avatar_6
import com.androidpoet.reply.data.resources.avatar_7
import com.androidpoet.reply.data.resources.avatar_8
import com.androidpoet.reply.data.resources.avatar_9
import com.androidpoet.reply.data.resources.avatar_express
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Inject
@SingleIn(AppScope::class)
class AccountStore {
    private val allUserAccounts = listOf(
        Account(1L, 0L, "Jeff", "Hansen", "hikingfan@gmail.com", "hkngfan@outside.com", Res.drawable.avatar_10, true),
        Account(2L, 0L, "Jeff", "H", "jeffersonloveshiking@gmail.com", "jeffersonloveshiking@work.com", Res.drawable.avatar_2),
        Account(3L, 0L, "Jeff", "Hansen", "jeffersonc@google.com", "jeffersonc@gmail.com", Res.drawable.avatar_9),
    )

    private val allUserContactAccounts = listOf(
        Account(4L, 1L, "Tracy", "Alvarez", "tracealvie@gmail.com", "tracealvie@gravity.com", Res.drawable.avatar_1),
        Account(5L, 2L, "Allison", "Trabucco", "atrabucco222@gmail.com", "atrabucco222@work.com", Res.drawable.avatar_3),
        Account(6L, 3L, "Ali", "Connors", "aliconnors@gmail.com", "aliconnors@android.com", Res.drawable.avatar_5),
        Account(7L, 4L, "Alberto", "Williams", "albertowilliams124@gmail.com", "albertowilliams124@chromeos.com", Res.drawable.avatar_0),
        Account(8L, 5L, "Kim", "Alen", "alen13@gmail.com", "alen13@mountainview.gov", Res.drawable.avatar_7),
        Account(9L, 6L, "Google", "Express", "express@google.com", "express@gmail.com", Res.drawable.avatar_express),
        Account(10L, 7L, "Sandra", "Adams", "sandraadams@gmail.com", "sandraadams@textera.com", Res.drawable.avatar_2),
        Account(11L, 8L, "Trevor", "Hansen", "trevorhandsen@gmail.com", "trevorhandsen@express.com", Res.drawable.avatar_8),
        Account(12L, 9L, "Sean", "Holt", "sholt@gmail.com", "sholt@art.com", Res.drawable.avatar_6),
        Account(13L, 10L, "Frank", "Hawkins", "fhawkank@gmail.com", "fhawkank@thisisme.com", Res.drawable.avatar_4),
    )

    private val _userAccounts = MutableStateFlow(allUserAccounts)

    val userAccounts: StateFlow<List<Account>> = _userAccounts.asStateFlow()

    fun getDefaultUserAccount(): Account = allUserAccounts.first()

    fun getAllUserAccounts(): List<Account> = _userAccounts.value

    fun isUserAccount(uid: Long): Boolean = allUserAccounts.any { it.uid == uid }

    fun setCurrentUserAccount(accountId: Long): Boolean {
        val current = _userAccounts.value
        val updated = current.map { it.copy(isCurrentAccount = it.id == accountId) }
        if (updated == current) return false
        _userAccounts.value = updated
        return true
    }

    fun getContactAccountById(accountId: Long): Account =
        allUserContactAccounts.firstOrNull { it.id == accountId } ?: allUserContactAccounts.first()
}
