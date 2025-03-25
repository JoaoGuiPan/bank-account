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
import java.math.BigDecimal
import java.util.*

private val BIG_DECIMAL_50 = BigDecimal(50.0)
private val BIG_DECIMAL_100 = BigDecimal(100.0)
private val BIG_DECIMAL_150 = BigDecimal(150.0)
private val BIG_DECIMAL_200 = BigDecimal(200.0)
private val BIG_DECIMAL_250 = BigDecimal(250.0)


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
        val openAccountDto = OpenAccountDto("Doe", CardType.DEBIT, "100.0")

        val createdAccount = accountService.createAccount(openAccountDto)

        assertNotNull(createdAccount.id)
        assertEquals("Doe", createdAccount.userLastName)
        assertEquals(CardType.DEBIT, createdAccount.cardType)
        assertTrue(BIG_DECIMAL_100.compareTo(createdAccount.balance) == 0)

        val retrievedAccount = accountRepository.findById(createdAccount.id)
        assertNotNull(retrievedAccount)
        assertEquals(createdAccount, retrievedAccount)
    }

    @Test
    fun `getAllAccountsBalance should return all accounts with correct balances`() = runTest {
        val account1 = Account(userLastName = "Doe", cardType = CardType.DEBIT, balance = BIG_DECIMAL_100)
        val account2 = Account(userLastName = "Smith", cardType = CardType.CREDIT, balance = BIG_DECIMAL_200)
        accountRepository.insert(account1)
        accountRepository.insert(account2)

        val balances = accountService.getAllAccountsBalance()

        assertEquals(2, balances.size)
        assertTrue(balances.any { it.account == account1.id && it.balance.compareTo(account1.balance) == 0 })
        assertTrue(balances.any { it.account == account2.id && it.balance.compareTo(account2.balance) == 0 })
    }

    @Test
    fun `withdrawAmount should withdraw amount and update account balance`() = runTest {
        val initialAccount = Account(userLastName = "Doe", cardType = CardType.DEBIT, balance = BIG_DECIMAL_100)
        val savedAccount = accountRepository.insert(initialAccount)
        val withdrawAmount = "50.0"

        val updatedAccount = accountService.withdrawAmount(savedAccount.id, withdrawAmount)

        assertTrue(BIG_DECIMAL_50.compareTo(updatedAccount.balance) == 0)

        val retrievedAccount = accountRepository.findById(savedAccount.id)
        assertNotNull(retrievedAccount)
        assertTrue(BIG_DECIMAL_50.compareTo(retrievedAccount!!.balance) == 0)
    }

    @Test
    fun `withdrawAmount should apply credit card fee and update account balance`() = runTest {
        val initialAccount = Account(userLastName = "Doe", cardType = CardType.CREDIT, balance = BIG_DECIMAL_100)
        val savedAccount = accountRepository.insert(initialAccount)
        val withdrawnAmount = "50.0"

        val updatedAccount = accountService.withdrawAmount(savedAccount.id, withdrawnAmount)

        assertTrue((BIG_DECIMAL_100 - (BigDecimal(withdrawnAmount) * BigDecimal("1.01"))).compareTo(updatedAccount.balance) == 0)

        val retrievedAccount = accountRepository.findById(savedAccount.id)
        assertNotNull(retrievedAccount)
        assertTrue(updatedAccount.balance.compareTo(retrievedAccount!!.balance) == 0)
    }

    @Test
    fun `transferAmount should transfer amount between accounts and update balances`() = runTest {
        val fromAccount = Account(userLastName = "Doe", cardType = CardType.DEBIT, balance = BIG_DECIMAL_100)
        val toAccount = Account(userLastName = "Smith", cardType = CardType.DEBIT, balance = BIG_DECIMAL_200)
        val savedFromAccount = accountRepository.insert(fromAccount)
        val savedToAccount = accountRepository.insert(toAccount)
        val transferAmount = "50.0"
        val transaction = TransferTransactionDto(savedToAccount.id, transferAmount)

        val updatedFromAccount = accountService.transferAmount(savedFromAccount.id, transaction)

        assertEquals(BIG_DECIMAL_50.compareTo(updatedFromAccount.balance), 0)

        val retrievedFromAccount = accountRepository.findById(savedFromAccount.id)
        assertNotNull(retrievedFromAccount)
        assertEquals(BIG_DECIMAL_50.compareTo(retrievedFromAccount!!.balance), 0)

        val retrievedToAccount = accountRepository.findById(savedToAccount.id)
        assertNotNull(retrievedToAccount)
        assertEquals(BIG_DECIMAL_250.compareTo(retrievedToAccount!!.balance), 0)
    }

    @Test
    fun `depositAmount should deposit amount and update account balance`() = runTest {
        val initialAccount = Account(userLastName = "Doe", cardType = CardType.DEBIT, balance = BIG_DECIMAL_100)
        val savedAccount = accountRepository.insert(initialAccount)
        val depositAmount = "50.0"

        val updatedAccount = accountService.depositAmount(savedAccount.id, depositAmount)

        assertEquals(BIG_DECIMAL_150.compareTo(updatedAccount.balance), 0)

        val retrievedAccount = accountRepository.findById(savedAccount.id)
        assertNotNull(retrievedAccount)
        assertEquals(BIG_DECIMAL_150.compareTo(retrievedAccount!!.balance), 0)
    }

    @Test
    fun `withdrawAmount should throw exception if account not found`() = runTest {
        val accountId = UUID.randomUUID().toString()
        val withdrawAmount = "50.0"

        org.junit.jupiter.api.assertThrows<Exception> {
            runTest { accountService.withdrawAmount(accountId, withdrawAmount) }
        }
    }

    @Test
    fun `depositAmount should throw exception if account not found`() = runTest {
        val accountId = UUID.randomUUID().toString()
        val depositAmount = "50.0"

        org.junit.jupiter.api.assertThrows<Exception> {
            runTest { accountService.depositAmount(accountId, depositAmount) }
        }
    }

    @Test
    fun `createAccount should throw exception if balance is negative`() = runTest {
        val openAccountDto = OpenAccountDto("Doe", CardType.DEBIT, "-100.0")

        assertThrows(IllegalStateException::class.java) {
            runTest { accountService.createAccount(openAccountDto) }
        }
    }

    @Test
    fun `createAccount should throw exception if user already exists`() = runTest {
        val openAccountDto = OpenAccountDto("Doe", CardType.DEBIT, "100.0")
        accountService.createAccount(openAccountDto)

        assertThrows(IllegalStateException::class.java) {
            runTest { accountService.createAccount(openAccountDto) }
        }
    }
}