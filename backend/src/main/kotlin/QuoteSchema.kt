package com.projectlumina

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.dao.java.UUIDEntity
import org.jetbrains.exposed.v1.dao.java.UUIDEntityClass
import org.jetbrains.exposed.v1.datetime.date
import org.jetbrains.exposed.v1.datetime.datetime
import java.util.UUID

object Quotes : UUIDTable("quotes") {
    val text        = text("text")
    val author      = varchar("author", 255)
    val category    = varchar("category", 50)
    val isPermanent = bool("is_permanent").default(false)
    val boardDate   = date("board_date")
    val createdAt   = datetime("created_at")
    val updatedAt   = datetime("updated_at")
}

class QuoteEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<QuoteEntity>(Quotes)
    var text        by Quotes.text
    var author      by Quotes.author
    var category    by Quotes.category
    var isPermanent by Quotes.isPermanent
    var boardDate   by Quotes.boardDate
    var createdAt   by Quotes.createdAt
    var updatedAt   by Quotes.updatedAt

    fun toQuote() = Quote(
        id          = id.value.toString(),
        text        = text,
        author      = author,
        category    = category,
        isPermanent = isPermanent,
        boardDate   = boardDate,
        createdAt   = createdAt,
        updatedAt   = updatedAt,
    )
}

@Serializable
data class QuoteInsert(
    val text: String,
    val author: String,
    val category: String,
    val boardDate: LocalDate,
    val isPermanent: Boolean = false,
)

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
