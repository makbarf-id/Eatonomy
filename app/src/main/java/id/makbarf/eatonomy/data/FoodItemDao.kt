package id.makbarf.eatonomy.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface FoodItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(foodItem: FoodItem)

    @Update
    suspend fun update(foodItem: FoodItem)

    @Delete
    suspend fun delete(foodItem: FoodItem)

    @Query("SELECT * FROM food_items")
    fun getAllFoodItems(): LiveData<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE id = :id")
    fun getFoodItemById(id: Int): LiveData<FoodItem>

    @Query("""
        SELECT * FROM food_items 
        WHERE (:nameQuery IS NULL OR LOWER(name) LIKE '%' || :nameQuery || '%')
        AND (:storeQuery IS NULL OR LOWER(storeSource) LIKE '%' || :storeQuery || '%')
        AND (:brandQuery IS NULL OR LOWER(brandName) LIKE '%' || :brandQuery || '%')
        AND (:categoryQuery IS NULL OR LOWER(category) LIKE '%' || :categoryQuery || '%')
        ORDER BY name ASC
    """)
    fun searchFoodItems(
        nameQuery: String?,
        storeQuery: String?,
        brandQuery: String?,
        categoryQuery: String?
    ): LiveData<List<FoodItem>>
} 