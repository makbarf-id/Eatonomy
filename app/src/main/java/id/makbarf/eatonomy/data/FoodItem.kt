package id.makbarf.eatonomy.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "food_items")
data class FoodItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "brandName") val brandName: String?,
    @ColumnInfo(name = "storeSource") val storeSource: String?,
    @ColumnInfo(name = "servingSize") val servingSize: Double,
    @ColumnInfo(name = "calories") val calories: Double,
    @ColumnInfo(name = "protein") val protein: Double?,
    @ColumnInfo(name = "carbohydrates") val carbohydrates: Double?,
    @ColumnInfo(name = "fats") val fats: Double?,
    @ColumnInfo(name = "fiber") val fiber: Double?,
    @ColumnInfo(name = "netWeight") val netWeight: Double,
    @ColumnInfo(name = "price") val price: Double,
    @ColumnInfo(name = "currency") val currency: String,
    @ColumnInfo(name = "notes") val notes: String?
) 