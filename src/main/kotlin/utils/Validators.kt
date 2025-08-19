package com.utils

import io.konform.validation.Validation
import io.konform.validation.jsonschema.*
import java.util.regex.Pattern

object Validators {
    private val phonePattern = Pattern.compile("^\\+?[1-9]\\d{1,14}$")
    private val emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")

    fun isValidPhone(phone: String): Boolean {
        return phonePattern.matcher(phone).matches()
    }

    fun isValidEmail(email: String): Boolean {
        return emailPattern.matcher(email).matches()
    }

    // Simple validation functions instead of complex Konform validations
    fun validateRegisterStep1(phone: String): List<String> {
        val errors = mutableListOf<String>()

        if (phone.length < 10) {
            errors.add("Phone number must be at least 10 characters")
        }
        if (phone.length > 15) {
            errors.add("Phone number must be at most 15 characters")
        }
        if (!isValidPhone(phone)) {
            errors.add("Invalid phone number format")
        }

        return errors
    }

    fun validateRegisterStep2(name: String, email: String, password: String): List<String> {
        val errors = mutableListOf<String>()

        // Name validation
        if (name.length < 2) {
            errors.add("Name must be at least 2 characters")
        }
        if (name.length > 100) {
            errors.add("Name must be at most 100 characters")
        }

        // Email validation
        if (!isValidEmail(email)) {
            errors.add("Invalid email format")
        }

        // Password validation
        if (password.length < 8) {
            errors.add("Password must be at least 8 characters")
        }
        if (password.length > 100) {
            errors.add("Password must be at most 100 characters")
        }

        return errors
    }

    fun validateLogin(identifier: String, password: String): List<String> {
        val errors = mutableListOf<String>()

        if (identifier.length < 3) {
            errors.add("Identifier must be at least 3 characters")
        }
        if (password.isEmpty()) {
            errors.add("Password is required")
        }

        return errors
    }

    // Alternative: Simpler Konform validations if you prefer
    val phoneValidation = Validation<String> {
        minLength(10) hint "Phone number must be at least 10 characters"
        maxLength(15) hint "Phone number must be at most 15 characters"
        pattern("^\\+?[1-9]\\d{1,14}$") hint "Invalid phone number format"
    }

    val emailValidation = Validation<String> {
        pattern("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$") hint "Invalid email format"
    }

    val nameValidation = Validation<String> {
        minLength(2) hint "Name must be at least 2 characters"
        maxLength(100) hint "Name must be at most 100 characters"
    }

    val passwordValidation = Validation<String> {
        minLength(8) hint "Password must be at least 8 characters"
        maxLength(100) hint "Password must be at most 100 characters"
    }
}