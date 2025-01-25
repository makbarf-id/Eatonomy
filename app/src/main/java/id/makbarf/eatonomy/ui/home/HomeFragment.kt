package id.makbarf.eatonomy.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.fragment.app.viewModels
import id.makbarf.eatonomy.R
import id.makbarf.eatonomy.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupButtons()
        observeStatistics()
    }

    private fun setupButtons() {
        binding.apply {
            buttonAddFood.setOnClickListener {
                findNavController().navigate(R.id.nav_food_item_form)
            }

            buttonViewDatabase.setOnClickListener {
                findNavController().navigate(R.id.nav_food_database)
            }
        }
    }

    private fun observeStatistics() {
        homeViewModel.totalItems.observe(viewLifecycleOwner) { count ->
            binding.textViewTotalItems.text = count.toString()
        }

        homeViewModel.usedCategories.observe(viewLifecycleOwner) { count ->
            binding.textViewCategories.text = count.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}