package com.jpan.banking_account_api.model.repository

import com.jpan.banking_account_api.model.Account
import jakarta.validation.Valid
import kotlinx.coroutines.reactor.asFlux
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import org.springframework.validation.annotation.Validated

interface AccountCrudRepository: CoroutineCrudRepository<Account, String>

@Validated
@Repository
class AccountRepository(
    private val entityTemplate: R2dbcEntityTemplate,
    private val accountRepository: AccountCrudRepository
) {
    suspend fun insert(@Valid account: Account): Account = entityTemplate.insert(account).awaitSingle()

    suspend fun update(@Valid account: Account): Account = accountRepository.save(account)

    suspend fun findById(id: String): Account? = accountRepository.findById(id)

    suspend fun findAll(): List<Account> = accountRepository.findAll().asFlux().collectList().awaitSingle()

    suspend fun deleteAll() = accountRepository.deleteAll()
}