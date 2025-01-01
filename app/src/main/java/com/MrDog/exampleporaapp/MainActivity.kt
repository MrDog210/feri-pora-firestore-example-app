package com.MrDog.exampleporaapp

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.MrDog.exampleporaapp.databinding.ActivityMainBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot


class MainActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: TaskAdapter
    private lateinit var taskList: ArrayList<TaskItem>
    private var taskListener: ListenerRegistration? = null

    private var taskInput: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()

        // Initialize UI Components
        val recyclerView = binding.recyclerView
        taskInput = binding.taskInput
        binding.addButton.setOnClickListener(this::addTask)


        // Setup RecyclerView
        taskList = ArrayList()
        adapter = TaskAdapter(taskList!!, object: TaskAdapter.TaskRemoveListener {
            override fun onRemove(taskId: String) {
                db.collection("tasks").document(taskId)
                    .delete()
                    .addOnCompleteListener { task: Task<Void?> ->
                        if (!task.isSuccessful) {
                            Toast.makeText(this@MainActivity, "Failed to delete task", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        })
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter


        // Listen for real-time updates
        taskListener = db.collection("tasks")
            .addSnapshotListener { snapshots: QuerySnapshot?, error: FirebaseFirestoreException? ->
                if (error != null) {
                    Toast.makeText(this@MainActivity, "Error fetching tasks", Toast.LENGTH_SHORT)
                        .show()
                    return@addSnapshotListener
                }
                taskList.clear()
                for (doc in snapshots!!) {
                    val task = doc.toObject(TaskItem::class.java)
                    task.id = doc.id
                    taskList.add(task)
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun addTask(view: View) {
        val taskText = taskInput!!.text.toString()
        if (TextUtils.isEmpty(taskText)) {
            Toast.makeText(this, "Task cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val task: MutableMap<String, Any> = HashMap()
        task["name"] = taskText

        db.collection("tasks")
            .add(task)
            .addOnCompleteListener { task: Task<DocumentReference?> ->
                if (task.isSuccessful) {
                    taskInput!!.setText("")
                } else {
                    Toast.makeText(this, "Failed to add task", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        taskListener?.remove()
    }
}