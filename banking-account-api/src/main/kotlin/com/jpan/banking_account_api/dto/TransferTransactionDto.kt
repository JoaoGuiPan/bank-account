package com.jpan.banking_account_api.dto

import com.jpan.banking_account_api.model.AccountId

data class TransferTransactionDto(
    val recipient: AccountId,
    val amount: Double
)
