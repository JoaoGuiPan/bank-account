package com.jpan.banking_account_api.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.jpan.banking_account_api.dto.AccountBalanceDto
import com.jpan.banking_account_api.dto.OpenAccountDto
import com.jpan.banking_account_api.dto.TransferTransactionDto
import com.jpan.banking_account_api.model.Account
import com.jpan.banking_account_api.model.CardType
import com.jpan.banking_account_api.service.AccountService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.math.BigDecimal
import java.util.*

val accountServiceMock: AccountService = mockk()

@WebFluxTest(
    controllers = [AccountController::class],
    excludeFilters = [
        ComponentScan.Filter(type = FilterType.ANNOTATION, classes = [EnableR2dbcRepositories::class])
    ]
)
class AccountControllerMvcTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val accountService: AccountService = accountServiceMock

    // prevents Springboot injection of AccountService
    @TestConfiguration
    class TestConfig {
        @Bean
        fun accountService(): AccountService = accountServiceMock
    }

    @BeforeEach
    fun setup() {
        io.mockk.clearAllMocks()
    }

    @Test
    fun `openAccount should return created account`() = runBlocking {
        val openAccountDto = OpenAccountDto("Doe", CardType.DEBIT, "100.0")
        val expectedAccount = Account(userLastName = "Doe", cardType = CardType.DEBIT, balance = BigDecimal(100.0))

        coEvery { accountService.createAccount(any()) } returns expectedAccount

        webTestClient.post()
            .uri("/api/accounts/open")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(openAccountDto))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.userLastName").isEqualTo("Doe")
            .jsonPath("$.cardType").isEqualTo("DEBIT")
            .jsonPath("$.balance").isEqualTo(BigDecimal(100.0))

        coVerify(exactly = 1) { accountService.createAccount(any()) }
    }

    @Test
    fun `getAllAccountBalances should return all account balances`() = runBlocking {
        val accountBalanceDto1 = AccountBalanceDto("account1", "Doe", BigDecimal(100.0))
        val accountBalanceDto2 = AccountBalanceDto("account2", "Smith", BigDecimal(200.0))
        val expectedResponse = listOf(accountBalanceDto1, accountBalanceDto2)

        coEvery { accountService.getAllAccountsBalance() } returns expectedResponse

        webTestClient.get()
            .uri("/api/accounts/balances")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.accounts.length()").isEqualTo(2)
            .jsonPath("$.accounts[0].account").isEqualTo("account1")
            .jsonPath("$.accounts[0].userLastName").isEqualTo("Doe")
            .jsonPath("$.accounts[0].balance").isEqualTo(BigDecimal(100.0))
            .jsonPath("$.accounts[1].account").isEqualTo("account2")
            .jsonPath("$.accounts[1].userLastName").isEqualTo("Smith")
            .jsonPath("$.accounts[1].balance").isEqualTo(BigDecimal(200.0))

        coVerify(exactly = 1) { accountService.getAllAccountsBalance() }
    }

    @Test
    fun `withdrawAmount should return updated account`() = runBlocking {
        val accountId = UUID.randomUUID().toString()
        val amount = "50.0"
        val expectedAccount = Account(id = accountId, userLastName = "Doe", cardType = CardType.DEBIT, balance = BigDecimal(50.0))

        coEvery { accountService.withdrawAmount(any(), any()) } returns expectedAccount

        webTestClient.put()
            .uri("/api/accounts/$accountId/withdraw?amount=$amount")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(accountId)
            .jsonPath("$.userLastName").isEqualTo("Doe")
            .jsonPath("$.cardType").isEqualTo("DEBIT")
            .jsonPath("$.balance").isEqualTo(BigDecimal(50.0))

        coVerify(exactly = 1) { accountService.withdrawAmount(accountId, amount) }
    }

    @Test
    fun `transferAmount should return updated account`() = runBlocking {
        val fromAccountId = UUID.randomUUID().toString()
        val toAccountId = UUID.randomUUID().toString()
        val amount = "50.0"
        val transaction = TransferTransactionDto(toAccountId, amount)
        val expectedAccount = Account(id = fromAccountId, userLastName = "Doe", cardType = CardType.DEBIT, balance = BigDecimal(50.0))

        coEvery { accountService.transferAmount(any(), any()) } returns expectedAccount

        webTestClient.put()
            .uri("/api/accounts/$fromAccountId/transfer")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(transaction))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(fromAccountId)
            .jsonPath("$.userLastName").isEqualTo("Doe")
            .jsonPath("$.cardType").isEqualTo("DEBIT")
            .jsonPath("$.balance").isEqualTo(BigDecimal(50.0))

        coVerify(exactly = 1) { accountService.transferAmount(fromAccountId, transaction) }
    }

    @Test
    fun `depositAmount should return updated account`() = runBlocking {
        val accountId = UUID.randomUUID().toString()
        val amount = "50.0"
        val expectedAccount = Account(id = accountId, userLastName = "Doe", cardType = CardType.DEBIT, balance = BigDecimal(150.0))

        coEvery { accountService.depositAmount(any(), any()) } returns expectedAccount

        webTestClient.put()
            .uri("/api/accounts/$accountId/deposit?amount=$amount")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(accountId)
            .jsonPath("$.userLastName").isEqualTo("Doe")
            .jsonPath("$.cardType").isEqualTo("DEBIT")
            .jsonPath("$.balance").isEqualTo(BigDecimal(150.0))

        coVerify(exactly = 1) { accountService.depositAmount(accountId, amount) }
    }

    @Test
    fun `openAccount should return Internal Server Error if balance is negative`() = runBlocking {
        val openAccountDto = OpenAccountDto("Doe", CardType.DEBIT, "-100.0")

        webTestClient.post()
            .uri("/api/accounts/open")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(openAccountDto))
            .exchange()
            .expectStatus().is5xxServerError

        coVerify(exactly = 1) { accountService.createAccount(openAccountDto) }
    }

    @Test
    fun `openAccount should return Internal Server Error if user already exists`() = runBlocking {
        val openAccountDto = OpenAccountDto("Doe", CardType.DEBIT, "100.0")

        coEvery { accountService.createAccount(any()) } throws IllegalStateException("User already exists")

        webTestClient.post()
            .uri("/api/accounts/open")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(openAccountDto))
            .exchange()
            .expectStatus().is5xxServerError

        coVerify(exactly = 1) { accountService.createAccount(openAccountDto) }
    }
}