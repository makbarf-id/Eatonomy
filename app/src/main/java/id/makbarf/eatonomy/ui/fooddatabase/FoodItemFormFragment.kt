package id.makbarf.eatonomy.ui.fooddatabase

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import id.makbarf.eatonomy.R
import id.makbarf.eatonomy.data.FoodItem
import id.makbarf.eatonomy.databinding.FragmentFoodItemFormBinding

class FoodItemFormFragment : Fragment() {

    private var _binding: FragmentFoodItemFormBinding? = null
    private val binding get() = _binding!!
    private val foodViewModel: FoodViewModel by viewModels()
    private var foodItemId: Int = 0
    private var isEditMode: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFoodItemFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        foodItemId = arguments?.getInt("foodItemId", 0) ?: 0
        
        setupButtons()
        if (foodItemId != 0) {
            loadFoodItem()
        } else {
            isEditMode = true
            setupFormMode()
        }
    }

    private fun loadFoodItem() {
        foodViewModel.getFoodItemById(foodItemId).observe(viewLifecycleOwner) { foodItem ->
            foodItem?.let { populateForm(it) }
        }
    }

    private fun setupFormMode() {
        binding.apply {
            // Enable/disable all input fields based on mode
            val views = listOf<View>(
                basicInfoCard.editTextFoodName,
                basicInfoCard.spinnerFoodCategory,
                basicInfoCard.editTextBrandName,
                basicInfoCard.editTextStoreSource,
                nutritionCard.editTextServingSize,
                nutritionCard.editTextCalories,
                nutritionCard.editTextProtein,
                nutritionCard.editTextCarbohydrates,
                nutritionCard.editTextFats,
                nutritionCard.editTextFiber,
                priceCard.editTextNetWeight,
                priceCard.editTextPrice,
                priceCard.spinnerCurrency,
                basicInfoCard.editTextNotes
            )

            views.forEach { view ->
                view.isEnabled = isEditMode
                if (view is EditText) {
                    view.isFocusable = isEditMode
                    view.isFocusableInTouchMode = isEditMode
                }
            }

            buttonEdit.visibility = if (!isEditMode && foodItemId != 0) View.VISIBLE else View.GONE
            buttonSaveFoodItem.visibility = if (isEditMode) View.VISIBLE else View.GONE
        }
    }

    private fun populateForm(foodItem: FoodItem) {
        binding.apply {
            basicInfoCard.editTextFoodName.setText(foodItem.name)
            
            // Set the correct category in spinner
            val categories = resources.getStringArray(R.array.food_categories_form)
            val categoryPosition = categories.indexOf(foodItem.category)
            if (categoryPosition != -1) {
                basicInfoCard.spinnerFoodCategory.setSelection(categoryPosition)
            }

            basicInfoCard.editTextBrandName.setText(foodItem.brandName)
            basicInfoCard.editTextStoreSource.setText(foodItem.storeSource)
            basicInfoCard.editTextNotes.setText(foodItem.notes)
            
            nutritionCard.apply {
                editTextServingSize.setText(foodItem.servingSize.toString())
                editTextCalories.setText(foodItem.calories.toString())
                editTextProtein.setText(foodItem.protein?.toString() ?: "")
                editTextCarbohydrates.setText(foodItem.carbohydrates?.toString() ?: "")
                editTextFats.setText(foodItem.fats?.toString() ?: "")
                editTextFiber.setText(foodItem.fiber?.toString() ?: "")
            }

            priceCard.apply {
                editTextNetWeight.setText(foodItem.netWeight.toString())
                editTextPrice.setText(foodItem.price.toString())
                val currencyPosition = resources.getStringArray(R.array.currencies)
                    .indexOf(foodItem.currency)
                if (currencyPosition != -1) {
                    spinnerCurrency.setSelection(currencyPosition)
                }
            }

            isEditMode = false
            setupFormMode()
        }
    }

    private fun setupButtons() {
        binding.apply {
            buttonEdit.setOnClickListener {
                isEditMode = true
                setupFormMode()
            }

            buttonSaveFoodItem.setOnClickListener {
                saveFoodItem()
            }
        }
    }

    private fun saveFoodItem() {
        if (!validateForm()) return

        try {
            binding.apply {
                val foodItem = FoodItem(
                    id = foodItemId,
                    name = basicInfoCard.editTextFoodName.text.toString(),
                    category = basicInfoCard.spinnerFoodCategory.selectedItem.toString(),
                    brandName = basicInfoCard.editTextBrandName.text.toString().takeIf { it.isNotBlank() },
                    storeSource = basicInfoCard.editTextStoreSource.text.toString().takeIf { it.isNotBlank() },
                    servingSize = nutritionCard.editTextServingSize.text.toString().toDoubleOrNull() ?: 0.0,
                    calories = nutritionCard.editTextCalories.text.toString().toDoubleOrNull() ?: 0.0,
                    protein = nutritionCard.editTextProtein.text.toString().toDoubleOrNull(),
                    carbohydrates = nutritionCard.editTextCarbohydrates.text.toString().toDoubleOrNull(),
                    fats = nutritionCard.editTextFats.text.toString().toDoubleOrNull(),
                    fiber = nutritionCard.editTextFiber.text.toString().toDoubleOrNull(),
                    netWeight = priceCard.editTextNetWeight.text.toString().toDoubleOrNull() ?: 0.0,
                    price = priceCard.editTextPrice.text.toString().toDoubleOrNull() ?: 0.0,
                    currency = priceCard.spinnerCurrency.selectedItem.toString(),
                    notes = basicInfoCard.editTextNotes.text.toString().takeIf { it.isNotBlank() }
                )

                if (foodItemId == 0) {
                    foodViewModel.insert(foodItem)
                    Log.d("FoodItemForm", "Attempting to insert new food item: ${foodItem.name}")
                } else {
                    foodViewModel.update(foodItem)
                    Log.d("FoodItemForm", "Attempting to update food item: ${foodItem.name}")
                }

                Toast.makeText(context, 
                    if (foodItemId == 0) "Food Item Added" else "Food Item Updated", 
                    Toast.LENGTH_SHORT
                ).show()

                findNavController().popBackStack()
            }
        } catch (e: Exception) {
            Log.e("FoodItemForm", "Error saving food item", e)
            Toast.makeText(context, "Error saving food item", Toast.LENGTH_LONG).show()
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true
        binding.apply {
            if (basicInfoCard.editTextFoodName.text.isNullOrBlank()) {
                basicInfoCard.editTextFoodName.error = "Food name is required"
                isValid = false
            }
        }
        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 