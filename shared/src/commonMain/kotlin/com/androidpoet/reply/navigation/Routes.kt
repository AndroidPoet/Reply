package com.androidpoet.reply.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.togetherWith
import androidx.navigation3.runtime.NavKey
import com.androidpoet.reply.data.Mailbox
import com.androidpoet.reply.designsystem.motion.MaterialMotion
import kotlinx.serialization.Serializable

@Serializable
data class HomeRoute(val mailbox: Mailbox = Mailbox.INBOX) : NavKey

@Serializable
data class EmailRoute(val emailId: Long) : NavKey

@Serializable
data class ComposeRoute(val replyToId: Long = -1L) : NavKey

@Serializable
data object SearchRoute : NavKey

internal fun materialTransition(from: NavKey?, to: NavKey?, pop: Boolean, zIndex: Float): ContentTransform {
    val transform = when {
        !pop && to is EmailRoute -> MaterialMotion.instantEnter togetherWith MaterialMotion.elevationScaleExit
        pop && from is EmailRoute -> MaterialMotion.elevationScaleReenter togetherWith MaterialMotion.instantExit
        !pop && to is ComposeRoute -> MaterialMotion.instantEnter togetherWith MaterialMotion.elevationScaleExit
        pop && from is ComposeRoute -> MaterialMotion.elevationScaleReenter togetherWith MaterialMotion.holdThenVanish
        !pop && to is SearchRoute -> MaterialMotion.sharedAxisZForwardEnter togetherWith MaterialMotion.sharedAxisZForwardExit
        pop && from is SearchRoute -> MaterialMotion.sharedAxisZBackwardEnter togetherWith MaterialMotion.sharedAxisZBackwardExit
        else -> MaterialMotion.fadeThroughEnter togetherWith MaterialMotion.fadeThroughExit
    }
    transform.targetContentZIndex = zIndex
    return transform
}
