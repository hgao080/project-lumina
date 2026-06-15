package com.projectlumina

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.installCors() {
    val origin = dotenv["ALLOWED_ORIGIN", "localhost:3000"]
    install(CORS) {
        allowHost(origin, schemes = listOf("http", "https"))
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.ContentType)
        allowHeader("X-Pin")
    }
}
