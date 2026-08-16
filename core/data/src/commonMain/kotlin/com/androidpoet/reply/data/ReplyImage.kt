package com.androidpoet.reply.data

import androidx.compose.runtime.Immutable
import org.jetbrains.compose.resources.DrawableResource

@Immutable
data class ReplyImage(
    val uri: String,
    val fallback: DrawableResource? = null,
)
