package com.objectiveoneshot.objectiveoneshot.presentation.objective

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.utils.widget.ImageFilterButton
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.objectiveoneshot.objectiveoneshot.R
import com.objectiveoneshot.objectiveoneshot.data.local.model.ObjectiveWithKeyResults
import com.objectiveoneshot.objectiveoneshot.databinding.ItemObjectiveBinding
import com.objectiveoneshot.objectiveoneshot.domain.viewmodel.ObjectiveViewModel
import com.objectiveoneshot.objectiveoneshot.util.ItemDiffCallback

class ObjectiveListAdapter(
    private val clickListener: ItemClickListener,
    private val objectiveViewModel: ObjectiveViewModel
): ListAdapter<ObjectiveWithKeyResults, RecyclerView.ViewHolder>(
    ItemDiffCallback<ObjectiveWithKeyResults>(
        onContentsTheSame = {old, new -> old == new},
        onItemsTheSame = {old, new -> old.objective.id == new.objective.id}
    ))
 {
     override fun onCreateViewHolder(
         parent: ViewGroup,
         viewType: Int
     ): RecyclerView.ViewHolder {
         val inflater = LayoutInflater.from(parent.context)
         return ObjectiveViewHolder(ItemObjectiveBinding.inflate(inflater, parent, false))
     }

     override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
         val item = getItem(position)
         when (holder) {
             is ObjectiveViewHolder -> {
                 holder.bind(item, clickListener)
             }
         }
     }

     inner class ObjectiveViewHolder(
         private val binding: ItemObjectiveBinding,
     ): RecyclerView.ViewHolder(binding.root) {

         fun bind(item: ObjectiveWithKeyResults, clickListener: ItemClickListener) {
             binding.objective = item
             binding.clickListener = clickListener

             val list = item.keyResults?.filter { it.progress < 100 }
             val newList = list?.sortedBy { it.progress }?.reversed()
             //KeyResult를 완료된 애들은 안보여준다.
             //KeyResult 진행 중인거 먼저,
             when (list?.size) { //TODO(추후 여기에 보여줄 KeyResult 를 선택하는 항목도 넣을 수 있음)
                 null, 0 -> {
                     binding.layoutKeyList.visibility = View.GONE
                 }
                 1 -> {
                     binding.tvKeyResult1.text = newList?.get(0)?.title
                     binding.tvKeyResult2.visibility = View.GONE
                 }
                 else -> {
                     binding.tvKeyResult1.text = newList?.get(0)?.title
                     binding.tvKeyResult2.text = newList?.get(1)?.title
                 }
             }
             binding.deleteItemView.setOnClickListener {
                 val dialog: Dialog = Dialog(itemView.context)
                 dialog.setContentView(R.layout.dialog_delete)
                 dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                 dialog.show()
                 dialog.findViewById<ImageFilterButton>(R.id.btn_delete_dialog).setOnClickListener {
                     dialog.dismiss()
                     objectiveViewModel.deleteObjective(item.objective.id)
                 }
                 dialog.findViewById<ImageFilterButton>(R.id.btn_cancel_dialog).setOnClickListener {
                     dialog.dismiss()
                 }
             }
         }
     }

     class ItemClickListener(val clickListener: (String) -> Unit) {
         fun onClick(objectiveWithKeyResults: ObjectiveWithKeyResults) = clickListener(objectiveWithKeyResults.objective.id)
     }
 }