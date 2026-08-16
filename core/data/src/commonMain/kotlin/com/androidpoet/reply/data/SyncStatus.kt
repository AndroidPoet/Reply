package com.androidpoet.reply.data

sealed interface SyncStatus {
    data object Idle : SyncStatus
    data object Syncing : SyncStatus
    data class Synced(val atEpochMillis: Long) : SyncStatus
    data class Failed(val message: String, val lastSyncEpochMillis: Long?) : SyncStatus
}
