package id.makbarf.eatonomy.data

import android.util.Log
import androidx.lifecycle.LiveData

class FoodRepository(private val foodItemDao: FoodItemDao) {

    val allFoodItems: LiveData<List<FoodItem>> = foodItemDao.getAllFoodItems()

    suspend fun insert(foodItem: FoodItem) {
        try {
            foodItemDao.insert(foodItem)
            Log.d("FoodRepository", "Successfully inserted food item: ${foodItem.name}")
        } catch (e: Exception) {
            Log.e("FoodRepository", "Error inserting food item: ${foodItem.name}", e)
        }
    }

    suspend fun update(foodItem: FoodItem) {
        try {
            foodItemDao.update(foodItem)
            Log.d("FoodRepository", "Successfully updated food item: ${foodItem.name}")
        } catch (e: Exception) {
            Log.e("FoodRepository", "Error updating food item: ${foodItem.name}", e)
        }
    }

    suspend fun delete(foodItem: FoodItem) {
        foodItemDao.delete(foodItem)
    }

    fun getFoodItemById(id: Int): LiveData<FoodItem> {
        return foodItemDao.getFoodItemById(id)
    }

    fun searchFoodItems(
        nameQuery: String?,
        storeQuery: String?,
        brandQuery: String?,
        categoryQuery: String?
    ): LiveData<List<FoodItem>> {
        return foodItemDao.searchFoodItems(nameQuery, storeQuery, brandQuery, categoryQuery)
    }
} 