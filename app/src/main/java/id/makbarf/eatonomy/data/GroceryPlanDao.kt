package id.makbarf.eatonomy.data


import androidx.lifecycle.LiveData
import androidx.room.*
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow

@Dao
interface GroceryPlanDao {
    // Basic CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plan: GroceryPlan)

    @Update
    suspend fun update(plan: GroceryPlan)

    @Delete
    suspend fun delete(plan: GroceryPlan)

    // Queries
    @Query("SELECT * FROM grocery_plans ORDER BY plannedDate DESC")
    fun getAllPlans(): LiveData<List<GroceryPlan>>

    @Query("SELECT * FROM grocery_plans WHERE id = :id")
    fun getPlanById(id: Int): LiveData<GroceryPlan>

    @Query("""
        SELECT * FROM grocery_plans 
        WHERE budgetId = :budgetId 
        ORDER BY plannedDate DESC
    """)
    fun getPlansByBudget(budgetId: Int): LiveData<List<GroceryPlan>>

    @Query("""
        SELECT * FROM grocery_plans 
        WHERE status = :status 
        ORDER BY plannedDate DESC
    """)
    fun getPlansByStatus(status: GroceryPlanStatus): LiveData<List<GroceryPlan>>

    // Date-based queries
    @Query("""
        SELECT * FROM grocery_plans 
        WHERE plannedDate >= :startDate AND plannedDate <= :endDate 
        ORDER BY plannedDate DESC
    """)
    fun getPlansBetweenDates(startDate: LocalDate, endDate: LocalDate): LiveData<List<GroceryPlan>>

    // Aggregate queries
    @Query("""
        SELECT SUM(actualTotal) 
        FROM grocery_plans 
        WHERE budgetId = :budgetId AND status = 'COMPLETED'
    """)
    fun getTotalSpentForBudget(budgetId: Int): LiveData<Double>

    // Status updates
    @Query("""
        UPDATE grocery_plans 
        SET status = :newStatus,
            completedDate = CASE 
                WHEN :newStatus = 'COMPLETED' THEN :completionDate 
                ELSE null 
            END,
            updatedAt = :timestamp 
        WHERE id = :planId
    """)
    suspend fun updatePlanStatus(
        planId: Int, 
        newStatus: GroceryPlanStatus, 
        completionDate: LocalDate?,
        timestamp: LocalDateTime
    )

    // Cost updates
    @Query("""
        UPDATE grocery_plans 
        SET estimatedTotal = :estimatedTotal,
            updatedAt = :timestamp 
        WHERE id = :planId
    """)
    suspend fun updateEstimatedTotal(planId: Int, estimatedTotal: Double, timestamp: LocalDateTime)

    @Query("""
        UPDATE grocery_plans 
        SET actualTotal = :actualTotal,
            updatedAt = :timestamp 
        WHERE id = :planId
    """)
    suspend fun updateActualTotal(planId: Int, actualTotal: Double, timestamp: LocalDateTime)

    @Query("SELECT * FROM planned_grocery_items WHERE planId = :planId")
    fun getItemsByPlan(planId: Int): Flow<List<PlannedGroceryItem>>
} 