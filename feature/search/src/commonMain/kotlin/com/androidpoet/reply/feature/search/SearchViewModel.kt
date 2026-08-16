package com.androidpoet.reply.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidpoet.reply.data.Email
import com.androidpoet.reply.data.EmailStore
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private const val DEBOUNCE_MILLIS = 250L

@OptIn(FlowPreview::class)
@Inject
class SearchViewModel(private val emailStore: EmailStore) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val results: StateFlow<List<Email>> = _query
        .map { it.trim() }
        .debounce(DEBOUNCE_MILLIS)
        .distinctUntilChanged()
        .flatMapLatest { term ->
            if (term.isEmpty()) flowOf(emptyList()) else emailStore.emails.map { list -> list.filter { it.matches(term) } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onQueryChange(value: String) {
        _query.value = value
    }

    private fun Email.matches(term: String): Boolean =
        subject.contains(term, ignoreCase = true) ||
            body.contains(term, ignoreCase = true) ||
            sender.fullName.contains(term, ignoreCase = true) ||
            sender.email.contains(term, ignoreCase = true)
}
