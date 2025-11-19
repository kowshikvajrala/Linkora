package com.sakethh.linkora.platform

import androidx.compose.runtime.Composable
import com.sakethh.linkora.RefreshAllLinksService
import com.sakethh.linkora.domain.PermissionStatus
import com.sakethh.linkora.domain.Platform
import com.sakethh.linkora.domain.repository.local.LocalLinksRepo
import com.sakethh.linkora.domain.repository.local.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow


actual val showFollowSystemThemeOption: Boolean = false
actual val BUILD_FLAVOUR: String = "desktop"
actual val platform: @Composable () -> Platform = {
    Platform.Desktop
}

actual val showDynamicThemingOption: Boolean = false


@Composable
actual fun PlatformSpecificBackHandler(init: () -> Unit) = Unit


actual fun platformSpecificLogging(string: String) {
    println("Linkora Log : $string")
}

actual class PermissionManager {
    actual suspend fun permittedToShowNotification(): PermissionStatus = PermissionStatus.Granted
    actual suspend fun isStorageAccessPermitted(): PermissionStatus = PermissionStatus.Granted
}

actual class NativeUtils {

    actual fun onShare(url: String) = Unit

    actual suspend fun onRefreshAllLinks(
        localLinksRepo: LocalLinksRepo, preferencesRepository: PreferencesRepository
    ) {
        RefreshAllLinksService.invoke(localLinksRepo)
    }

    actual suspend fun isAnyRefreshingScheduled(): Flow<Boolean?> {
        return emptyFlow()
    }

    actual fun cancelRefreshingLinks() {
        RefreshAllLinksService.cancel()
    }

    actual class DataSyncingNotificationService {
        actual fun showNotification() = Unit
        actual fun clearNotification() = Unit
    }

    actual fun onIconChange(
        allIconCodes: List<String>,
        newIconCode: String,
        onCompletion: () -> Unit
    ) = Unit
}