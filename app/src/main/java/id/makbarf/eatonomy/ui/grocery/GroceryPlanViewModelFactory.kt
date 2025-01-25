package id.makbarf.eatonomy.ui.grocery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.makbarf.eatonomy.data.*
import id.makbarf.eatonomy.utils.*

class GroceryPlanViewModelFactory(
    private val groceryPlanRepository: GroceryPlanRepository,
    private val plannedItemRepository: PlannedGroceryItemRepository,
    private val foodRepository: FoodRepository,
    private val shoppingManager: ShoppingListManager,
    private val smartShopping: SmartShopping
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroceryPlanViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GroceryPlanViewModel(
                groceryPlanRepository,
                plannedItemRepository,
                foodRepository,
                shoppingManager,
                smartShopping
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 