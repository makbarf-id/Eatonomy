package id.makbarf.eatonomy.ui.budget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import id.makbarf.eatonomy.R
import id.makbarf.eatonomy.databinding.FragmentBudgetOverviewBinding
import id.makbarf.eatonomy.data.Budget
import id.makbarf.eatonomy.data.BudgetStatus
import com.google.android.material.progressindicator.LinearProgressIndicator

class BudgetOverviewFragment : Fragment() {
    private var _binding: FragmentBudgetOverviewBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BudgetViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.fabAddBudget.setOnClickListener {
            findNavController().navigate(R.id.action_nav_budget_overview_to_budget_planning)
        }
    }

    private fun observeViewModel() {
        viewModel.activeBudget.observe(viewLifecycleOwner) { budget ->
            updateBudgetInfo(budget)
        }

        viewModel.spendingProgress.observe(viewLifecycleOwner) { progress ->
            binding.currentBudgetCard.findViewById<LinearProgressIndicator>(R.id.progressSpending).progress = progress.toInt()
        }

        viewModel.budgetWarnings.observe(viewLifecycleOwner) { warnings ->
            // Update warnings UI
        }
    }

    private fun updateBudgetInfo(budget: Budget?) {
        if (budget == null) {
            showEmptyState()
            return
        }
        
        hideEmptyState()
        // Update UI with budget details
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