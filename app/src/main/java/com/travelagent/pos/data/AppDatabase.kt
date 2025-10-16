// AppDatabase.kt - COMPLETE UPDATED VERSION
// Location: app/src/main/java/com/travelagent/pos/data/AppDatabase.kt
package com.travelagent.pos.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Customer::class,
        Trip::class,
        Seat::class,
        Ticket::class,
        Driver::class,    // NEW
        Vehicle::class    // NEW
    ],
    version = 2,  // CHANGED from 1 to 2
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // Original DAOs
    abstract fun customerDao(): CustomerDao
    abstract fun tripDao(): TripDao
    abstract fun seatDao(): SeatDao
    abstract fun ticketDao(): TicketDao

    // NEW DAOs
    abstract fun driverDao(): DriverDao
    abstract fun vehicleDao(): VehicleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "travel_agent_db"
                )
                    .fallbackToDestructiveMigration()  // IMPORTANT: Add this line
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}