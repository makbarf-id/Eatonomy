package id.makbarf.eatonomy.utils

import id.makbarf.eatonomy.data.Budget
import java.time.LocalDate

sealed class BudgetValidationResult {
    object Valid : BudgetValidationResult()
    data class Invalid(val errors: List<String>) : BudgetValidationResult()
}

class BudgetValidator {
    fun validate(budget: Budget): BudgetValidationResult {
        val errors = mutableListOf<String>()

        // Name validation
        if (budget.name.isBlank()) {
            errors.add("Budget name cannot be empty")
        }

        // Amount validation
        if (budget.amount <= 0) {
            errors.add("Budget amount must be greater than 0")
        }

        // Date validation
        if (budget.startDate.isAfter(budget.endDate)) {
            errors.add("Start date cannot be after end date")
        }

        // Currency validation
        if (!isValidCurrency(budget.currency)) {
            errors.add("Invalid currency code")
        }

        return if (errors.isEmpty()) {
            BudgetValidationResult.Valid
        } else {
            BudgetValidationResult.Invalid(errors)
        }
    }

    private fun isValidCurrency(currency: String): Boolean {
        return currency.matches(Regex("[A-Z]{3}"))
    }
} 