package id.makbarf.eatonomy.utils

import id.makbarf.eatonomy.data.Budget
import id.makbarf.eatonomy.data.BudgetRepository
import id.makbarf.eatonomy.data.BudgetStatus
import java.time.LocalDate
import java.time.LocalDateTime

class BudgetManager(
    private val repository: BudgetRepository,
    private val validator: BudgetValidator,
    private val calculator: BudgetCalculator
) {
    suspend fun createBudget(budget: Budget): Result<Budget> {
        return when (val validationResult = validator.validate(budget)) {
            is BudgetValidationResult.Valid -> {
                try {
                    repository.insert(budget)
                    Result.success(budget)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
            is BudgetValidationResult.Invalid -> {
                Result.failure(IllegalArgumentException(validationResult.errors.joinToString("\n")))
            }
        }
    }

    suspend fun updateBudgetAmount(budgetId: Int, newAmount: Double): Result<Unit> {
        return try {
            repository.getBudgetById(budgetId).value?.let { budget ->
                val updatedBudget = budget.copy(
                    amount = newAmount,
                    remainingAmount = newAmount - budget.spentAmount,
                    updatedAt = LocalDateTime.now()
                )
                repository.update(updatedBudget)
                Result.success(Unit)
            } ?: Result.failure(IllegalArgumentException("Budget not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recordExpense(budgetId: Int, amount: Double): Result<Unit> {
        return try {
            repository.updateBudgetSpending(budgetId, amount)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun closeBudget(budgetId: Int): Result<Unit> {
        return try {
            repository.updateBudgetStatus(budgetId, BudgetStatus.COMPLETED)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun checkBudgetStatus(budget: Budget): BudgetStatus {
        return when {
            LocalDate.now().isBefore(budget.startDate) -> BudgetStatus.UPCOMING
            LocalDate.now().isAfter(budget.endDate) -> BudgetStatus.COMPLETED
            else -> BudgetStatus.ACTIVE
        }
    }

    fun getBudgetWarnings(budget: Budget): List<String> {
        val warnings = mutableListOf<String>()
        
        // Check overspending
        if (calculator.isOverBudget(budget)) {
            warnings.add("Budget exceeded by ${budget.spentAmount - budget.amount}")
        }

        // Check if budget is nearly depleted
        val progress = calculator.calculateSpendingProgress(budget)
        if (progress >= 90) {
            warnings.add("Budget is nearly depleted (${progress.toInt()}% used)")
        }

        // Check projected overspend
        val averageDailySpending = calculator.calculateAverageDailySpending(budget)
        val projectedOverspend = calculator.calculateProjectedOverspend(budget, averageDailySpending)
        if (projectedOverspend > 0) {
            warnings.add("Projected to exceed budget by $projectedOverspend")
        }

        return warnings
    }
} 