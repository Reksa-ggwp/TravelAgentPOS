package com.travelagent.pos.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.travelagent.pos.data.AppDatabase
import com.travelagent.pos.data.Trip
import androidx.lifecycle.lifecycleScope
import com.travelagent.pos.databinding.ActivityTripListBinding
import kotlinx.coroutines.launch

class TripListActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var adapter: TripAdapter
    private lateinit var binding: ActivityTripListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTripListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        adapter = TripAdapter(mutableListOf(), lifecycleScope) { trip ->
            val intent = Intent(this, TripDetailsActivity::class.java)
            intent.putExtra("tripId", trip.id)
            startActivity(intent)
        }

        binding.rvTrips.layoutManager = LinearLayoutManager(this)
        binding.rvTrips.adapter = adapter

        binding.btnAddTrip.setOnClickListener {
            startActivity(Intent(this, AddTripActivity::class.java))
        }

        binding.btnBack.setOnClickListener { finish() }

        loadTrips()
    }

    private fun loadTrips() {
        lifecycleScope.launch {
            val trips = db.tripDao().getAllTrips()
            adapter.updateList(trips.toMutableList())
        }
    }

    override fun onResume() {
        super.onResume()
        loadTrips()
    }
}

