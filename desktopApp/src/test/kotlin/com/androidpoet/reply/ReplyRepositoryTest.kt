package com.androidpoet.reply

import com.androidpoet.reply.data.DataSource
import com.androidpoet.reply.di.buildAppGraph
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReplyRepositoryTest {

    @Test
    fun bundledDataLoads() = runBlocking {
        val graph = buildAppGraph()
        graph.repository.loadBundled()
        assertEquals(DataSource.BUNDLED, graph.repository.source.value)
        assertEquals(12, graph.emailStore.emails.value.size)
        assertEquals(3, graph.accountStore.userAccounts.value.size)
        assertEquals("hikingfan@gmail.com", graph.accountStore.getDefaultUserAccount()?.email)
    }

    @Test
    fun remoteDataLoadsFromGitHub() = runBlocking {
        val graph = buildAppGraph()
        graph.repository.load()
        assertEquals(DataSource.REMOTE, graph.repository.source.value)
        assertTrue(graph.emailStore.emails.value.any { it.subject == "Bonjour from Paris" })
        assertEquals(6, graph.emailStore.folders.value.size)
    }
}
