package id.makbarf.eatonomy.data

object NutritionGuidelines {
    const val MINIMUM_DAILY_CALORIES_FEMALE = 1200
    const val MINIMUM_DAILY_CALORIES_MALE = 1500
    const val MAXIMUM_WEEKLY_WEIGHT_LOSS = 0.9  // kg/week
    const val MAXIMUM_WEEKLY_WEIGHT_GAIN = 0.5  // kg/week

    data class CalorieResult(
        val calories: Int,
        val warning: String? = null,
        val weeklyWeightChange: Double = 0.0
    )

    fun getRecommendedCalories(
        weight: Double,
        height: Double,
        age: Int,
        isMale: Boolean,
        activityLevel: ActivityLevel,
        weightGoalType: WeightGoalType? = null,
        targetWeight: Double? = null,
        targetDate: Long? = null
    ): CalorieResult {
        val bmr = if (isMale) {
            88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age)
        } else {
            447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age)
        }
        
        val tdee = (bmr * activityLevel.multiplier).toInt()
        val minimumCalories = if (isMale) MINIMUM_DAILY_CALORIES_MALE else MINIMUM_DAILY_CALORIES_FEMALE

        if (weightGoalType == null || weightGoalType == WeightGoalType.MAINTAIN) {
            return CalorieResult(tdee)
        }

        if (targetWeight != null && targetDate != null) {
            val weeksDifference = ((targetDate - System.currentTimeMillis()) / 
                (7L * 24 * 60 * 60 * 1000)).toDouble().coerceAtLeast(1.0)
            val weightDifference = targetWeight - weight
            val weeklyChange = weightDifference / weeksDifference

            // Calculate raw calorie adjustment
            val dailyCalorieAdjustment = (weeklyChange * 7700) / 7
            val rawAdjustedCalories = tdee + dailyCalorieAdjustment.toInt()

            // For weight loss goals
            if (weightGoalType == WeightGoalType.LOSS && rawAdjustedCalories < minimumCalories) {
                val actualWeeklyLoss = ((tdee - minimumCalories) * 7.0) / 7700
                return CalorieResult(
                    calories = minimumCalories,
                    warning = "Target requires unsafe calorie restriction. " +
                            "Adjusted to minimum safe intake (${minimumCalories} kcal). " +
                            "Maximum safe weight loss will be ${String.format("%.1f", -actualWeeklyLoss)} kg/week.",
                    weeklyWeightChange = actualWeeklyLoss
                )
            }

            // For weight gain goals
            if (weightGoalType == WeightGoalType.GAIN && weeklyChange > MAXIMUM_WEEKLY_WEIGHT_GAIN) {
                val maxCalories = tdee + ((MAXIMUM_WEEKLY_WEIGHT_GAIN * 7700) / 7).toInt()
                return CalorieResult(
                    calories = maxCalories,
                    warning = "Target requires too rapid weight gain. " +
                            "Adjusted to maximum safe rate (${MAXIMUM_WEEKLY_WEIGHT_GAIN} kg/week).",
                    weeklyWeightChange = MAXIMUM_WEEKLY_WEIGHT_GAIN
                )
            }

            return CalorieResult(
                calories = rawAdjustedCalories,
                weeklyWeightChange = weeklyChange
            )
        }

        return CalorieResult(tdee)
    }

    enum class ActivityLevel(val multiplier: Double) {
        SEDENTARY(1.2),
        LIGHTLY_ACTIVE(1.375),
        MODERATELY_ACTIVE(1.55),
        VERY_ACTIVE(1.725),
        EXTRA_ACTIVE(1.9)
    }
} 