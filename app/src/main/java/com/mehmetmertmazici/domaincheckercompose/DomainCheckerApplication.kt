package com.mehmetmertmazici.domaincheckercompose

import android.app.Application
import com.mehmetmertmazici.domaincheckercompose.data.ServiceLocator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DomainCheckerApplication : Application() {

    // Application scope coroutine
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        // ServiceLocator'ı initialize et
        ServiceLocator.initialize(this)

        // Kaydedilmiş session'ı restore et
        applicationScope.launch {
            try {
                ServiceLocator.authRepository.restoreSession()
            } catch (e: Exception) {
                // Session restore hatası görmezden gel
                e.printStackTrace()
            }
        }
    }
}