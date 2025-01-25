package id.makbarf.eatonomy.utils

import id.makbarf.eatonomy.data.Budget
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class BudgetCalculator {
    fun calculateDailyAllowance(budget: Budget): Double {
        val days = ChronoUnit.DAYS.between(budget.startDate, budget.endDate) + 1
        return budget.remainingAmount / days
    }

    fun calculateSpendingProgress(budget: Budget): Double {
        return (budget.spentAmount / budget.amount) * 100
    }

    fun calculateRemainingDays(budget: Budget): Long {
        return ChronoUnit.DAYS.between(LocalDate.now(), budget.endDate)
    }

    fun isOverBudget(budget: Budget): Boolean {
        return budget.spentAmount > budget.amount
    }

    fun calculateProjectedOverspend(budget: Budget, averageDailySpending: Double): Double {
        val remainingDays = calculateRemainingDays(budget)
        val projectedSpending = budget.spentAmount + (averageDailySpending * remainingDays)
        return if (projectedSpending > budget.amount) {
            projectedSpending - budget.amount
        } else 0.0
    }

    fun calculateAverageDailySpending(budget: Budget): Double {
        val daysSinceStart = ChronoUnit.DAYS.between(budget.startDate, LocalDate.now()) + 1
        return budget.spentAmount / daysSinceStart
    }
} 