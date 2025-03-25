package com.jpan.banking_account_api.dto

import com.jpan.banking_account_api.model.AccountId
import java.math.BigDecimal

data class AccountBalanceDto(
    val account: AccountId,
    val userLastName: String,
    val balance: BigDecimal
)