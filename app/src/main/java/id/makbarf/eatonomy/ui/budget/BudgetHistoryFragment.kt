package id.makbarf.eatonomy.ui.budget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import id.makbarf.eatonomy.databinding.FragmentBudgetHistoryBinding

class BudgetHistoryFragment : Fragment() {
    private var _binding: FragmentBudgetHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BudgetViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetHistoryBinding.inflate(inflater, container, false)
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
        // Observe budget history
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 