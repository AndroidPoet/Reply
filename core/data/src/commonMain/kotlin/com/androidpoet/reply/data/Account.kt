package com.androidpoet.reply.data

import androidx.compose.runtime.Immutable

@Immutable
data class Account(
    val id: Long,
    val uid: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val altEmail: String,
    val avatar: ReplyImage,
    val isCurrentAccount: Boolean = false,
) {
    val fullName: String get() = "$firstName $lastName"
}
