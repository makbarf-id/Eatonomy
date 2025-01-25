package id.makbarf.eatonomy.ui.household

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import id.makbarf.eatonomy.data.HouseholdMember
import id.makbarf.eatonomy.data.MacroRatios
import id.makbarf.eatonomy.data.NutritionGuidelines
import id.makbarf.eatonomy.data.WeightGoalType
import id.makbarf.eatonomy.data.NutritionGuidelines.ActivityLevel
import id.makbarf.eatonomy.databinding.FragmentHouseholdMemberFormBinding
import id.makbarf.eatonomy.R
import java.text.SimpleDateFormat
import java.util.*
import android.app.DatePickerDialog
import android.view.animation.AnimationUtils
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.util.Log
import kotlin.concurrent.schedule
import java.time.LocalDate

class HouseholdMemberFormFragment : Fragment() {
    private var _binding: FragmentHouseholdMemberFormBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HouseholdMemberViewModel by viewModels()
    private var memberId: Int = 0
    private var selectedRatios = MacroRatios.BALANCED
    private var activityLevel = ActivityLevel.MODERATELY_ACTIVE
    private var isLoadingMember = false
    private var validationState = mutableMapOf<String, Boolean>()
    private var calculationJob: Timer? = null

    companion object {
        private const val MIN_WEIGHT = 30.0
        private const val MAX_WEIGHT = 300.0
        private const val MIN_HEIGHT = 100.0
        private const val MAX_HEIGHT = 250.0
        private const val MIN_AGE = 15
        private const val MAX_AGE = 100
        private const val MIN_WEEKS_FOR_GOAL = 4
        private const val MAX_WEEKS_FOR_GOAL = 52
    }

    private fun Double.roundToInt() = kotlin.math.round(this).toInt()
    private fun Double.roundToDecimal(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return kotlin.math.round(this * multiplier) / multiplier
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHouseholdMemberFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        memberId = arguments?.getInt("memberId", 0) ?: 0
        
        setupButtons()
        if (memberId != 0) {
            loadMember()
        }
        setupCalorieCalculation()
        setupRatioSelection()
        setupSpinners()
        setupCalculatorButton()
        setupWeightGoalUI()
        setupValidations()
    }

