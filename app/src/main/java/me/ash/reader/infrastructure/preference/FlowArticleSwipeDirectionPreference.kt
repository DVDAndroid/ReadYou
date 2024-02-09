package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.ui.ext.DataStoreKeys
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

sealed class FlowArticleSwipeDirectionPreference(val value: Int) : Preference() {
    data object None : FlowArticleSwipeDirectionPreference(0)
    data object StartToEnd : FlowArticleSwipeDirectionPreference(1)
    data object EndToStart : FlowArticleSwipeDirectionPreference(2)
    data object Both : FlowArticleSwipeDirectionPreference(3)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKeys.FlowArticleSwipeDirection,
                value
            )
        }
    }

    val description: String
        @Composable get() {
            return when (this) {
                None -> stringResource(id = R.string.article_swipe_direction_none)
                StartToEnd -> stringResource(id = R.string.article_swipe_direction_start_to_end)
                EndToStart -> stringResource(id = R.string.article_swipe_direction_end_to_start)
                Both -> stringResource(id = R.string.article_swipe_direction_both)
            }
        }

    companion object {
        val default = StartToEnd
        val values = listOf(None, StartToEnd, EndToStart, Both)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKeys.FlowArticleSwipeDirection.key]) {
                0 -> None
                1 -> StartToEnd
                2 -> EndToStart
                3 -> Both
                else -> default
            }
    }

}


fun FlowArticleSwipeDirectionPreference.isEnabled() = this != FlowArticleSwipeDirectionPreference.None
fun FlowArticleSwipeDirectionPreference.isStartToEnd() = this == FlowArticleSwipeDirectionPreference.StartToEnd || this == FlowArticleSwipeDirectionPreference.Both
fun FlowArticleSwipeDirectionPreference.isEndToStart() = this == FlowArticleSwipeDirectionPreference.EndToStart || this == FlowArticleSwipeDirectionPreference.Both
