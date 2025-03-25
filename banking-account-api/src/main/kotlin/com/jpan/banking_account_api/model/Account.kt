package com.jpan.banking_account_api.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
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
    val balance: BigDecimal,
    val updatedAt: LocalDate = LocalDate.now()
) {
    // had to add this due to compareTo with BigDecimal
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Account) return false

        if (id != other.id) return false
        if (user != other.user) return false
        if (userLastName != other.userLastName) return false
        if (card != other.card) return false
        if (cardType != other.cardType) return false
        if (balance.compareTo(other.balance) != 0) return false
        if (updatedAt != other.updatedAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + user.hashCode()
        result = 31 * result + userLastName.hashCode()
        result = 31 * result + card.hashCode()
        result = 31 * result + cardType.hashCode()
        result = 31 * result + balance.hashCode()
        result = 31 * result + updatedAt.hashCode()
        return result
    }
}
