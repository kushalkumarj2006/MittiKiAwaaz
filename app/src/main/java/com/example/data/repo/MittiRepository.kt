package com.example.data.repo

import com.example.data.db.*
import com.example.data.network.GeminiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

class MittiRepository(
    private val soilScanDao: SoilScanDao,
    private val disasterAlertDao: DisasterAlertDao,
    private val schemeApplicationDao: SchemeApplicationDao,
    private val geminiService: GeminiService
) {
    val allSoilScans: Flow<List<SoilScan>> = soilScanDao.getAllSoilScans()
    val allAlerts: Flow<List<DisasterAlert>> = disasterAlertDao.getAllAlerts()
    val allApplications: Flow<List<SchemeApplication>> = schemeApplicationDao.getAllApplications()

    suspend fun insertSoilScan(scan: SoilScan) {
        soilScanDao.insertSoilScan(scan)
    }

    suspend fun deleteSoilScan(id: Int) {
        soilScanDao.deleteSoilScanById(id)
    }

    suspend fun insertAlert(alert: DisasterAlert) {
        disasterAlertDao.insertAlert(alert)
    }

    suspend fun updateAlert(alert: DisasterAlert) {
        disasterAlertDao.updateAlert(alert)
    }

    suspend fun acknowledgeAlert(id: Int) {
        disasterAlertDao.acknowledgeAlert(id)
    }

    suspend fun insertApplication(app: SchemeApplication) {
        schemeApplicationDao.insertApplication(app)
    }

    suspend fun updateApplication(app: SchemeApplication) {
        schemeApplicationDao.updateApplication(app)
    }

    suspend fun deleteApplication(id: Int) {
        schemeApplicationDao.deleteApplication(id)
    }

    suspend fun askKrishiSakhi(query: String): String {
        return geminiService.generateContent(query)
    }

    suspend fun populateDemoDataIfNeeded() {
        // Only populate if empty
        val currentScans = allSoilScans.first()
        if (currentScans.isEmpty()) {
            // 1. Initial Soil Scans
            soilScanDao.insertSoilScan(
                SoilScan(
                    farmerName = "Ramesh Kumar",
                    location = "Ramnagar Village, Ward 4",
                    ph = 5.8,
                    nitrogen = "Low",
                    phosphorus = "Medium",
                    potassium = "High",
                    recommendations = "Apply 2 kg of lime per bigha. Use organic manure.",
                    cropType = "Groundnut"
                )
            )
            soilScanDao.insertSoilScan(
                SoilScan(
                    farmerName = "Sita Devi",
                    location = "Ramnagar Village, Ward 1",
                    ph = 6.2,
                    nitrogen = "Medium",
                    phosphorus = "Low",
                    potassium = "Medium",
                    recommendations = "Add bone meal fertilizer for phosphorus boost. Best for mustard.",
                    cropType = "Mustard"
                )
            )

            // 2. Disaster Alerts
            disasterAlertDao.insertAlert(
                DisasterAlert(
                    alertType = "Flood Alert",
                    message = "IMD alerts heavy rainfall exceeding 120mm in next 48 hours. Flood warning issued for low-lying areas of River Gandak. Secure livestock and relocate pumps.",
                    severity = "CRITICAL",
                    isAcknowledged = false
                )
            )
            disasterAlertDao.insertAlert(
                DisasterAlert(
                    alertType = "Heat Wave Warning",
                    message = "Temperatures likely to reach 45°C this week. Ensure frequent light irrigation for standing vegetable crops in evening hours.",
                    severity = "WARNING",
                    isAcknowledged = false
                )
            )

            // 3. Scheme Applications for Sarpanch
            schemeApplicationDao.insertApplication(
                SchemeApplication(
                    schemeName = "PM-KISAN Nidhi Yojna",
                    farmerCount = 42,
                    location = "Ramnagar Village",
                    estimatedBenefit = "₹84,000",
                    status = "SUBMITTED"
                )
            )
            schemeApplicationDao.insertApplication(
                SchemeApplication(
                    schemeName = "Agricultural Lime Subsidy Scheme",
                    farmerCount = 12,
                    location = "Ramnagar Village (Acidic Soil Hotspot)",
                    estimatedBenefit = "₹2,40,000",
                    status = "DRAFT"
                )
            )
        }
    }
}
