package com.androidpoet.reply.data

enum class SearchSuggestionIcon { SCHEDULE, HOME }

data class SearchSuggestion(
    val icon: SearchSuggestionIcon,
    val title: String,
    val subtitle: String,
)

object SearchSuggestionStore {
    val YESTERDAY_SUGGESTIONS = listOf(
        SearchSuggestion(SearchSuggestionIcon.SCHEDULE, "481 Van Brunt Street", "Brooklyn, NY"),
        SearchSuggestion(SearchSuggestionIcon.HOME, "Home", "199 Pacific Street, Brooklyn, NY"),
    )

    val THIS_WEEK_SUGGESTIONS = listOf(
        SearchSuggestion(SearchSuggestionIcon.SCHEDULE, "BEP GA", "Forsyth Street, New York, NY"),
        SearchSuggestion(SearchSuggestionIcon.SCHEDULE, "Sushi Nakazawa", "Commerce Street, New York, NY"),
        SearchSuggestion(SearchSuggestionIcon.SCHEDULE, "IFC Center", "6th Avenue, New York, NY"),
    )
}
