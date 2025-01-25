package id.makbarf.eatonomy.ui.household

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import id.makbarf.eatonomy.R
import id.makbarf.eatonomy.databinding.FragmentHouseholdMemberListBinding
import id.makbarf.eatonomy.data.HouseholdMember

class HouseholdMemberListFragment : Fragment() {
    private var _binding: FragmentHouseholdMemberListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HouseholdMemberViewModel by viewModels()
    private lateinit var adapter: HouseholdMemberAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHouseholdMemberListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupButtons()
        observeMembers()
    }

    private fun setupRecyclerView() {
        adapter = HouseholdMemberAdapter().apply {
            setOnItemClickListener { member ->
                val bundle = Bundle().apply {
                    putInt("memberId", member.id)
                }
                findNavController().navigate(R.id.nav_household_member_form, bundle)
            }
            setOnDeleteClickListener { member ->
                showDeleteConfirmationDialog(member)
            }
        }

        binding.recyclerViewMembers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@HouseholdMemberListFragment.adapter
        }
    }

    private fun setupButtons() {
        binding.buttonAddMember.setOnClickListener {
            findNavController().navigate(R.id.nav_household_member_form)
        }
    }

    private fun observeMembers() {
        viewModel.allMembers.observe(viewLifecycleOwner) { members ->
            if (members.isEmpty()) {
                showEmptyState()
            } else {
                hideEmptyState()
            }
            adapter.submitList(members)
        }
    }

    private fun showEmptyState() {
        binding.recyclerViewMembers.visibility = View.GONE
        if (binding.root.findViewById<View>(R.id.layoutEmptyState) == null) {
            val emptyView = LayoutInflater.from(context)
                .inflate(R.layout.layout_empty_state_household, binding.root as ViewGroup, false)
            emptyView.id = R.id.layoutEmptyState
            binding.root.addView(emptyView)
        }
    }

    private fun hideEmptyState() {
        binding.recyclerViewMembers.visibility = View.VISIBLE
        binding.root.findViewById<View>(R.id.layoutEmptyState)?.let {
            (binding.root as ViewGroup).removeView(it)
        }
    }

    private fun showDeleteConfirmationDialog(member: HouseholdMember) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Member")
            .setMessage("Are you sure you want to delete '${member.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.delete(member)
                Toast.makeText(context, "Member deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 