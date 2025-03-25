package com.jpan.banking_account_api.controller

import com.jpan.banking_account_api.dto.AccountBalanceResponseDto
import com.jpan.banking_account_api.dto.InternalServerErrorDto
import com.jpan.banking_account_api.dto.OpenAccountDto
import com.jpan.banking_account_api.dto.TransferTransactionDto
import com.jpan.banking_account_api.model.Account
import com.jpan.banking_account_api.model.AccountId
import com.jpan.banking_account_api.service.AccountService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Accounts", description = "Operations related to bank accounts")
class AccountController(
    val accountService: AccountService
) {

    val logger: Logger = LoggerFactory.getLogger(AccountController::class.java)

    @PostMapping("/open")
    @Operation(summary = "Opens a new account")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Account opened successfully", content = [Content(mediaType = "application/json", schema = Schema(implementation = AccountBalanceResponseDto::class))]),
            ApiResponse(responseCode = "500", description = "Internal server error", content = [Content(mediaType = "application/json", schema = Schema(implementation = InternalServerErrorDto::class))])
        ]
    )
    suspend fun openAccount(
        @RequestBody
        openAccountDto: OpenAccountDto
    ): ResponseEntity<Account> {
        logger.info("Opening account")
        val account = accountService.createAccount(openAccountDto)
        logger.debug("Account opened successfully")
        return ResponseEntity.ok(account)
    }

    @GetMapping("/balances")
    @Operation(summary = "Returns all account balances per user id")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Balances returned successfully", content = [Content(mediaType = "application/json", schema = Schema(implementation = AccountBalanceResponseDto::class))]),
            ApiResponse(responseCode = "500", description = "Internal server error", content = [Content(mediaType = "application/json", schema = Schema(implementation = InternalServerErrorDto::class))])
        ]
    )
    suspend fun getAllAccountBalances(): ResponseEntity<AccountBalanceResponseDto> {
        logger.info("Getting all accounts balance")
        val dtos = accountService.getAllAccountsBalance()
        return ResponseEntity.ok(AccountBalanceResponseDto(dtos))
    }

    @PutMapping("/{id}/withdraw")
    @Operation(summary = "Withdraw amount from account.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Amount withdrawn successfully"),
            ApiResponse(responseCode = "500", description = "Internal server error", content = [Content(mediaType = "application/json", schema = Schema(implementation = InternalServerErrorDto::class))])
        ]
    )
    suspend fun withdrawAmount(
        @PathVariable
        @Parameter(description = "Account id.")
        id: AccountId,
        @RequestParam
        @Parameter(description = "Amount to be withdrawn.")
        amount: String
    ): ResponseEntity<Account> {
        logger.info("Withdrawing {} from account {}", amount, id)
        val account = accountService.withdrawAmount(id, amount)
        logger.debug("Amount withdrawn successfully from account {}", id)
        return ResponseEntity.ok(account)
    }

    @PutMapping("/{id}/transfer")
    @Operation(summary = "Transfer amount from one account to another.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Amount transferred successfully"),
            ApiResponse(responseCode = "500", description = "Internal server error", content = [Content(mediaType = "application/json", schema = Schema(implementation = InternalServerErrorDto::class))])
        ]
    )
    suspend fun transferAmount(
        @PathVariable
        @Parameter(description = "Account id.")
        id: AccountId,
        @Parameter(description = "Recipient's account id and Amount to be transferred.")
        @RequestBody
        transaction: TransferTransactionDto
    ): ResponseEntity<Account> {
        logger.info("Transferring amount from account {} to account {}", id, transaction.recipient)
        val account = accountService.transferAmount(id, transaction)
        logger.debug("Amount transferred successfully from account {} to account {}", id, transaction.recipient)
        return ResponseEntity.ok(account)
    }

    @PutMapping("/{id}/deposit")
    @Operation(summary = "Deposit amount to account.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Amount deposited successfully"),
            ApiResponse(responseCode = "500", description = "Internal server error", content = [Content(mediaType = "application/json", schema = Schema(implementation = InternalServerErrorDto::class))])
        ]
    )
    suspend fun depositAmount(
        @PathVariable
        @Parameter(description = "Account id.")
        id: AccountId,
        @RequestParam
        @Parameter(description = "Amount to be deposited.")
        amount: String
    ): ResponseEntity<Account> {
        logger.info("Depositing {} to account {}", amount, id)
        val account = accountService.depositAmount(id, amount)
        logger.debug("Amount deposited successfully to account {}", id)
        return ResponseEntity.ok(account)
    }
}