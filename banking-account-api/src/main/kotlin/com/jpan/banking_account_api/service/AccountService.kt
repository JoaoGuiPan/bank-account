package com.jpan.banking_account_api.service

import com.jpan.banking_account_api.dto.AccountBalanceDto
import com.jpan.banking_account_api.dto.OpenAccountDto
import com.jpan.banking_account_api.dto.TransferTransactionDto
import com.jpan.banking_account_api.model.Account
import com.jpan.banking_account_api.model.AccountId
import com.jpan.banking_account_api.model.CardType
import com.jpan.banking_account_api.model.repository.AccountRepository
import jakarta.validation.executable.ValidateOnExecution
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate

@Service
class AccountService(
    private val accountRepository: AccountRepository
) {
    val logger: Logger = LoggerFactory.getLogger(AccountService::class.java)

    @ValidateOnExecution
    suspend fun createAccount(openAccountDto: OpenAccountDto): Account {
        validateOpenAccount(openAccountDto)
        logger.debug("Creating account")
        val account = Account(
            userLastName = openAccountDto.userLastName,
            cardType = openAccountDto.cardType,
            balance = BigDecimal(openAccountDto.balance)
        )
        return accountRepository.insert(account)
    }

    suspend fun getAllAccountsBalance(): List<AccountBalanceDto> {
        logger.debug("Getting all accounts balance")
        return accountRepository.findAll().map {
            AccountBalanceDto(
                it.id,
                it.userLastName,
                it.balance
            )
        }
    }

    /**
     * Withdraws amount from user account.
     */
    suspend fun withdrawAmount(
        accountId: AccountId,
        amount: String
    ): Account {
        logger.debug("Withdrawing amount from account {}", accountId)
        val account = getExistingAccountValidated(accountId)
        val updatedAccount = account.withdraw(BigDecimal(amount))
        validateBalance(updatedAccount.balance)
        return accountRepository.update(updatedAccount)
    }

    /**
     * Transfers amount from user account to another specified by transaction recipient.
     */
    suspend fun transferAmount(
        account: AccountId,
        transaction: TransferTransactionDto
    ): Account {
        logger.debug("Transferring amount from account {} to account {}", account, transaction.recipient)
        val accountToWithdrawn = getExistingAccountValidated(account)
        val accountToDeposit = getExistingAccountValidated(transaction.recipient)

        val amount = BigDecimal(transaction.amount)

        val deposit = accountToDeposit.deposit(amount)
        validateBalance(deposit.balance)

        val withdrawn = accountToWithdrawn.withdraw(amount)
        validateBalance(withdrawn.balance)

        // makes sure both entities are updated at the same time
        accountRepository.updateAll(listOf(deposit, withdrawn))

        return withdrawn
    }

    /**
     * Deposits amount to user account.
     */
    suspend fun depositAmount(
        accountId: AccountId,
        amount: String
    ): Account {
        logger.debug("Depositing amount to account {}", accountId)
        val account = getExistingAccountValidated(accountId)
        val updatedAccount = account.deposit(BigDecimal(amount))
        validateBalance(updatedAccount.balance)
        return accountRepository.update(updatedAccount)
    }

    /**
     * Balance should be positive and lastName is unique.
     */
    private suspend fun validateOpenAccount(openAccountDto: OpenAccountDto) {
        logger.debug("Validating account")
        val balance = BigDecimal(openAccountDto.balance)
        validateBalance(balance)
        check(accountRepository.findByUserLastName(openAccountDto.userLastName) == null) {
            "User already exists"
        }
    }

    /**
     * Balance should be positive.
     */
    private fun validateBalance(balance: BigDecimal) {
        check(balance >= BigDecimal.ZERO) { "Balance must be positive" }
    }

    private suspend fun getExistingAccountValidated(accountId: AccountId): Account {
        val account = accountRepository.findById(accountId)
        checkNotNull(account) { "Account not found" }
        return account
    }
}

/**
 * If a transfer/withdraw is done with a credit card, 1% of the amount is charged extra.
 */
private fun BigDecimal.withCardFee(cardType: CardType): BigDecimal {
    if (cardType == CardType.CREDIT) {
        val cardFee = BigDecimal("1.01")
        return this * cardFee
    }
    return this
}

private fun Account.deposit(amount: BigDecimal): Account = this.copy(
    balance = this.balance + amount,
    updatedAt = LocalDate.now()
)

private fun Account.withdraw(amount: BigDecimal): Account = this.copy(
    balance = this.balance - amount.withCardFee(this.cardType),
    updatedAt = LocalDate.now()
)