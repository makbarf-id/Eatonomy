package id.makbarf.eatonomy.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import id.makbarf.eatonomy.data.FoodDatabase
import id.makbarf.eatonomy.data.FoodItem
import id.makbarf.eatonomy.data.FoodRepository

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FoodRepository
    private val _totalItems = MutableLiveData<Int>()
    val totalItems: LiveData<Int> = _totalItems
    
    private val _usedCategories = MutableLiveData<Int>()
    val usedCategories: LiveData<Int> = _usedCategories

    init {
        val foodItemDao = FoodDatabase.getDatabase(application).foodItemDao()
        repository = FoodRepository(foodItemDao)
        
        // Observe food items to update statistics
        repository.allFoodItems.observeForever { items ->
            updateStatistics(items)
        }
    }

    private fun updateStatistics(items: List<FoodItem>) {
        _totalItems.value = items.size
        _usedCategories.value = items.map { it.category }.distinct().size
    }

    override fun onCleared() {
        super.onCleared()
        repository.allFoodItems.removeObserver { updateStatistics(it) }
    }
}