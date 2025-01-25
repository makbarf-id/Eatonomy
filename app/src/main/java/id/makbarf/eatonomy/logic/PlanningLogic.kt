package id.makbarf.eatonomy.logic

import id.makbarf.eatonomy.data.FoodItem
import id.makbarf.eatonomy.data.PlannedGroceryItem

class PlanningLogic {
    data class NutritionalGoals(
        val calories: Double,
        val protein: Double,
        val carbs: Double,
        val fats: Double,
        val fiber: Double
    )

    data class OptimizationResult(
        val optimizedItems: List<PlannedGroceryItem>,
        val totalCost: Double,
        val savings: Double,
        val nutritionalBalance: NutritionalBalance
    )

    data class NutritionalBalance(
        val proteinPercentage: Double,
        val carbsPercentage: Double,
        val fatsPercentage: Double,
        val fiberGrams: Double,
        val isBalanced: Boolean
    )

    fun optimizeShoppingList(
        items: List<PlannedGroceryItem>,
        availableFoods: List<FoodItem>,
        budget: Double,
        nutritionalGoals: NutritionalGoals
    ): OptimizationResult {
        val originalCost = items.sumOf { it.estimatedCost }
        val optimizedItems = if (originalCost > budget) {
            findCheaperAlternatives(items, availableFoods, budget)
        } else {
            items
        }

        val newCost = optimizedItems.sumOf { it.estimatedCost }
        val nutritionalBalance = calculateNutritionalBalance(optimizedItems, availableFoods, nutritionalGoals)

        return OptimizationResult(
            optimizedItems = optimizedItems,
            totalCost = newCost,
            savings = originalCost - newCost,
            nutritionalBalance = nutritionalBalance
        )
    }

    private fun findCheaperAlternatives(
        items: List<PlannedGroceryItem>,
        availableFoods: List<FoodItem>,
        budget: Double
    ): List<PlannedGroceryItem> {
        val optimizedItems = items.toMutableList()
        var currentCost = items.sumOf { it.estimatedCost }

        // Sort items by cost to optimize expensive items first
        val sortedItems = items.sortedByDescending { it.estimatedCost }

        for (item in sortedItems) {
            if (currentCost <= budget) break

            val currentFood = availableFoods.find { it.id == item.foodItemId } ?: continue
            val alternatives = findAlternatives(currentFood, availableFoods)

            for (alternative in alternatives) {
                if (alternative.price < currentFood.price) {
                    val index = optimizedItems.indexOf(item)
                    val newEstimatedCost = alternative.price * item.quantity
                    val costDifference = item.estimatedCost - newEstimatedCost

                    optimizedItems[index] = item.copy(
                        foodItemId = alternative.id,
                        estimatedCost = newEstimatedCost
                    )
                    currentCost -= costDifference
                    break
                }
            }
        }

        return optimizedItems
    }

    private fun findAlternatives(
        food: FoodItem,
        availableFoods: List<FoodItem>
    ): List<FoodItem> {
        return availableFoods
            .filter { it.id != food.id }
            .filter { it.category == food.category }
            .filter { it.calories > 0 && food.calories > 0 }
            .sortedBy { it.price / it.calories }
            .take(5)
    }

    private fun calculateNutritionalBalance(
        items: List<PlannedGroceryItem>,
        foods: List<FoodItem>,
        goals: NutritionalGoals
    ): NutritionalBalance {
        var totalProtein = 0.0
        var totalCarbs = 0.0
        var totalFats = 0.0
        var totalFiber = 0.0

        items.forEach { item ->
            foods.find { it.id == item.foodItemId }?.let { food ->
                val multiplier = item.quantity
                totalProtein += (food.protein ?: 0.0) * multiplier
                totalCarbs += (food.carbohydrates ?: 0.0) * multiplier
                totalFats += (food.fats ?: 0.0) * multiplier
                totalFiber += (food.fiber ?: 0.0) * multiplier
            }
        }

        val totalMacros = totalProtein + totalCarbs + totalFats
        val proteinPercentage = if (totalMacros > 0) (totalProtein / totalMacros) * 100 else 0.0
        val carbsPercentage = if (totalMacros > 0) (totalCarbs / totalMacros) * 100 else 0.0
        val fatsPercentage = if (totalMacros > 0) (totalFats / totalMacros) * 100 else 0.0

        val isBalanced = isNutritionallyBalanced(
            proteinPercentage,
            carbsPercentage,
            fatsPercentage,
            totalFiber,
            goals
        )

        return NutritionalBalance(
            proteinPercentage = proteinPercentage,
            carbsPercentage = carbsPercentage,
            fatsPercentage = fatsPercentage,
            fiberGrams = totalFiber,
            isBalanced = isBalanced
        )
    }

    private fun isNutritionallyBalanced(
        proteinPercentage: Double,
        carbsPercentage: Double,
        fatsPercentage: Double,
        fiber: Double,
        goals: NutritionalGoals
    ): Boolean {
        val proteinInRange = proteinPercentage >= 10 && proteinPercentage <= 35
        val carbsInRange = carbsPercentage >= 45 && carbsPercentage <= 65
        val fatsInRange = fatsPercentage >= 20 && fatsPercentage <= 35
        val fiberMeetsGoal = fiber >= goals.fiber

        return proteinInRange && carbsInRange && fatsInRange && fiberMeetsGoal
    }
} 