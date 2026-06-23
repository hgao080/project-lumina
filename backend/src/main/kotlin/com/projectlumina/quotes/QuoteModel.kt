package com.projectlumina.quotes

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Quote(
    val id: String,
    val text: String,
    val author: String,
    val category: String,
    val isPermanent: Boolean,
    val boardDate: LocalDate,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

@Serializable
data class QuoteInsert(
    val text: String,
    val author: String,
    val category: String,
    val isPermanent: Boolean = false,
)

