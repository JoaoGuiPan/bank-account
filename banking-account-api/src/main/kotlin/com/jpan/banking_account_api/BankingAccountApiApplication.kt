package com.jpan.banking_account_api

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@OpenAPIDefinition(
	info = Info(
		title = "Rabobank Banking API",
		version = "1.0",
		description = "API for banking operations"
	)
)
class BankingAccountApiApplication

fun main(args: Array<String>) {
	runApplication<BankingAccountApiApplication>(*args)
}
