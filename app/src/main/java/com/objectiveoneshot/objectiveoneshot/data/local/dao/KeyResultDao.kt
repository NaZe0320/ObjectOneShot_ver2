package com.objectiveoneshot.objectiveoneshot.data.local.dao

import androidx.room.*
import com.objectiveoneshot.objectiveoneshot.data.local.model.KeyResult
import com.objectiveoneshot.objectiveoneshot.data.local.model.KeyResultWithTasks
import com.objectiveoneshot.objectiveoneshot.data.local.model.Task

@Dao
interface KeyResultDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(keyResult: List<KeyResult>)

    @Update
    suspend fun update(keyResult: KeyResult)

    @Delete
    suspend fun delete(keyResult: KeyResult)

    @Query("SELECT * FROM key_results WHERE objective_id = :objectiveId")
    suspend fun getKeyResultsByObjective(objectiveId: Long): List<KeyResult>

    @Transaction
    @Query("SELECT * FROM KEY_RESULTS WHERE objective_id = :objectiveId ")
    suspend fun getKeyResultWithTasksById(objectiveId: String): List<KeyResultWithTasks>

    @Query("DELETE FROM KEY_RESULTS WHERE objective_id = :objectiveId")
    suspend fun deleteKeyResultByObjectiveId(objectiveId: String) //업데이트를 위하여 objective와 관련된 모든 keyresult를 삭제

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun updateKeyResult(keyResult: List<KeyResult>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun updateTask(task: List<Task>)

    @Transaction
    suspend fun updateKeyResultWithTask(keyResult: List<KeyResult>,task: List<Task>, id: String) {
        deleteKeyResultByObjectiveId(id)
        updateKeyResult(keyResult)
        updateTask(task)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertKeyResult(keyResult: KeyResult)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTask(task: Task)

    @Transaction
    suspend fun insertKeyResultsWithTasks(keyResultsWithTasks: List<KeyResultWithTasks>) {
        for (keyResultWithTasks in keyResultsWithTasks) {
            insertKeyResult(keyResultWithTasks.keyResult)

            for (task in keyResultWithTasks.tasks) {
                insertTask(task)
            }
        }
    }

    @Transaction
    suspend fun updateKeyResultsWithTasks(keyResultsWithTasks: List<KeyResultWithTasks>, objectiveId: String) {
        deleteKeyResultByObjectiveId(objectiveId)
        for (keyResultWithTasks in keyResultsWithTasks) {
            insertKeyResult(keyResultWithTasks.keyResult)

            for (task in keyResultWithTasks.tasks) {
                insertTask(task)
            }
        }
    }
}