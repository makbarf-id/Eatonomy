package id.makbarf.eatonomy.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromWeightGoalType(value: WeightGoalType): String {
        return value.name
    }

    @TypeConverter
    fun toWeightGoalType(value: String): WeightGoalType {
        return WeightGoalType.valueOf(value)
    }
} 