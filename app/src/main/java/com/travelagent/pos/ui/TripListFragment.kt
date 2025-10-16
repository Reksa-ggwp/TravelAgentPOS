package com.travelagent.pos.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import com.travelagent.pos.R
import com.travelagent.pos.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TripListFragment : Fragment() {
    private lateinit var db: AppDatabase
    private lateinit var adapter: TripAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_trip_list, container, false)

        db = AppDatabase.getDatabase(requireContext())

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvTrips)
        val btnAdd = view.findViewById<Button>(R.id.btnAddTrip)

        adapter = TripAdapter(mutableListOf()) { trip ->
            val intent = Intent(requireContext(), TripDetailsActivity::class.java)
            intent.putExtra("tripId", trip.id)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        btnAdd.setOnClickListener {
            startActivity(Intent(requireContext(), AddTripActivity::class.java))
        }

        loadTrips()

        return view
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