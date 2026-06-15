package com.projectlumina

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.util.UUID

class QuoteRepository {
    suspend fun insert(input: QuoteInsert): Quote = suspendTransaction {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)

        QuoteEntity.new {
            text        = input.text
            author      = input.author
            category    = input.category
            isPermanent = input.isPermanent
            boardDate   = input.boardDate
            createdAt   = now
            updatedAt   = now
        }.toQuote()
    }

    suspend fun getByDate(date: LocalDate): List<Quote> = suspendTransaction {
        QuoteEntity.find { Quotes.boardDate eq date }.map { it.toQuote() }
    }

    suspend fun getPermanent(): List<Quote> = suspendTransaction {
        QuoteEntity.find { Quotes.isPermanent eq true }
            .orderBy(Quotes.boardDate to SortOrder.DESC)
            .map { it.toQuote() }
    }

    suspend fun getArchiveDates(): List<LocalDate> = suspendTransaction {
        QuoteEntity.all()
            .orderBy(Quotes.boardDate to SortOrder.DESC)
            .map { it.boardDate }
            .distinct()
    }

    suspend fun toggleIsPermanent(id: UUID): Quote? = suspendTransaction {
        QuoteEntity.findById(id)?.apply {
            isPermanent = !isPermanent
            updatedAt   = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        }?.toQuote()
    }
}
