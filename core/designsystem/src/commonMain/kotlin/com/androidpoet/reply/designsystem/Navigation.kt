package com.androidpoet.reply.designsystem

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
inline fun <reified VM : ViewModel> rememberViewModel(
    key: String? = null,
    crossinline factory: () -> VM,
): VM = viewModel(key = key) { factory() }
