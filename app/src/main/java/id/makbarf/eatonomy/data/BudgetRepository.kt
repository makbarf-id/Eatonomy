package id.makbarf.eatonomy.data

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

class BudgetRepository(private val budgetDao: BudgetDao) {
    // Keep LiveData for UI lists that don't need real-time updates
    val allBudgets: LiveData<List<Budget>> = budgetDao.getAllBudgets()

    fun getActiveBudgetForDate(date: LocalDate): LiveData<Budget?> {
        return budgetDao.getActiveBudgetForDate(date)
    }

    suspend fun insert(budget: Budget) {
        budgetDao.insert(budget)
    }

    suspend fun update(budget: Budget) {
        budgetDao.update(budget)
    }

    suspend fun delete(budget: Budget) {
        budgetDao.delete(budget)
    }

    // Queries
    fun getBudgetById(id: Int): LiveData<Budget> {
        return budgetDao.getBudgetById(id)
    }

    fun getBudgetsByStatus(status: BudgetStatus): LiveData<List<Budget>> {
        return budgetDao.getBudgetsByStatus(status)
    }

    fun getBudgetsBetweenDates(startDate: LocalDate, endDate: LocalDate): LiveData<List<Budget>> {
        return budgetDao.getBudgetsBetweenDates(startDate, endDate)
    }

    // Aggregate Queries
    fun getTotalSpentBetweenDates(startDate: LocalDate, endDate: LocalDate): LiveData<Double> {
        return budgetDao.getTotalSpentBetweenDates(startDate, endDate)
    }

    // Status Updates
    suspend fun updateBudgetStatus(budgetId: Int, newStatus: BudgetStatus) {
        budgetDao.updateBudgetStatus(budgetId, newStatus, LocalDateTime.now())
    }

    // Spending Updates
    suspend fun updateBudgetSpending(budgetId: Int, amount: Double) {
        budgetDao.updateBudgetSpending(budgetId, amount, LocalDateTime.now())
    }
} 