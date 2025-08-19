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

    val registerStep1Validation = Validation<Map<String, String>> {
        required("phone") {
            minLength(10)
            maxLength(15)
            pattern("^\\+?[1-9]\\d{1,14}$") hint "Invalid phone number format"
        }
    }

    val registerStep2Validation = Validation<Map<String, String>> {
        required("name") {
            minLength(2)
            maxLength(100)
        }
        required("email") {
            pattern("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$") hint "Invalid email format"
        }
        required("password") {
            minLength(8)
            maxLength(100)
        }
    }

    val loginValidation = Validation<Map<String, String>> {
        required("identifier") {
            minLength(3)
        }
        required("password") {
            minLength(1)
        }
    }
}