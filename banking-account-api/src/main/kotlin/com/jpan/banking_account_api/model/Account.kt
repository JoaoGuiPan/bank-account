package com.jpan.banking_account_api.model

import jakarta.validation.constraints.Positive
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.util.*

typealias AccountId = String
typealias UserId = String
typealias CardId = String

enum class CardType {
    DEBIT,
    CREDIT
}

@Table("accounts")
data class Account(
    @Id
    val id: AccountId = UUID.randomUUID().toString(),
    @Column("user_id")
    val user: UserId = UUID.randomUUID().toString(),
    val userLastName: String,
    @Column("card_id")
    val card: CardId = UUID.randomUUID().toString(),
    val cardType: CardType,
    @field:Positive(message = "Balance must be positive")
    val balance: Double,
    val updatedAt: LocalDate = LocalDate.now()
)
