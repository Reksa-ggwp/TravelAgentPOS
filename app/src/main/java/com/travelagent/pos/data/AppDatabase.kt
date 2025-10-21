package com.travelagent.pos.data

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Customer::class,
        Trip::class,
        Seat::class,
        Ticket::class,
        Driver::class,
        Vehicle::class,
        Payment::class,
        CustomerStats::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun customerDao(): CustomerDao
    abstract fun tripDao(): TripDao
    abstract fun seatDao(): SeatDao
    abstract fun ticketDao(): TicketDao
    abstract fun driverDao(): DriverDao
    abstract fun vehicleDao(): VehicleDao
    abstract fun paymentDao(): PaymentDao
    abstract fun customerStatsDao(): CustomerStatsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create drivers table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS drivers (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        namaSopir TEXT NOT NULL,
                        nomorTelepon TEXT NOT NULL,
                        createdDate INTEGER NOT NULL
                    )
                """.trimIndent())

                // Create vehicles table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS vehicles (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        nomorPolisi TEXT NOT NULL,
                        createdDate INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create payments table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS payments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        ticketId INTEGER NOT NULL,
                        amount REAL NOT NULL,
                        paymentMethod TEXT NOT NULL,
                        receiptNumber TEXT,
                        notes TEXT,
                        timestamp INTEGER NOT NULL,
                        FOREIGN KEY(ticketId) REFERENCES tickets(id) ON DELETE CASCADE
                    )
                """.trimIndent())

                db.execSQL("CREATE INDEX IF NOT EXISTS index_payments_ticketId ON payments(ticketId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_payments_timestamp ON payments(timestamp)")

                // Create customer_stats table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS customer_stats (
                        customerId INTEGER PRIMARY KEY NOT NULL,
                        totalTrips INTEGER NOT NULL DEFAULT 0,
                        totalSpent REAL NOT NULL DEFAULT 0,
                        lastTripDate INTEGER,
                        loyaltyPoints INTEGER NOT NULL DEFAULT 0,
                        tier TEXT NOT NULL DEFAULT 'Bronze'
                    )
                """.trimIndent())

                // Update tickets table to add totalPaid column if it doesn't exist
                db.execSQL("ALTER TABLE tickets ADD COLUMN totalPaid REAL NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "travel_agent_db"
                )
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                        // Only allow destructive migration in debug/dev builds to avoid accidental data loss in production
                                .apply {
                                    val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
                                    if (isDebuggable) {
                                        this.fallbackToDestructiveMigration()
                                    }
                                }
                        .build()
                INSTANCE = instance
                instance
            }
        }
    }
}