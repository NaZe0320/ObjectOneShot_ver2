package com.objectiveoneshot.objectiveoneshot.presentation.task

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.objectiveoneshot.objectiveoneshot.data.local.model.Task
import com.objectiveoneshot.objectiveoneshot.databinding.ItemTaskBinding
import com.objectiveoneshot.objectiveoneshot.domain.viewmodel.AppViewModel
import com.objectiveoneshot.objectiveoneshot.util.ItemDiffCallback

class TaskListAdapter(
    private val keyResultId: String,
    private val viewModel: AppViewModel
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

        }

        fun bind(item: Task) {
            binding.task = item

            binding.btnDeleteTask.visibility = View.GONE

            binding.btnAddTask.setOnClickListener {
                if (binding.etTaskName.text.toString().isNotEmpty()) {
                    viewModel.addTask(item.key_result_id)
                    binding.btnAddTask.visibility = View.GONE
                }
            }

            binding.btnDeleteTask.setOnClickListener {
                deleteItem(item)
            }

            when (adapterPosition) {
                itemCount - 1 -> { //삭제 시에 마지막 아이템이면 Add Task 보여주기
                    binding.btnAddTask.visibility = View.VISIBLE
                }
                else -> {
                    binding.btnAddTask.visibility = View.GONE
                }
            }

            binding.etTaskName.setOnFocusChangeListener { v, hasFocus ->
                val text = binding.etTaskName.text.toString()
                if (hasFocus) {
                    binding.btnDeleteTask.visibility = View.VISIBLE
                    binding.btnAddTask.visibility = View.GONE
                } else {
                    if (text.isNotEmpty()) {
                        binding.btnDeleteTask.visibility = View.GONE

                        if (adapterPosition == itemCount - 1) {
                            binding.btnAddTask.visibility = View.VISIBLE
                        }
                    } else {
                        if (adapterPosition == itemCount - 1) {
                            deleteItem(item)
                            notifyItemChanged(adapterPosition - 1,)
                        } else {
                            deleteItem(item)
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
                if(binding.etTaskName.text.toString().isNotEmpty()) {
                    viewModel.setKeyResultProgress(item.key_result_id)
                } else {
                    binding.cbTaskComplete.isChecked = false
                }
            }
        }

        private fun deleteItem(item: Task) {
            viewModel.deleteTask(item.key_result_id, item.id)
            //TODO(하단 삭제해도 되나?)
            //ubmitList(currentList.toMutableList().apply { removeAt(adapterPosition) })
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
}