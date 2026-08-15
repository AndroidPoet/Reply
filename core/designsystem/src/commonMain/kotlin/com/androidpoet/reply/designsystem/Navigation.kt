@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.androidpoet.reply.designsystem

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

/** The app-wide [SharedTransitionScope]; screens read it to morph the email card into the detail. */
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

/** The Nav3 entry's [AnimatedVisibilityScope]; provided per destination by the nav host. */
val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/** Transition names shared between the home card and the email detail card. */
object SharedKeys {
    fun emailCard(id: Long): String = "email_card_$id"
}

/**
 * Entry-scoped ViewModel: Nav3's ViewModelStore decorator owns the store, so this survives
 * configuration change and is cleared when the entry is popped.
 */
@Composable
inline fun <reified VM : ViewModel> rememberViewModel(
    key: String? = null,
    crossinline factory: () -> VM,
): VM = viewModel(key = key) { factory() }

/**
 * `MaterialContainerTransform` stand-in: shares this element's bounds with the destination card
 * of the same [key], resizing content to the animated bounds and clipping to [shape]. No-op when
 * there is no enclosing [SharedTransitionScope].
 */
@Composable
fun Modifier.sharedCardBounds(key: String, shape: androidx.compose.ui.graphics.Shape): Modifier {
    val sts = LocalSharedTransitionScope.current ?: return this
    val avs = androidx.navigation3.ui.LocalNavAnimatedContentScope.current
    return with(sts) {
        this@sharedCardBounds.sharedBounds(
            sharedContentState = rememberSharedContentState(key),
            animatedVisibilityScope = avs,
            boundsTransform = { _, _ ->
                androidx.compose.animation.core.tween(
                    durationMillis = 300,
                    easing = androidx.compose.animation.core.CubicBezierEasing(0.4f, 0f, 0.2f, 1f),
                )
            },
            resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
            clipInOverlayDuringTransition = OverlayClip(shape),
        )
    }
}
