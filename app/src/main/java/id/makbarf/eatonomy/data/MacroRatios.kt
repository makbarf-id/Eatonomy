package id.makbarf.eatonomy.data

data class MacroRatios(
    val proteinRatio: Double = 0.25,  // 25%
    val carbsRatio: Double = 0.50,    // 50%
    val fatsRatio: Double = 0.25      // 25%
) {
    companion object {
        val BALANCED = MacroRatios(0.25, 0.50, 0.25)
        val HIGH_PROTEIN = MacroRatios(0.35, 0.40, 0.25)
        val LOW_CARB = MacroRatios(0.30, 0.30, 0.40)
    }
} 