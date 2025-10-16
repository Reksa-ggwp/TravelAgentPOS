package com.travelagent.pos.ui

import com.travelagent.pos.data.*

// UPDATE AppDatabase.kt - Add new entities and DAOs
@androidx.room.Database(
    entities = [Customer::class, Trip::class, Seat::class, Ticket::class, Driver::class, Vehicle::class],
    version = 2  // IMPORTANT: Changed from 1 to 2
)
abstract class AppDatabase : androidx.room.RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun tripDao(): TripDao
    abstract fun seatDao(): SeatDao
    abstract fun ticketDao(): TicketDao
    abstract fun driverDao(): DriverDao  // NEW
    abstract fun vehicleDao(): VehicleDao  // NEW

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "travel_agent_db"
                )
                    .fallbackToDestructiveMigration()  // ADD THIS LINE
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}