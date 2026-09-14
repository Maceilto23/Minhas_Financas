package com.maceilto.minhasfinancas.backup

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.maceilto.minhasfinancas.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object BackupManager {
    private const val PREFS = "backup_prefs"
    private const val KEY_URI = "drive_folder_uri"
    private const val BACKUP_NAME = "Minhas_Financas_Backup.sqlite"

    fun saveFolder(context: Context, uri: Uri) {
        context.contentResolver.takePersistableUriPermission(
            uri,
            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_URI, uri.toString())
            .apply()
    }

    fun configuredFolder(context: Context): Uri? {
        val value = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_URI, null)
        return value?.let(Uri::parse)
    }

    suspend fun backup(context: Context): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val folderUri = configuredFolder(context)
                ?: error("Escolha primeiro uma pasta do Google Drive.")

            val db = AppDatabase.getInstance(context)
            db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { }

            val localDb = context.getDatabasePath(AppDatabase.DB_NAME)
            if (!localDb.exists()) error("Banco SQLite ainda não foi criado.")

            val folder = DocumentFile.fromTreeUri(context, folderUri)
                ?: error("Não foi possível abrir a pasta selecionada.")

            folder.findFile(BACKUP_NAME)?.delete()

            val target = folder.createFile("application/x-sqlite3", BACKUP_NAME)
                ?: error("Não foi possível criar o backup.")

            context.contentResolver.openOutputStream(target.uri, "w").use { out ->
                requireNotNull(out) { "Falha ao abrir arquivo de destino." }
                localDb.inputStream().use { input -> input.copyTo(out) }
            }

            "Backup SQLite concluído: $BACKUP_NAME"
        }
    }

    suspend fun restore(context: Context): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val folderUri = configuredFolder(context)
                ?: error("Escolha primeiro uma pasta do Google Drive.")

            val folder = DocumentFile.fromTreeUri(context, folderUri)
                ?: error("Não foi possível abrir a pasta selecionada.")

            val source = folder.findFile(BACKUP_NAME)
                ?: error("Backup $BACKUP_NAME não encontrado nessa pasta.")

            AppDatabase.closeInstance()

            val dbFile = context.getDatabasePath(AppDatabase.DB_NAME)
            dbFile.parentFile?.mkdirs()

            // remove WAL/SHM antigos antes da restauração
            File(dbFile.path + "-wal").delete()
            File(dbFile.path + "-shm").delete()

            context.contentResolver.openInputStream(source.uri).use { input ->
                requireNotNull(input) { "Falha ao abrir backup." }
                dbFile.outputStream().use { out -> input.copyTo(out) }
            }

            "Backup restaurado. Reabra o aplicativo."
        }
    }
}
