package com.jpan.banking_account_api.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@Configuration
@EnableR2dbcRepositories(basePackages = ["com.jpan.banking_account_api.model.repository"])
class DatabaseConfig