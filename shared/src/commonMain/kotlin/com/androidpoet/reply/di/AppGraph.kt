package com.androidpoet.reply.di

import com.androidpoet.reply.data.AccountStore
import com.androidpoet.reply.data.EmailStore
import com.androidpoet.reply.data.ReplyRepository
import com.androidpoet.reply.data.remote.replyHttpClient
import io.ktor.client.HttpClient
import com.androidpoet.reply.feature.compose.ComposeViewModel
import com.androidpoet.reply.feature.email.EmailViewModel
import com.androidpoet.reply.feature.home.HomeViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.createGraph

@DependencyGraph(AppScope::class)
interface AppGraph {
    val emailStore: EmailStore
    val accountStore: AccountStore
    val repository: ReplyRepository

    val homeViewModelFactory: HomeViewModel.Factory
    val emailViewModelFactory: EmailViewModel.Factory
    val composeViewModelFactory: ComposeViewModel.Factory

    @Provides
    @SingleIn(AppScope::class)
    fun provideHttpClient(): HttpClient = replyHttpClient()
}

fun buildAppGraph(): AppGraph = createGraph<AppGraph>()
