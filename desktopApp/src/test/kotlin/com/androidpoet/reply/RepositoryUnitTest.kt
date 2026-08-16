@file:OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)

package com.androidpoet.reply

import com.androidpoet.reply.data.AccountStore
import com.androidpoet.reply.data.DataSource
import com.androidpoet.reply.data.DispatcherProvider
import com.androidpoet.reply.data.EmailStore
import com.androidpoet.reply.data.ImageResolver
import com.androidpoet.reply.data.Mailbox
import com.androidpoet.reply.data.ReplyRepository
import com.androidpoet.reply.data.SettingsRepository
import com.androidpoet.reply.data.SyncStatus
import com.androidpoet.reply.data.local.BundledData
import com.androidpoet.reply.data.remote.AccountsPayload
import com.androidpoet.reply.data.remote.EmailsPayload
import com.androidpoet.reply.data.remote.ReplyApi
import com.androidpoet.reply.database.buildReplyDatabase
import com.androidpoet.reply.database.inMemoryReplyDatabaseBuilder
import com.androidpoet.reply.datastore.createTemporarySettingsDataStore
import com.androidpoet.reply.feature.search.SearchViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private class FakeApi(var failWith: Throwable? = null) : ReplyApi {
    var calls = 0
    override suspend fun accounts(): AccountsPayload {
        calls++
        failWith?.let { throw it }
        return BundledData.accounts()
    }

    override suspend fun emails(): EmailsPayload {
        failWith?.let { throw it }
        return BundledData.emails()
    }
}

private class FixedClock(var nowMillis: Long) : Clock {
    override fun now(): Instant = Instant.fromEpochMilliseconds(nowMillis)
}

private class TestDispatchers(dispatcher: CoroutineDispatcher) : DispatcherProvider {
    override val io = dispatcher
    override val default = dispatcher
    override val main = dispatcher
}

private class World(val api: FakeApi = FakeApi(), val clock: FixedClock = FixedClock(1_000_000L)) {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val database = inMemoryReplyDatabaseBuilder().buildReplyDatabase()
    val images = ImageResolver()
    val settings = SettingsRepository(createTemporarySettingsDataStore())
    val accounts = AccountStore(database.accountDao(), images, scope)
    val emails = EmailStore(database.emailDao(), database.folderDao(), accounts, images, scope)
    val repository = ReplyRepository(api, database, images, emails, settings, TestDispatchers(Dispatchers.Default), clock)
}

class RepositoryUnitTest {

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun refreshFailureKeepsLocalDataAndReportsFailed() = runBlocking {
        val world = World(FakeApi(failWith = IllegalStateException("offline")))
        world.repository.loadBundled()
        val status = world.repository.refresh()
        assertIs<SyncStatus.Failed>(status)
        assertEquals("offline", status.message)
        assertEquals(DataSource.BUNDLED, world.repository.source.value)
        assertEquals(12, world.emails.emails.value.size)
    }

    @Test
    fun refreshSuccessRecordsSyncTimeAndSwitchesToRemote() = runBlocking {
        val world = World()
        world.repository.loadBundled()
        val status = world.repository.refresh()
        assertIs<SyncStatus.Synced>(status)
        assertEquals(1_000_000L, status.atEpochMillis)
        assertEquals(1_000_000L, world.settings.lastSyncEpochMillis.first())
        assertEquals(DataSource.REMOTE, world.repository.source.value)
        assertTrue(world.emails.emails.value.first().sender.avatar.uri.startsWith("https://"))
    }

    @Test
    fun refreshIfStaleThrottlesWithinAMinute() = runBlocking {
        val world = World()
        world.repository.load()
        assertEquals(1, world.api.calls)
        world.clock.nowMillis += 30_000
        world.repository.refreshIfStale()
        assertEquals(1, world.api.calls)
        world.clock.nowMillis += 31_000
        world.repository.refreshIfStale()
        assertEquals(2, world.api.calls)
    }

    @Test
    fun moveToTrashIsUndoable() = runBlocking {
        val world = World()
        world.repository.loadBundled()
        val previous = world.emails.moveTo(1L, Mailbox.TRASH)
        assertEquals(Mailbox.INBOX, previous)
        withTimeout(5_000) { world.emails.emails.first { list -> list.first { it.id == 1L }.mailbox == Mailbox.TRASH } }
        world.emails.moveTo(1L, previous!!)
        withTimeout(5_000) { world.emails.emails.first { list -> list.first { it.id == 1L }.mailbox == Mailbox.INBOX } }
        Unit
    }

    @Test
    fun searchDebouncesAndMatchesSubjectBodyAndSender() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val world = World()
        world.repository.loadBundled()
        val viewModel = SearchViewModel(world.emails)
        val results = mutableListOf<Int>()
        backgroundScope.launch { viewModel.results.collect { results += it.size } }
        runCurrent()
        viewModel.onQueryChange("par")
        viewModel.onQueryChange("paris")
        advanceTimeBy(100)
        runCurrent()
        assertEquals(listOf(0), results)
        advanceTimeBy(300)
        runCurrent()
        assertEquals(1, results.last())
        viewModel.onQueryChange("Ali Connors")
        advanceTimeBy(400)
        runCurrent()
        assertEquals(1, results.last())
        viewModel.onQueryChange("")
        advanceTimeBy(400)
        runCurrent()
        assertEquals(0, results.last())
    }
}
