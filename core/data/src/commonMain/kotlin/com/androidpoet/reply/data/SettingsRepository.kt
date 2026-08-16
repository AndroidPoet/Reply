package com.androidpoet.reply.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

enum class ThemeMode { LIGHT, DARK, SYSTEM }

@Inject
@SingleIn(AppScope::class)
class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    val themeMode: Flow<ThemeMode> = dataStore.data
        .map { prefs -> prefs[THEME_MODE]?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } } ?: ThemeMode.SYSTEM }
        .distinctUntilChanged()

    val lastSyncEpochMillis: Flow<Long?> = dataStore.data
        .map { it[LAST_SYNC] }
        .distinctUntilChanged()

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[THEME_MODE] = mode.name }
    }

    suspend fun setLastSync(epochMillis: Long) {
        dataStore.edit { it[LAST_SYNC] = epochMillis }
    }

    private companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LAST_SYNC = longPreferencesKey("last_sync_epoch_millis")
    }
}
