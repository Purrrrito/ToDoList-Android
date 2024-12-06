package com.example.todolistapp

import android.content.Context
import android.content.Intent
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
import org.w3c.dom.Text

class MainActivity : AppCompatActivity() {
    private lateinit var taskList: ListView
    private val tasks = mutableListOf<Task>()
    private lateinit var adapter: TaskAdapter
    var points: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize UI elements
        val addButton = findViewById<Button>(R.id.button_add)
        val editText = findViewById<EditText>(R.id.editText_add_task)
        val storeButton = findViewById<Button>(R.id.button_store)
        val pointsTextView: TextView = findViewById(R.id.textView_points)
        taskList = findViewById(R.id.taskList)

        // Set up the adapter for the task list
        adapter = TaskAdapter(this, tasks) { updatedPoints ->
            points = updatedPoints
            pointsTextView.text = "Points: $points"
            saveTasks()
        }
        taskList.adapter = adapter

        // Loads the saved tasks
        loadTasks()

        // On click listener to open Store page
        storeButton.setOnClickListener {
            val intent = Intent(this, StoreActivity::class.java)
            startActivity(intent)
        }

        // On click listener for Add Task
        addButton.setOnClickListener { addTask(editText) }
    }

    override fun onResume() {
        super.onResume()
        val sharedPreferences = getSharedPreferences("tasks", Context.MODE_PRIVATE)
        points =  sharedPreferences.getInt("points", 0)
        findViewById<TextView>(R.id.textView_points).text = "Points: $points"
    }

    /**
     * Adds the new task to the list.
     * Clears the input field after adding.
     */
    private fun addTask(editText: EditText) {
        val taskText = editText.text.toString()
        if (taskText.isNotEmpty()) {
            tasks.add(Task(taskText, false))
            adapter.notifyDataSetChanged()
            saveTasks()
            editText.text.clear()
        }
    }

    /**
     * Saves the tasks and the points to shared preferences.
     */
    fun saveTasks() {
        val sharedPreferences = getSharedPreferences("tasks", Context.MODE_PRIVATE) // Use named preferences
        val editor = sharedPreferences.edit()

        val taskSet = tasks.map { "${it.text},${it.completed}" }.toSet()
        editor.putStringSet("tasks", taskSet)
        editor.putInt("points", points)
        editor.apply()
    }

    /**
     * Loads tasks and points from shared preferences.
     */
    private fun loadTasks() {
        val sharedPreferences = getSharedPreferences("tasks", Context.MODE_PRIVATE)
        val savedTasks = sharedPreferences.getStringSet("tasks", emptySet()) ?: emptySet()
        points = sharedPreferences.getInt("points", 0)

        tasks.clear()
        savedTasks.forEach { taskString ->
            val taskData = taskString.split(",")
            if (taskData.size == 2) {
                tasks.add(Task(taskData[0], taskData[1].toBoolean()))
            }
        }

        findViewById<TextView>(R.id.textView_points).text = "Points: $points"
        adapter.notifyDataSetChanged()
    }

}


/**
 * Data class that represents a task
 * @property text The name of the task.
 * @property completed Whether the task is completed or not.
 */
data class Task(val text: String, var completed: Boolean)


/**
 * Customer adapter for managing the tasks in the ListView.
 */
class TaskAdapter(
    context: Context,
    private val tasks: MutableList<Task>,
    private val onPointsUpdated: (Int) -> Unit
) : ArrayAdapter<Task>(context, 0, tasks) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val task = getItem(position)
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.task_item, parent, false)

        val taskText = view.findViewById<TextView>(R.id.task_text)
        val taskButton = view.findViewById<Button>(R.id.task_button)

        taskText.text = task?.text
        updateTaskButton(task, taskButton)

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

    /**
     * Updates the appearance of the task button based on its state.
     */
    private fun updateTaskButton(task: Task?, button: Button) {
        if (task?.completed == true) {
            button.text = context.getString(R.string.delete)
            button.setBackgroundColor(Color.RED)
        }
        else {
            button.text = context.getString(R.string.complete)
            button.setBackgroundColor(Color.GREEN)
        }
    }

    /**
     * Displays a confirmation dialog when marking a task as completed.
     */
    private fun showCompletedConfirmationDialog(task: Task, taskButton: Button) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Confirmation")
        builder.setMessage("Have you completed: ${task.text}")

        builder.setPositiveButton("Yes") { dialog, _ ->
            task.let {
                it.completed = true
                taskButton.text = context.getString(R.string.delete)
                notifyDataSetChanged()
                onPointsUpdated((context as MainActivity).points + 10)
                dialog.dismiss()
            }
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()

    }

}
