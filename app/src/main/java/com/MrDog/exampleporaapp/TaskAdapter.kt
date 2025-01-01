package com.MrDog.exampleporaapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.MrDog.exampleporaapp.databinding.ItemTaskBinding

class TaskAdapter (private val tasks: ArrayList<TaskItem>, private val taskRemoveListener: TaskRemoveListener): RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

    interface TaskRemoveListener {
        fun onRemove(taskId: String)
    }

    class ViewHolder(
        private val binding: ItemTaskBinding,
        private val taskRemoveListener: TaskRemoveListener
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: TaskItem, position: Int) {
            binding.taskName.text = task.name

            binding.deleteButton.setOnClickListener {
                taskRemoveListener.onRemove(task.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, taskRemoveListener)
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task, position)
    }
}