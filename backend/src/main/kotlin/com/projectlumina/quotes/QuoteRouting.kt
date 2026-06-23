package com.projectlumina.quotes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
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

        post {
            val input = call.receive<QuoteInsert>()
            val quote = repository.insert(input)
            call.respond(HttpStatusCode.Created, quote)
        }
    }
}
