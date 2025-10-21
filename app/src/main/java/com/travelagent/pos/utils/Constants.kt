package com.travelagent.pos.utils

object Constants {
    // Database
    const val DATABASE_NAME = "travel_agent_db"
    const val DATABASE_VERSION = 3
    
    // Seat Configuration
    const val DEFAULT_SEAT_COUNT = 10
    const val SEAT_STATUS_AVAILABLE = "available"
    const val SEAT_STATUS_BOOKED = "booked"
    const val SEAT_STATUS_PAID = "paid"
    
    // Ticket Status
    const val TICKET_STATUS_PENDING = "pending"
    const val TICKET_STATUS_PAID = "paid"
    const val TICKET_STATUS_CANCELLED = "cancelled"
    
    // Payment Methods
    const val PAYMENT_CASH = "cash"
    const val PAYMENT_CARD = "card"
    const val PAYMENT_TRANSFER = "transfer"
    
    // Customer Tiers
    const val TIER_BRONZE = "Bronze"
    const val TIER_SILVER = "Silver"
    const val TIER_GOLD = "Gold"
    
    // Cache
    const val CACHE_DURATION_MS = 5 * 60 * 1000L // 5 minutes
    
    // File Export
    const val EXPORT_FILE_PREFIX = "travel_agent_export_"
    const val EXPORT_FILE_EXTENSION = ".csv"
    
    // Date Format
    const val DATE_FORMAT_DISPLAY = "dd/MM/yyyy"
    const val DATE_FORMAT_FILE = "yyyyMMdd_HHmmss"
}
