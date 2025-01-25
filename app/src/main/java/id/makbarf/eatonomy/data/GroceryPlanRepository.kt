package id.makbarf.eatonomy.data

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

class GroceryPlanRepository(private val groceryPlanDao: GroceryPlanDao) {
    val allPlans: LiveData<List<GroceryPlan>> = groceryPlanDao.getAllPlans()

    fun getPlanById(id: Int): LiveData<GroceryPlan> {
        return groceryPlanDao.getPlanById(id)
    }

    suspend fun insert(plan: GroceryPlan) {
        groceryPlanDao.insert(plan)
    }

    suspend fun update(plan: GroceryPlan) {
        groceryPlanDao.update(plan)
    }

    suspend fun delete(plan: GroceryPlan) {
        groceryPlanDao.delete(plan)
    }

    fun getPlansByBudget(budgetId: Int): LiveData<List<GroceryPlan>> {
        return groceryPlanDao.getPlansByBudget(budgetId)
    }

    fun getPlansByStatus(status: GroceryPlanStatus): LiveData<List<GroceryPlan>> {
        return groceryPlanDao.getPlansByStatus(status)
    }

    fun getPlansBetweenDates(startDate: LocalDate, endDate: LocalDate): LiveData<List<GroceryPlan>> {
        return groceryPlanDao.getPlansBetweenDates(startDate, endDate)
    }

    fun getTotalSpentForBudget(budgetId: Int): LiveData<Double> {
        return groceryPlanDao.getTotalSpentForBudget(budgetId)
    }

    suspend fun updatePlanStatus(planId: Int, newStatus: GroceryPlanStatus) {
        val completionDate = if (newStatus == GroceryPlanStatus.COMPLETED) LocalDate.now() else null
        groceryPlanDao.updatePlanStatus(planId, newStatus, completionDate, LocalDateTime.now())
    }

    suspend fun updateEstimatedTotal(planId: Int, estimatedTotal: Double) {
        groceryPlanDao.updateEstimatedTotal(planId, estimatedTotal, LocalDateTime.now())
    }

    suspend fun updateActualTotal(planId: Int, actualTotal: Double) {
        groceryPlanDao.updateActualTotal(planId, actualTotal, LocalDateTime.now())
    }

    fun getItemsByPlan(planId: Int): Flow<List<PlannedGroceryItem>> {
        return groceryPlanDao.getItemsByPlan(planId)
    }
} 