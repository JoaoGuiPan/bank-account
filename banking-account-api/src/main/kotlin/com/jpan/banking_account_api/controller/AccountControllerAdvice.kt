package com.jpan.banking_account_api.controller

import com.jpan.banking_account_api.dto.InternalServerErrorDto
import io.swagger.v3.oas.annotations.Hidden
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Captures exceptions thrown by AccountController and handles http responses accordingly
 */
@Hidden // avoids conflicts with openapi docs
@RestControllerAdvice(basePackageClasses = [AccountController::class])
class AccountControllerAdvice {

    val logger: Logger = LoggerFactory.getLogger(AccountControllerAdvice::class.java)

    /**
     * @param ex generic/unexpected exceptions
     * @return generic error object
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception?): ResponseEntity<InternalServerErrorDto> {
        logger.error("An unexpected error occurred.", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(InternalServerErrorDto("An unexpected error occurred. Detail: ${ex?.message}", ex))
    }
}