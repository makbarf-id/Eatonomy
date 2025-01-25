package id.makbarf.eatonomy.ui.grocery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import id.makbarf.eatonomy.R
import id.makbarf.eatonomy.databinding.FragmentShoppingListBinding
import id.makbarf.eatonomy.data.GroceryPlanStatus
import id.makbarf.eatonomy.data.GroceryPlan

class ShoppingListFragment : Fragment() {
    private var _binding: FragmentShoppingListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GroceryPlanViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShoppingListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupViews()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewShoppingItems.apply {
            layoutManager = LinearLayoutManager(context)
            // Set adapter
        }
    }

    private fun setupViews() {
        binding.buttonComplete.setOnClickListener {
            completeShopping()
        }
    }

    private fun observeViewModel() {
        viewModel.currentPlan.observe(viewLifecycleOwner) { plan ->
            updatePlanInfo(plan)
        }

        viewModel.plannedItems.observe(viewLifecycleOwner) { items ->
            // Update items list
        }
    }

    private fun updatePlanInfo(plan: GroceryPlan?) {
        if (plan == null) {
            showEmptyState()
            return
        }
        hideEmptyState()
        binding.textPlanName.text = plan.name
        binding.buttonComplete.isEnabled = plan.status == GroceryPlanStatus.SHOPPING
    }

    private fun completeShopping() {
        viewModel.completePlan()
    }

    private fun showEmptyState() {
        // Show empty state view
    }

    private fun hideEmptyState() {
        // Hide empty state view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 