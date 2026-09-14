package com.maceilto.minhasfinancas

import android.app.Application
import androidx.work.*
import com.maceilto.minhasfinancas.backup.BackupWorker
import com.maceilto.minhasfinancas.data.AppDatabase
import java.util.concurrent.TimeUnit

class FinanceApp : Application() {
    val database by lazy { AppDatabase.getInstance(this) }

    override fun onCreate() {
        super.onCreate()

        val request = PeriodicWorkRequestBuilder<BackupWorker>(12, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "drive_sqlite_backup",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
