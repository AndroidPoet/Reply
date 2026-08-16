package com.androidpoet.reply.data.local

import com.androidpoet.reply.data.remote.AccountsPayload
import com.androidpoet.reply.data.remote.EmailsPayload
import com.androidpoet.reply.data.resources.Res
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

private val json = Json { ignoreUnknownKeys = true }

@OptIn(ExperimentalResourceApi::class)
object BundledData {
    suspend fun accounts(): AccountsPayload = json.decodeFromString(Res.readBytes("files/accounts.json").decodeToString())

    suspend fun emails(): EmailsPayload = json.decodeFromString(Res.readBytes("files/emails.json").decodeToString())
}
