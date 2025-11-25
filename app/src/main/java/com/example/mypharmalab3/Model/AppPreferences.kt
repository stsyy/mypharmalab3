package com.example.mypharmalab3.Model
import java.io.Serializable
import java.util.Date

data class AppPreferences(
    val lastSyncDate: Date,
    val isDarkModeEnabled: Boolean,
    val defaultReminderDays: Int = 3
) : Serializable