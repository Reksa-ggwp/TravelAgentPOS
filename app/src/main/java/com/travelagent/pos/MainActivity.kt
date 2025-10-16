// NEW MainActivity.kt - Replace entire file
package com.travelagent.pos

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import android.widget.ImageButton
import com.travelagent.pos.ui.*

class MainActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawerLayout)
        val navView = findViewById<NavigationView>(R.id.navView)
        val btnMenu = findViewById<ImageButton>(R.id.btnMenu)

        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(androidx.core.view.GravityCompat.START)
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_trips -> {
                    // Already on trips page
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
            }
            drawerLayout.closeDrawers()
            true
        }

        // Load TripListFragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, TripListFragment())
            .commit()
    }
}