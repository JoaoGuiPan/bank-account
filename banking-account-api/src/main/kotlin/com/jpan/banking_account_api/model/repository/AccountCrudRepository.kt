package com.jpan.banking_account_api.model.repository

import com.jpan.banking_account_api.model.Account
import kotlinx.coroutines.reactor.asFlux
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

interface AccountCrudRepository: CoroutineCrudRepository<Account, String> {
    suspend fun findByUserLastName(userLastName: String): Account?
}

@Repository
class AccountRepository(
    private val entityTemplate: R2dbcEntityTemplate,
    private val accountRepository: AccountCrudRepository
) {
    suspend fun insert(account: Account): Account = entityTemplate.insert(account).awaitSingle()

    suspend fun update(account: Account): Account = accountRepository.save(account)

    suspend fun findById(id: String): Account? = accountRepository.findById(id)

    suspend fun findByUserLastName(userLastName: String): Account? = accountRepository.findByUserLastName(userLastName)

    suspend fun findAll(): List<Account> = accountRepository.findAll().asFlux().collectList().awaitSingle()

    suspend fun deleteAll() = accountRepository.deleteAll()
}