    private fun setupButtons() {
        binding.apply {
            buttonSave.setOnClickListener {
                saveMember()
            }
            
            buttonCancel.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun loadMember() {
        isLoadingMember = true
        viewModel.getMemberById(memberId).observe(viewLifecycleOwner) { member ->
            member?.let {
                binding.apply {
                    // Load basic info
                    editTextName.setText(it.name)
                    editTextWeight.setText(it.weight.toString())
                    editTextHeight.setText(it.height.toString())
                    editTextAge.setText(it.age.toString())
                    
                    // Set gender
                    radioGroupGender.check(if (it.isMale) R.id.radioButtonMale else R.id.radioButtonFemale)
                    
                    // Set activity level
                    val activityPosition = when (it.activityLevel) {
                        "SEDENTARY" -> 0
                        "LIGHTLY_ACTIVE" -> 1
                        "MODERATELY_ACTIVE" -> 2
                        "VERY_ACTIVE" -> 3
                        "EXTRA_ACTIVE" -> 4
                        else -> 2 // Default to moderately active
                    }
                    spinnerActivityLevel.setSelection(activityPosition)
                    
                    // Set weight goal type and details
                    when (it.weightGoalType) {
                        WeightGoalType.GAIN -> radioGroupWeightGoal.check(R.id.radioButtonGain)
                        WeightGoalType.LOSS -> radioGroupWeightGoal.check(R.id.radioButtonLoss)
                        WeightGoalType.MAINTAIN -> radioGroupWeightGoal.check(R.id.radioButtonMaintain)
                    }
                    
                    // Show/hide and set weight goal details
                    if (it.weightGoalType != WeightGoalType.MAINTAIN) {
                        layoutWeightGoalDetails.visibility = View.VISIBLE
                        it.targetWeight?.let { weight -> 
                            editTextTargetWeight.setText(weight.toString())
                        }
                        it.targetDate?.let { date ->
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            editTextTargetDate.setText(dateFormat.format(Date(date)))
                        }
                    } else {
                        layoutWeightGoalDetails.visibility = View.GONE
                    }
                    
                    // Set calorie and macro goals
                    editTextCalorieGoal.setText(it.dailyCalorieGoal.toString())
                    editTextProteinGoal.setText(it.proteinGoal.toString())
                    editTextCarbsGoal.setText(it.carbsGoal.toString())
                    editTextFatsGoal.setText(it.fatsGoal.toString())
                    
                    // Set notes
                    editTextNotes.setText(it.notes)
                }
            }
            isLoadingMember = false
        }
    }

    private fun saveMember() {
        binding.apply {
            // Basic validation
            val name = editTextName.text.toString()
            if (name.isBlank()) {
                editTextName.error = "Name is required"
                return
            }

            // Physical properties validation
            val weight = editTextWeight.text.toString().toDoubleOrNull()
            if (weight == null || weight <= 0) {
                editTextWeight.error = "Valid weight is required"
                return
            }

            val height = editTextHeight.text.toString().toDoubleOrNull()
            if (height == null || height <= 0) {
                editTextHeight.error = "Valid height is required"
                return
            }

            val age = editTextAge.text.toString().toIntOrNull()
            if (age == null || age <= 0) {
                editTextAge.error = "Valid age is required"
                return
            }

            // Weight goal validation
            val weightGoalType = when (radioGroupWeightGoal.checkedRadioButtonId) {
                R.id.radioButtonGain -> WeightGoalType.GAIN
                R.id.radioButtonLoss -> WeightGoalType.LOSS
                else -> WeightGoalType.MAINTAIN
            }

            // Target weight and date validation for GAIN/LOSS
            var targetWeight: Double? = null
            var targetDate: Long? = null
            
            if (weightGoalType != WeightGoalType.MAINTAIN) {
                targetWeight = editTextTargetWeight.text.toString().toDoubleOrNull()
                if (targetWeight == null) {
                    editTextTargetWeight.error = "Target weight is required"
                    return
                }

                val dateStr = editTextTargetDate.text.toString()
                if (dateStr.isBlank()) {
                    editTextTargetDate.error = "Target date is required"
                    return
                }
                
                try {
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    targetDate = dateFormat.parse(dateStr)?.time
                    
                    if (targetDate != null && targetDate <= System.currentTimeMillis()) {
                        editTextTargetDate.error = "Target date must be in the future"
                        return
                    }
                } catch (e: Exception) {
                    editTextTargetDate.error = "Invalid date format"
                    return
                }
            }

            // Macro validation
            val calorieGoal = editTextCalorieGoal.text.toString().toIntOrNull()
            if (calorieGoal == null) {
                editTextCalorieGoal.error = "Valid calorie goal is required"
                return
            }

            val proteinGoal = editTextProteinGoal.text.toString().toDoubleOrNull()?.toFloat()
            if (proteinGoal == null) {
                editTextProteinGoal.error = "Valid protein goal is required"
                return
            }

            val carbsGoal = editTextCarbsGoal.text.toString().toDoubleOrNull()?.toFloat()
            if (carbsGoal == null) {
                editTextCarbsGoal.error = "Valid carbs goal is required"
                return
            }

            val fatsGoal = editTextFatsGoal.text.toString().toDoubleOrNull()?.toFloat()
            if (fatsGoal == null) {
                editTextFatsGoal.error = "Valid fats goal is required"
                return
            }

            val member = HouseholdMember(
                id = memberId,
                name = name,
                height = height,
                weight = weight,
                age = age,
                isMale = radioButtonMale.isChecked,
                activityLevel = activityLevel.name,
                weightGoalType = weightGoalType,
                targetWeight = targetWeight,
                targetDate = targetDate,
                dailyCalorieGoal = calorieGoal,
                proteinGoal = proteinGoal,
                carbsGoal = carbsGoal,
                fatsGoal = fatsGoal,
                notes = editTextNotes.text.toString().takeIf { it.isNotBlank() }
            )

            if (memberId == 0) {
                viewModel.insert(member)
                Toast.makeText(context, "Member added successfully", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.update(member)
                Toast.makeText(context, "Member updated successfully", Toast.LENGTH_SHORT).show()
            }

            findNavController().navigateUp()
        }
    }

    private fun setupCalorieCalculation() {
        binding.editTextCalorieGoal.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                calculateMacros()
            }
        })
    }

