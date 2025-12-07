package com.sakethh.linkora.worker

import com.sakethh.linkora.data.remote.GitHubClient
import com.sakethh.linkora.di.DependencyContainer
import com.sakethh.linkora.preferences.AppPreferenceType
import com.sakethh.linkora.preferences.AppPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sakethh.linkora.domain.onSuccess
import com.sakethh.linkora.utils.linkoraLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

object DesktopExportScheduler {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var isScheduled = false

    fun startScheduler() {
        if (isScheduled) return
        isScheduled = true
        scope.launch {
            while (true) {
                if (AppPreferences.isAutoBackupEnabled.value) {
                    val lastSynced = 0L // TODO: Store last backup time to respect interval properly
                    // For simplicity, we just try to backup every hour if enabled, 
                    // or we could implement a more robust check against the INTERVAL preference.
                    // Given the request for "easiest", a simple periodic check is fine.
                    
                    performBackup()
                }
                // Check every hour
                delay(60 * 60 * 1000L)
            }
        }
    }

    private suspend fun performBackup() {
        val token = AppPreferences.gitHubToken.value
        if (token.isBlank()) return

        try {
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
                            linkoraLog("GitHub Backup (Desktop): Updated Gist $existingGistId")
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
                            linkoraLog("GitHub Backup (Desktop): Created Gist ${response.id}")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
