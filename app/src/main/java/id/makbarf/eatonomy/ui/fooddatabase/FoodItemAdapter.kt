package id.makbarf.eatonomy.ui.fooddatabase

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import id.makbarf.eatonomy.data.FoodItem
import id.makbarf.eatonomy.databinding.ItemFoodBinding

class FoodItemAdapter : ListAdapter<FoodItem, FoodItemAdapter.FoodItemViewHolder>(FoodItemDiffCallback()) {

    private var onItemClickListener: ((FoodItem) -> Unit)? = null
    private var onDeleteClickListener: ((FoodItem) -> Unit)? = null

    fun setOnItemClickListener(listener: (FoodItem) -> Unit) {
        onItemClickListener = listener
    }

    fun setOnDeleteClickListener(listener: (FoodItem) -> Unit) {
        onDeleteClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodItemViewHolder {
        val binding = ItemFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodItemViewHolder(binding, onItemClickListener, onDeleteClickListener)
    }

    override fun onBindViewHolder(holder: FoodItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class FoodItemViewHolder(
        private val binding: ItemFoodBinding,
        private val onItemClickListener: ((FoodItem) -> Unit)?,
        private val onDeleteClickListener: ((FoodItem) -> Unit)?
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(foodItem: FoodItem) {
            binding.apply {
                textViewFoodName.text = foodItem.name
                textViewFoodCategory.text = foodItem.category
                textViewPrice.text = "${foodItem.currency} ${foodItem.price}"
                
                // Set store and brand info
                val storeBrandText = buildString {
                    if (!foodItem.storeSource.isNullOrBlank()) append(foodItem.storeSource)
                    if (!foodItem.storeSource.isNullOrBlank() && !foodItem.brandName.isNullOrBlank()) append(" • ")
                    if (!foodItem.brandName.isNullOrBlank()) append(foodItem.brandName)
                }
                textViewStoreBrand.visibility = if (storeBrandText.isNotEmpty()) View.VISIBLE else View.GONE
                textViewStoreBrand.text = storeBrandText

                // Calculate and set caloric efficiency (calories per currency unit)
                if (foodItem.calories > 0 && foodItem.price > 0) {
                    val efficiency = foodItem.calories / foodItem.price
                    textViewCaloriesEfficiency.text = String.format("%.1f cal/%s", efficiency, foodItem.currency)
                    textViewCaloriesEfficiency.visibility = View.VISIBLE
                } else {
                    textViewCaloriesEfficiency.visibility = View.GONE
                }
                
                // Set click listeners
                root.setOnClickListener {
                    onItemClickListener?.invoke(foodItem)
                }

                buttonDelete.setOnClickListener {
                    onDeleteClickListener?.invoke(foodItem)
                }
            }
        }
    }

    class FoodItemDiffCallback : DiffUtil.ItemCallback<FoodItem>() {
        override fun areItemsTheSame(oldItem: FoodItem, newItem: FoodItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FoodItem, newItem: FoodItem): Boolean {
            return oldItem == newItem
        }
    }
} 