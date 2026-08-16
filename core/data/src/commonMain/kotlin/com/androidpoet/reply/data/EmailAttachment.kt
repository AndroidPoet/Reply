package com.androidpoet.reply.data

import androidx.compose.runtime.Immutable

@Immutable
data class EmailAttachment(
    val image: ReplyImage,
    val contentDesc: String,
)
