package id.makbarf.eatonomy.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "grocery_plans",
    indices = [
        Index(value = ["budgetId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = Budget::class,
            parentColumns = ["id"],
            childColumns = ["budgetId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class GroceryPlan(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val budgetId: Int,
    val plannedDate: LocalDate,
    val status: GroceryPlanStatus,
    val estimatedTotal: Double = 0.0,
    val actualTotal: Double = 0.0,
    val completedDate: LocalDate? = null,
    val notes: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) 