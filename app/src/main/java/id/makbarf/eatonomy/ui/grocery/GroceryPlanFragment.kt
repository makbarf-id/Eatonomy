package id.makbarf.eatonomy.ui.grocery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import id.makbarf.eatonomy.R
import id.makbarf.eatonomy.databinding.FragmentGroceryPlanBinding

class GroceryPlanFragment : Fragment() {
    private var _binding: FragmentGroceryPlanBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GroceryPlanViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroceryPlanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.buttonAddItem.setOnClickListener {
            // Navigate to add item
        }

        binding.buttonStartShopping.setOnClickListener {
            // Start shopping
        }
    }

    private fun observeViewModel() {
        viewModel.currentPlan.observe(viewLifecycleOwner) { plan ->
            // Update UI with plan details
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 