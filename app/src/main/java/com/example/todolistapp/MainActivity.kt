package com.example.todolistapp

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private lateinit var taskList: ListView
    private val tasks = mutableListOf<Task>()
    private lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val addButton = findViewById<Button>(R.id.button_add)
        val editText = findViewById<EditText>(R.id.editText_add_task)
        taskList = findViewById(R.id.taskList)
        adapter = TaskAdapter(this, tasks)
        taskList.adapter = adapter

        loadTasks()

        addButton.setOnClickListener {
            addTask(editText)
        }
    }

    private fun addTask(editText: EditText) {
        val taskText = editText.text.toString()

        if (taskText.isNotEmpty()) {
            tasks.add(Task(taskText, false))
            adapter.notifyDataSetChanged()
            saveTasks()
            editText.text.clear()
        }
    }

    fun saveTasks() {
        val sharedPreferences = getSharedPreferences("tasks", Context.MODE_PRIVATE) // Use named preferences
        val editor = sharedPreferences.edit()
        editor.putStringSet("tasks", tasks.map { "${it.text},${it.completed}" }.toSet())
        editor.apply()
    }

    private fun loadTasks() {
        val sharedPreferences = getSharedPreferences("tasks", Context.MODE_PRIVATE) // Use same named preferences
        val savedTasks = sharedPreferences.getStringSet("tasks", setOf()) ?: setOf()
        tasks.clear()
        savedTasks.forEach {
            val taskData = it.split(",")
            if (taskData.size == 2) {
                tasks.add(Task(taskData[0], taskData[1].toBoolean()))
            }
        }
        adapter.notifyDataSetChanged()
    }

}



data class Task(val text: String, var completed: Boolean)

class TaskAdapter(context: Context, private val tasks: MutableList<Task>) :
    ArrayAdapter<Task>(context, 0, tasks) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val task = getItem(position)
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.task_item, parent, false)

        val taskText = view.findViewById<TextView>(R.id.task_text)
        val taskButton = view.findViewById<Button>(R.id.task_button)

        taskText.text = task?.text

        if (task?.completed == true) {
            taskButton.text = context.getString(R.string.delete)
            taskButton.setBackgroundColor(Color.RED)
        }
        else {
            taskButton.text = context.getString(R.string.complete)
            taskButton.setBackgroundColor(Color.GREEN)
        }

        taskButton.setOnClickListener {
            if (task?.completed == false) {
                showCompletedConfirmationDialog(task, taskButton)
            }
            else {
                tasks.remove(task)
                notifyDataSetChanged()
                (context as? MainActivity)?.saveTasks()
            }
        }
        return view
    }

    private fun showCompletedConfirmationDialog(task: Task, taskButton: Button) {
        // Build the AlertDialog
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Confirmation")
        builder.setMessage("Have you completed: ${task.text}")

        builder.setPositiveButton("Yes") { dialog, _ ->
            task.let {
                it.completed = true
                taskButton.text = context.getString(R.string.delete)
                notifyDataSetChanged()
                (context as? MainActivity)?.saveTasks()
                dialog.dismiss()
            }
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()

    }

}
