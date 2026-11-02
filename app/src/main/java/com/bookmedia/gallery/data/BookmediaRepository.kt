package com.bookmedia.gallery.data

import com.bookmedia.gallery.model.ArchiveItem
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

class BookmediaRepository(private var baseUrl: String = "http://192.168.18.11:8080") {

    private val client = HttpClient(OkHttp) {
        engine {
            config {
                connectTimeout(15, TimeUnit.SECONDS)
                readTimeout(30, TimeUnit.SECONDS)
            }
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
                isLenient = true
            })
        }
    }

    fun updateBaseUrl(newUrl: String) {
        baseUrl = newUrl.trimEnd('/')
    }

    fun getBaseUrl(): String = baseUrl

    suspend fun fetchArchives(): Result<List<ArchiveItem>> = withContext(Dispatchers.IO) {
        runCatching {
            val response: List<ArchiveItem> = client.get("$baseUrl/api/archives?sort=id&order=desc").body()
            response
        }
    }

    suspend fun submitUrl(url: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val escapedUrl = url.replace("\"", "\\\"")
            val connection = URL("$baseUrl/api/submit").openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.doOutput = true
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.outputStream.use { os ->
                val body = "{\"urls\":\"$escapedUrl\",\"keep_local\":true}".toByteArray(Charsets.UTF_8)
                os.write(body)
            }
            val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
            stream?.bufferedReader()?.use { it.readText() } ?: "OK"
        }
    }

    suspend fun triggerLocalDownload(rowId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            client.post("$baseUrl/api/archives/$rowId/download-local")
            Unit
        }
    }

    suspend fun deleteLocalMedia(rowId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            client.post("$baseUrl/api/archives/$rowId/delete-local")
            Unit
        }
    }
}
