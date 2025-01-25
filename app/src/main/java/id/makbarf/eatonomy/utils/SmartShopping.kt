package id.makbarf.eatonomy.utils

import id.makbarf.eatonomy.data.FoodItem
import id.makbarf.eatonomy.data.PlannedGroceryItem
import kotlin.math.abs

class SmartShopping {
    data class NutritionalBalance(
        val protein: Double,
        val carbs: Double,
        val fats: Double,
        val fiber: Double
    )

    data class CostEfficiency(
        val costPerCalorie: Double,
        val costPerProtein: Double,
        val costPerNutrient: Map<String, Double>
    )

    fun calculateNutritionalBalance(items: List<PlannedGroceryItem>, foods: List<FoodItem>): NutritionalBalance {
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

        return NutritionalBalance(
            protein = totalProtein,
            carbs = totalCarbs,
            fats = totalFats,
            fiber = totalFiber
        )
    }

    fun isBalanced(balance: NutritionalBalance, targetBalance: NutritionalBalance, tolerance: Double = 0.1): Boolean {
        return abs(balance.protein - targetBalance.protein) <= tolerance &&
               abs(balance.carbs - targetBalance.carbs) <= tolerance &&
               abs(balance.fats - targetBalance.fats) <= tolerance &&
               balance.fiber >= targetBalance.fiber
    }

    fun calculateCostEfficiency(food: FoodItem): CostEfficiency {
        val costPerCalorie = if (food.calories > 0) food.price / food.calories else 0.0
        val costPerProtein = if (food.protein != null && food.protein > 0) 
            food.price / food.protein else 0.0

        val nutrientCosts = mutableMapOf<String, Double>()
        if (food.protein != null && food.protein > 0) {
            nutrientCosts["protein"] = food.price / food.protein
        }
        if (food.carbohydrates != null && food.carbohydrates > 0) {
            nutrientCosts["carbs"] = food.price / food.carbohydrates
        }
        if (food.fats != null && food.fats > 0) {
            nutrientCosts["fats"] = food.price / food.fats
        }

        return CostEfficiency(
            costPerCalorie = costPerCalorie,
            costPerProtein = costPerProtein,
            costPerNutrient = nutrientCosts
        )
    }

    fun suggestAlternatives(
        targetFood: FoodItem,
        availableFoods: List<FoodItem>,
        maxPriceDifference: Double = 50.0
    ): List<FoodItem> {
        val targetEfficiency = calculateCostEfficiency(targetFood)
        
        return availableFoods
            .filter { it.id != targetFood.id }
            .filter { abs(it.price - targetFood.price) <= maxPriceDifference }
            .filter { it.calories > 0 && targetFood.calories > 0 }
            .sortedBy { food ->
                val efficiency = calculateCostEfficiency(food)
                efficiency.costPerCalorie - targetEfficiency.costPerCalorie
            }
            .take(5)
    }

    fun optimizeShopping(
        plannedItems: List<PlannedGroceryItem>,
        availableFoods: List<FoodItem>,
        budget: Double
    ): List<PlannedGroceryItem> {
        // Simple knapsack-like optimization
        val optimizedItems = plannedItems.toMutableList()
        var currentCost = plannedItems.sumOf { it.estimatedCost }

        if (currentCost <= budget) return plannedItems

        // Try to optimize by finding cheaper alternatives
        plannedItems.forEach { item ->
            if (currentCost <= budget) return@forEach

            availableFoods.find { it.id == item.foodItemId }?.let { currentFood ->
                val alternatives = suggestAlternatives(currentFood, availableFoods)
                alternatives.firstOrNull { it.price < currentFood.price }?.let { alternative ->
                    val index = optimizedItems.indexOf(item)
                    val newEstimatedCost = (alternative.price * item.quantity)
                    val costDifference = item.estimatedCost - newEstimatedCost
                    
                    optimizedItems[index] = item.copy(
                        foodItemId = alternative.id,
                        estimatedCost = newEstimatedCost
                    )
                    currentCost -= costDifference
                }
            }
        }

        return optimizedItems
    }
} 