package com.objectiveoneshot.objectiveoneshot.presentation.task

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.objectiveoneshot.objectiveoneshot.data.local.model.Task
import com.objectiveoneshot.objectiveoneshot.databinding.ItemTaskBinding
import com.objectiveoneshot.objectiveoneshot.domain.viewmodel.ObjectiveViewModel
import com.objectiveoneshot.objectiveoneshot.util.ItemDiffCallback

class TaskAddAdapter(
    private val keyResultId: String,
    private val objectiveViewModel: ObjectiveViewModel
): ListAdapter<Task, RecyclerView.ViewHolder>(
    ItemDiffCallback<Task> (
        onContentsTheSame = {old, new -> old == new},
        onItemsTheSame = {old, new -> old.id == new.id}
    )
) {

    inner class TaskAddViewHolder(
        private val binding: ItemTaskBinding,
    ): RecyclerView.ViewHolder(binding.root) {
        init {
            binding.btnAddTask.setOnClickListener {
                if (binding.etTaskName.text.toString().isNotEmpty()) {
                    binding.btnAddTask.visibility = View.GONE
                    addItem()
                }
            }
            binding.btnDeleteTask.setOnClickListener {
                binding.etTaskName.text.clear()
            }
        }

        fun bind(item: Task) {
            binding.task = item

            binding.btnDeleteTask.visibility = View.GONE
            when (adapterPosition) {
                itemCount - 1 -> { //삭제 시에 마지막 아이템이면 Add Task 보여주기
                    binding.btnAddTask.visibility = View.VISIBLE
                }
                itemCount - 2 -> { //마지막 아이템이면 Add Task 보여주기
                    binding.btnAddTask.visibility = View.VISIBLE
                }
                else -> {
                    binding.btnAddTask.visibility = View.GONE
                }
            }
            if (adapterPosition != 0) {
                binding.etTaskName.requestFocus()
            }
            binding.etTaskName.setOnFocusChangeListener { v, hasFocus ->
                val text = binding.etTaskName.text.toString()
                Log.d("TEST_Task","$adapterPosition / ${itemCount - 1}")
                if (hasFocus) {
                    if (adapterPosition != currentList.size-1) {
                        binding.btnDeleteTask.visibility = View.VISIBLE
                    }
                } else {
                    if (text.isNotEmpty()) {
                        addOrUpdateTaskList(item)

                        binding.btnDeleteTask.visibility = View.GONE

                        if (adapterPosition == itemCount - 1) {
                            binding.btnAddTask.visibility = View.VISIBLE
                        }
                    } else {
                        if (adapterPosition == itemCount - 1) {
                            deleteItem()
                            notifyItemChanged(adapterPosition - 1)
                        } else {
                            deleteItem()
                        }
                    }
                }
            }
            binding.etTaskName.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    binding.etTaskName.clearFocus()
                    return@setOnEditorActionListener true
                } else {
                    return@setOnEditorActionListener false
                }
            }
            binding.cbTaskComplete.setOnClickListener {
                if (binding.etTaskName.text.toString().isNotEmpty()) {
                    addOrUpdateTaskList(item)
                    objectiveViewModel.changeKeyResultProgress(keyResultId)
                } else {
                    binding.cbTaskComplete.isChecked = false
                }
            }

        }
        private fun addItem() {
            if (itemCount < 5) {
                val newTask = Task("",false,keyResultId)
                submitList(currentList.toMutableList().apply { add(newTask) })
            }
        }
        private fun deleteItem() {
            deleteTaskList(currentList[adapterPosition])
            submitList(currentList.toMutableList().apply { removeAt(adapterPosition) })
            objectiveViewModel.changeKeyResultProgress(keyResultId)
            binding.etTaskName.clearFocus()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return TaskAddViewHolder(ItemTaskBinding.inflate(inflater,parent,false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TaskAddViewHolder -> {
                holder.bind(getItem(position))
            }
        }
    }

    override fun submitList(list: List<Task>?) {
        if (list.isNullOrEmpty()) {
            super.submitList(listOf(Task("", key_result_id = keyResultId)))
        } else {
            super.submitList(list)
        }
    }

    private fun addOrUpdateTaskList(item: Task) {
        objectiveViewModel.addOrUpdateNewTaskData(Task(
            item.content,
            item.check,
            item.key_result_id,
            item.id
        ))
    }

    private fun deleteTaskList(item: Task) {
        objectiveViewModel.deleteNewTaskData(item)
    }
}