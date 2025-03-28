package com.jpan.banking_account_api.service

import com.jpan.banking_account_api.dto.OpenAccountDto
import com.jpan.banking_account_api.dto.TransferTransactionDto
import com.jpan.banking_account_api.model.Account
import com.jpan.banking_account_api.model.CardType
import com.jpan.banking_account_api.model.repository.AccountRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.util.*

@ExtendWith(MockKExtension::class)
class AccountServiceUnitTest(
    @MockK
    private val accountRepository: AccountRepository
) {

    private val accountService = AccountService(accountRepository)

    @Test
    fun `createAccount should create and return a new account`() = runTest {
        val openAccountDto = OpenAccountDto("Doe", CardType.DEBIT, balance = "100.0")
        val expectedAccount = Account(userLastName = "Doe", cardType = CardType.DEBIT, balance = BigDecimal(100.0))

        coEvery { accountRepository.insert(any()) } returns expectedAccount
        coEvery { accountRepository.findByUserLastName(any()) } returns null

        val actualAccount = accountService.createAccount(openAccountDto)

        assertEquals(expectedAccount, actualAccount)
        coVerify(exactly = 1) { accountRepository.insert(any()) }
    }

    @Test
    fun `getAllAccountsBalance should return a list of AccountBalanceDto`() = runTest {
        val account1 = Account(userLastName = "Doe", cardType = CardType.DEBIT, balance = BigDecimal(100.0), id = "account1")
        val account2 = Account(userLastName = "Smith", cardType = CardType.CREDIT, balance = BigDecimal(200.0), id = "account2")
        val accounts = listOf(account1, account2)

        coEvery { accountRepository.findAll() } returns accounts

        val result = accountService.getAllAccountsBalance()

        assertEquals(2, result.size)
        assertEquals("account1", result[0].account)
        assertEquals(BigDecimal(100.0), result[0].balance)
        assertEquals("account2", result[1].account)
        assertEquals(BigDecimal(200.0), result[1].balance)
        coVerify(exactly = 1) { accountRepository.findAll() }
    }

    @Test
    fun `withdrawAmount should withdraw amount from account`() = runTest {
        val accountId = UUID.randomUUID().toString()
        val initialBalance = BigDecimal(100.0)
        val withdrawAmount = "50.0"
        val account = Account(id = accountId, userLastName = "Doe", cardType = CardType.DEBIT, balance = initialBalance)
        val expectedAccount = account.copy(balance = initialBalance - BigDecimal(withdrawAmount))

        coEvery { accountRepository.findById(accountId) } returns account
        coEvery { accountRepository.update(any()) } returns expectedAccount

        val actualAccount = accountService.withdrawAmount(accountId, withdrawAmount)

        assertEquals(expectedAccount, actualAccount)
        coVerify(exactly = 1) { accountRepository.findById(accountId) }
        coVerify(exactly = 1) { accountRepository.update(any()) }
    }

    @Test
    fun `withdrawAmount should apply credit card fee`() = runTest {
        val accountId = UUID.randomUUID().toString()
        val initialBalance = BigDecimal(100.0)
        val withdrawAmount = "50.0"
        val account = Account(id = accountId, userLastName = "Doe", cardType = CardType.CREDIT, balance = initialBalance)
        val expectedAccount = account.copy(balance = initialBalance - (BigDecimal(withdrawAmount) * BigDecimal(1.01)))

        coEvery { accountRepository.findById(accountId) } returns account
        coEvery { accountRepository.update(any()) } returns expectedAccount

        val actualAccount = accountService.withdrawAmount(accountId, withdrawAmount)

        assertEquals(expectedAccount, actualAccount)
        coVerify(exactly = 1) { accountRepository.findById(accountId) }
        coVerify(exactly = 1) { accountRepository.update(any()) }
    }

    @Test
    fun `withdrawAmount should throw exception if account not found`() = runTest {
        val accountId = UUID.randomUUID().toString()
        val withdrawAmount = "50.0"

        coEvery { accountRepository.findById(accountId) } returns null

        assertThrows(Exception::class.java) {
            runTest { accountService.withdrawAmount(accountId, withdrawAmount) }
        }
        coVerify(exactly = 0) { accountRepository.updateAll(any()) }
    }

    @Test
    fun `transferAmount should transfer amount between accounts`() = runTest {
        val fromAccountId = UUID.randomUUID().toString()
        val toAccountId = UUID.randomUUID().toString()
        val transferAmount = "50.0"
        val fromAccountInitialBalance = BigDecimal(100.0)
        val toAccountInitialBalance = BigDecimal(200.0)
        val fromAccount = Account(id = fromAccountId, userLastName = "Doe", cardType = CardType.DEBIT, balance = fromAccountInitialBalance)
        val toAccount = Account(id = toAccountId, userLastName = "Smith", cardType = CardType.DEBIT, balance = toAccountInitialBalance)
        val expectedFromAccount = fromAccount.copy(balance = fromAccountInitialBalance - BigDecimal(transferAmount))
        val expectedToAccount = toAccount.copy(balance = toAccountInitialBalance + BigDecimal(transferAmount))
        val transaction = TransferTransactionDto(toAccountId, transferAmount)

        coEvery { accountRepository.findById(fromAccountId) } returns fromAccount
        coEvery { accountRepository.findById(toAccountId) } returns toAccount
        coEvery { accountRepository.updateAll(any()) } returns listOf(expectedToAccount, expectedFromAccount)

        val actualAccount = accountService.transferAmount(fromAccountId, transaction)

        assertEquals(expectedFromAccount, actualAccount)
        coVerify(exactly = 1) { accountRepository.findById(fromAccountId) }
        coVerify(exactly = 1) { accountRepository.findById(toAccountId) }
        coVerify(exactly = 1) { accountRepository.updateAll(any()) }
    }

    @Test
    fun `depositAmount should deposit amount to account`() = runTest {
        val accountId = UUID.randomUUID().toString()
        val initialBalance = BigDecimal(100.0)
        val depositAmount = "50.0"
        val account = Account(id = accountId, userLastName = "Doe", cardType = CardType.DEBIT, balance = initialBalance)
        val expectedAccount = account.copy(balance = initialBalance + BigDecimal(depositAmount))

        coEvery { accountRepository.findById(accountId) } returns account
        coEvery { accountRepository.update(any()) } returns expectedAccount

        val actualAccount = accountService.depositAmount(accountId, depositAmount)

        // Assert
        assertEquals(expectedAccount, actualAccount)
        coVerify(exactly = 1) { accountRepository.findById(accountId) }
        coVerify(exactly = 1) { accountRepository.update(any()) }
    }

    @Test
    fun `depositAmount should throw exception if account not found`() = runTest {
        val accountId = UUID.randomUUID().toString()
        val depositAmount = "50.0"

        coEvery { accountRepository.findById(accountId) } returns null

        assertThrows(Exception::class.java) {
            runTest { accountService.depositAmount(accountId, depositAmount) }
        }
        coVerify(exactly = 0) { accountRepository.updateAll(any()) }
    }

    @Test
    fun `createAccount should throw exception if balance is negative`() = runTest {
        val openAccountDto = OpenAccountDto("Doe", CardType.DEBIT, "-100.0")

        assertThrows(Exception::class.java) {
            runTest { accountService.createAccount(openAccountDto) }
        }
    }

    @Test
    fun `createAccount should throw exception if user already exists`() = runTest {
        val openAccountDto = OpenAccountDto("Doe", CardType.DEBIT, "100.0")
        val account = Account(userLastName = "Doe", cardType = CardType.DEBIT, balance = BigDecimal(100.0))

        coEvery { accountRepository.findByUserLastName(any()) } returns account

        assertThrows(Exception::class.java) {
            runTest { accountService.createAccount(openAccountDto) }
        }
    }
}