package id.makbarf.eatonomy.ui.fooddatabase

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import id.makbarf.eatonomy.data.FoodDatabase
import id.makbarf.eatonomy.data.FoodItem
import id.makbarf.eatonomy.data.FoodRepository
import kotlinx.coroutines.launch
import androidx.lifecycle.MediatorLiveData

class FoodViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FoodRepository
    private val _searchQuery = MutableLiveData<String>()
    private val _selectedCategory = MutableLiveData<String>()
    private val _sortOrder = MutableLiveData<SortOrder>()
    private val _foodItems = MediatorLiveData<List<FoodItem>>()
    val foodItems: LiveData<List<FoodItem>> = _foodItems

    init {
        val foodItemDao = FoodDatabase.getDatabase(application).foodItemDao()
        repository = FoodRepository(foodItemDao)
        
        _searchQuery.value = ""
        _selectedCategory.value = "All Categories"
        _sortOrder.value = SortOrder.NAME_ASC

        // Add repository's allFoodItems as a source
        _foodItems.addSource(repository.allFoodItems) { items ->
            updateFoodItems()
        }
        _foodItems.addSource(_searchQuery) { updateFoodItems() }
        _foodItems.addSource(_selectedCategory) { updateFoodItems() }
        _foodItems.addSource(_sortOrder) { updateFoodItems() }
    }

    private fun parseSearchQuery(query: String): SearchCriteria {
        var remainingQuery = query.trim()
        var nameQuery: String? = null
        var storeQuery: String? = null
        var brandQuery: String? = null
        var categoryQuery: String? = null

        // Extract store query (@Store)
        val storeRegex = Regex("""@(\S+)""")
        storeRegex.find(remainingQuery)?.let { match ->
            storeQuery = match.groupValues[1]
            remainingQuery = remainingQuery.replace(match.value, "").trim()
        }

        // Extract brand query (#Brand)
        val brandRegex = Regex("""#(\S+)""")
        brandRegex.find(remainingQuery)?.let { match ->
            brandQuery = match.groupValues[1]
            remainingQuery = remainingQuery.replace(match.value, "").trim()
        }

        // Extract category query ($Category)
        val categoryRegex = Regex("""\$(\S+)""")
        categoryRegex.find(remainingQuery)?.let { match ->
            categoryQuery = match.groupValues[1]
            remainingQuery = remainingQuery.replace(match.value, "").trim()
        }

        // Whatever remains is the name query
        if (remainingQuery.isNotEmpty()) {
            nameQuery = remainingQuery
        }

        return SearchCriteria(nameQuery, storeQuery, brandQuery, categoryQuery)
    }

    fun searchFoodItems(query: String) {
        _searchQuery.value = query
    }

    fun insert(foodItem: FoodItem) = viewModelScope.launch {
        repository.insert(foodItem)
    }

    fun update(foodItem: FoodItem) = viewModelScope.launch {
        repository.update(foodItem)
    }

    fun delete(foodItem: FoodItem) = viewModelScope.launch {
        repository.delete(foodItem)
    }

    fun getFoodItemById(id: Int): LiveData<FoodItem> {
        return repository.getFoodItemById(id)
    }

    enum class SortOrder {
        NAME_ASC, NAME_DESC, 
        PRICE_ASC, PRICE_DESC,
        EFFICIENCY_ASC, EFFICIENCY_DESC
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSortOrder(sortOrder: SortOrder) {
        _sortOrder.value = sortOrder
    }

    private fun updateFoodItems() {
        viewModelScope.launch {
            val searchCriteria = parseSearchQuery(_searchQuery.value ?: "")
            val category = _selectedCategory.value
            val sortOrder = _sortOrder.value

            // Get base list from repository
            val items = repository.allFoodItems.value ?: emptyList()

            // Apply filters
            val filteredItems = items.filter { item ->
                val matchesSearch = if (searchCriteria.nameQuery.isNullOrEmpty() &&
                    searchCriteria.storeQuery.isNullOrEmpty() &&
                    searchCriteria.brandQuery.isNullOrEmpty()) {
                    true
                } else {
                    (searchCriteria.nameQuery?.let { item.name.contains(it, ignoreCase = true) } ?: true) &&
                    (searchCriteria.storeQuery?.let { item.storeSource?.contains(it, ignoreCase = true) } ?: true) &&
                    (searchCriteria.brandQuery?.let { item.brandName?.contains(it, ignoreCase = true) } ?: true)
                }

                val matchesCategory = category == "All Categories" || item.category == category

                matchesSearch && matchesCategory
            }

            // Apply sorting
            val sortedItems = when (sortOrder) {
                SortOrder.NAME_ASC -> filteredItems.sortedBy { it.name }
                SortOrder.NAME_DESC -> filteredItems.sortedByDescending { it.name }
                SortOrder.PRICE_ASC -> filteredItems.sortedBy { it.price }
                SortOrder.PRICE_DESC -> filteredItems.sortedByDescending { it.price }
                SortOrder.EFFICIENCY_ASC -> filteredItems.sortedBy { 
                    if (it.calories > 0 && it.price > 0) it.calories / it.price else 0.0 
                }
                SortOrder.EFFICIENCY_DESC -> filteredItems.sortedByDescending { 
                    if (it.calories > 0 && it.price > 0) it.calories / it.price else 0.0 
                }
                null -> filteredItems
            }

            _foodItems.postValue(sortedItems)
        }
    }
}

data class SearchCriteria(
    val nameQuery: String?,
    val storeQuery: String?,
    val brandQuery: String?,
    val categoryQuery: String?
) 