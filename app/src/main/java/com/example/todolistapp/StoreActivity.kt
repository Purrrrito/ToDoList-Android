package com.example.todolistapp

import android.content.Context
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
            pointsTextView.text = getString(R.string.points_label, points)
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
            StoreItem("Pastel Pink", Color.parseColor("#FFB6C1"), purchasedItems.contains("Pastel Pink"), selectedItem == "Pastel Pink", 40),
            StoreItem("Light Sky Blue", Color.parseColor("#87CEFA"), purchasedItems.contains("Light Sky Blue"), selectedItem == "Light Sky Blue", 50),
            StoreItem("Mint Green", Color.parseColor("#98FF98"), purchasedItems.contains("Mint Green"), selectedItem == "Mint Green", 60),
            StoreItem("Lavender", Color.parseColor("#E6E6FA"), purchasedItems.contains("Lavender"), selectedItem == "Lavender", 70),
            StoreItem("Peach", Color.parseColor("#FFDAB9"), purchasedItems.contains("Peach"), selectedItem == "Peach", 80),
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
        points = sharedPreferences.getInt("points", 0)
        findViewById<TextView>(R.id.textView_points).text = getString(R.string.points_label, points)

        val storePreferences = getSharedPreferences("store", Context.MODE_PRIVATE)
        val selectedItem = storePreferences.getString("selectedItem", null)
        val selectedColor = when (selectedItem) {
            "Pastel Pink" -> Color.parseColor("#FFB6C1")
            "Light Sky Blue" -> Color.parseColor("#87CEFA")
            "Mint Green" -> Color.parseColor("#98FF98")
            "Lavender" -> Color.parseColor("#E6E6FA")
            "Peach" -> Color.parseColor("#FFDAB9")
            else -> Color.WHITE
        }
        findViewById<View>(R.id.main).setBackgroundColor(selectedColor)
    }
}

data class StoreItem(
    val colorName: String,
    val colorCode: Int,
    var purchased: Boolean,
    var selected: Boolean,
    val price: Int
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
        val itemPrice = view.findViewById<TextView>(R.id.item_price)
        val itemButton = view.findViewById<Button>(R.id.item_button)

        itemName.text = item?.colorName
        itemPrice.text = context.getString(R.string.price_label, item?.price)
        updateButtonState(item, itemButton)

        itemButton.setOnClickListener {
            item?.let {
                if (!it.purchased) {
                    purchaseItem(it)
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

    private fun purchaseItem(item: StoreItem) {
        val sharedPreferences = context.getSharedPreferences("tasks", Context.MODE_PRIVATE)
        val currentPoints = sharedPreferences.getInt("points", 0)

        if (currentPoints >= item.price) { // Example cost
            item.purchased = true
            notifyDataSetChanged()

            val updatedPoints = currentPoints - item.price
            sharedPreferences.edit().putInt("points", updatedPoints).apply()

            (context as? StoreActivity)?.apply {
                points = updatedPoints
                saveStoreData()
                onPointsUpdated(updatedPoints)
            }
        } else {
            AlertDialog.Builder(context)
                .setTitle("Insufficient Points")
                .setMessage("You need more points to purchase this item.")
                .setPositiveButton("OK", null)
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
