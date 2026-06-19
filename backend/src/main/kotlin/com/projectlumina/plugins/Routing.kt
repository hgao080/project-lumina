package com.projectlumina.plugins

import com.projectlumina.quotes.QuoteRepository
import com.projectlumina.quotes.quoteRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(repository: QuoteRepository) {
    routing {
        get("/") {
            call.respondText("Hello, World!")
        }
        get("/json/kotlinx-serialization") {
            call.respond(mapOf("hello" to "world"))
        }
        // TODO: remove — temp CORS smoke test
        get("/ping") {
            call.respond(mapOf("status" to "ok"))
        }
        quoteRoutes(repository)
    }
}
