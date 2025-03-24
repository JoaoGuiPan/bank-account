package com.jpan.banking_account_api.service

import com.jpan.banking_account_api.dto.AccountBalanceDto
import com.jpan.banking_account_api.dto.OpenAccountDto
import com.jpan.banking_account_api.dto.TransferTransactionDto
import com.jpan.banking_account_api.model.Account
import com.jpan.banking_account_api.model.AccountId
import com.jpan.banking_account_api.model.CardType
import com.jpan.banking_account_api.model.repository.AccountRepository
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated
import java.time.LocalDate

@Service
@Validated
class AccountService(
    private val accountRepository: AccountRepository
) {

    val logger: Logger = LoggerFactory.getLogger(AccountService::class.java)

    suspend fun createAccount(
        @Valid openAccountDto: OpenAccountDto
    ): Account {
        logger.debug("Creating account")
        val account = Account(
            userLastName = openAccountDto.userLastName,
            cardType = openAccountDto.cardType,
            balance = openAccountDto.balance
        )
        return accountRepository.insert(account)
    }

    suspend fun getAllAccountsBalance(): List<AccountBalanceDto> {
        logger.debug("Getting all accounts balance")
        return accountRepository.findAll().map {
            AccountBalanceDto(
                user = it.user,
                balance = it.balance
            )
        }
    }

    suspend fun withdrawAmount(
        accountId: AccountId,
        amount: Double
    ): Account {
        logger.debug("Withdrawing amount from account {}", accountId)
        val account = accountRepository.findById(accountId) ?: throw Exception("Account not found")
        val updatedAccount = account.copy(
            balance = account.balance - amount.withCardFee(account.cardType),
            updatedAt = LocalDate.now()
        )
        return accountRepository.update(updatedAccount)
    }

    suspend fun transferAmount(
        from: AccountId,
        transaction: TransferTransactionDto
    ): Account {
        logger.debug("Transferring amount from account {} to account {}", from, transaction.recipient)
        depositAmount(transaction.recipient, transaction.amount)
        return withdrawAmount(from, transaction.amount)
    }

    suspend fun depositAmount(
        accountId: AccountId,
        amount: Double
    ): Account {
        logger.debug("Depositing amount to account {}", accountId)
        val account = accountRepository.findById(accountId) ?: throw Exception("Account not found")
        val updatedAccount = account.copy(
            balance = account.balance + amount,
            updatedAt = LocalDate.now()
        )
        return accountRepository.update(updatedAccount)
    }
}

/**
 * If a transfer/withdraw is done with a credit card, 1% of the amount is charged extra.
 */
private fun Double.withCardFee(cardType: CardType): Double {
    if (cardType == CardType.CREDIT) {
        return this * 1.01
    }
    return this
}