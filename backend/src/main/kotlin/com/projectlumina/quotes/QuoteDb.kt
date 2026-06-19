@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.projectlumina.quotes

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.dao.java.UUIDEntity
import org.jetbrains.exposed.v1.dao.java.UUIDEntityClass
import org.jetbrains.exposed.v1.datetime.date
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.inTopLevelSuspendTransaction
import java.util.UUID

object QuoteTable : UUIDTable("quotes") {
    val userId      = uuid("user_id")
    val text        = text("text")
    val author      = varchar("author", 255)
    val category    = varchar("category", 50)
    val isPermanent = bool("is_permanent").default(false)
    val boardDate   = date("board_date")
    val createdAt   = datetime("created_at")
    val updatedAt   = datetime("updated_at")
}

class QuoteDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<QuoteDao>(QuoteTable)

    var userId      by QuoteTable.userId
    var text        by QuoteTable.text
    var author      by QuoteTable.author
    var category    by QuoteTable.category
    var isPermanent by QuoteTable.isPermanent
    var boardDate   by QuoteTable.boardDate
    var createdAt   by QuoteTable.createdAt
    var updatedAt   by QuoteTable.updatedAt
}

suspend fun <T> withTransaction(block: suspend JdbcTransaction.() -> T): T = withContext(Dispatchers.IO) {
    inTopLevelSuspendTransaction { block() }
}

fun daoToModel(dao: QuoteDao) = Quote(
    dao.id.toString(),
    dao.text,
    dao.author,
    dao.category,
    dao.isPermanent,
    dao.boardDate,
    dao.createdAt,
    dao.updatedAt,
)
