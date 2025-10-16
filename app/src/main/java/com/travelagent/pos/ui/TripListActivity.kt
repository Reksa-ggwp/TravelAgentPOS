package com.travelagent.pos.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import android.widget.ImageButton
import com.travelagent.pos.R
import com.travelagent.pos.data.AppDatabase
import com.travelagent.pos.data.Trip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TripListActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var adapter: TripAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_list)

        db = AppDatabase.getDatabase(this)

        val recyclerView = findViewById<RecyclerView>(R.id.rvTrips)
        val btnAdd = findViewById<Button>(R.id.btnAddTrip)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        adapter = TripAdapter(mutableListOf()) { trip ->
            val intent = Intent(this, TripDetailsActivity::class.java)
            intent.putExtra("tripId", trip.id)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnAdd.setOnClickListener {
            startActivity(Intent(this, AddTripActivity::class.java))
        }

        btnBack.setOnClickListener { finish() }

        loadTrips()
    }

    private fun loadTrips() {
        GlobalScope.launch(Dispatchers.Main) {
            val trips = db.tripDao().getAllTrips()
            adapter.updateList(trips.toMutableList())
        }
    }

    override fun onResume() {
        super.onResume()
        loadTrips()
    }
}

