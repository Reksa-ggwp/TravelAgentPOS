package com.travelagent.pos
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.travelagent.pos.databinding.ActivityMainBinding
import com.travelagent.pos.ui.*
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(androidx.core.view.GravityCompat.START)
        }
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_trips -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, TripListFragment())
                        .commit()
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
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, TripListFragment())
            .commit()
    }
    override fun onResume() {
        super.onResume()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, TripListFragment())
            .commit()
    }
}
