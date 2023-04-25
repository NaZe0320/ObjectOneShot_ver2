package com.objectiveoneshot.objectiveoneshot.presentation.keyresult

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.objectiveoneshot.objectiveoneshot.data.local.model.KeyResultWithTasks
import com.objectiveoneshot.objectiveoneshot.databinding.ItemKeyResultUneditBinding
import com.objectiveoneshot.objectiveoneshot.domain.viewmodel.AppViewModel
import com.objectiveoneshot.objectiveoneshot.presentation.task.TaskListUnEditAdapter
import com.objectiveoneshot.objectiveoneshot.util.ItemDiffCallback


class KeyResultUnEditAdapter(
    private val viewModel: AppViewModel
): ListAdapter<KeyResultWithTasks, RecyclerView.ViewHolder>(
    ItemDiffCallback<KeyResultWithTasks>(
        onContentsTheSame = {old, new -> old == new},
        onItemsTheSame = {old, new -> old.keyResult.id == new.keyResult.id}
    )
) {

    inner class KeyViewHolder(
        private val binding: ItemKeyResultUneditBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(keyResult: KeyResultWithTasks) {
            binding.keyResult = keyResult
            binding.executePendingBindings()
            Log.d("TEST_KeyResultAdapter","KeyResult Id : ${keyResult.keyResult.id}")
            val taskListAdapter = TaskListUnEditAdapter(keyResult.keyResult.id)

            binding.rvTaskList.apply {
                adapter = taskListAdapter
                layoutManager = LinearLayoutManager(context)
                taskListAdapter.submitList(viewModel.keyResultWithTasks.value?.first { (keyResult.keyResult.id) == it.keyResult.id }?.tasks)
                //objectiveViewModel 에서 해당 keyResult에 해당하는 TaskList 가져오기
            }
            binding.btnExpand.setOnClickListener {
                if (binding.rvTaskList.visibility == View.GONE) { //안 보일 때 보이게 하기
                    binding.rvTaskList.visibility = View.VISIBLE
                    binding.btnExpand.rotation = 180f
                } else {
                    binding.rvTaskList.visibility = View.GONE
                    binding.btnExpand.rotation = 360f
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return KeyViewHolder(ItemKeyResultUneditBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val keyResult = getItem(position)
        when (holder) {
            is KeyViewHolder -> {
                holder.bind(keyResult)
            }
        }
    }
}
