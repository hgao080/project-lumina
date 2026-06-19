package com.projectlumina.quotes

import kotlinx.datetime.LocalDate
import java.util.UUID

interface QuoteRepository {
    suspend fun insert(input: QuoteInsert): Quote
    suspend fun getByDate(date: LocalDate): List<Quote>
    suspend fun getPermanent(): List<Quote>
    suspend fun getArchiveDates(): List<LocalDate>
    suspend fun toggleIsPermanent(id: UUID): Quote?
}
