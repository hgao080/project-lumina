package com.projectlumina.quotes

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

        get("/permanent") {
            val quotes = repository.getPermanent()
            
            call.respond(quotes)
        }
    }
}
