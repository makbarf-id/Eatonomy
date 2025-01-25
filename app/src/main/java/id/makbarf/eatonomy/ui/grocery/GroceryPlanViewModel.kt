package id.makbarf.eatonomy.ui.grocery

import androidx.lifecycle.*
import id.makbarf.eatonomy.data.*
import id.makbarf.eatonomy.utils.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.Flow

class GroceryPlanViewModel(
    private val groceryPlanRepository: GroceryPlanRepository,
    private val plannedItemRepository: PlannedGroceryItemRepository,
    private val foodRepository: FoodRepository,
    private val shoppingManager: ShoppingListManager,
    private val smartShopping: SmartShopping
) : ViewModel() {

    private val _uiState = MutableStateFlow<GroceryPlanUiState>(GroceryPlanUiState.Loading)
    val uiState: StateFlow<GroceryPlanUiState> = _uiState

    private val _currentPlan = MutableLiveData<GroceryPlan?>()
    val currentPlan: LiveData<GroceryPlan?> = _currentPlan

    private val _plannedItems = MutableLiveData<List<PlannedGroceryItem>>()
    val plannedItems: LiveData<List<PlannedGroceryItem>> = _plannedItems

    private val _nutritionalBalance = MutableLiveData<SmartShopping.NutritionalBalance>()
    val nutritionalBalance: LiveData<SmartShopping.NutritionalBalance> = _nutritionalBalance

    fun loadPlan(planId: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = GroceryPlanUiState.Loading
                groceryPlanRepository.getPlanById(planId).value?.let { plan ->
                    _currentPlan.value = plan
                    _uiState.value = GroceryPlanUiState.Success(plan)
                    loadPlannedItems(plan.id)
                }
            } catch (e: Exception) {
                _uiState.value = GroceryPlanUiState.Error(e.message ?: "Failed to load plan")
            }
        }
    }

    private fun loadPlannedItems(planId: Int) {
        viewModelScope.launch {
            try {
                plannedItemRepository.getItemsByPlan(planId).value?.let { items ->
                    _plannedItems.value = items
                    updateNutritionalBalance(items)
                }
            } catch (e: Exception) {
                _uiState.value = GroceryPlanUiState.Error(e.message ?: "Failed to load items")
            }
        }
    }

    private suspend fun updateNutritionalBalance(items: List<PlannedGroceryItem>) {
        val foods = items.mapNotNull { item ->
            foodRepository.getFoodItemById(item.foodItemId).value
        }
        _nutritionalBalance.value = smartShopping.calculateNutritionalBalance(items, foods)
    }

    fun createPlan(name: String, budgetId: Int, plannedDate: LocalDate) {
        viewModelScope.launch {
            try {
                _uiState.value = GroceryPlanUiState.Loading
                shoppingManager.createShoppingList(name, budgetId, plannedDate).fold(
                    onSuccess = { plan ->
                        _currentPlan.value = plan
                        _uiState.value = GroceryPlanUiState.Success(plan)
                    },
                    onFailure = { e ->
                        _uiState.value = GroceryPlanUiState.Error(e.message ?: "Failed to create plan")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = GroceryPlanUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun addItem(foodItem: FoodItem, quantity: Double, unit: String) {
        viewModelScope.launch {
            _currentPlan.value?.let { plan ->
                try {
                    shoppingManager.addItemToPlan(plan.id, foodItem, quantity, unit).fold(
                        onSuccess = { loadPlannedItems(plan.id) },
                        onFailure = { e ->
                            _uiState.value = GroceryPlanUiState.Error(e.message ?: "Failed to add item")
                        }
                    )
                } catch (e: Exception) {
                    _uiState.value = GroceryPlanUiState.Error(e.message ?: "Unknown error occurred")
                }
            }
        }
    }

    fun updateItemQuantity(itemId: Int, newQuantity: Double, foodItem: FoodItem) {
        viewModelScope.launch {
            try {
                shoppingManager.updateItemQuantity(itemId, newQuantity, foodItem).fold(
                    onSuccess = { _currentPlan.value?.let { loadPlannedItems(it.id) } },
                    onFailure = { e ->
                        _uiState.value = GroceryPlanUiState.Error(e.message ?: "Failed to update quantity")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = GroceryPlanUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun completePlan() {
        viewModelScope.launch {
            _currentPlan.value?.let { plan ->
                try {
                    shoppingManager.completePlan(plan.id).fold(
                        onSuccess = { loadPlan(plan.id) },
                        onFailure = { e ->
                            _uiState.value = GroceryPlanUiState.Error(e.message ?: "Failed to complete plan")
                        }
                    )
                } catch (e: Exception) {
                    _uiState.value = GroceryPlanUiState.Error(e.message ?: "Unknown error occurred")
                }
            }
        }
    }

    fun getPlansByStatus(status: GroceryPlanStatus): LiveData<List<GroceryPlan>> {
        return groceryPlanRepository.getPlansByStatus(status)
    }
}

sealed class GroceryPlanUiState {
    object Loading : GroceryPlanUiState()
    data class Success(val plan: GroceryPlan) : GroceryPlanUiState()
    data class Error(val message: String) : GroceryPlanUiState()
} 