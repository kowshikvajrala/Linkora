package com.sakethh.linkora.platform

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.sakethh.linkora.Localization
import com.sakethh.linkora.R
import com.sakethh.linkora.domain.repository.local.LocalLinksRepo
import com.sakethh.linkora.domain.repository.local.PreferencesRepository
import com.sakethh.linkora.preferences.AppPreferenceType
import com.sakethh.linkora.preferences.AppPreferences
import com.sakethh.linkora.utils.getLocalizedString
import com.sakethh.linkora.worker.RefreshAllLinksWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import java.util.UUID

actual class NativeUtils(private val context: Context) {
    actual fun onShare(url: String) {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, url)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(intent, null)
        shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(shareIntent)
    }

    actual suspend fun onRefreshAllLinks(
        localLinksRepo: LocalLinksRepo, preferencesRepository: PreferencesRepository
    ) {
        val workManager = WorkManager.getInstance(context)
        val request = OneTimeWorkRequestBuilder<RefreshAllLinksWorker>().setConstraints(
            Constraints(requiredNetworkType = NetworkType.CONNECTED)
        ).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).build()

        AppPreferences.refreshLinksWorkerTag.value = request.id.toString()
        preferencesRepository.changePreferenceValue(
            preferenceKey = stringPreferencesKey(
                AppPreferenceType.CURRENT_WORK_MANAGER_WORK_UUID.name
            ), newValue = AppPreferences.refreshLinksWorkerTag.value
        )
        preferencesRepository.changePreferenceValue(
            preferenceKey = longPreferencesKey(AppPreferenceType.LAST_REFRESHED_LINK_INDEX.name),
            newValue = -1
        )
        workManager.enqueueUniqueWork(
            AppPreferences.refreshLinksWorkerTag.value, ExistingWorkPolicy.KEEP, request
        )
    }

    actual suspend fun isAnyRefreshingScheduled(): Flow<Boolean?> {
        return channelFlow {
            WorkManager.getInstance(context)
                .getWorkInfoByIdFlow(UUID.fromString(AppPreferences.refreshLinksWorkerTag.value))
                .collectLatest {
                    if (it != null) {
                        send(it.state == WorkInfo.State.ENQUEUED)
                    } else {
                        send(null)
                    }
                }
        }
    }

    actual fun cancelRefreshingLinks() {
        RefreshAllLinksWorker.cancelLinksRefreshing(context)
    }

    actual class DataSyncingNotificationService(private val context: Context) {

        private val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        actual fun showNotification() {
            val notification =
                NotificationCompat.Builder(context, "1").setSmallIcon(R.drawable.ic_stat_name)
                    .setContentTitle(Localization.Key.SyncingDataLabel.getLocalizedString())
                    .setProgress(
                        0, 0, true
                    ).setPriority(NotificationCompat.PRIORITY_LOW).setSilent(true).build()

            notificationManager.notify(1, notification)
        }

        actual fun clearNotification() {
            notificationManager.cancelAll()
        }
    }

    private val packageManager = context.packageManager
    private val packageName = context.applicationContext.packageName

    actual fun onIconChange(
        allIconCodes: List<String>, newIconCode: String, onCompletion: () -> Unit
    ) {
        allIconCodes.forEach {
            if (it != newIconCode) {
                packageManager.setComponentEnabledSetting(
                    ComponentName(packageName, "$packageName.$it"),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
            }
        }

        val newAppIconComponent = ComponentName(packageName, "$packageName.$newIconCode")

        packageManager.setComponentEnabledSetting(
            newAppIconComponent,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

        onCompletion()
    }

    actual fun scheduleGitHubExport() {
        val workManager = WorkManager.getInstance(context)
        val request = androidx.work.PeriodicWorkRequestBuilder<com.sakethh.linkora.worker.GitHubExportWorker>(
            1, java.util.concurrent.TimeUnit.DAYS
        ).setConstraints(
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        ).build()

        workManager.enqueueUniquePeriodicWork(
            "GitHubExportWorker",
            androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}