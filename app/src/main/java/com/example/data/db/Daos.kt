package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SoilScanDao {
    @Query("SELECT * FROM soil_scans ORDER BY timestamp DESC")
    fun getAllSoilScans(): Flow<List<SoilScan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSoilScan(scan: SoilScan)

    @Query("DELETE FROM soil_scans WHERE id = :id")
    suspend fun deleteSoilScanById(id: Int)
}

@Dao
interface DisasterAlertDao {
    @Query("SELECT * FROM disaster_alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<DisasterAlert>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: DisasterAlert)

    @Update
    suspend fun updateAlert(alert: DisasterAlert)

    @Query("UPDATE disaster_alerts SET isAcknowledged = 1 WHERE id = :id")
    suspend fun acknowledgeAlert(id: Int)
}

@Dao
interface SchemeApplicationDao {
    @Query("SELECT * FROM scheme_applications ORDER BY timestamp DESC")
    fun getAllApplications(): Flow<List<SchemeApplication>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplication(app: SchemeApplication)

    @Update
    suspend fun updateApplication(app: SchemeApplication)

    @Query("DELETE FROM scheme_applications WHERE id = :id")
    suspend fun deleteApplication(id: Int)
}
