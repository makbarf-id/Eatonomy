package id.makbarf.eatonomy.logic

import id.makbarf.eatonomy.data.Budget
import id.makbarf.eatonomy.utils.CurrencyFormatter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class BudgetLogic {
    companion object {
        const val WARNING_THRESHOLD = 0.9 // 90% of budget used
        const val CRITICAL_THRESHOLD = 0.95 // 95% of budget used
    }

    data class BudgetAnalysis(
        val dailySpending: Double,
        val projectedSpending: Double,
        val savingsRate: Double,
        val daysRemaining: Long,
        val isOverBudget: Boolean,
        val warningLevel: WarningLevel
    )

    enum class WarningLevel {
        NONE,
        WARNING,
        CRITICAL,
        OVER_BUDGET
    }

    fun analyzeBudget(budget: Budget): BudgetAnalysis {
        val daysElapsed = ChronoUnit.DAYS.between(budget.startDate, LocalDate.now()) + 1
        val daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), budget.endDate)
        val dailySpending = if (daysElapsed > 0) budget.spentAmount / daysElapsed else 0.0
        val projectedSpending = budget.spentAmount + (dailySpending * daysRemaining)
        val savingsRate = if (budget.amount > 0) 
            (budget.amount - budget.spentAmount) / budget.amount else 0.0

        val warningLevel = when {
            budget.spentAmount > budget.amount -> WarningLevel.OVER_BUDGET
            budget.spentAmount / budget.amount > CRITICAL_THRESHOLD -> WarningLevel.CRITICAL
            budget.spentAmount / budget.amount > WARNING_THRESHOLD -> WarningLevel.WARNING
            else -> WarningLevel.NONE
        }

        return BudgetAnalysis(
            dailySpending = dailySpending,
            projectedSpending = projectedSpending,
            savingsRate = savingsRate,
            daysRemaining = daysRemaining,
            isOverBudget = budget.spentAmount > budget.amount,
            warningLevel = warningLevel
        )
    }

    fun calculatePeriodBoundaries(startDate: LocalDate): Pair<LocalDate, LocalDate> {
        val endDate = startDate.plusMonths(1).minusDays(1)
        return Pair(startDate, endDate)
    }

    fun convertCurrency(
        amount: Double,
        fromCurrency: String,
        toCurrency: String,
        exchangeRates: Map<String, Double>
    ): Double {
        if (fromCurrency == toCurrency) return amount
        val baseRate = exchangeRates[fromCurrency] ?: return amount
        val targetRate = exchangeRates[toCurrency] ?: return amount
        return amount * (targetRate / baseRate)
    }

    fun generateAlerts(budget: Budget, analysis: BudgetAnalysis): List<Alert> {
        val alerts = mutableListOf<Alert>()

        // Check overspending
        if (analysis.isOverBudget) {
            alerts.add(
                Alert(
                    type = AlertType.CRITICAL,
                    message = "Budget exceeded by ${
                        CurrencyFormatter.format(
                            budget.spentAmount - budget.amount,
                            budget.currency
                        )
                    }"
                )
            )
        }

        // Check projected overspending
        if (analysis.projectedSpending > budget.amount) {
            alerts.add(
                Alert(
                    type = AlertType.WARNING,
                    message = "Projected to exceed budget by ${
                        CurrencyFormatter.format(
                            analysis.projectedSpending - budget.amount,
                            budget.currency
                        )
                    }"
                )
            )
        }

        // Check spending rate
        when (analysis.warningLevel) {
            WarningLevel.CRITICAL -> {
                alerts.add(
                    Alert(
                        type = AlertType.CRITICAL,
                        message = "Critical: Only ${
                            String.format(
                                "%.1f",
                                (1 - budget.spentAmount / budget.amount) * 100
                            )
                        }% of budget remaining"
                    )
                )
            }
            WarningLevel.WARNING -> {
                alerts.add(
                    Alert(
                        type = AlertType.WARNING,
                        message = "Warning: Budget usage at ${
                            String.format(
                                "%.1f",
                                (budget.spentAmount / budget.amount) * 100
                            )
                        }%"
                    )
                )
            }
            else -> {}
        }

        return alerts
    }

    fun trackHistory(budget: Budget): List<BudgetHistoryEntry> {
        val entries = mutableListOf<BudgetHistoryEntry>()
        
        // Initial budget creation
        entries.add(
            BudgetHistoryEntry(
                date = budget.createdAt,
                type = HistoryEntryType.CREATION,
                amount = budget.amount,
                description = "Budget created"
            )
        )

        // Budget adjustments
        if (budget.updatedAt != budget.createdAt) {
            entries.add(
                BudgetHistoryEntry(
                    date = budget.updatedAt,
                    type = HistoryEntryType.ADJUSTMENT,
                    amount = budget.amount,
                    description = "Budget adjusted"
                )
            )
        }

        return entries
    }
}

data class Alert(
    val type: AlertType,
    val message: String
)

enum class AlertType {
    INFO,
    WARNING,
    CRITICAL
}

data class BudgetHistoryEntry(
    val date: LocalDateTime,
    val type: HistoryEntryType,
    val amount: Double,
    val description: String
)

enum class HistoryEntryType {
    CREATION,
    ADJUSTMENT,
    EXPENSE,
    COMPLETION
} 