package com.androidpoet.reply.feature.nav

import androidx.compose.runtime.Immutable
import com.androidpoet.reply.data.Mailbox
import com.androidpoet.reply.designsystem.resources.Res
import com.androidpoet.reply.designsystem.resources.ic_twotone_delete
import com.androidpoet.reply.designsystem.resources.ic_twotone_drafts
import com.androidpoet.reply.designsystem.resources.ic_twotone_error
import com.androidpoet.reply.designsystem.resources.ic_twotone_inbox
import com.androidpoet.reply.designsystem.resources.ic_twotone_send
import com.androidpoet.reply.designsystem.resources.ic_twotone_stars
import org.jetbrains.compose.resources.DrawableResource

@Immutable
sealed interface NavigationItem {
    data class Menu(
        val icon: DrawableResource,
        val title: String,
        val mailbox: Mailbox,
    ) : NavigationItem

    data class Divider(val title: String) : NavigationItem

    data class Folder(val name: String) : NavigationItem
}

object NavigationModel {
    val menuItems: List<NavigationItem.Menu> = listOf(
        NavigationItem.Menu(Res.drawable.ic_twotone_inbox, "Inbox", Mailbox.INBOX),
        NavigationItem.Menu(Res.drawable.ic_twotone_stars, "Starred", Mailbox.STARRED),
        NavigationItem.Menu(Res.drawable.ic_twotone_send, "Sent", Mailbox.SENT),
        NavigationItem.Menu(Res.drawable.ic_twotone_delete, "Trash", Mailbox.TRASH),
        NavigationItem.Menu(Res.drawable.ic_twotone_error, "Spam", Mailbox.SPAM),
        NavigationItem.Menu(Res.drawable.ic_twotone_drafts, "Drafts", Mailbox.DRAFTS),
    )

    fun items(folders: List<String>): List<NavigationItem> =
        menuItems + NavigationItem.Divider("Folders") + folders.map { NavigationItem.Folder(it) }

    fun titleFor(mailbox: Mailbox): String = menuItems.first { it.mailbox == mailbox }.title
}
