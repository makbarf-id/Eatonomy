package id.makbarf.eatonomy.ui.grocery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import id.makbarf.eatonomy.databinding.FragmentShoppingHistoryBinding
import id.makbarf.eatonomy.data.GroceryPlanStatus

class ShoppingHistoryFragment : Fragment() {
    private var _binding: FragmentShoppingHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GroceryPlanViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShoppingHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewHistory.apply {
            layoutManager = LinearLayoutManager(context)
            // Set adapter
        }
    }

    private fun observeViewModel() {
        viewModel.getPlansByStatus(GroceryPlanStatus.COMPLETED).observe(viewLifecycleOwner) { plans ->
            if (plans.isEmpty()) {
                showEmptyState()
            } else {
                hideEmptyState()
                // Update history list
            }
        }
    }

    private fun showEmptyState() {
        binding.recyclerViewHistory.visibility = View.GONE
        binding.layoutEmptyState.root.visibility = View.VISIBLE
    }

    private fun hideEmptyState() {
        binding.recyclerViewHistory.visibility = View.VISIBLE
        binding.layoutEmptyState.root.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 