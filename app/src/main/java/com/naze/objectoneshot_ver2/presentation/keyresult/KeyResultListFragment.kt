package com.naze.objectoneshot_ver2.presentation.keyresult

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.naze.objectoneshot_ver2.R
import com.naze.objectoneshot_ver2.databinding.FragmentKeyResultListBinding
import com.naze.objectoneshot_ver2.domain.type.KeyResultState
import com.naze.objectoneshot_ver2.domain.viewmodel.ObjectiveViewModel
import com.naze.objectoneshot_ver2.util.BindingFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class KeyResultListFragment(val state: KeyResultState): BindingFragment<FragmentKeyResultListBinding>(R.layout.fragment_key_result_list) {
    private val objectiveViewModel: ObjectiveViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setList()
    }

    private fun setList() {
        val adapterKeyResult = KeyResultAdapter(objectiveViewModel,viewLifecycleOwner)

        binding.rvKeyList.apply {
            adapter = adapterKeyResult
            layoutManager = LinearLayoutManager(requireContext())
        }

        when (state) {
            KeyResultState.BEFORE_PROGRESS -> {
                objectiveViewModel.keyResultList.observe(viewLifecycleOwner) { item ->
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                        adapterKeyResult.submitList(item?.filter { it.progress <= 0.0 })
                    }
                }
            }
            KeyResultState.ON_PROGRESS -> {
                objectiveViewModel.keyResultList.observe(viewLifecycleOwner) { item ->
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                        adapterKeyResult.submitList(item?.filter { it.progress in 1.0..99.0 })
                    }
                }
            }
            KeyResultState.COMPLETE -> {
                objectiveViewModel.keyResultList.observe(viewLifecycleOwner) { item ->
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                        adapterKeyResult.submitList(item?.filter { it.progress >= 100.0 })
                    }
                }
            }
        }
    }
}