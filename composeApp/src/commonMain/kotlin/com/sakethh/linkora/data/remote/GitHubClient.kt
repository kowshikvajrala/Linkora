package com.sakethh.linkora.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class GitHubClient(
    private val httpClient: HttpClient
) {
    @Serializable
    data class GistFile(
        val content: String
    )

    @Serializable
    data class GistRequest(
        val description: String,
        val public: Boolean,
        val files: Map<String, GistFile>
    )

    @Serializable
    data class GistResponse(
        val id: String,
        val html_url: String,
        val files: Map<String, GistFile>
    )

    suspend fun getGist(token: String, gistId: String): GistResponse {
        return httpClient.get("https://api.github.com/gists/$gistId") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.Accept, "application/vnd.github+json")
            header("X-GitHub-Api-Version", "2022-11-28")
        }.body()
    }

    suspend fun createGist(
        token: String,
        description: String,
        filename: String,
        content: String,
        isPublic: Boolean = false
    ): GistResponse {
        val requestBody = GistRequest(
            description = description,
            public = isPublic,
            files = mapOf(filename to GistFile(content))
        )

        return httpClient.post("https://api.github.com/gists") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.Accept, "application/vnd.github+json")
            header("X-GitHub-Api-Version", "2022-11-28")
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.body()
    }

    suspend fun updateGist(
        token: String,
        gistId: String,
        filename: String,
        content: String,
        description: String? = null
    ): GistResponse {
        val requestBody = GistRequest(
            description = description ?: "Updated by Linkora",
            public = false, // This check is ignored for updates usually but required by struct
            files = mapOf(filename to GistFile(content))
        )

        return httpClient.patch("https://api.github.com/gists/$gistId") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.Accept, "application/vnd.github+json")
            header("X-GitHub-Api-Version", "2022-11-28")
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.body()
    }
}
