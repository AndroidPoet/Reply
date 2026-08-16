package com.androidpoet.reply.data

import androidx.compose.runtime.Immutable
import org.jetbrains.compose.resources.DrawableResource

@Immutable
data class EmailAttachment(
    val image: DrawableResource,
    val contentDesc: String,
)
