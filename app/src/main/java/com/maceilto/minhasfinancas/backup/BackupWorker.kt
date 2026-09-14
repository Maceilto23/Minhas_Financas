package com.maceilto.minhasfinancas.backup

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class BackupWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        if (BackupManager.configuredFolder(applicationContext) == null) {
            return Result.success()
        }

        return if (BackupManager.backup(applicationContext).isSuccess) {
            Result.success()
        } else {
            Result.retry()
        }
    }
}
