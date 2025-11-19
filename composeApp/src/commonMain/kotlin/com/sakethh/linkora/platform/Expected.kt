package com.sakethh.linkora.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import com.sakethh.linkora.domain.ExportFileType
import com.sakethh.linkora.domain.ImportFileType
import com.sakethh.linkora.domain.PermissionStatus
import com.sakethh.linkora.domain.Platform
import com.sakethh.linkora.domain.RawExportString
import com.sakethh.linkora.domain.repository.local.LocalLinksRepo
import com.sakethh.linkora.domain.repository.local.PreferencesRepository
import com.sakethh.linkora.ui.screens.settings.section.data.ExportLocationType
import kotlinx.coroutines.flow.Flow
import java.io.File

expect val showFollowSystemThemeOption: Boolean
expect val showDynamicThemingOption: Boolean
expect val BUILD_FLAVOUR: String

expect val platform: @Composable () -> Platform

@Composable
expect fun PlatformSpecificBackHandler(init: () -> Unit = {})


expect fun platformSpecificLogging(string: String)

expect class PermissionManager {
    suspend fun permittedToShowNotification(): PermissionStatus
    suspend fun isStorageAccessPermitted(): PermissionStatus
}

expect class FileManager {
    suspend fun writeRawExportStringToFile(
        exportLocation: String,
        exportFileType: ExportFileType,
        exportLocationType: ExportLocationType,
        rawExportString: RawExportString,
        onCompletion: suspend (String) -> Unit
    )


    suspend fun pickAValidFileForImporting(
        importFileType: ImportFileType, onStart: () -> Unit
    ): File?

    suspend fun saveSyncServerCertificateInternally(file: File, onCompletion: () -> Unit)

    suspend fun loadSyncServerCertificate(): File

    suspend fun exportSnapshotData(
        exportLocation: String,
        rawExportString: String,
        fileType: ExportFileType,
        onCompletion: suspend (String) -> Unit = {}
    )

    suspend fun pickADirectory(): String?

    fun getDefaultExportLocation(): String?

    suspend fun deleteAutoBackups(
        backupLocation: String,
        // maximum number of backups allowed to keep
        threshold: Int, onCompletion: (deletionCount: Int) -> Unit
    )
}

expect class NativeUtils {
    fun onShare(url: String)

    suspend fun onRefreshAllLinks(
        localLinksRepo: LocalLinksRepo, preferencesRepository: PreferencesRepository
    )

    suspend fun isAnyRefreshingScheduled(): Flow<Boolean?>

    fun cancelRefreshingLinks()

    class DataSyncingNotificationService {
        fun showNotification()
        fun clearNotification()
    }

    fun onIconChange(allIconCodes: List<String>, newIconCode: String, onCompletion: () -> Unit)
}