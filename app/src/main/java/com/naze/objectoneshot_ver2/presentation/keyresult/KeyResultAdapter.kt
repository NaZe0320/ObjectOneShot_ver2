package com.naze.objectoneshot_ver2.presentation.keyresult

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.naze.objectoneshot_ver2.data.local.model.KeyResult
import com.naze.objectoneshot_ver2.databinding.ItemKeyResultBinding
import com.naze.objectoneshot_ver2.domain.viewmodel.ObjectiveViewModel
import com.naze.objectoneshot_ver2.presentation.task.TaskListAdapter
import com.naze.objectoneshot_ver2.util.ItemDiffCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class KeyResultAdapter(
    private val objectiveViewModel: ObjectiveViewModel,
    private val lifecycleOwner: LifecycleOwner
): ListAdapter<KeyResult, RecyclerView.ViewHolder>(
    ItemDiffCallback<KeyResult>(
        onContentsTheSame = {old, new -> old == new},
        onItemsTheSame = {old, new -> old.id == new.id}
    )
){
    inner class KeyViewHolder(
        private val binding: ItemKeyResultBinding
    ): RecyclerView.ViewHolder(binding.root) {
        init {
            binding.btnExpand.setOnClickListener { //확장
                when (binding.rvTaskList.visibility) {
                    View.VISIBLE -> {
                        binding.rvTaskList.visibility = View.GONE
                    }
                    View.GONE -> {
                        binding.rvTaskList.visibility = View.VISIBLE
                    }
                    else -> {}
                }
            }
        }

        fun bind(keyResult: KeyResult) {
            Log.d("TEST_KeyResultAdapter","KeyResult : $keyResult")
            binding.keyResult = keyResult
            binding.executePendingBindings()

            val taskListAdapter = TaskListAdapter(keyResult.id, objectiveViewModel)

            binding.rvTaskList.apply {
                adapter = taskListAdapter
                layoutManager = LinearLayoutManager(context)
                taskListAdapter.submitList(objectiveViewModel.getTaskList(keyResult.id))
                //objectiveViewModel 에서 해당 keyResult에 해당하는 TaskList 가져오기
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return KeyViewHolder(ItemKeyResultBinding.inflate(inflater, parent, false))
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
