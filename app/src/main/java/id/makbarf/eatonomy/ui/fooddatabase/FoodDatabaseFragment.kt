package id.makbarf.eatonomy.ui.fooddatabase

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import id.makbarf.eatonomy.R
import id.makbarf.eatonomy.databinding.FragmentFoodDatabaseBinding
import id.makbarf.eatonomy.data.FoodItem

class FoodDatabaseFragment : Fragment() {

    private var _binding: FragmentFoodDatabaseBinding? = null
    private val binding get() = _binding!!
    private val foodViewModel: FoodViewModel by viewModels()
    private lateinit var adapter: FoodItemAdapter
    private var isSearchTipsExpanded = false
    private var isFiltersExpanded = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFoodDatabaseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchFunctionality()
        setupSearchTips()
        setupFilters()
        observeFoodItems()

        binding.buttonAddFood.setOnClickListener {
            findNavController().navigate(R.id.nav_food_item_form)
        }
    }

    private fun setupRecyclerView() {
        adapter = FoodItemAdapter().apply {
            setOnItemClickListener { foodItem ->
                val bundle = bundleOf("foodItemId" to foodItem.id)
                findNavController().navigate(R.id.nav_food_item_form, bundle)
            }
            setOnDeleteClickListener { foodItem ->
                showDeleteConfirmationDialog(foodItem)
            }
        }

        binding.recyclerViewFoodItems.apply {
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            this.adapter = this@FoodDatabaseFragment.adapter
        }
    }

    private fun setupSearchFunctionality() {
        binding.editTextSearch.apply {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    foodViewModel.searchFoodItems(s?.toString() ?: "")
                }
            })
        }
    }

    private fun setupSearchTips() {
        binding.apply {
            // Set initial state
            layoutSearchTipsContent.visibility = if (isSearchTipsExpanded) View.VISIBLE else View.GONE
            updateExpandCollapseIcon()

            // Setup click listener
            layoutSearchTipsHeader.setOnClickListener {
                isSearchTipsExpanded = !isSearchTipsExpanded
                toggleSearchTipsVisibility()
            }
        }
    }

    private fun toggleSearchTipsVisibility() {
        binding.apply {
            val content = layoutSearchTipsContent
            if (isSearchTipsExpanded) {
                // Expanding animation
                content.visibility = View.VISIBLE
                content.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                val targetHeight = content.measuredHeight

                content.layoutParams.height = 0
                content.alpha = 0f
                content.visibility = View.VISIBLE

                val animation = ValueAnimator.ofInt(0, targetHeight)
                animation.duration = 300
                animation.interpolator = DecelerateInterpolator()
                animation.addUpdateListener { valueAnimator ->
                    content.layoutParams.height = valueAnimator.animatedValue as Int
                    content.requestLayout()
                }

                animation.start()
                content.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            } else {
                // Collapsing animation
                val initialHeight = content.height

                val animation = ValueAnimator.ofInt(initialHeight, 0)
                animation.duration = 300
                animation.interpolator = DecelerateInterpolator()
                animation.addUpdateListener { valueAnimator ->
                    content.layoutParams.height = valueAnimator.animatedValue as Int
                    content.requestLayout()
                }
                animation.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        content.visibility = View.GONE
                        content.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    }
                })

                animation.start()
                content.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }

            // Animate the arrow rotation
            imageViewExpandCollapse.animate()
                .rotation(if (isSearchTipsExpanded) 180f else 0f)
                .setDuration(300)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun updateExpandCollapseIcon() {
        binding.imageViewExpandCollapse.rotation = if (isSearchTipsExpanded) 180f else 0f
    }

    private fun setupFilters() {
        binding.apply {
            // Set initial state
            layoutFiltersContent.visibility = if (isFiltersExpanded) View.VISIBLE else View.GONE
            updateFilterExpandCollapseIcon()

            // Setup click listener
            layoutFiltersHeader.setOnClickListener {
                isFiltersExpanded = !isFiltersExpanded
                toggleFiltersVisibility()
            }

            // Setup dropdowns
            val categories = resources.getStringArray(R.array.food_categories)
            val categoryAdapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, categories)
            dropdownCategory.setAdapter(categoryAdapter)

            val sortOptions = arrayOf(
                "Name (A-Z)", 
                "Name (Z-A)", 
                "Price (Low-High)", 
                "Price (High-Low)",
                "Caloric Efficiency (Low-High)",
                "Caloric Efficiency (High-Low)"
            )
            val sortAdapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, sortOptions)
            dropdownSort.setAdapter(sortAdapter)

            // Set initial values
            dropdownCategory.setText("All Categories", false)
            dropdownSort.setText("Name (A-Z)", false)

            // Setup filter buttons
            buttonApplyFilters.setOnClickListener {
                applyFilters()
            }

            buttonClearFilters.setOnClickListener {
                clearFilters()
            }
        }
    }

    private fun toggleFiltersVisibility() {
        binding.apply {
            val content = layoutFiltersContent
            if (isFiltersExpanded) {
                // Expanding animation
                content.visibility = View.VISIBLE
                content.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                val targetHeight = content.measuredHeight

                content.layoutParams.height = 0
                content.alpha = 0f
                content.visibility = View.VISIBLE

                val animation = ValueAnimator.ofInt(0, targetHeight)
                animation.duration = 300
                animation.interpolator = DecelerateInterpolator()
                animation.addUpdateListener { valueAnimator ->
                    content.layoutParams.height = valueAnimator.animatedValue as Int
                    content.requestLayout()
                }

                animation.start()
                content.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            } else {
                // Collapsing animation
                val initialHeight = content.height

                val animation = ValueAnimator.ofInt(initialHeight, 0)
                animation.duration = 300
                animation.interpolator = DecelerateInterpolator()
                animation.addUpdateListener { valueAnimator ->
                    content.layoutParams.height = valueAnimator.animatedValue as Int
                    content.requestLayout()
                }
                animation.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        content.visibility = View.GONE
                        content.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    }
                })

                animation.start()
                content.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }

            // Animate the arrow rotation
            imageViewExpandCollapseFilters.animate()
                .rotation(if (isFiltersExpanded) 180f else 0f)
                .setDuration(300)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun updateFilterExpandCollapseIcon() {
        binding.imageViewExpandCollapseFilters.rotation = if (isFiltersExpanded) 180f else 0f
    }

    private fun applyFilters() {
        binding.apply {
            // Apply category filter
            val selectedCategory = dropdownCategory.text.toString()
            if (selectedCategory.isNotEmpty()) {
                foodViewModel.setCategory(selectedCategory)
            }

            // Apply sort order
            when (dropdownSort.text.toString()) {
                "Name (A-Z)" -> foodViewModel.setSortOrder(FoodViewModel.SortOrder.NAME_ASC)
                "Name (Z-A)" -> foodViewModel.setSortOrder(FoodViewModel.SortOrder.NAME_DESC)
                "Price (Low-High)" -> foodViewModel.setSortOrder(FoodViewModel.SortOrder.PRICE_ASC)
                "Price (High-Low)" -> foodViewModel.setSortOrder(FoodViewModel.SortOrder.PRICE_DESC)
                "Caloric Efficiency (Low-High)" -> foodViewModel.setSortOrder(FoodViewModel.SortOrder.EFFICIENCY_ASC)
                "Caloric Efficiency (High-Low)" -> foodViewModel.setSortOrder(FoodViewModel.SortOrder.EFFICIENCY_DESC)
            }

            // Collapse the filter panel
            isFiltersExpanded = false
            toggleFiltersVisibility()

            // Show confirmation
            Toast.makeText(context, "Filters applied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearFilters() {
        binding.apply {
            // Reset dropdowns
            dropdownCategory.setText("All Categories", false)
            dropdownSort.setText("Name (A-Z)", false)

            // Reset in ViewModel
            foodViewModel.setCategory("All Categories")
            foodViewModel.setSortOrder(FoodViewModel.SortOrder.NAME_ASC)

            // Show confirmation
            Toast.makeText(context, "Filters cleared", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeFoodItems() {
        foodViewModel.foodItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            
            // Show/hide empty state
            if (items.isEmpty()) {
                showEmptyState()
            } else {
                binding.recyclerViewFoodItems.visibility = View.VISIBLE
                // Remove empty state if it exists
                binding.root.findViewById<View>(R.id.layoutEmptyState)?.let {
                    (binding.root as ViewGroup).removeView(it)
                }
            }
        }
    }

    private fun showEmptyState() {
        binding.recyclerViewFoodItems.visibility = View.GONE
        // Check if empty state view already exists
        if (binding.root.findViewById<View>(R.id.layoutEmptyState) == null) {
            val emptyView = LayoutInflater.from(context)
                .inflate(R.layout.layout_empty_state, binding.root as ViewGroup, false)
            emptyView.id = R.id.layoutEmptyState
            binding.root.addView(emptyView)
        }
    }

    private fun showDeleteConfirmationDialog(foodItem: FoodItem) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Food Item")
            .setMessage("Are you sure you want to delete '${foodItem.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                foodViewModel.delete(foodItem)
                Toast.makeText(context, "Food item deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 