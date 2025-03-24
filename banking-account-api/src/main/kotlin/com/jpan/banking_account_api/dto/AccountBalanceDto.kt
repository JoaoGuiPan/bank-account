package com.jpan.banking_account_api.dto

import com.jpan.banking_account_api.model.AccountId

data class AccountBalanceDto(
    val account: AccountId,
    val userLastName: String,
    val balance: Double
)