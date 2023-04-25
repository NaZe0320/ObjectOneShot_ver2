package com.objectiveoneshot.objectiveoneshot.presentation.objective

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.objectiveoneshot.objectiveoneshot.R
import com.objectiveoneshot.objectiveoneshot.databinding.FragmentObjectiveAchieveListBinding
import com.objectiveoneshot.objectiveoneshot.domain.viewmodel.AppViewModel
import com.objectiveoneshot.objectiveoneshot.presentation.tips.TipsFragment
import com.objectiveoneshot.objectiveoneshot.util.BindingFragment

class ObjectiveAchieveFragment: BindingFragment<FragmentObjectiveAchieveListBinding>(R.layout.fragment_objective_achieve_list) {
    private val viewModel : AppViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecyclerView()

        binding.btnHelp.setOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.fl_main, TipsFragment(), "Tips")
                addToBackStack(null)
                commit()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setRecyclerView() {
        val adapterObjective = ObjectiveAchieveListAdapter(ObjectiveAchieveListAdapter.ItemClickListener {
            viewModel.getObjectiveData(it)
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.fl_main, ObjectiveAchieveViewFragment(), "ObjectiveAchieveView")
                addToBackStack(null)
                commit()
            }
        },viewModel)

        binding.rvObjective.apply {
            adapter = adapterObjective
            layoutManager = LinearLayoutManager(requireContext())
        }
        viewModel.getObjectiveAchieveList()

        viewModel.objectiveListWithKeyResults.observe(viewLifecycleOwner) {
            adapterObjective.submitList(it)
        }
    }
}