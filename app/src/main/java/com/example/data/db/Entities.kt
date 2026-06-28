package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "soil_scans")
data class SoilScan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val farmerName: String,
    val location: String,
    val ph: Double,
    val nitrogen: String, // e.g. "High", "Medium", "Low"
    val phosphorus: String,
    val potassium: String,
    val recommendations: String,
    val cropType: String
)

@Entity(tableName = "disaster_alerts")
data class DisasterAlert(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val alertType: String, // e.g. "Flood Alert", "Drought Warning", "Heat Wave"
    val message: String,
    val severity: String, // e.g. "CRITICAL", "WARNING", "INFO"
    val isAcknowledged: Boolean = false
)

@Entity(tableName = "scheme_applications")
data class SchemeApplication(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val schemeName: String, // e.g. "PM-KISAN", "Soil Health Card", "Lime Subsidy Scheme"
    val farmerCount: Int,
    val location: String,
    val estimatedBenefit: String,
    val status: String // e.g. "DRAFT", "SUBMITTED"
)
