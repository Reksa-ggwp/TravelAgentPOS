package com.travelagent.pos.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.travelagent.pos.R
import com.travelagent.pos.data.AppDatabase
import com.travelagent.pos.databinding.FragmentTripListBinding
import com.travelagent.pos.repository.TripRepository
import com.travelagent.pos.viewmodel.TripViewModel
import com.travelagent.pos.viewmodel.TripViewModelFactory

class TripListFragment : Fragment() {
    private var _binding: FragmentTripListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TripAdapter

    private val viewModel: TripViewModel by viewModels {
        val db = AppDatabase.getDatabase(requireContext())
        TripViewModelFactory(
            TripRepository(db.tripDao(), db.seatDao(), db.ticketDao())
        )
    }

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

        setupRecyclerView()
        setupSwipeRefresh()
        setupFab()
        observeViewModel()

        // Initial load
        viewModel.loadTrips()
    }

    private fun setupRecyclerView() {
        adapter = TripAdapter(
            trips = mutableListOf(),
            scope = viewLifecycleOwner.lifecycleScope,
            onItemClick = { trip ->
                val intent = Intent(requireContext(), TripDetailsActivity::class.java)
                intent.putExtra("tripId", trip.id)
                startActivity(intent)
            }
        )

        binding.rvTrips.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@TripListFragment.adapter
            setHasFixedSize(true)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.apply {
            setColorSchemeResources(
                R.color.info,
                R.color.success,
                R.color.warning
            )

            setOnRefreshListener {
                viewModel.loadTrips(forceRefresh = true)
            }
        }
    }

    private fun setupFab() {
        binding.btnAddTrip.setOnClickListener {
            startActivity(Intent(requireContext(), AddTripActivity::class.java))
        }
    }

    private fun observeViewModel() {
        // Observe trips data
        viewModel.trips.observe(viewLifecycleOwner) { trips ->
            if (trips.isEmpty()) {
                showEmptyState()
            } else {
                hideEmptyState()
                adapter.updateList(trips.toMutableList())
                updateSummary(trips)
            }
        }

        // Observe loading state - THIS IS THE FIX
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
        }

        // Observe errors
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                android.widget.Toast.makeText(
                    requireContext(),
                    it,
                    android.widget.Toast.LENGTH_LONG
                ).show()
                viewModel.clearError()
            }
        }
    }

    private fun updateSummary(trips: List<com.travelagent.pos.data.Trip>) {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            binding.tvTotalTrips.text = trips.size.toString()

            // Calculate total available seats
            val db = AppDatabase.getDatabase(requireContext())
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
        viewModel.loadTrips(forceRefresh = true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}