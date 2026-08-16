package com.androidpoet.reply.data

import androidx.compose.runtime.Immutable
import org.jetbrains.compose.resources.DrawableResource

@Immutable
data class Account(
    val id: Long,
    val uid: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val altEmail: String,
    val avatar: DrawableResource,
    val isCurrentAccount: Boolean = false,
) {
    val fullName: String get() = "$firstName $lastName"
}
