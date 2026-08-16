package com.androidpoet.reply.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import com.androidpoet.reply.designsystem.motion.Interpolators

private const val EDIT_PATH =
    "M 3 17.25 L 3 21 L 6.75 21 L 17.81 9.94 L 14.06 6.19 L 3 17.25 Z M 21.41 6.34 L 17.66 2.59 L 15.13 5.13 L 18.88 8.88 L 21.41 6.34 Z"
private const val REPLY_PATH =
    "M 13 9 L 13 5 L 6 12 L 13 19 L 13 14.9 C 18 14.9 21.5 16.5 24 20 C 23 15 20 10 13 9 Z"
private const val REPLY_CHEVRON_PATH = "M 7 8 L 7 5 L 0 12 L 7 19 L 7 16 L 3 12 L 7 8 Z"

private const val TOTAL_MS = 300f

private fun seg(t: Float, start: Float, duration: Float): Float =
    Interpolators.FastOutSlowIn.transform(((t - start) / duration).coerceIn(0f, 1f))

@Composable
fun EditReplyIcon(
    activated: Boolean,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    val edit = remember { PathParser().parsePathString(EDIT_PATH).toPath() }
    val reply = remember { PathParser().parsePathString(REPLY_PATH).toPath() }
    val chevron = remember { PathParser().parsePathString(REPLY_CHEVRON_PATH).toPath() }

    val time = remember { Animatable(if (activated) TOTAL_MS else 0f) }
    LaunchedEffect(activated) {
        val target = if (activated) TOTAL_MS else 0f
        if (time.value != target) {
            time.snapTo(if (activated) 0f else TOTAL_MS)
            time.animateTo(target, tween(TOTAL_MS.toInt(), easing = LinearEasing))
        }
    }

    Canvas(modifier) {
        val unit = size.minDimension / 24f
        scale(unit, pivot = Offset.Zero) {
            if (activated) drawEditToReply(time.value, edit, reply, chevron, tint)
            else drawReplyToEdit(TOTAL_MS - time.value, edit, reply, chevron, tint)
        }
    }
}

private fun DrawScope.drawEditToReply(t: Float, edit: Path, reply: Path, chevron: Path, tint: Color) {
    val editScale = 1f - seg(t, 0f, 100f)
    if (editScale > 0f) {
        scale(editScale, pivot = Offset(12f, 12f)) { drawPath(edit, tint) }
    }

    val replyScale = seg(t, 100f, 200f)
    val rotation = 30f * (1f - seg(t, 100f, 177f))
    val chevronX = 15f * (1f - seg(t, 105f, 195f))
    if (t >= 100f) {
        withTransform({
            translate(0f, -0.8f)
            rotate(rotation, pivot = Offset(22f, 16f))
            scale(replyScale, pivot = Offset(22f, 16f))
        }) {
            drawPath(reply, tint)
            if (t >= 188f) translate(chevronX, 0f) { drawPath(chevron, tint) }
        }
    }
}

private fun DrawScope.drawReplyToEdit(t: Float, edit: Path, reply: Path, chevron: Path, tint: Color) {
    val replyScale = 1f - seg(t, 0f, 100f)
    val rotation = 30f * seg(t, 0f, 100f)
    val chevronX = 15f * seg(t, 0f, 100f)
    val chevronAlpha = 1f - (t / 31f).coerceIn(0f, 1f)
    if (replyScale > 0f) {
        withTransform({
            translate(0f, -0.8f)
            rotate(rotation, pivot = Offset(22f, 16f))
            scale(replyScale, pivot = Offset(22f, 16f))
        }) {
            drawPath(reply, tint)
            if (chevronAlpha > 0f) translate(chevronX, 0f) { drawPath(chevron, tint.copy(alpha = tint.alpha * chevronAlpha)) }
        }
    }

    if (t >= 100f) {
        val editScale = seg(t, 100f, 200f)
        scale(editScale, pivot = Offset(12f, 12f)) { drawPath(edit, tint) }
    }
}
