package id.makbarf.eatonomy.data

import androidx.lifecycle.LiveData
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow

class PlannedGroceryItemRepository(private val plannedGroceryItemDao: PlannedGroceryItemDao) {
    // Basic CRUD
    suspend fun insert(item: PlannedGroceryItem) {
        plannedGroceryItemDao.insert(item)
    }

    suspend fun update(item: PlannedGroceryItem) {
        plannedGroceryItemDao.update(item)
    }

    suspend fun delete(item: PlannedGroceryItem) {
        plannedGroceryItemDao.delete(item)
    }

    // Queries
    fun getItemsByPlan(planId: Int): LiveData<List<PlannedGroceryItem>> {
        return plannedGroceryItemDao.getItemsByPlan(planId)
    }

    fun getItemsByPurchaseStatus(planId: Int, isPurchased: Boolean): LiveData<List<PlannedGroceryItem>> {
        return plannedGroceryItemDao.getItemsByPurchaseStatus(planId, isPurchased)
    }

    // Aggregate Queries
    fun getTotalEstimatedCost(planId: Int): LiveData<Double> {
        return plannedGroceryItemDao.getTotalEstimatedCost(planId)
    }

    fun getTotalActualCost(planId: Int): LiveData<Double> {
        return plannedGroceryItemDao.getTotalActualCost(planId)
    }

    // Status Updates
    suspend fun updatePurchaseStatus(itemId: Int, isPurchased: Boolean, actualCost: Double?) {
        plannedGroceryItemDao.updatePurchaseStatus(itemId, isPurchased, actualCost, LocalDateTime.now())
    }

    // Batch Operations
    suspend fun markAllAsPurchased(planId: Int) {
        plannedGroceryItemDao.markAllAsPurchased(planId, LocalDateTime.now())
    }

    // Quantity Calculations
    fun getTotalQuantityForFood(foodItemId: Int, planId: Int): LiveData<Double> {
        return plannedGroceryItemDao.getTotalQuantityForFood(foodItemId, planId)
    }
} 