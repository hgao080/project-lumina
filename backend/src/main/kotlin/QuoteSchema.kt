package com.projectlumina

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.datetime.date
import org.jetbrains.exposed.v1.datetime.datetime

object Quotes : UUIDTable("quotes") {
    val text        = text("text")
    val author      = varchar("author", 255)
    val category    = varchar("category", 50)
    val isPermanent = bool("is_permanent").default(false)
    val boardDate   = date("board_date")
    val createdAt   = datetime("created_at")
    val updatedAt   = datetime("updated_at")
}

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
