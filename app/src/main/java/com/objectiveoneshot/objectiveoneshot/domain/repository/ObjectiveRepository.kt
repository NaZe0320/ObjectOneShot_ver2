package com.objectiveoneshot.objectiveoneshot.domain.repository

import com.objectiveoneshot.objectiveoneshot.data.local.model.*

interface ObjectiveRepository {
    suspend fun insertObjective(objective: Objective)
    suspend fun insertKeyResult(keyResult: List<KeyResult>)
    suspend fun insertTask(task: Task)

    suspend fun getObjective(): List<ObjectiveWithKeyResults>
    suspend fun getAchieveObjective(): List<ObjectiveWithKeyResults>

    suspend fun getObjectiveById(id: String): Objective
    suspend fun getKeyResultWithTasksById(id: String): List<KeyResultWithTasks>

    suspend fun updateObjective(objective: Objective)
    suspend fun updateKeyResultWithTask(keyResult: List<KeyResult>, task: List<Task>, objectiveId: String)

    suspend fun deleteObjective(objectiveId: String)

    suspend fun getObjectiveComplete() : List<Objective>
    suspend fun getObjectiveUnComplete() : List<Objective>
    suspend fun updateObjectiveComplete(objectives: List<Objective>)

    suspend fun insertKeyResultsWithTasks(keyResultsWithTasks: List<KeyResultWithTasks>)
    suspend fun updateKeyResultsWithTasks(keyResultsWithTasks: List<KeyResultWithTasks>, objectiveId: String)

}