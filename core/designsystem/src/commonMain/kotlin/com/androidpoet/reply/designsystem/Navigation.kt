package com.androidpoet.reply.designsystem

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Entry-scoped ViewModel: Nav3's ViewModelStore decorator owns the store, so this survives
 * configuration change and is cleared when the entry is popped.
 */
@Composable
inline fun <reified VM : ViewModel> rememberViewModel(
    key: String? = null,
    crossinline factory: () -> VM,
): VM = viewModel(key = key) { factory() }
