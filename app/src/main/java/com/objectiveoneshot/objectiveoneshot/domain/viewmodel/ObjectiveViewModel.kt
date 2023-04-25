package com.objectiveoneshot.objectiveoneshot.domain.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.objectiveoneshot.objectiveoneshot.data.local.model.*
import com.objectiveoneshot.objectiveoneshot.domain.repository.ObjectiveRepository
import com.objectiveoneshot.objectiveoneshot.domain.type.KeyResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ObjectiveViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val objectiveRepository: ObjectiveRepository
): ViewModel() {
    private val _objectiveListWithKeyResults = MutableLiveData<List<ObjectiveWithKeyResults>>()
    val objectiveListWithKeyResults : LiveData<List<ObjectiveWithKeyResults>> get() = _objectiveListWithKeyResults

    private val _keyResultWithTasks = MutableLiveData<List<KeyResultWithTasks>>()
    val keyResultWithTasks : LiveData<List<KeyResultWithTasks>> get() = _keyResultWithTasks

    private val _objective = MutableLiveData<Objective>()
    val objective: LiveData<Objective> get() = _objective

    private val _keyResultList = MutableLiveData<List<KeyResult>?>()
    val keyResultList: LiveData<List<KeyResult>?> get() = _keyResultList

    private val _keyResult = MutableLiveData<KeyResult>()
    val keyResult: LiveData<KeyResult> get() = _keyResult //KeyList 추가할 때 쓰는 아이템

    private val _taskList = MutableLiveData<List<Task>>()
    val taskList: LiveData<List<Task>> get() = _taskList

    private val _newTaskList = MutableLiveData<List<Task>>()
    val newTaskList: LiveData<List<Task>> get() = _newTaskList

    private val _task = MutableLiveData<Task>()
    val task: LiveData<Task> get() = _task

    private val _keyResultState = MutableLiveData<KeyResultState>()
    val keyResultState: LiveData<KeyResultState> get() = _keyResultState

    /***
     * 데이터 Insert
     */
    //insert
    fun insertObjective() {
        viewModelScope.launch(Dispatchers.IO) {
            _objective.value?.let { objectiveRepository.insertObjective(it) }
            insertKeyResult()
        }
    }

    private fun insertKeyResult() {
        viewModelScope.launch(Dispatchers.IO) {
            _keyResultList.value?.let { objectiveRepository.insertKeyResult(it)}
            insertTask()
        }
    }

    private fun insertTask() {
        viewModelScope.launch(Dispatchers.IO) {
            _taskList.value?.let {
                for(i in it) {
                    objectiveRepository.insertTask(i)
                }
            }
        }
    }

    fun updateObjective() {
        viewModelScope.launch(Dispatchers.IO) {
//            objectiveRepository.updateObjective(_objective.value!!)
            val keyResultsList = _keyResultList.value?: mutableListOf()
            val tasksList = _taskList.value?: mutableListOf()
            objectiveRepository.updateKeyResultWithTask(keyResultsList, tasksList, _objective.value!!.id)
        }

    }

    /***
     * 데이터 SELECT (GET)
     */
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



    /** 데이터 초기화 - Add Fragment
     *  데이터 신규 생성
     */

    fun initObjectiveData() {
        _objective.value = Objective(
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
        //Log.d("TEST_initObjective","id = ${_objective.value?.id}")
        initKeyResultList()
    } //ObjectiveData Add 위해 초기값 설정

    fun initKeyResultData() {
        _keyResult.value = KeyResult(
            title = "",
            progress = 0.0,
            objective_id = _objective.value!!.id,
        )
        //Log.d("TEST_initKeyResult","objective id = ${_objective.value?.id}")
        //Log.d("TEST_initKeyResult","keyResult id = ${_keyResult.value?.id}")
    }//objective_id를 가지고 새로운 KeyResult 를 추가

    private fun initKeyResultList() {
        _keyResultList.value = null
    }

    /**데이터 초기화(데이터 가져오기)
     * Modify Fragment - 수정 위한 데이터
     */
    fun getObjectiveData(id : String) {
        viewModelScope.launch(Dispatchers.Main) {
            _objective.value = objectiveRepository.getObjectiveById(id)
            _keyResultWithTasks.value = objectiveRepository.getKeyResultWithTasksById(id)
            _keyResultList.value = _keyResultWithTasks.value?.map { it.keyResult }
            val task = mutableListOf<Task>()
            _keyResultWithTasks.value?.forEach {
                task.addAll(it.tasks)
            }
            _taskList.value = task
            Log.d("TEST_getAllData1","${_keyResultWithTasks.value}")
            Log.d("TEST_getAllData2","${_keyResultList.value}")
            Log.d("TEST_getAllData3","${_taskList.value}")
        }
    }

    /** 데이터 초기화 - 기타
     *  KeyResult State 값
     */
    fun initKeyResultState() {
        _keyResultState.value = KeyResultState.BEFORE_PROGRESS //시작할 땐 _keyResultState를 BEFORE로 초기화
    }

    fun initAchieveKeyResultState() {
        _keyResultState.value = KeyResultState.COMPLETE
    }

    /** Objective 삭제 */
    fun deleteObjective(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            objectiveRepository.deleteObjective(id)
            getObjectiveList()
        }
    }

    fun deleteAchieveObjective(id: String) {
        viewModelScope.launch(Dispatchers.Main) {
            objectiveRepository.deleteObjective(id)
            getObjectiveAchieveList()
        }
    }

    fun deleteKeyResult(id: String) {
        val currentList = _keyResultList.value ?: mutableListOf()
        val index = currentList.indexOfFirst { it.id == id }
        if (index != -1) {
            _keyResultList.value = currentList.filterNot { it.id == id }
            deleteTaskDataByKeyResult(id)
            setObjectiveProgress()
        }
    }

    private fun deleteTaskDataByKeyResult(id : String) {
        val currentList = _taskList.value ?: mutableListOf()
        _taskList.value = currentList.filterNot { it.key_result_id == id }
    }

    /** 하단 미분류 */

    /** update data */
    fun modifyKeyResultData(keyResult: KeyResult) {
        val currentList = _keyResultList.value ?: mutableListOf()
        val index = currentList.indexOfFirst { it.id == keyResult.id }
        if (index != -1) {
            val newList = currentList.toMutableList().apply {
                set(index, keyResult)
            }
            _keyResultList.value = newList
        }
    }

    /** 변경 사항 있는지 확인 */
    suspend fun isChange(id: String): Boolean {
        val task = mutableListOf<Task>()

        val objectiveBefore = objectiveRepository.getObjectiveById(id)
        val keyResultTasks = objectiveRepository.getKeyResultWithTasksById(id)

        keyResultTasks.forEach {
            task.addAll(it.tasks)
        }
        return !(_keyResultList.value == keyResultTasks.map { it.keyResult }
                && _taskList.value == task && objectiveBefore == _objective.value)
    }

    fun checkEmpty(): Boolean {
        val hasEmptyContent = _taskList.value?.any { it.content.isEmpty() } ?: false
        if (hasEmptyContent) {
            return true
        }
        val hasEmptyObjective = _objective.value?.title?.isEmpty() ?: false
        if (hasEmptyObjective) {
            return true
        }
        val hasEmptyTitle = _keyResultList.value?.any { it.title.isEmpty() } ?: false
        if (hasEmptyTitle) {
            return true
        }
        return false
    }

    /** view 에서 button 으로 state 변경할 때 사용  */
    fun setKeyResultState(keyResultState: KeyResultState) {
        _keyResultState.value = keyResultState
    }

    /**
     * @param progress KeyResult의 Progress 값
     * KeyResult Progress 값을 통하여 KeyResultState 변경
     */
    private fun setKeyResultStateByProgress(progress: Double) {
        _keyResultState.value = when (progress) {
            0.0 -> {
                KeyResultState.BEFORE_PROGRESS
            }
            in 0.1..99.9 -> {
                KeyResultState.ON_PROGRESS
            }
            else -> {
                KeyResultState.COMPLETE
            }
        }
    }

    /**
     * @param startDate 시작일
     * @param endDate 종료일
     * Objective 의 Date Range 설정
     */
    fun setObjectiveDateRange(startDate: Long, endDate: Long) {
        _objective.value = _objective.value?.copy(startDate = startDate, endDate = endDate)
        Log.d("TEST_ObjectiveViewModel","${_objective.value}")
    } //TextView 이기 때문에 입력값이 없어서 직접 메소드로 입력

    /**
     * Objective Progress 설정
     */
    private fun setObjectiveProgress() {
        val list = _keyResultList.value ?: mutableListOf()
        var sum = 0.0
        for (i in list) {
            if (i.progress >= 100)
                sum ++
        }
        _objective.value = _objective.value?.copy(progress = if (list.isNotEmpty()) sum/list.size*100 else 0.0)
    } //KeyResult 데이터를 가지고 계산해야 하기에 처리

    /**
     * init 한 KeyResult 를 가지고 List 에 추가
     */
    fun addKeyResultList() {
        addNewTaskToTaskData()
        val currentList = _keyResultList.value ?: mutableListOf()
        val newList = currentList.toMutableList().apply {
            _keyResult.value?.let { add(it) }
        }
        _keyResultList.value = newList
        Log.d("TEST_ObjectiveViewModel","KeyResultList : ${_keyResultList.value}")

        setKeyResultStateByProgress(_keyResult.value?.progress?:0.0)
        setObjectiveProgress()

        //TODO(List에 추가된 경우 -> Objective 의 Progress가 변한다)
    } //init 한 KeyResult 를 가지고 List 에 추가

    /**
     * Task 를 Add 나 Update 할 때 사용
     * viewModel 내부 TaskList
     */
    fun addOrUpdateTaskData(task: Task) {
        val currentList = _taskList.value ?: mutableListOf()
        val index = currentList.indexOfFirst { it.id == task.id }
        if (index == -1) {
            _taskList.value = currentList + task
        } else {
            val newList = currentList.toMutableList().apply {
                set(index, task)
            }
            _taskList.value = newList
        }
        Log.d("TEST_ObjectiveViewModel","taskList Add or Update: ${_taskList.value}")
        changeKeyResultListProgress(task.key_result_id)
    }

    fun addNewTaskToTaskData() {
        Log.d("TEST_Task","니 작동하니?\n${_newTaskList.value}")
        val currentList = _taskList.value ?: mutableListOf()
        val newList = currentList + _newTaskList.value as List<Task>
        _taskList.value = newList
    }

    fun deleteNewTask() {
        _newTaskList.value = mutableListOf()
    }

    fun addOrUpdateNewTaskData(task: Task) {
        val currentList = _newTaskList.value ?: mutableListOf()
        val index = currentList.indexOfFirst { it.id == task.id }
        if (index == -1) {
            _newTaskList.value = currentList + task
        } else {
            val newList = currentList.toMutableList().apply {
                set(index, task)
            }
            _newTaskList.value = newList
        }
        Log.d("TEST_Task","taskList Add or Update: ${_newTaskList.value}")
        changeKeyResultListProgress(task.key_result_id)
    }

    /**
     * Task 삭제
     */
    fun deleteTaskData(task: Task) {
        val currentList = _taskList.value ?: mutableListOf()
        val index = currentList.indexOfFirst { it.id == task.id }
        if (index != -1) {
            Log.d("TEST_ObjectiveViewModel","taskList Delete : ${currentList[index]}")
            _taskList.value = currentList.filterNot { it.id == task.id }
        }
        Log.d("TEST_ObjectiveViewModel","taskList Delete : ${_taskList.value}")
    }

    fun deleteNewTaskData(task: Task) {
        val currentList = _newTaskList.value ?: mutableListOf()
        val index = currentList.indexOfFirst { it.id == task.id }
        if (index != -1) {
            Log.d("TEST_ObjectiveViewModel","taskList Delete : ${currentList[index]}")
            _newTaskList.value = currentList.filterNot { it.id == task.id }
        }
        Log.d("TEST_ObjectiveViewModel","taskList Delete : ${_newTaskList.value}")
    }

    /**
     * @param id = key_result_id
     */
    fun changeKeyResultListProgress(id: String){
        Log.d("TEST_changeKey","id: $id")
        val list = _taskList.value ?: mutableListOf()
        val newList = list.filter { it.key_result_id == id }
        val progress = if (newList.isNotEmpty()) {
            var cnt = 0 //check 된 개수
            for (i in newList) {
                if (i.check) cnt ++
            }
            100 * cnt / newList.size
        } else { 0 }.toDouble()

        val currentList = _keyResultList.value ?: mutableListOf()
        val updatedList = currentList.map {
            if (it.id == id) {
                it.copy(progress = progress)
            } else {
                it
            }
        }
        _keyResultList.value = updatedList
        setKeyResultStateByProgress(progress)
        setObjectiveProgress()
    } //KeyResultList Progress 변경

    /**
     * @param id = key_result_id
     */
    fun changeKeyResultProgress(id: String) {
        val list = _taskList.value ?: mutableListOf()
        val newList = list.filter { it.key_result_id == id }
        val progress = if (newList.isNotEmpty()) {
            var cnt = 0 //check 된 개수
            for (i in newList) {
                if (i.check) cnt ++
            }
            100 * cnt / newList.size
        } else { 0 }.toDouble()
        _keyResult.value = _keyResult.value?.copy(progress = progress)
    } //KeyResult Progress (등록하기 전, state 안바꾸는게 맞을듯)

    /**
     * @param id KeyResultId
     * KeyResultId 로 해당하는 TaskList 가져오기
     */
    fun getTaskList(id: String): List<Task>? {
        return _taskList.value?.filter { it.key_result_id == id }
    }

    /**
     * 완료 혹은 미완료된 데이터가 있는지 확인
     */
    suspend fun checkAchieveComplete(): Boolean {
        var check = false
        val list = objectiveRepository.getObjectiveComplete()
        if (list.isNotEmpty()) {
            check = true
            list.forEach { it.complete = true }
            objectiveRepository.updateObjectiveComplete(list)
        }
        return check
    }

    suspend fun checkAchieveUnComplete(): Boolean {
        var check = false
        val list = objectiveRepository.getObjectiveUnComplete()
        if (list.isNotEmpty()) {
            check = true
            list.forEach { it.complete = true }
            objectiveRepository.updateObjectiveComplete(list)
        }
        return check
    }
}