package com.objectiveoneshot.objectiveoneshot.presentation.objective

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.objectiveoneshot.objectiveoneshot.R
import com.objectiveoneshot.objectiveoneshot.databinding.FragmentObjectiveAchieveViewBinding
import com.objectiveoneshot.objectiveoneshot.domain.type.KeyResultState
import com.objectiveoneshot.objectiveoneshot.domain.viewmodel.AppViewModel
import com.objectiveoneshot.objectiveoneshot.presentation.keyresult.KeyResultListUnEditFragment
import com.objectiveoneshot.objectiveoneshot.presentation.tips.TipsFragment
import com.objectiveoneshot.objectiveoneshot.util.BindingFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ObjectiveAchieveViewFragment: BindingFragment<FragmentObjectiveAchieveViewBinding>(R.layout.fragment_objective_achieve_view){

    private val viewModel : AppViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = this
        binding.viewmodel = viewModel
        init()
    }

    private fun init() {
        setFragment()
        setTitleBarBtn()
        setFragmentBtn()
    }

    private fun setTitleBarBtn() {
        binding.toolBarBackBtn.setOnClickListener {
            parentFragmentManager.popBackStackImmediate()
        }
        binding.btnHelp.setOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.fl_main, TipsFragment(), "Tips")
                addToBackStack(null)
                commit()
            }
        }
    }

    private lateinit var transaction: FragmentTransaction
    private val fragment1 = KeyResultListUnEditFragment(KeyResultState.BEFORE_PROGRESS)
    private val fragment2 = KeyResultListUnEditFragment(KeyResultState.ON_PROGRESS)
    private val fragment3 = KeyResultListUnEditFragment(KeyResultState.COMPLETE)

    private fun setFragment() {
        viewModel.setKeyResultState(KeyResultState.COMPLETE)
        keyResultStateFragmentSetting()
        transaction = childFragmentManager.beginTransaction()
        transaction.add(R.id.fl_key, fragment1)
        transaction.add(R.id.fl_key, fragment2)
        transaction.add(R.id.fl_key, fragment3)
        transaction.commit()
    }

    private fun keyResultStateFragmentSetting() {
        viewModel.keyResultState.observe(viewLifecycleOwner) {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                when (it) {
                    KeyResultState.BEFORE_PROGRESS -> {
                        binding.btnBeforeProgress.isSelected = true
                        binding.btnOnProgress.isSelected = false
                        binding.btnComplete.isSelected = false
                    }
                    KeyResultState.ON_PROGRESS -> {
                        binding.btnBeforeProgress.isSelected = false
                        binding.btnOnProgress.isSelected = true
                        binding.btnComplete.isSelected = false
                    }
                    KeyResultState.COMPLETE -> {
                        binding.btnBeforeProgress.isSelected = false
                        binding.btnOnProgress.isSelected = false
                        binding.btnComplete.isSelected = true
                    }
                }
                showFragment(it)
            }
        }
    }

    private fun showFragment(keyState: KeyResultState) {
        when (keyState) {
            KeyResultState.BEFORE_PROGRESS -> {
                transaction = childFragmentManager.beginTransaction().apply {
                    show(fragment1)
                    hide(fragment2)
                    hide(fragment3)
                    commit()
                }
            }
            KeyResultState.ON_PROGRESS -> {
                transaction = childFragmentManager.beginTransaction().apply {
                    hide(fragment1)
                    show(fragment2)
                    hide(fragment3)
                    commit()
                }
            }
            KeyResultState.COMPLETE -> {
                transaction = childFragmentManager.beginTransaction().apply {
                    hide(fragment1)
                    hide(fragment2)
                    show(fragment3)
                    commit()
                }
            }
        }
    }

    private fun setFragmentBtn() {
        binding.btnBeforeProgress.setOnClickListener {
            if (!it.isSelected) viewModel.setKeyResultState(KeyResultState.BEFORE_PROGRESS)
        }
        binding.btnOnProgress.setOnClickListener {
            if (!it.isSelected) viewModel.setKeyResultState(KeyResultState.ON_PROGRESS)
        }
        binding.btnComplete.setOnClickListener {
            if (!it.isSelected) viewModel.setKeyResultState(KeyResultState.COMPLETE)
        }
    }
}