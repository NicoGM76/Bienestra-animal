package com.bienestaranimal.zipaquira.admin

interface AdminGateway {
    fun loadDashboard(callback: (AdminOutcome) -> Unit)
}