    private fun calculateMacros() {
        val calories = binding.editTextCalorieGoal.text.toString().toIntOrNull() ?: return
        calculateMacros(calories)
    }

    private fun calculateMacros(calories: Int) {
        // Calculate macros based on selected ratio
        val proteinCalories = calories * selectedRatios.proteinRatio
        val carbsCalories = calories * selectedRatios.carbsRatio
        val fatsCalories = calories * selectedRatios.fatsRatio

        // Convert calories to grams (protein: 4 cal/g, carbs: 4 cal/g, fats: 9 cal/g)
        val proteinGrams = (proteinCalories / 4).roundToDecimal(1)
        val carbsGrams = (carbsCalories / 4).roundToDecimal(1)
        val fatsGrams = (fatsCalories / 9).roundToDecimal(1)

        // Update the UI
        binding.apply {
            editTextProteinGoal.setText(proteinGrams.toString())
            editTextCarbsGoal.setText(carbsGrams.toString())
            editTextFatsGoal.setText(fatsGrams.toString())
        }
    }

    private fun validateMacros(): Boolean {
        binding.apply {
            val calories = editTextCalorieGoal.text.toString().toDoubleOrNull() ?: return false
            
            // Calculate recommended macros
            val recommendedProtein = when (selectedRatios) {
                MacroRatios.HIGH_PROTEIN -> calories * 0.35
                MacroRatios.LOW_CARB -> calories * 0.30
                else -> calories * 0.25
            } / 4  // Convert calories to grams (4 calories per gram of protein)

            val recommendedCarbs = when (selectedRatios) {
                MacroRatios.HIGH_PROTEIN -> calories * 0.40
                MacroRatios.LOW_CARB -> calories * 0.30
                else -> calories * 0.50
            } / 4  // Convert calories to grams (4 calories per gram of carbs)

            val recommendedFats = when (selectedRatios) {
                MacroRatios.LOW_CARB -> calories * 0.40
                else -> calories * 0.25
            } / 9  // Convert calories to grams (9 calories per gram of fat)

            // Get user input
            val userProtein = editTextProteinGoal.text.toString().toDoubleOrNull()
            val userCarbs = editTextCarbsGoal.text.toString().toDoubleOrNull()
            val userFats = editTextFatsGoal.text.toString().toDoubleOrNull()

            // Validate inputs
            if (userProtein == null || userProtein < recommendedProtein * 0.8) {
                editTextProteinGoal.error = "Protein should be at least ${(recommendedProtein * 0.8).roundToDecimal(1)}g"
                return false
            }

            if (userCarbs == null || userCarbs < recommendedCarbs * 0.8) {
                editTextCarbsGoal.error = "Carbs should be at least ${(recommendedCarbs * 0.8).roundToDecimal(1)}g"
                return false
            }

            if (userFats == null || userFats < recommendedFats * 0.8) {
                editTextFatsGoal.error = "Fats should be at least ${(recommendedFats * 0.8).roundToDecimal(1)}g"
                return false
            }

            // Check total calories from macros
            val totalCalories = (userProtein * 4) + (userCarbs * 4) + (userFats * 9)
            if (kotlin.math.abs(totalCalories - calories) > 100) {
                Toast.makeText(context, 
                    "Warning: Total calories from macros (${totalCalories.roundToDecimal(1)}) differs significantly from goal ($calories)", 
                    Toast.LENGTH_LONG).show()
            }

            return true
        }
    }

    private fun setupRatioSelection() {
        binding.spinnerMacroType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedRatios = when(position) {
                    0 -> MacroRatios.BALANCED
                    1 -> MacroRatios.HIGH_PROTEIN
                    2 -> MacroRatios.LOW_CARB
                    else -> MacroRatios.BALANCED
                }
                updateMacroRatiosText()
                calculateMacros()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        // Set initial text
        updateMacroRatiosText()
    }

    private fun updateMacroRatiosText() {
        binding.textViewMacroRatios.text = with(selectedRatios) {
            "• Protein: ${(proteinRatio * 100).toInt()}% (4 kcal/g)\n" +
            "• Carbs: ${(carbsRatio * 100).toInt()}% (4 kcal/g)\n" +
            "• Fats: ${(fatsRatio * 100).toInt()}% (9 kcal/g)"
        }
    }

