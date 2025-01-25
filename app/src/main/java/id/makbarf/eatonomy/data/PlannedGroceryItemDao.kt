package id.makbarf.eatonomy.data


import androidx.lifecycle.LiveData
import androidx.room.*
import java.time.LocalDateTime

@Dao
interface PlannedGroceryItemDao {
    // Basic CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PlannedGroceryItem)

    @Update
    suspend fun update(item: PlannedGroceryItem)

    @Delete
    suspend fun delete(item: PlannedGroceryItem)

    // Queries
    @Query("SELECT * FROM planned_grocery_items WHERE planId = :planId")
    fun getItemsByPlan(planId: Int): LiveData<List<PlannedGroceryItem>>

    @Query("""
        SELECT * FROM planned_grocery_items 
        WHERE planId = :planId AND isPurchased = :isPurchased
    """)
    fun getItemsByPurchaseStatus(planId: Int, isPurchased: Boolean): LiveData<List<PlannedGroceryItem>>

    // Aggregate queries
    @Query("""
        SELECT SUM(estimatedCost) 
        FROM planned_grocery_items 
        WHERE planId = :planId
    """)
    fun getTotalEstimatedCost(planId: Int): LiveData<Double>

    @Query("""
        SELECT SUM(actualCost) 
        FROM planned_grocery_items 
        WHERE planId = :planId AND isPurchased = 1
    """)
    fun getTotalActualCost(planId: Int): LiveData<Double>

    // Status updates
    @Query("""
        UPDATE planned_grocery_items 
        SET isPurchased = :isPurchased,
            actualCost = CASE 
                WHEN :isPurchased = 1 THEN :actualCost 
                ELSE null 
            END,
            updatedAt = :timestamp 
        WHERE id = :itemId
    """)
    suspend fun updatePurchaseStatus(
        itemId: Int, 
        isPurchased: Boolean, 
        actualCost: Double?,
        timestamp: LocalDateTime
    )

    // Batch operations
    @Query("""
        UPDATE planned_grocery_items 
        SET isPurchased = 1,
            actualCost = estimatedCost,
            updatedAt = :timestamp 
        WHERE planId = :planId
    """)
    suspend fun markAllAsPurchased(planId: Int, timestamp: LocalDateTime)

    // Quantity calculations
    @Query("""
        SELECT SUM(quantity) 
        FROM planned_grocery_items 
        WHERE foodItemId = :foodItemId AND planId = :planId
    """)
    fun getTotalQuantityForFood(foodItemId: Int, planId: Int): LiveData<Double>
} 