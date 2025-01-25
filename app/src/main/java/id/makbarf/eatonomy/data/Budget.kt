package id.makbarf.eatonomy.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val amount: Double,
    val currency: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val status: BudgetStatus,
    val spentAmount: Double = 0.0,
    val remainingAmount: Double = amount,
    val notes: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) 