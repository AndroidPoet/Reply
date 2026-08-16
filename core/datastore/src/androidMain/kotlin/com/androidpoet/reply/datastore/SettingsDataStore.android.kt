package com.androidpoet.reply.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

fun createSettingsDataStore(context: Context): DataStore<Preferences> =
    createSettingsDataStore { context.filesDir.resolve(SETTINGS_FILE_NAME).absolutePath }
