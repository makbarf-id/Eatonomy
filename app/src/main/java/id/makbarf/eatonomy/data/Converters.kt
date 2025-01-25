package id.makbarf.eatonomy.data

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

class Converters {
    @TypeConverter
    fun fromWeightGoalType(value: WeightGoalType): String {
        return value.name
    }

    @TypeConverter
    fun toWeightGoalType(value: String): WeightGoalType {
        return WeightGoalType.valueOf(value)
    }

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): Long? = value?.toEpochDay()
    
    @TypeConverter
    fun toLocalDate(value: Long?): LocalDate? = value?.let { LocalDate.ofEpochDay(it) }
    
    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): Long? = value?.toEpochSecond(ZoneOffset.UTC)
    
    @TypeConverter
    fun toLocalDateTime(value: Long?): LocalDateTime? = 
        value?.let { LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC) }
    
    @TypeConverter
    fun fromBudgetStatus(value: BudgetStatus): String = value.name
    
    @TypeConverter
    fun toBudgetStatus(value: String): BudgetStatus = BudgetStatus.valueOf(value)
    
    @TypeConverter
    fun fromGroceryPlanStatus(value: GroceryPlanStatus): String = value.name
    
    @TypeConverter
    fun toGroceryPlanStatus(value: String): GroceryPlanStatus = GroceryPlanStatus.valueOf(value)
} 