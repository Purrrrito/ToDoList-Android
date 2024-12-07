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
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class StoreActivity : AppCompatActivity() {
    private lateinit var storeList: ListView
    private val storeItems = mutableListOf<StoreItem>()
    private lateinit var adapter: StoreItemAdapter
    var points: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_store)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val pointsTextView = findViewById<TextView>(R.id.textView_points)
        val backButton = findViewById<Button>(R.id.button_back)
        storeList = findViewById(R.id.storeList)

        // Load points and store items
        loadPoints()
        loadStoreItems()

        // Set up adapter
        adapter = StoreItemAdapter(this, storeItems) { updatedPoints ->
            points = updatedPoints
            pointsTextView.text = "Points: $points"
            saveStoreData()
        }
        storeList.adapter = adapter

        // On click listener for Back button
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun loadPoints() {
        val sharedPreferences = getSharedPreferences("tasks", Context.MODE_PRIVATE)
        points = sharedPreferences.getInt("points", 0)
    }

    private fun loadStoreItems() {
        val sharedPreferences = getSharedPreferences("store", Context.MODE_PRIVATE)
        val purchasedItems = sharedPreferences.getStringSet("purchasedItems", emptySet()) ?: emptySet()
        val selectedItem = sharedPreferences.getString("selectedItem", null)

        storeItems.clear()
        val colors = listOf(
            StoreItem("Red", Color.RED, purchasedItems.contains("Red"), selectedItem == "Red"),
            StoreItem("Blue", Color.BLUE, purchasedItems.contains("Blue"), selectedItem == "Blue"),
            StoreItem("Green", Color.GREEN, purchasedItems.contains("Green"), selectedItem == "Green")
        )
        storeItems.addAll(colors)
    }

    fun saveStoreData() {
        val sharedPreferences = getSharedPreferences("store", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val purchasedItems = storeItems.filter { it.purchased }.map { it.colorName }.toSet()
        val selectedItem = storeItems.find { it.selected }?.colorName

        editor.putStringSet("purchasedItems", purchasedItems)
        editor.putString("selectedItem", selectedItem)
        editor.putInt("points", points)
        editor.apply()
    }

    // Loads points to match Main and Store
    override fun onResume() {
        super.onResume()
        val sharedPreferences = getSharedPreferences("tasks", Context.MODE_PRIVATE)
        val points = sharedPreferences.getInt("points", 0)
        findViewById<TextView>(R.id.textView_points).text = "Points: $points"

        val storePreferences = getSharedPreferences("store", Context.MODE_PRIVATE)
        val selectedItem = storePreferences.getString("selectedItem", null)
        val selectedColor = when (selectedItem) {
            "Red" -> Color.RED
            "Blue" -> Color.BLUE
            "Green" -> Color.GREEN
            else -> Color.WHITE
        }
        findViewById<View>(R.id.main).setBackgroundColor(selectedColor)
    }
}

data class StoreItem(
    val colorName: String,   // Name of the color (e.g., "Red")
    val colorCode: Int,     // Color value (e.g., Color.RED)
    var purchased: Boolean, // If the item has been purchased
    var selected: Boolean   // If the item is currently selected as background
)

class StoreItemAdapter(
    context: Context,
    private val items: MutableList<StoreItem>,
    private val onPointsUpdated: (Int) -> Unit
) : ArrayAdapter<StoreItem>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = getItem(position)
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.storeitem, parent, false)

        val itemName = view.findViewById<TextView>(R.id.item_name)
        val itemButton = view.findViewById<Button>(R.id.item_button)

        itemName.text = item?.colorName
        updateButtonState(item, itemButton)

        itemButton.setOnClickListener {
            item?.let {
                if (!it.purchased) {
                    purchaseItem(it, itemButton)
                } else {
                    selectItem(it)
                }
            }
        }
        return view
    }

    private fun updateButtonState(item: StoreItem?, button: Button) {
        when {
            item?.selected == true -> {
                button.text = context.getString(R.string.selected)
                button.setBackgroundColor(Color.GRAY)
            }
            item?.purchased == true -> {
                button.text = context.getString(R.string.select)
                button.setBackgroundColor(Color.DKGRAY)
            }
            else -> {
                button.text = context.getString(R.string.purchase)
                button.setBackgroundColor(Color.BLACK)
            }
        }
    }

    private fun purchaseItem(item: StoreItem, button: Button) {
        val activity = context as StoreActivity
        if (activity.points >= 50) { // Example cost
            item.purchased = true
            activity.points -= 50
            onPointsUpdated(activity.points)
            notifyDataSetChanged()
        } else {
            AlertDialog.Builder(context)
                .setTitle("Insufficient Points")
                .setMessage("You don't have enough points to purchase ${item.colorName}.")
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    private fun selectItem(item: StoreItem) {
        items.forEach { it.selected = false }
        item.selected = true
        notifyDataSetChanged()
        val sharedPreferences = context.getSharedPreferences("store", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("selectedItem", item.colorName).apply()

        (context as? StoreActivity)?.findViewById<View>(R.id.main)?.setBackgroundColor(item.colorCode)
    }
}
