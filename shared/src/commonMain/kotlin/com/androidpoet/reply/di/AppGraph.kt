package com.androidpoet.reply.di

import com.androidpoet.reply.data.AccountStore
import com.androidpoet.reply.data.EmailStore
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.androidpoet.reply.data.ReplyRepository
import com.androidpoet.reply.data.SettingsRepository
import com.androidpoet.reply.data.remote.replyHttpClient
import com.androidpoet.reply.database.AccountDao
import com.androidpoet.reply.database.EmailDao
import com.androidpoet.reply.database.FolderDao
import com.androidpoet.reply.database.ReplyDatabase
import com.androidpoet.reply.feature.compose.ComposeViewModel
import com.androidpoet.reply.feature.email.EmailViewModel
import com.androidpoet.reply.feature.home.HomeViewModel
import com.androidpoet.reply.feature.search.SearchViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.createGraphFactory
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@DependencyGraph(AppScope::class)
interface AppGraph {
    val emailStore: EmailStore
    val accountStore: AccountStore
    val repository: ReplyRepository
    val settings: SettingsRepository

    val homeViewModelFactory: HomeViewModel.Factory
    val emailViewModelFactory: EmailViewModel.Factory
    val composeViewModelFactory: ComposeViewModel.Factory
    val searchViewModel: SearchViewModel

    @Provides
    @SingleIn(AppScope::class)
    fun provideHttpClient(): HttpClient = replyHttpClient()

    @Provides
    @SingleIn(AppScope::class)
    fun provideScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    fun provideAccountDao(database: ReplyDatabase): AccountDao = database.accountDao()

    @Provides
    fun provideEmailDao(database: ReplyDatabase): EmailDao = database.emailDao()

    @Provides
    fun provideFolderDao(database: ReplyDatabase): FolderDao = database.folderDao()

    @DependencyGraph.Factory
    interface Factory {
        fun create(
            @Provides database: ReplyDatabase,
            @Provides settingsDataStore: DataStore<Preferences>,
        ): AppGraph
    }
}

fun buildAppGraph(database: ReplyDatabase, settingsDataStore: DataStore<Preferences>): AppGraph =
    createGraphFactory<AppGraph.Factory>().create(database, settingsDataStore)
