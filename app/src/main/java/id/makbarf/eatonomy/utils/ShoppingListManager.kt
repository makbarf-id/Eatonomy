package id.makbarf.eatonomy.utils

import id.makbarf.eatonomy.data.*
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime

class ShoppingListManager(
    private val groceryPlanRepository: GroceryPlanRepository,
    private val plannedItemRepository: PlannedGroceryItemRepository,
    private val budgetRepository: BudgetRepository,
    private val calculator: BudgetCalculator
) {
    suspend fun createShoppingList(
        name: String,
        budgetId: Int,
        plannedDate: LocalDate
    ): Result<GroceryPlan> {
        return try {
            val plan = GroceryPlan(
                name = name,
                budgetId = budgetId,
                status = GroceryPlanStatus.PLANNING,
                plannedDate = plannedDate
            )
            groceryPlanRepository.insert(plan)
            Result.success(plan)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addItemToPlan(
        planId: Int,
        foodItem: FoodItem,
        quantity: Double,
        unit: String
    ): Result<PlannedGroceryItem> {
        return try {
            val estimatedCost = calculateEstimatedCost(foodItem.price, quantity)
            val plannedItem = PlannedGroceryItem(
                planId = planId,
                foodItemId = foodItem.id,
                quantity = quantity,
                unit = unit,
                estimatedCost = estimatedCost
            )
            plannedItemRepository.insert(plannedItem)
            updatePlanEstimatedTotal(planId)
            Result.success(plannedItem)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun updatePlanEstimatedTotal(planId: Int) {
        plannedItemRepository.getItemsByPlan(planId).value?.let { items ->
            val total = items.sumOf { item -> item.estimatedCost }
            groceryPlanRepository.updateEstimatedTotal(planId, total)
        }
    }

    private fun calculateEstimatedCost(unitPrice: Double, quantity: Double): Double {
        return unitPrice * quantity
    }

    suspend fun updateItemQuantity(
        itemId: Int,
        newQuantity: Double,
        foodItem: FoodItem
    ): Result<Unit> = try {
        plannedItemRepository.getItemsByPlan(itemId).value?.firstOrNull()?.let { item ->
            val updatedItem = item.copy(
                quantity = newQuantity,
                estimatedCost = calculateEstimatedCost(foodItem.price, newQuantity),
                updatedAt = LocalDateTime.now()
            )
            plannedItemRepository.update(updatedItem)
            updatePlanEstimatedTotal(item.planId)
            Result.success(Unit)
        } ?: Result.failure(IllegalArgumentException("Item not found"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun markItemAsPurchased(
        itemId: Int,
        actualCost: Double
    ): Result<Unit> {
        return try {
            plannedItemRepository.updatePurchaseStatus(itemId, true, actualCost)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun completePlan(planId: Int): Result<Unit> {
        return try {
            groceryPlanRepository.updatePlanStatus(planId, GroceryPlanStatus.COMPLETED)
            plannedItemRepository.getTotalActualCost(planId).value?.let { actualTotal ->
                groceryPlanRepository.updateActualTotal(planId, actualTotal)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 