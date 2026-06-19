package com.projectlumina.plugins

import com.projectlumina.quotes.QuoteTable
import io.ktor.server.application.*
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

fun Application.configureDatabase() {
    val database = Database.connect(
        url = "jdbc:h2:file:./lumina",
        driver = "org.h2.Driver"
    )
    transaction(database) {
        SchemaUtils.create(QuoteTable)
    }
}
