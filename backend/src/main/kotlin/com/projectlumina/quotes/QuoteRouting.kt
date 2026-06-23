package com.projectlumina.quotes

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

fun Route.quoteRoutes(repository: QuoteRepository) {
    route("/quotes") {
        get("/today") {
            val today = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
            val quotes = repository.getByDate(today)
                .filter { !it.isPermanent }

            call.respond(quotes)
        }
        patch("/{id}") {
            val idStr = call.parameters["id"]
                ?: return@patch call.respond(HttpStatusCode.BadRequest)
            val id = try {
                UUID.fromString(idStr)
            } catch (e: Exception) {
                return@patch call.respond(HttpStatusCode.BadRequest)
            }
            val quote = repository.toggleIsPermanent(id)
                ?: return@patch call.respond(HttpStatusCode.NotFound)
            call.respond(quote)
        }
    }
}
