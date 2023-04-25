package com.objectiveoneshot.objectiveoneshot.domain.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.objectiveoneshot.objectiveoneshot.data.local.model.*
import com.objectiveoneshot.objectiveoneshot.domain.repository.ObjectiveRepository
import com.objectiveoneshot.objectiveoneshot.domain.type.KeyResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val objectiveRepository: ObjectiveRepository
) : ViewModel() {
    private val _objectiveListWithKeyResults = MutableLiveData<List<ObjectiveWithKeyResults>>()
    val objectiveListWithKeyResults : LiveData<List<ObjectiveWithKeyResults>> get() = _objectiveListWithKeyResults

    private val _objectiveData = MutableLiveData<Objective>()
    val objectiveData: LiveData<Objective> get() = _objectiveData
    private val _keyResultWithTasks = MutableLiveData<List<KeyResultWithTasks>>()
    val keyResultWithTasks : LiveData<List<KeyResultWithTasks>> get() = _keyResultWithTasks

    private val _keyResultState = MutableLiveData<KeyResultState>()
    val keyResultState : LiveData<KeyResultState> get() = _keyResultState

    /** 데이터 insert */
    fun insertObjectiveData() {
        viewModelScope.launch(Dispatchers.IO) {
            _objectiveData.value?.let { objectiveRepository.insertObjective(it) }
            _keyResultWithTasks.value?.let { objectiveRepository.insertKeyResultsWithTasks(it) }
        }
    }

    fun updateObjectiveData() {
        viewModelScope.launch(Dispatchers.IO) {
            _objectiveData.value?.let { objectiveRepository.updateObjective(it) }
            _keyResultWithTasks.value?.let { objectiveRepository.updateKeyResultsWithTasks(it, _objectiveData.value?.id?:"") }
        }
    }

    /** 데이터 select / get */
    fun getObjectiveList() {
        viewModelScope.launch(Dispatchers.Main) {
            _objectiveListWithKeyResults.value = objectiveRepository.getObjective()
        }
    } //ObjectiveList 가져오기 추후 변경 예정

    fun getObjectiveAchieveList() {
        viewModelScope.launch(Dispatchers.Main) {
            _objectiveListWithKeyResults.value = objectiveRepository.getAchieveObjective()
        }
    } //ObjectiveAchieveList 가져오기 추후 변경 예정

    /** 데이터 초기화 */
    fun initObjectiveData() {
        _objectiveData.value = Objective(
            title = "",
            startDate = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis,
            endDate = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis,
            progress = 0.0,
            complete = false,
        )
        _keyResultWithTasks.value = mutableListOf()
    }

    fun getObjectiveData(id: String) {
        viewModelScope.launch(Dispatchers.Main) {
            _objectiveData.value = objectiveRepository.getObjectiveById(id)
            _keyResultWithTasks.value = objectiveRepository.getKeyResultWithTasksById(id)
        }
    } //Add/Modify Objective 시 데이터 가져오기

    fun setObjectiveDateRange(startDate: Long, endDate: Long) {
        _objectiveData.value = _objectiveData.value?.copy(startDate = startDate, endDate = endDate)
    } //Objective Date 범위

    fun setKeyResultState(keyResultState: KeyResultState) {
        _keyResultState.value = keyResultState
    }

    fun addKeyResult(): String {
        val currentList = _keyResultWithTasks.value ?: mutableListOf()
        val newKeyResult = KeyResultWithTasks(
            keyResult = KeyResult(title = "", progress = 0.0, objective_id = _objectiveData.value?.id?:""),
            tasks = emptyList()
        )
        val newItem = newKeyResult.tasks.toMutableList().apply {
            add(Task(content = "", key_result_id = newKeyResult.keyResult.id))
        }

        val newList = currentList.toMutableList().apply {
            add(newKeyResult.copy(tasks = newItem))
        }
        _keyResultWithTasks.value = newList
        Log.d("TT_keyResult","${_keyResultWithTasks.value}\n$newItem")
        setObjectiveProgress()

        return newKeyResult.keyResult.id
    }

    /**
     * @param id KeyResult_id
     */
    fun addTask(keyResultId: String) {
        val currentList = _keyResultWithTasks.value ?: mutableListOf()
        val newList = currentList.map { keyResultWithTasks ->
            if (keyResultWithTasks.keyResult.id == keyResultId) {
                val newTasks = keyResultWithTasks.tasks.toMutableList().apply {
                    add(Task(content = "", key_result_id = keyResultId))
                }
                keyResultWithTasks.copy(tasks = newTasks)
            } else {
                keyResultWithTasks
            }
        }
        Log.d("TT_tasks","${_keyResultWithTasks.value}")
        _keyResultWithTasks.value = newList

        setKeyResultProgress(keyResultId)
    } //이거 Task 추가하는 방식 개편 가능할 듯

    fun deleteObjective(objectiveId: String) {
        viewModelScope.launch {
            objectiveRepository.deleteObjective(objectiveId)
            getObjectiveList() //삭제 후 모든 리스트 불러오기
        }
    }

    fun deleteAchieveObjective(objectiveId: String) {
        viewModelScope.launch {
            objectiveRepository.deleteObjective(objectiveId)
            getObjectiveAchieveList() //삭제 후 모든 리스트 불러오기
        }
    }

    fun deleteKeyResult(keyResultId: String) {
        val newList = _keyResultWithTasks.value.orEmpty().filter { it.keyResult.id != keyResultId }
        _keyResultWithTasks.value = newList

        setObjectiveProgress()
    }

    fun deleteTask(keyResultId: String, taskId: String) {
        val newList = _keyResultWithTasks.value.orEmpty().map { keyResultWithTasks ->
            if (keyResultWithTasks.keyResult.id == keyResultId) {
                val newTasks = keyResultWithTasks.tasks.filter { it.id != taskId }
                keyResultWithTasks.copy(tasks = newTasks)
            } else {
                keyResultWithTasks
            }
        }
        _keyResultWithTasks.value = newList

        setKeyResultProgress(keyResultId)
    }

    fun setKeyResultProgress(keyResultId: String) {
        val tasks = _keyResultWithTasks.value?.first { it.keyResult.id == keyResultId }?.tasks
        val progress = if (!tasks.isNullOrEmpty()) {
            100 * tasks.filter { it.check }.size / tasks.size //100 곱해주는 이유는 % 값이라서
        } else {
            0
        }.toDouble()
        val newList = _keyResultWithTasks.value.orEmpty().map { keyResultWithTasks ->
            if (keyResultWithTasks.keyResult.id == keyResultId) {
                keyResultWithTasks.copy(keyResult = keyResultWithTasks.keyResult.copy(progress = progress))
            } else {
                keyResultWithTasks
            }
        }
        _keyResultWithTasks.value = newList

        setObjectiveProgress()
    }

    private fun setObjectiveProgress() {
        val keyResults = _keyResultWithTasks.value
        val progress = if (!keyResults.isNullOrEmpty()) {
            100 * keyResults.filter { it.keyResult.progress >= 100 }.size / keyResults.size
        } else {
            0
        }.toDouble()
        val objective = _objectiveData.value
        _objectiveData.value = objective?.copy(progress = progress)
        Log.d("TT_progress","$progress")
    } //Objective 의 progress 계산. keyResult progress 가 100 인 개수

    fun checkIsEmpty(): Boolean { //비었으면 true, 안비었으면 false
        val test1 =
            _keyResultWithTasks.value?.any { it.keyResult.title.isEmpty() }
        val test2 =
            _keyResultWithTasks.value?.any {
                it.tasks.isEmpty()
            }
        val test3 =
            _objectiveData.value?.title?.isEmpty()

        return (test1?:false || test2?:false || test3?:false)
    }

    suspend fun checkIsChange(): Boolean { //변하면 true, 안변하면 false
        val objectiveBefore = objectiveRepository.getObjectiveById(_objectiveData.value?.id?:"")
        val keyResultWithTasksBefore = objectiveRepository.getKeyResultWithTasksById(_objectiveData.value?.id?:"")
        Log.d("TT_keyResult_check","$keyResultWithTasksBefore\n${_keyResultWithTasks.value}")
        return !(objectiveBefore == _objectiveData.value
                && keyResultWithTasksBefore == _keyResultWithTasks.value)
    }

    suspend fun checkAchieveComplete(): Boolean {
        return false
    }

    suspend fun checkAchieveUnComplete(): Boolean {
        return false
    }
}