package id.makbarf.eatonomy.ui.household

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import id.makbarf.eatonomy.data.HouseholdMember
import id.makbarf.eatonomy.databinding.ItemHouseholdMemberBinding

class HouseholdMemberAdapter : ListAdapter<HouseholdMember, HouseholdMemberAdapter.ViewHolder>(HouseholdMemberDiffCallback()) {

    private var onItemClickListener: ((HouseholdMember) -> Unit)? = null
    private var onDeleteClickListener: ((HouseholdMember) -> Unit)? = null

    fun setOnItemClickListener(listener: (HouseholdMember) -> Unit) {
        onItemClickListener = listener
    }

    fun setOnDeleteClickListener(listener: (HouseholdMember) -> Unit) {
        onDeleteClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemHouseholdMemberBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemHouseholdMemberBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(member: HouseholdMember) {
            binding.apply {
                textViewMemberName.text = member.name
                textViewCalorieGoal.text = "${member.dailyCalorieGoal} kcal/day"
                textViewMacros.text = "P: ${member.proteinGoal}g • C: ${member.carbsGoal}g • F: ${member.fatsGoal}g"

                root.setOnClickListener {
                    onItemClickListener?.invoke(member)
                }

                buttonDelete.setOnClickListener {
                    onDeleteClickListener?.invoke(member)
                }
            }
        }
    }

    class HouseholdMemberDiffCallback : DiffUtil.ItemCallback<HouseholdMember>() {
        override fun areItemsTheSame(oldItem: HouseholdMember, newItem: HouseholdMember): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: HouseholdMember, newItem: HouseholdMember): Boolean {
            return oldItem == newItem
        }
    }
} 