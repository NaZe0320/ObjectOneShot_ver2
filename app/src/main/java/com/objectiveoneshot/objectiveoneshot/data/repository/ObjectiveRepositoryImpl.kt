package com.objectiveoneshot.objectiveoneshot.data.repository

import com.objectiveoneshot.objectiveoneshot.data.local.dao.KeyResultDao
import com.objectiveoneshot.objectiveoneshot.data.local.dao.ObjectiveDao
import com.objectiveoneshot.objectiveoneshot.data.local.dao.TaskDao
import com.objectiveoneshot.objectiveoneshot.data.local.model.*
import com.objectiveoneshot.objectiveoneshot.domain.repository.ObjectiveRepository
import com.objectiveoneshot.objectiveoneshot.util.getCurrentDate
import javax.inject.Inject

class ObjectiveRepositoryImpl @Inject constructor(
    private val objectiveDao: ObjectiveDao,
    private val keyResultDao: KeyResultDao,
    private val taskDao: TaskDao
): ObjectiveRepository {
    override suspend fun insertObjective(objective: Objective) {
        objectiveDao.insert(objective)
    }

    override suspend fun insertKeyResult(keyResult: List<KeyResult>) {
        keyResultDao.insert(keyResult)
    }

    override suspend fun insertTask(task: Task) {
        taskDao.insert(task)
    }

    override suspend fun getObjective(): List<ObjectiveWithKeyResults> {
        return objectiveDao.getObjectives(getCurrentDate())
    } //TODO (추후 변경 : KeyResult 데이터를 포함한 구조로 변경 필요)

    override suspend fun getAchieveObjective(): List<ObjectiveWithKeyResults> {
        return objectiveDao.getAchieveObjectives(getCurrentDate())
    } //TODO (추후 변경 : KeyResult 데이터를 포함한 구조로 변경 필요)

    override suspend fun getObjectiveById(id: String): Objective {
        return objectiveDao.getObjectiveById(id)
    }

    override suspend fun getKeyResultWithTasksById(id: String): List<KeyResultWithTasks> {
        return keyResultDao.getKeyResultWithTasksById(id)
    }

    override suspend fun updateObjective(objective: Objective) {
        objectiveDao.update(objective)
    }

    override suspend fun updateKeyResultWithTask(
        keyResult: List<KeyResult>,
        task: List<Task>,
        objectiveId: String
    ) {
        keyResultDao.updateKeyResultWithTask(keyResult, task, objectiveId)
    }

    override suspend fun deleteObjective(objectiveId: String) {
        objectiveDao.deleteObjective(objectiveId)
    }

    override suspend fun getObjectiveComplete(): List<Objective> {
        return objectiveDao.getObjectiveComplete()
    }

    override suspend fun getObjectiveUnComplete(): List<Objective> {
        return objectiveDao.getObjectiveUnComplete(getCurrentDate())
    }

    override suspend fun updateObjectiveComplete(objectives: List<Objective>) {
        return objectiveDao.updateCompleteObjective(objectives)
    }

    override suspend fun insertKeyResultsWithTasks(keyResultsWithTasks: List<KeyResultWithTasks>) {
        keyResultDao.insertKeyResultsWithTasks(keyResultsWithTasks)
    }

    override suspend fun updateKeyResultsWithTasks(keyResultsWithTasks: List<KeyResultWithTasks>, objectiveId: String) {
        keyResultDao.updateKeyResultsWithTasks(keyResultsWithTasks, objectiveId)
    }
}