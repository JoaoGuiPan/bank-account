package com.jpan.banking_account_api.dto

import com.jpan.banking_account_api.model.CardType
import jakarta.validation.constraints.Positive

data class OpenAccountDto(
    val userLastName: String,
    val cardType: CardType,
    @Positive(message = "Balance must be greater than 0")
    val balance: Double
)
