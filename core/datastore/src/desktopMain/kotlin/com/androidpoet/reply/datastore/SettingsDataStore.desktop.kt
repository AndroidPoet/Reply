package com.androidpoet.reply.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import java.io.File

fun createSettingsDataStore(): DataStore<Preferences> = createSettingsDataStore {
    File(System.getProperty("user.home"), ".reply").apply { mkdirs() }.resolve(SETTINGS_FILE_NAME).absolutePath
}

fun createTemporarySettingsDataStore(): DataStore<Preferences> = createSettingsDataStore {
    File.createTempFile("reply", ".preferences_pb").apply { delete() }.absolutePath
}
