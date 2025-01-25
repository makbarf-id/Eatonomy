package id.makbarf.eatonomy.ui.budget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import id.makbarf.eatonomy.databinding.FragmentBudgetAnalyticsBinding

class BudgetAnalyticsFragment : Fragment() {
    private var _binding: FragmentBudgetAnalyticsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BudgetViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupCharts()
        observeViewModel()
    }

    private fun setupCharts() {
        // Initialize charts
    }

    private fun observeViewModel() {
        viewModel.activeBudget.observe(viewLifecycleOwner) { budget ->
            // Update analytics data
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 