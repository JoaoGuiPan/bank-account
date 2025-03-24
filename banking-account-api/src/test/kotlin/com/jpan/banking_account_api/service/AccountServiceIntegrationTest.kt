package com.jpan.banking_account_api.service

import com.jpan.banking_account_api.dto.OpenAccountDto
import com.jpan.banking_account_api.dto.TransferTransactionDto
import com.jpan.banking_account_api.model.Account
import com.jpan.banking_account_api.model.CardType
import com.jpan.banking_account_api.model.repository.AccountRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.*

@SpringBootTest
@ActiveProfiles("test")
class AccountServiceIntegrationTest {

    @Autowired
    private lateinit var accountRepository: AccountRepository

    private lateinit var accountService: AccountService

    @BeforeEach
    fun setUp() = runTest {
        accountRepository.deleteAll()
        accountService = AccountService(accountRepository)
    }

    @Test
    fun `createAccount should create and save a new account`() = runTest {
        val openAccountDto = OpenAccountDto("Doe", CardType.DEBIT, 100.0)

        val createdAccount = accountService.createAccount(openAccountDto)

        assertNotNull(createdAccount.id)
        assertEquals("Doe", createdAccount.userLastName)
        assertEquals(CardType.DEBIT, createdAccount.cardType)
        assertEquals(100.0, createdAccount.balance)

        val retrievedAccount = accountRepository.findById(createdAccount.id)
        assertNotNull(retrievedAccount)
        assertEquals(createdAccount, retrievedAccount)
    }

    @Test
    fun `getAllAccountsBalance should return all accounts with correct balances`() = runTest {
        val account1 = Account(userLastName = "Doe", cardType = CardType.DEBIT, balance = 100.0)
        val account2 = Account(userLastName = "Smith", cardType = CardType.CREDIT, balance = 200.0)
        accountRepository.insert(account1)
        accountRepository.insert(account2)

        val balances = accountService.getAllAccountsBalance()

        assertEquals(2, balances.size)
        assertTrue(balances.any { it.user == account1.user && it.balance == account1.balance })
        assertTrue(balances.any { it.user == account2.user && it.balance == account2.balance })
    }

    @Test
    fun `withdrawAmount should withdraw amount and update account balance`() = runTest {
        val initialAccount = Account(userLastName = "Doe", cardType = CardType.DEBIT, balance = 100.0)
        val savedAccount = accountRepository.insert(initialAccount)
        val withdrawAmount = 50.0

        val updatedAccount = accountService.withdrawAmount(savedAccount.id, withdrawAmount)

        assertEquals(50.0, updatedAccount.balance)

        val retrievedAccount = accountRepository.findById(savedAccount.id)
        assertNotNull(retrievedAccount)
        assertEquals(50.0, retrievedAccount!!.balance)
    }

    @Test
    fun `withdrawAmount should apply credit card fee and update account balance`() = runTest {
        val initialAccount = Account(userLastName = "Doe", cardType = CardType.CREDIT, balance = 100.0)
        val savedAccount = accountRepository.insert(initialAccount)
        val withdrawAmount = 50.0

        val updatedAccount = accountService.withdrawAmount(savedAccount.id, withdrawAmount)

        assertEquals(100.0 - (withdrawAmount * 1.01), updatedAccount.balance)

        val retrievedAccount = accountRepository.findById(savedAccount.id)
        assertNotNull(retrievedAccount)
        assertEquals(100.0 - (withdrawAmount * 1.01), retrievedAccount!!.balance)
    }

    @Test
    fun `transferAmount should transfer amount between accounts and update balances`() = runTest {
        val fromAccount = Account(userLastName = "Doe", cardType = CardType.DEBIT, balance = 100.0)
        val toAccount = Account(userLastName = "Smith", cardType = CardType.DEBIT, balance = 200.0)
        val savedFromAccount = accountRepository.insert(fromAccount)
        val savedToAccount = accountRepository.insert(toAccount)
        val transferAmount = 50.0
        val transaction = TransferTransactionDto(savedToAccount.id, transferAmount)

        val updatedFromAccount = accountService.transferAmount(savedFromAccount.id, transaction)

        assertEquals(50.0, updatedFromAccount.balance)

        val retrievedFromAccount = accountRepository.findById(savedFromAccount.id)
        assertNotNull(retrievedFromAccount)
        assertEquals(50.0, retrievedFromAccount!!.balance)

        val retrievedToAccount = accountRepository.findById(savedToAccount.id)
        assertNotNull(retrievedToAccount)
        assertEquals(250.0, retrievedToAccount!!.balance)
    }

    @Test
    fun `depositAmount should deposit amount and update account balance`() = runTest {
        val initialAccount = Account(userLastName = "Doe", cardType = CardType.DEBIT, balance = 100.0)
        val savedAccount = accountRepository.insert(initialAccount)
        val depositAmount = 50.0

        val updatedAccount = accountService.depositAmount(savedAccount.id, depositAmount)

        assertEquals(150.0, updatedAccount.balance)

        val retrievedAccount = accountRepository.findById(savedAccount.id)
        assertNotNull(retrievedAccount)
        assertEquals(150.0, retrievedAccount!!.balance)
    }

    @Test
    fun `withdrawAmount should throw exception if account not found`() = runTest {
        val accountId = UUID.randomUUID().toString()
        val withdrawAmount = 50.0

        org.junit.jupiter.api.assertThrows<Exception> {
            runTest { accountService.withdrawAmount(accountId, withdrawAmount) }
        }
    }

    @Test
    fun `depositAmount should throw exception if account not found`() = runTest {
        val accountId = UUID.randomUUID().toString()
        val depositAmount = 50.0

        org.junit.jupiter.api.assertThrows<Exception> {
            runTest { accountService.depositAmount(accountId, depositAmount) }
        }
    }
}