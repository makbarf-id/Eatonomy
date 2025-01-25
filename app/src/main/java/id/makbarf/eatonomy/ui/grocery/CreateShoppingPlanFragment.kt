package id.makbarf.eatonomy.ui.grocery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import id.makbarf.eatonomy.databinding.FragmentCreateShoppingPlanBinding
import java.time.LocalDate

class CreateShoppingPlanFragment : Fragment() {
    private var _binding: FragmentCreateShoppingPlanBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GroceryPlanViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateShoppingPlanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews()
        setupDatePicker()
        setupBudgetSelection()
    }

    private fun setupViews() {
        binding.buttonCreate.setOnClickListener {
            createPlan()
        }
    }

    private fun setupDatePicker() {
        binding.editTextPlannedDate.setOnClickListener {
            // Show date picker
        }
    }

    private fun setupBudgetSelection() {
        // Setup budget spinner/dropdown
    }

    private fun createPlan() {
        // Validate and create plan
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 