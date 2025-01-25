package id.makbarf.eatonomy.data


import androidx.lifecycle.LiveData
import androidx.room.*
import java.time.LocalDate
import java.time.LocalDateTime

@Dao
interface BudgetDao {
    // Basic CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: Budget)

    @Update
    suspend fun update(budget: Budget)

    @Delete
    suspend fun delete(budget: Budget)

    // Queries
    @Query("SELECT * FROM budgets ORDER BY startDate DESC")
    fun getAllBudgets(): LiveData<List<Budget>>

    @Query("SELECT * FROM budgets WHERE id = :id")
    fun getBudgetById(id: Int): LiveData<Budget>

    @Query("""
        SELECT * FROM budgets 
        WHERE status = :status 
        ORDER BY startDate DESC
    """)
    fun getBudgetsByStatus(status: BudgetStatus): LiveData<List<Budget>>

    @Query("""
        SELECT * FROM budgets 
        WHERE startDate >= :startDate AND endDate <= :endDate 
        ORDER BY startDate DESC
    """)
    fun getBudgetsBetweenDates(startDate: LocalDate, endDate: LocalDate): LiveData<List<Budget>>

    @Query("""
        SELECT * FROM budgets 
        WHERE status = 'ACTIVE' 
        AND startDate <= :date 
        AND endDate >= :date 
        LIMIT 1
    """)
    fun getActiveBudgetForDate(date: LocalDate): LiveData<Budget?>

    // Aggregate Queries
    @Query("""
        SELECT SUM(spentAmount) 
        FROM budgets 
        WHERE startDate >= :startDate AND endDate <= :endDate
    """)
    fun getTotalSpentBetweenDates(startDate: LocalDate, endDate: LocalDate): LiveData<Double>

    // Status Updates
    @Query("""
        UPDATE budgets 
        SET status = :newStatus, 
            updatedAt = :timestamp 
        WHERE id = :budgetId
    """)
    suspend fun updateBudgetStatus(budgetId: Int, newStatus: BudgetStatus, timestamp: LocalDateTime)

    // Spending Updates
    @Query("""
        UPDATE budgets 
        SET spentAmount = spentAmount + :amount,
            remainingAmount = amount - (spentAmount + :amount),
            updatedAt = :timestamp 
        WHERE id = :budgetId
    """)
    suspend fun updateBudgetSpending(budgetId: Int, amount: Double, timestamp: LocalDateTime)
} 