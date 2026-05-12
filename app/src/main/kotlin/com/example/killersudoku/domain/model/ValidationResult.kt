package com.example.killersudoku.domain.model

data class ValidationResult(
    val isValid: Boolean,
    val reason: String? = null,
)
