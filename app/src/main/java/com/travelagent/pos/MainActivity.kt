package com.travelagent.pos

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.travelagent.pos.databinding.ActivityMainBinding
import com.travelagent.pos.ui.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupNavigationDrawer()

        // Load initial fragment
        if (savedInstanceState == null) {
            loadTripListFragment()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun setupNavigationDrawer() {
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_trips -> {
                    loadTripListFragment()
                    binding.toolbar.title = "Data Perjalanan"
                }
                R.id.nav_customers -> {
                    startActivity(Intent(this, CustomerListActivity::class.java))
                }
                R.id.nav_drivers -> {
                    startActivity(Intent(this, DriverVehicleActivity::class.java))
                }
                R.id.nav_booking -> {
                    startActivity(Intent(this, BookingActivity::class.java))
                }
                R.id.nav_payments -> {
                    startActivity(Intent(this, PaymentHistoryActivity::class.java))
                }
                R.id.nav_printer_settings -> {
                    // FIXED: Now properly launches Printer Settings
                    startActivity(Intent(this, PrinterSettingsActivity::class.java))
                }
                R.id.nav_export -> {
                    startActivity(Intent(this, ExportActivity::class.java))
                }
                R.id.nav_backup -> {
                    startActivity(Intent(this, BackupRestoreActivity::class.java))
                }
                R.id.nav_reports -> {
                    startActivity(Intent(this, ReportsActivity::class.java))
                }
                R.id.nav_customer_stats -> {
                    startActivity(Intent(this, CustomerStatsActivity::class.java))
                }
            }
            binding.drawerLayout.closeDrawers()
            true
        }
    }

    private fun loadTripListFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, TripListFragment())
            .commit()
    }

    override fun onResume() {
        super.onResume()
        // Refresh fragment if needed
        loadTripListFragment()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}