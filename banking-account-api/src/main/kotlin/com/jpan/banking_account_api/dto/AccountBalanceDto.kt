package com.jpan.banking_account_api.dto

import com.jpan.banking_account_api.model.UserId

data class AccountBalanceDto(
    val user: UserId,
    val userLastName: String,
    val balance: Double
)