    private fun setupSpinners() {
        // Activity Level Spinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.activity_levels,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerActivityLevel.adapter = adapter
        }

        binding.spinnerActivityLevel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                activityLevel = getActivityLevelFromPosition(position)
                calculateCalories()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Macro Type Spinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.macro_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerMacroType.adapter = adapter
        }
    }

    private fun setupCalculatorButton() {
        binding.buttonCalculate.setOnClickListener {
            calculateCalories()
        }
    }

    private fun setupWeightGoalUI() {
        binding.apply {
            // Initialize visibility based on current selection
            val initialCheckedId = radioGroupWeightGoal.checkedRadioButtonId
            layoutWeightGoalDetails.visibility = if (initialCheckedId == R.id.radioButtonMaintain) View.GONE else View.VISIBLE

            // Show/hide weight goal details based on selection
            radioGroupWeightGoal.setOnCheckedChangeListener { _, checkedId ->
                val showDetails = checkedId != R.id.radioButtonMaintain
                layoutWeightGoalDetails.visibility = if (showDetails) View.VISIBLE else View.GONE
                
                if (!showDetails && !isLoadingMember) { // Only clear if not loading
                    editTextTargetWeight.text?.clear()
                    editTextTargetDate.text?.clear()
                } else {
                    editTextTargetWeight.isEnabled = true
                    editTextTargetDate.isEnabled = true
                }

                // Validate if fields are filled
                if (showDetails && editTextTargetWeight.text?.isNotEmpty() == true) {
                    val currentWeight = editTextWeight.text.toString().toDoubleOrNull()
                    val targetWeight = editTextTargetWeight.text.toString().toDoubleOrNull()
                    val goalType = when (checkedId) {
                        R.id.radioButtonGain -> "GAIN"
                        R.id.radioButtonLoss -> "LOSS"
                        else -> "MAINTAIN"
                    }
                    validateTargetWeight(currentWeight, targetWeight, goalType)
                }
            }

            // Setup date picker and recalculate on date change
            editTextTargetDate.setOnClickListener {
                showDatePicker()
            }

            // Add text change listener for target weight
            editTextTargetWeight.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (!s.isNullOrEmpty()) {
                        calculateCalories()
                    }
                }
            })
        }
    }

    private fun validateTargetWeight(currentWeight: Double?, targetWeight: Double?, goalType: String): Boolean {
        return binding.editTextTargetWeight.let { editText ->
            when {
                goalType == "MAINTAIN" -> {
                    showValidationSuccess(editText)
                    true
                }
                currentWeight == null -> {
                    showValidationError(editText, "Please set current weight first", false)
                    false
                }
                targetWeight == null -> {
                    showValidationError(editText, "Please enter a valid number", false)
                    false
                }
                targetWeight !in MIN_WEIGHT..MAX_WEIGHT -> {
                    showValidationError(editText, "Target weight must be between $MIN_WEIGHT and $MAX_WEIGHT kg", false)
                    false
                }
                goalType == "GAIN" && targetWeight <= currentWeight -> {
                    showValidationError(editText, "For weight gain, target must be higher than current weight", false)
                    false
                }
                goalType == "LOSS" && targetWeight >= currentWeight -> {
                    showValidationError(editText, "For weight loss, target must be lower than current weight", false)
                    false
                }
                kotlin.math.abs(targetWeight - currentWeight) > currentWeight * 0.3 -> {
                    showValidationError(editText, "Weight change cannot exceed 30% of current weight", false)
                    false
                }
                else -> {
                    showValidationSuccess(editText)
                    true
                }
            }
        }
    }

    private fun validateAllInputs(): Boolean {
        if (isLoadingMember) return false
        
        binding.apply {
            // Basic info validation - fail fast
            val weight = editTextWeight.text.toString().toDoubleOrNull() ?: return false
            if (!validateWeight(weight, false)) return false

            val height = editTextHeight.text.toString().toDoubleOrNull() ?: return false
            if (!validateHeight(height)) return false

            val age = editTextAge.text.toString().toIntOrNull() ?: return false
            if (!validateAge(age)) return false

            // Only validate weight goal if not maintaining
            if (radioGroupWeightGoal.checkedRadioButtonId != R.id.radioButtonMaintain) {
                val targetWeight = editTextTargetWeight.text.toString().toDoubleOrNull()
                val goalType = when (radioGroupWeightGoal.checkedRadioButtonId) {
                    R.id.radioButtonGain -> "GAIN"
                    R.id.radioButtonLoss -> "LOSS"
                    else -> "MAINTAIN"
                }
                
                if (!validateTargetWeight(weight, targetWeight, goalType)) return false

                val dateStr = editTextTargetDate.text.toString()
                if (dateStr.isNotBlank()) {
                    try {
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val targetDate = dateFormat.parse(dateStr)?.time
                        if (!validateTargetDate(dateStr, targetDate)) return false
                    } catch (e: Exception) {
                        return false
                    }
                }
            }

            return true
        }
    }

    private fun calculateCalories() {
        // Get input values
        val weight = binding.editTextWeight.text.toString().toDoubleOrNull()
        val height = binding.editTextHeight.text.toString().toDoubleOrNull()
        val age = binding.editTextAge.text.toString().toIntOrNull()
        val isMale = binding.radioButtonMale.isChecked
        
        // Get weight goal type
        val weightGoalType = when (binding.radioGroupWeightGoal.checkedRadioButtonId) {
            R.id.radioButtonGain -> WeightGoalType.GAIN
            R.id.radioButtonLoss -> WeightGoalType.LOSS
            else -> WeightGoalType.MAINTAIN
        }

        // Get target weight and date if applicable
        val targetWeight = binding.editTextTargetWeight.text.toString().toDoubleOrNull()
        var targetDate: Long? = null
        try {
            val dateStr = binding.editTextTargetDate.text.toString()
            if (dateStr.isNotBlank()) {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                targetDate = dateFormat.parse(dateStr)?.time
            }
        } catch (e: Exception) {
            Log.e("HouseholdMemberForm", "Date parsing error", e)
        }

        // Calculate calories with the new method
        val activityLevel = when (binding.spinnerActivityLevel.selectedItem.toString()) {
            "Sedentary" -> ActivityLevel.SEDENTARY
            "Lightly Active" -> ActivityLevel.LIGHTLY_ACTIVE
            "Moderately Active" -> ActivityLevel.MODERATELY_ACTIVE
            "Very Active" -> ActivityLevel.VERY_ACTIVE
            "Extra Active" -> ActivityLevel.EXTRA_ACTIVE
            else -> ActivityLevel.SEDENTARY
        }

        val result = NutritionGuidelines.getRecommendedCalories(
            weight = weight ?: 0.0,
            height = height ?: 0.0,
            age = age ?: 0,
            isMale = isMale,
            activityLevel = activityLevel,
            weightGoalType = weightGoalType,
            targetWeight = targetWeight,
            targetDate = targetDate
        )

        // Set the calculated calories
        binding.editTextCalorieGoal.setText(result.calories.toString())

        // Show warning if present
        result.warning?.let { warning ->
            binding.editTextCalorieGoal.error = warning
            // Optionally show a toast for better visibility
            Toast.makeText(context, warning, Toast.LENGTH_LONG).show()
        }

        // Update macro calculations based on the calories
        calculateMacros(result.calories)

        // Add text change listener for manual override warning
        binding.editTextCalorieGoal.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                val calories = s.toString().toIntOrNull() ?: return
                val minimum = if (isMale) 
                    NutritionGuidelines.MINIMUM_DAILY_CALORIES_MALE 
                else 
                    NutritionGuidelines.MINIMUM_DAILY_CALORIES_FEMALE

                if (calories < minimum) {
                    binding.editTextCalorieGoal.error = 
                        "Warning: Value below minimum safe intake ($minimum kcal/day)"
                } else {
                    binding.editTextCalorieGoal.error = null
                }
                
                // Recalculate macros when calories change
                calculateMacros(calories)
            }
        })
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _: android.widget.DatePicker, year: Int, month: Int, day: Int ->
                calendar.set(year, month, day)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                binding.editTextTargetDate.setText(dateFormat.format(calendar.time))
                // Validate immediately after setting date
                validateTargetDate(dateFormat.format(calendar.time), calendar.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun getActivityLevelFromPosition(position: Int): ActivityLevel {
        return when (position) {
            0 -> ActivityLevel.SEDENTARY
            1 -> ActivityLevel.LIGHTLY_ACTIVE
            2 -> ActivityLevel.MODERATELY_ACTIVE
            3 -> ActivityLevel.VERY_ACTIVE
            4 -> ActivityLevel.EXTRA_ACTIVE
            else -> ActivityLevel.MODERATELY_ACTIVE
        }
    }

    private fun showValidationError(editText: TextInputEditText, message: String, showAnimation: Boolean) {
        try {
            (editText.parent?.parent as? TextInputLayout)?.let { textInputLayout ->
                textInputLayout.error = message
                textInputLayout.isErrorEnabled = true
                if (showAnimation) {
                    editText.startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake))
                }
                updateValidationState(editText.id.toString(), false)
            }
        } catch (e: Exception) {
            Log.e("Validation", "Error showing validation error", e)
        }
    }

    private fun showValidationSuccess(editText: TextInputEditText) {
        try {
            (editText.parent?.parent as? TextInputLayout)?.let { textInputLayout ->
                textInputLayout.error = null
                textInputLayout.isErrorEnabled = false
                updateValidationState(editText.id.toString(), true)
            }
        } catch (e: Exception) {
            Log.e("Validation", "Error showing validation success", e)
        }
    }

    private fun validateWeight(weight: Double?, showAnimation: Boolean = false): Boolean {
        return binding.editTextWeight.let { editText ->
            when {
                weight == null -> {
                    showValidationError(editText, "Please enter a valid number", showAnimation)
                    false
                }
                weight !in MIN_WEIGHT..MAX_WEIGHT -> {
                    showValidationError(editText, "Weight must be between $MIN_WEIGHT and $MAX_WEIGHT kg", showAnimation)
                    false
                }
                else -> {
                    showValidationSuccess(editText)
                    true
                }
            }
        }
    }

    private fun validateHeight(height: Double?): Boolean {
        return binding.editTextHeight.let { editText ->
            when {
                height == null -> {
                    showValidationError(editText, "Please enter a valid number", false)
                    false
                }
                height !in MIN_HEIGHT..MAX_HEIGHT -> {
                    showValidationError(editText, "Height must be between $MIN_HEIGHT and $MAX_HEIGHT cm", false)
                    false
                }
                height.toString().split(".").getOrNull(1)?.length ?: 0 > 1 -> {
                    showValidationError(editText, "Maximum 1 decimal place allowed", false)
                    false
                }
                else -> {
                    showValidationSuccess(editText)
                    true
                }
            }
        }
    }

    private fun validateAge(age: Int?): Boolean {
        return binding.editTextAge.let { editText ->
            when {
                age == null -> {
                    showValidationError(editText, "Please enter a valid number", false)
                    false
                }
                age !in MIN_AGE..MAX_AGE -> {
                    showValidationError(editText, "Age must be between $MIN_AGE and $MAX_AGE years", false)
                    false
                }
                else -> {
                    showValidationSuccess(editText)
                    true
                }
            }
        }
    }

    private fun validateTargetDate(dateStr: String, targetDate: Long?): Boolean {
        return binding.editTextTargetDate.let { editText ->
            when {
                dateStr.isBlank() -> {
                    showValidationError(editText, "Target date is required", false)
                    false
                }
                targetDate == null -> {
                    showValidationError(editText, "Invalid date format (use DD/MM/YYYY)", false)
                    false
                }
                targetDate <= System.currentTimeMillis() -> {
                    showValidationError(editText, "Target date must be in the future", false)
                    false
                }
                ((targetDate - System.currentTimeMillis()) / (7L * 24 * 60 * 60 * 1000)) < MIN_WEEKS_FOR_GOAL -> {
                    showValidationError(editText, "Goal must be at least $MIN_WEEKS_FOR_GOAL weeks from now", false)
                    false
                }
                ((targetDate - System.currentTimeMillis()) / (7L * 24 * 60 * 60 * 1000)) > MAX_WEEKS_FOR_GOAL -> {
                    showValidationError(editText, "Goal timeline cannot exceed $MAX_WEEKS_FOR_GOAL weeks", false)
                    false
                }
                else -> {
                    showValidationSuccess(editText)
                    true
                }
            }
        }
    }

    // Add setup for all validations
    private fun setupValidations() {
        setupWeightValidation()
        setupHeightValidation()
        setupAgeValidation()
        setupTargetWeightValidation()
        setupTargetDateValidation()
    }

    private fun setupHeightValidation() {
        binding.editTextHeight.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val height = s.toString().toDoubleOrNull()
                validateHeight(height)
            }
        })
    }

    private fun setupAgeValidation() {
        binding.editTextAge.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val age = s.toString().toIntOrNull()
                validateAge(age)
            }
        })
    }

    private fun setupWeightValidation() {
        var lastValidInput = ""
        var isFieldFocused = false
        
        binding.editTextWeight.apply {
            setOnFocusChangeListener { _, hasFocus ->
                isFieldFocused = hasFocus
                if (!hasFocus) {
                    val weight = text.toString().toDoubleOrNull()
                    validateWeight(weight, true)
                }
            }
            
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (!isFieldFocused) return
                    val input = s.toString()
                    if (input == lastValidInput) return
                    
                    val weight = input.toDoubleOrNull()
                    if (validateWeight(weight, false)) {
                        lastValidInput = input
                        calculateCaloriesWithDebounce()
                    }
                }
            })
        }
    }

    private fun setupTargetWeightValidation() {
        // Add listener for goal type changes
        binding.radioGroupWeightGoal.setOnCheckedChangeListener { _, checkedId ->
            val currentWeight = binding.editTextWeight.text.toString().toDoubleOrNull()
            val targetWeight = binding.editTextTargetWeight.text.toString().toDoubleOrNull()
            val goalType = when (checkedId) {
                R.id.radioButtonGain -> "GAIN"
                R.id.radioButtonLoss -> "LOSS"
                else -> "MAINTAIN"
            }
            validateTargetWeight(currentWeight, targetWeight, goalType)
        }

        // Add text change listener
        binding.editTextTargetWeight.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val currentWeight = binding.editTextWeight.text.toString().toDoubleOrNull()
                val targetWeight = s.toString().toDoubleOrNull()
                val goalType = when (binding.radioGroupWeightGoal.checkedRadioButtonId) {
                    R.id.radioButtonGain -> "GAIN"
                    R.id.radioButtonLoss -> "LOSS"
                    else -> "MAINTAIN"
                }
                validateTargetWeight(currentWeight, targetWeight, goalType)
            }
        })
    }

    private fun setupTargetDateValidation() {
        var lastValidDate: Long? = null
        
        binding.editTextTargetDate.apply {
            setOnClickListener {
                showDatePicker()
            }
            
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val dateStr = s.toString()
                    if (dateStr.isBlank()) return
                    
                    try {
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val date = dateFormat.parse(dateStr)?.time
                        if (validateTargetDate(dateStr, date)) {
                            lastValidDate = date
                        }
                    } catch (e: Exception) {
                        showValidationError(this@apply, "Invalid date format (use DD/MM/YYYY)", false)
                    }
                }
            })
        }
    }

    private fun updateValidationState(field: String, isValid: Boolean) {
        validationState[field] = isValid
        updateSaveButtonState()
    }

    private fun updateSaveButtonState() {
        binding.buttonSave.isEnabled = validationState.values.all { it }
    }

    private fun calculateCaloriesWithDebounce() {
        calculationJob?.cancel()
        calculationJob = Timer().apply {
            schedule(300) { // 300ms delay
                activity?.runOnUiThread {
                    calculateCalories()
                }
            }
        }
    }

    override fun onDestroyView() {
        calculationJob?.cancel()
        calculationJob = null
        // Clear all animations and drawables
        binding.apply {
            editTextWeight.clearAnimation()
            editTextHeight.clearAnimation()
            editTextAge.clearAnimation()
            editTextTargetWeight.clearAnimation()
            editTextTargetDate.clearAnimation()
            
            editTextWeight.setCompoundDrawables(null, null, null, null)
            editTextHeight.setCompoundDrawables(null, null, null, null)
            editTextAge.setCompoundDrawables(null, null, null, null)
            editTextTargetWeight.setCompoundDrawables(null, null, null, null)
            editTextTargetDate.setCompoundDrawables(null, null, null, null)
        }
        super.onDestroyView()
        _binding = null
    }

    private fun validateDateOfBirth(date: LocalDate): Boolean {
        val now = LocalDate.now()
        return !date.isAfter(now)
    }
} 