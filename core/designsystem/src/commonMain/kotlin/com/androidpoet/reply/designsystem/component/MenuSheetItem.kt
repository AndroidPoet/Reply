package com.androidpoet.reply.designsystem.component

import androidx.compose.runtime.Immutable
import org.jetbrains.compose.resources.DrawableResource

@Immutable
data class MenuSheetItem(
    val id: String,
    val title: String,
    val icon: DrawableResource? = null,
)
