package id.makbarf.eatonomy.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index
import java.time.LocalDateTime

@Entity(
    tableName = "planned_grocery_items",
    indices = [
        Index("planId"),
        Index("foodItemId")
    ],
    foreignKeys = [
        ForeignKey(
            entity = GroceryPlan::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FoodItem::class,
            parentColumns = ["id"],
            childColumns = ["foodItemId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PlannedGroceryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val planId: Int,
    val foodItemId: Int,
    val quantity: Double,
    val unit: String,
    val estimatedCost: Double,
    val actualCost: Double? = null,
    val isPurchased: Boolean = false,
    val notes: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) 