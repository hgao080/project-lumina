package com.projectlumina.quotes

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import java.util.UUID

class ExposedQuoteRepository : QuoteRepository {
    override suspend fun insert(input: QuoteInsert): Quote = withTransaction {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)

        daoToModel(QuoteDao.new {
            text        = input.text
            author      = input.author
            category    = input.category
            isPermanent = input.isPermanent
            boardDate   = input.boardDate
            createdAt   = now
            updatedAt   = now
        })
    }

    override suspend fun getByDate(date: LocalDate): List<Quote> = withTransaction {
        QuoteDao.find { QuoteTable.boardDate eq date }
            .map { daoToModel(it) }
    }

    override suspend fun getPermanent(): List<Quote> = withTransaction {
        QuoteDao.find { QuoteTable.isPermanent eq true }
            .orderBy(QuoteTable.boardDate to SortOrder.DESC)
            .map { daoToModel(it) }
    }

    override suspend fun getArchiveDates(): List<LocalDate> = withTransaction {
        QuoteDao.all()
            .orderBy(QuoteTable.boardDate to SortOrder.DESC)
            .map { it.boardDate }
            .distinct()
    }

    override suspend fun toggleIsPermanent(id: UUID): Quote? = withTransaction {
        QuoteDao.findById(id)
            ?.apply {
                isPermanent = !isPermanent
                updatedAt   = Clock.System.now().toLocalDateTime(TimeZone.UTC)
            }
            ?.let { daoToModel(it) }
    }
}
