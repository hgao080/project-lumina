@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.projectlumina.quotes

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.uuid.Uuid
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import java.util.UUID

// Replaced by real user identity when auth is implemented (#29)
private val STUB_USER_ID = Uuid.parse("00000000-0000-0000-0000-000000000001")

class ExposedQuoteRepository : QuoteRepository {
    override suspend fun insert(input: QuoteInsert): Quote = withTransaction {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)

        daoToModel(QuoteDao.new {
            userId      = STUB_USER_ID
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
        QuoteDao.find { (QuoteTable.boardDate eq date) and (QuoteTable.userId eq STUB_USER_ID) }
            .map { daoToModel(it) }
    }

    override suspend fun getPermanent(): List<Quote> = withTransaction {
        QuoteDao.find { (QuoteTable.isPermanent eq true) and (QuoteTable.userId eq STUB_USER_ID) }
            .orderBy(QuoteTable.boardDate to SortOrder.DESC)
            .map { daoToModel(it) }
    }

    override suspend fun getArchiveDates(): List<LocalDate> = withTransaction {
        QuoteDao.find { QuoteTable.userId eq STUB_USER_ID }
            .orderBy(QuoteTable.boardDate to SortOrder.DESC)
            .map { it.boardDate }
            .distinct()
    }

    override suspend fun toggleIsPermanent(id: UUID): Quote? = withTransaction {
        QuoteDao.findById(id)
            ?.takeIf { it.userId == STUB_USER_ID }
            ?.apply {
                isPermanent = !isPermanent
                updatedAt   = Clock.System.now().toLocalDateTime(TimeZone.UTC)
            }
            ?.let { daoToModel(it) }
    }
}
