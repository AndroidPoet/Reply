package com.androidpoet.reply.data.remote

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val BASE_URL =
    "https://raw.githubusercontent.com/AndroidPoet/Reply/main/core/data/src/commonMain/composeResources/files"

@Inject
@SingleIn(AppScope::class)
class ReplyApi(private val client: HttpClient) {

    suspend fun accounts(): AccountsPayload = client.get("$BASE_URL/accounts.json").body()

    suspend fun emails(): EmailsPayload = client.get("$BASE_URL/emails.json").body()
}

private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

fun replyHttpClient(): HttpClient = HttpClient {
    install(ContentNegotiation) {
        json(json)
        json(json, ContentType.Text.Plain)
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 10_000
        connectTimeoutMillis = 5_000
    }
}
