package com.jpan.banking_account_api.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Error response object")
data class InternalServerErrorDto(
    @Schema(description = "Error message", example = "An unexpected error occurred.")
    val message: String,
    @Schema(description = "Captured throwable exception", example = "Runtime exception")
    val exception: Throwable? = null
)