package com.androidpoet.reply.data

import org.jetbrains.compose.resources.DrawableResource

/**
 * An account which can belong to the current user (one of their sign-in identities) or a contact.
 */
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
