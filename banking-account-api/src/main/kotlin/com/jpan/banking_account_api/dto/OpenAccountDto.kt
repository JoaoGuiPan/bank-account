package com.jpan.banking_account_api.dto

import com.jpan.banking_account_api.model.CardType

data class OpenAccountDto(
    val userLastName: String,
    val cardType: CardType,
    val balance: String
)
