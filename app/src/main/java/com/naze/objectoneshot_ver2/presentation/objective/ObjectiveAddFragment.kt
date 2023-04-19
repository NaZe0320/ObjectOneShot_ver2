package com.naze.objectoneshot_ver2.presentation.objective

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.naze.objectoneshot_ver2.R
import com.naze.objectoneshot_ver2.databinding.FragmentObjectiveAddBinding
import com.naze.objectoneshot_ver2.domain.type.KeyResultState
import com.naze.objectoneshot_ver2.domain.viewmodel.ObjectiveViewModel
import com.naze.objectoneshot_ver2.presentation.keyresult.KeyResultListFragment
import com.naze.objectoneshot_ver2.presentation.task.TaskAddAdapter
import com.naze.objectoneshot_ver2.presentation.tips.TipsFragment
import com.naze.objectoneshot_ver2.util.BindingFragment
import com.naze.objectoneshot_ver2.util.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class ObjectiveAddFragment: BindingFragment<FragmentObjectiveAddBinding>(R.layout.fragment_objective_add) {

    private val objectiveViewModel: ObjectiveViewModel by activityViewModels()
    private var isCheck: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = this
        binding.viewmodel = objectiveViewModel
        init()
    }

    private fun init() {
        setBtnEnable(false) //시작할 때 버튼 비활성화
        binding.etObjectiveName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {  }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }

            override fun afterTextChanged(s: Editable?) {
                setBtnEnable(!s.isNullOrEmpty())
                //목표 등록하기 비활성화
                //KeyResult 추가 비활성화
            }
        })

        binding.btnAddObjective.setOnClickListener {
            objectiveViewModel.insertObjective()
            parentFragmentManager.popBackStackImmediate()
        } //Objective 등록

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

        setCalendar() //달력 설정
        setAddKeyResult()
        if (isCheck) setFragment() // KeyResult List 설정
        setFragmentBtn() //추후 버튼 관련 함수들과 묶음
    }

    private fun setBtnEnable(isEnabled: Boolean) {
        binding.btnAddObjective.isEnabled = isEnabled
        binding.btnAddKeyResult.isEnabled = isEnabled
    }

    private fun setCalendar() {
        binding.btnObjectiveDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("기간을 선택해주세요")
                .build()
            datePicker.show(childFragmentManager, "date_picker")
            datePicker.addOnPositiveButtonClickListener {
                val calendar = Calendar.getInstance()
                objectiveViewModel.setObjectiveDateRange(
                    startDate = it.first ?: calendar.timeInMillis,
                    endDate = it.second ?: calendar.timeInMillis)
            }
        }
    }

    /**  Add Key Result   */
    private lateinit var adapterTask: TaskAddAdapter
    private fun setAddKeyResult () {
        binding.btnAddKeyResult.setOnClickListener { //Add btn 을 눌렀을 때
            if (binding.keyAddItem.layoutKeyAdd.visibility == View.VISIBLE) { //추가 상태일 때
                if (binding.keyAddItem.etKeyName.text.toString().isNotEmpty()) { //KeyResult 명이 비어있지 않으면
                    if (adapterTask.currentList[0].content.isNotEmpty() || adapterTask.currentList.size > 1) {
                        objectiveViewModel.addKeyResultList() //데이터 입력
                        setVisibleKeyResult(false)
                        binding.keyAddItem.etKeyName.setHintTextColor(Color.parseColor("#FF808080"))
                    } else {
                        requireContext().showToast("Task를 1개 이상 입력해주세요.")
                    }
                } else {
                    binding.keyAddItem.etKeyName.setHintTextColor(Color.parseColor("#80FF0000"))
                }

            } else if (binding.keyAddItem.layoutKeyAdd.visibility == View.GONE) {
                objectiveViewModel.initKeyResultData() //신규 데이터 생성

                setVisibleKeyResult(true)
                adapterTask = TaskAddAdapter(objectiveViewModel.keyResult.value?.id?:"", objectiveViewModel)
                binding.keyAddItem.rvTaskList.apply {
                    adapterTask.submitList(null)
                    adapter = adapterTask
                    layoutManager = LinearLayoutManager(requireContext())
                }
            }
        }
        binding.keyAddItem.btnDeleteKey.setOnClickListener {
            setVisibleKeyResult(false)
        }

    }
    private fun setVisibleKeyResult(isAdd: Boolean) { //추가인지 저장인지
        if (isAdd) {
            binding.keyAddItem.layoutKeyAdd.visibility = View.VISIBLE
            binding.btnAddKeyResult.setImageResource(R.drawable.ic_text_keyresult_save)
        } else {
            binding.keyAddItem.layoutKeyAdd.visibility = View.GONE
            binding.btnAddKeyResult.setImageResource(R.drawable.ic_text_key_result_add)
        }
    }

    private fun keyResultStateFragmentSetting() {
        objectiveViewModel.keyResultState.observe(viewLifecycleOwner) {
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

    private lateinit var transaction: FragmentTransaction
    private val fragment1 = KeyResultListFragment(KeyResultState.BEFORE_PROGRESS)
    private val fragment2 = KeyResultListFragment(KeyResultState.ON_PROGRESS)
    private val fragment3 = KeyResultListFragment(KeyResultState.COMPLETE)

    private fun setFragment() {
        isCheck = false
        objectiveViewModel.initKeyResultState()
        keyResultStateFragmentSetting()
        transaction = childFragmentManager.beginTransaction()
        transaction.add(R.id.fl_key, fragment1)
        transaction.add(R.id.fl_key, fragment2)
        transaction.add(R.id.fl_key, fragment3)
        transaction.commit()
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
            if (!it.isSelected) objectiveViewModel.setKeyResultState(KeyResultState.BEFORE_PROGRESS)
        }
        binding.btnOnProgress.setOnClickListener {
            if (!it.isSelected) objectiveViewModel.setKeyResultState(KeyResultState.ON_PROGRESS)
        }
        binding.btnComplete.setOnClickListener {
            if (!it.isSelected) objectiveViewModel.setKeyResultState(KeyResultState.COMPLETE)
        }
    }

}