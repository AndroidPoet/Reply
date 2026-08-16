package com.androidpoet.reply.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.navigation3.runtime.NavKey
import com.androidpoet.reply.data.Email
import com.androidpoet.reply.data.EmailStore
import com.androidpoet.reply.data.Mailbox

@Stable
sealed interface Transform {
    val entering: Boolean

    data class CardToEmail(
        val email: Email,
        val cardBounds: Rect,
        val topLeftCornerPx: Float,
        override val entering: Boolean,
    ) : Transform

    data class FabToCompose(
        val replyToId: Long,
        val fabBounds: Rect,
    ) : Transform {
        override val entering: Boolean get() = true
    }
}

@Stable
class ReplyNavigator(private val emailStore: EmailStore) {
    val backStack = mutableStateListOf<NavKey>(HomeRoute())

    var lastChangeWasPop by mutableStateOf(false)
        private set

    var transform by mutableStateOf<Transform?>(null)

    private val cardGeometry = HashMap<Long, Pair<Rect, Float>>()
    private val routesByContentKey = HashMap<String, NavKey>()

    init {
        register(backStack.first())
    }

    fun routeFor(contentKey: Any): NavKey? = routesByContentKey[contentKey.toString()]

    private fun register(route: NavKey) {
        routesByContentKey[route.toString()] = route
    }

    private fun push(route: NavKey) {
        lastChangeWasPop = false
        register(route)
        backStack.add(route)
    }

    val current: NavKey get() = backStack.last()

    val currentMailbox: Mailbox
        get() = (backStack.firstOrNull { it is HomeRoute } as? HomeRoute)?.mailbox ?: Mailbox.INBOX

    val currentEmailId: Long get() = (current as? EmailRoute)?.emailId ?: -1L

    fun navigateToHome(mailbox: Mailbox) {
        backStack.clear()
        push(HomeRoute(mailbox))
    }

    fun navigateToCompose(replyToId: Long, fabBounds: Rect) {
        transform = Transform.FabToCompose(replyToId, fabBounds)
        push(ComposeRoute(replyToId))
    }

    fun openEmail(email: Email, cardBounds: Rect, topLeftCornerPx: Float) {
        cardGeometry[email.id] = cardBounds to topLeftCornerPx
        transform = Transform.CardToEmail(email, cardBounds, topLeftCornerPx, entering = true)
        push(EmailRoute(email.id))
    }

    fun openSearch() = push(SearchRoute)

    fun deleteCurrentEmail() {
        emailStore.delete(currentEmailId)
        transform = null
        pop()
    }

    fun goBack() {
        when (val top = current) {
            is EmailRoute -> {
                val email = emailStore.get(top.emailId)
                val geometry = cardGeometry[top.emailId]
                if (email != null && geometry != null) {
                    transform = Transform.CardToEmail(email, geometry.first, geometry.second, entering = false)
                }
                pop()
            }
            is HomeRoute -> if (backStack.size == 1 && top.mailbox != Mailbox.INBOX) navigateToHome(Mailbox.INBOX)
            else -> pop()
        }
    }

    private fun pop() {
        if (backStack.size > 1) {
            lastChangeWasPop = true
            backStack.removeAt(backStack.lastIndex)
        }
    }
}

@Composable
fun rememberReplyNavigator(emailStore: EmailStore): ReplyNavigator = remember(emailStore) { ReplyNavigator(emailStore) }
