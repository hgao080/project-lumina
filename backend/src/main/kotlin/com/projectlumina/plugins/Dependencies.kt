package com.projectlumina.plugins

import com.projectlumina.quotes.ExposedQuoteRepository
import com.projectlumina.quotes.QuoteRepository
import io.ktor.server.application.*
import io.ktor.server.plugins.di.dependencies

fun Application.configureDependencies() {
    dependencies {
        provide<QuoteRepository> { ExposedQuoteRepository() }
    }
}
