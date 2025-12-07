package com.sakethh.linkora.worker

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sakethh.linkora.data.remote.GitHubClient
import com.sakethh.linkora.di.DependencyContainer
import com.sakethh.linkora.preferences.AppPreferenceType
import com.sakethh.linkora.preferences.AppPreferences
import com.sakethh.linkora.domain.onSuccess
import com.sakethh.linkora.ui.utils.linkoraLog
import kotlinx.coroutines.flow.collectLatest

class GitHubExportWorker(appContext: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        if (!AppPreferences.isAutoBackupEnabled.value) {
            return Result.success()
        }

        val token = AppPreferences.gitHubToken.value
        if (token.isBlank()) {
            return Result.failure()
        }

        return try {
            var result = Result.failure()
            DependencyContainer.exportDataRepo.rawExportDataAsJSON().collectLatest { exportResult ->
                exportResult.onSuccess { success ->
                    try {
                        val existingGistId = AppPreferences.gitHubGistId.value
                        if (existingGistId.isNotBlank()) {
                            DependencyContainer.gitHubClient.updateGist(
                                token = token,
                                gistId = existingGistId,
                                filename = "linkora_backup.json",
                                content = success.data
                            )
                            linkoraLog("GitHub Backup: Updated Gist $existingGistId")
                        } else {
                            val response = DependencyContainer.gitHubClient.createGist(
                                token = token,
                                description = "Linkora Backup",
                                filename = "linkora_backup.json",
                                content = success.data
                            )
                            DependencyContainer.preferencesRepo.changePreferenceValue(
                                preferenceKey = stringPreferencesKey(AppPreferenceType.GITHUB_GIST_ID.name),
                                newValue = response.id
                            )
                            AppPreferences.gitHubGistId.value = response.id
                            linkoraLog("GitHub Backup: Created Gist ${response.id}")
                        }
                        result = Result.success()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        linkoraLog("GitHub Backup Failed: ${e.message}")
                        result = Result.retry()
                    }
                }
            }
            result
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
