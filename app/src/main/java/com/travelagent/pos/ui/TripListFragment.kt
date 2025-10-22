package com.travelagent.pos.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.travelagent.pos.data.AppDatabase
import com.travelagent.pos.databinding.FragmentTripListBinding
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class TripListFragment : Fragment() {
    private var _binding: FragmentTripListBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    private lateinit var adapter: TripAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTripListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = AppDatabase.getDatabase(requireContext())
        setupRecyclerView()
        setupFab()
        loadTrips()
    }

    private fun setupRecyclerView() {
        adapter = TripAdapter(mutableListOf(), viewLifecycleOwner.lifecycleScope) { trip ->
            val intent = Intent(requireContext(), TripDetailsActivity::class.java)
            intent.putExtra("tripId", trip.id)
            startActivity(intent)
        }

        binding.rvTrips.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@TripListFragment.adapter
            setHasFixedSize(true)
        }
    }

    private fun setupFab() {
        binding.btnAddTrip.setOnClickListener {
            startActivity(Intent(requireContext(), AddTripActivity::class.java))
        }
    }

    private fun loadTrips() {
        viewLifecycleOwner.lifecycleScope.launch {
            val trips = db.tripDao().getAllTrips()

            if (trips.isEmpty()) {
                showEmptyState()
            } else {
                hideEmptyState()
                adapter.updateList(trips.toMutableList())
                updateSummary(trips.size)
            }
        }
    }

    private fun updateSummary(totalTrips: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            binding.tvTotalTrips.text = totalTrips.toString()

            // Calculate total available seats
            val trips = db.tripDao().getAllTrips()
            var totalAvailable = 0

            trips.forEach { trip ->
                val seats = db.seatDao().getSeatsByTrip(trip.id)
                totalAvailable += seats.count { it.status == "available" }
            }

            binding.tvAvailableSeats.text = totalAvailable.toString()
        }
    }

    private fun showEmptyState() {
        binding.rvTrips.visibility = View.GONE
        binding.emptyState.visibility = View.VISIBLE
        binding.tvTotalTrips.text = "0"
        binding.tvAvailableSeats.text = "0"

        // Setup empty state button
        binding.btnEmptyAction.setOnClickListener {
            startActivity(Intent(requireContext(), AddTripActivity::class.java))
        }
    }

    private fun hideEmptyState() {
        binding.rvTrips.visibility = View.VISIBLE
        binding.emptyState.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        loadTrips()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}