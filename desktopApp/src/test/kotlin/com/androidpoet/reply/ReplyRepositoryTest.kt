package com.androidpoet.reply

import com.androidpoet.reply.data.DataSource
import com.androidpoet.reply.database.buildReplyDatabase
import com.androidpoet.reply.database.inMemoryReplyDatabaseBuilder
import com.androidpoet.reply.di.buildAppGraph
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReplyRepositoryTest {

    @Test
    fun bundledDataLoads() = runBlocking {
        val graph = buildAppGraph(inMemoryReplyDatabaseBuilder().buildReplyDatabase())
        graph.repository.loadBundled()
        assertEquals(DataSource.BUNDLED, graph.repository.source.value)
        assertEquals(12, graph.emailStore.emails.value.size)
        assertEquals(3, graph.accountStore.userAccounts.value.size)
        assertEquals("hikingfan@gmail.com", graph.accountStore.getDefaultUserAccount()?.email)
    }

    @Test
    fun starPersistsInDatabase() = runBlocking {
        val database = inMemoryReplyDatabaseBuilder().buildReplyDatabase()
        val graph = buildAppGraph(database)
        graph.repository.loadBundled()
        graph.emailStore.setStarred(1L, true)
        graph.emailStore.emails.first { list -> list.first { it.id == 1L }.isStarred }
        val again = buildAppGraph(database)
        again.repository.loadBundled()
        assertTrue(again.emailStore.emails.value.first { it.id == 1L }.isStarred)
    }

    @Test
    fun remoteDataLoadsFromGitHub() = runBlocking {
        val graph = buildAppGraph(inMemoryReplyDatabaseBuilder().buildReplyDatabase())
        graph.repository.load()
        assertEquals(DataSource.REMOTE, graph.repository.source.value)
        assertTrue(graph.emailStore.emails.value.any { it.subject == "Bonjour from Paris" })
        assertEquals(6, graph.emailStore.folders.value.size)
    }
}
