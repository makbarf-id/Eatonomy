package id.makbarf.eatonomy.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "household_members")
data class HouseholdMember(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    // Physical Properties
    val height: Double,      // in cm
    val weight: Double,      // in kg
    val age: Int,
    val isMale: Boolean,
    val activityLevel: String,
    // Weight Goals
    val weightGoalType: WeightGoalType,  // Changed from String to WeightGoalType
    val targetWeight: Double?,   // null if MAINTAIN
    val targetDate: Long?,       // null if MAINTAIN
    // Calculated Values
    val dailyCalorieGoal: Int,
    val proteinGoal: Float,
    val carbsGoal: Float,
    val fatsGoal: Float,
    val notes: String? = null
)

// Add enum for weight goals
/*
enum class WeightGoalType {
    MAINTAIN,
    GAIN,
    LOSS
}
*/ 