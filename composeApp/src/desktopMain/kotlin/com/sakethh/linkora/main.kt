package com.sakethh.linkora

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Maximize
import androidx.compose.material.icons.filled.Minimize
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.outlined.Window
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.sakethh.linkora.data.local.LocalDatabase
import com.sakethh.linkora.di.DependencyContainer
import com.sakethh.linkora.di.LinkoraSDK
import com.sakethh.linkora.domain.Platform
import com.sakethh.linkora.platform.FileManager
import com.sakethh.linkora.platform.NativeUtils
import com.sakethh.linkora.platform.PermissionManager
import com.sakethh.linkora.preferences.AppPreferences
import com.sakethh.linkora.ui.App
import com.sakethh.linkora.ui.LocalNavController
import com.sakethh.linkora.ui.LocalPlatform
import com.sakethh.linkora.ui.theme.DarkColors
import com.sakethh.linkora.ui.theme.DesktopTypography
import com.sakethh.linkora.ui.theme.LightColors
import com.sakethh.linkora.ui.theme.LinkoraTheme
import com.sakethh.linkora.utils.Constants
import com.sakethh.linkora.utils.getLocalizedString
import com.sakethh.linkora.utils.rememberLocalizedString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import linkora.composeapp.generated.resources.Res
import linkora.composeapp.generated.resources.linkora_char
import okio.Path.Companion.toPath
import org.jetbrains.compose.resources.Font
import java.awt.Dimension
import java.io.File

val linkoraSpecificFolder = System.getProperty("user.home").run {
    val appDataDir = File(this, ".linkora")
    if (appDataDir.exists().not()) {
        appDataDir.mkdirs()
    }
    appDataDir
}

suspend fun main() {

    LinkoraSDK.set(
        linkoraSdk = LinkoraSDK(
            nativeUtils = NativeUtils(),
            fileManager = FileManager(),
            permissionManager = PermissionManager(),
            localDatabase = File(linkoraSpecificFolder, "${LocalDatabase.NAME}.db").run {
                Room.databaseBuilder<LocalDatabase>(name = this.absolutePath)
                    .setDriver(BundledSQLiteDriver()).addMigrations(
                        LocalDatabase.MIGRATION_9_10,
                        LocalDatabase.MIGRATION_10_11,
                        LocalDatabase.MIGRATION_11_12
                    ).build()
            },
            dataStore = PreferenceDataStoreFactory.createWithPath {
                linkoraSpecificFolder.resolve(Constants.DATA_STORE_NAME).absolutePath.toPath()
            },
            dataSyncingNotificationService = NativeUtils.DataSyncingNotificationService()
        )
    )

    withContext(Dispatchers.IO) {
        awaitAll(async {
            AppPreferences.readAll(
                defaultExportLocation = LinkoraSDK.getInstance().fileManager.getDefaultExportLocation(),
                preferencesRepository = DependencyContainer.preferencesRepo
            )
        }, async {
            Localization.loadLocalizedStrings(
                AppPreferences.preferredAppLanguageCode.value
            )
        })
        com.sakethh.linkora.worker.DesktopExportScheduler.startScheduler()
    }
    application {
        val windowState = rememberWindowState(
            width = 1054.dp, height = 600.dp
        )
        val navController = rememberNavController()
        Window(
            state = windowState,
            onCloseRequest = ::exitApplication,
            title = Localization.Key.Linkora.getLocalizedString(),
            undecorated = AppPreferences.useLinkoraTopDecoratorOnDesktop.value
        ) {
            window.minimumSize = Dimension(1054, 600)
            CompositionLocalProvider(
                LocalNavController provides navController, LocalPlatform provides Platform.Desktop
            ) {
                LinkoraTheme(
                    typography = DesktopTypography,
                    colorScheme = if (AppPreferences.shouldUseForceDarkTheme.value) DarkColors else LightColors
                ) {
                    Scaffold(
                        topBar = {
                            if (AppPreferences.useLinkoraTopDecoratorOnDesktop.value) {
                                WindowDraggableArea {
                                    TopDecorator(windowState)
                                }
                            }
                        }, modifier = Modifier.border(
                            0.5.dp, MaterialTheme.colorScheme.outline.copy(0.25f)
                        )
                    ) {
                        App(modifier = Modifier.padding(it))
                    }
                }
            }
        }
    }
}

@Composable
private fun ApplicationScope.TopDecorator(windowState: WindowState) {
    rememberCoroutineScope()
    Column {
        Box(Modifier.fillMaxWidth().padding(2.dp), contentAlignment = Alignment.CenterEnd) {
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(
                    Alignment.CenterStart
                )
            ) {
                if (AppPreferences.isServerConfigured()) {
                    Spacer(Modifier.padding(start = 15.dp))
                    Icon(
                        imageVector = Icons.Default.WbCloudy, contentDescription = null
                    )
                }
                Spacer(
                    Modifier.padding(
                        start = if (AppPreferences.isServerConfigured().not()) 15.dp else 5.dp
                    )
                )
                if (AppPreferences.useCustomAppVersionLabel.value) {
                    Text(
                        text = "!", style = TextStyle(
                            fontFamily = FontFamily(
                                Font(
                                    Res.font.linkora_char, FontWeight.Normal
                                )
                            ), fontWeight = FontWeight.Normal
                        ), fontSize = 65.sp
                    )
                } else {
                    Text(
                        text = Localization.Key.Linkora.rememberLocalizedString(),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand), onClick = {
                        windowState.isMinimized = true
                    }) {
                    Icon(imageVector = Icons.Default.Minimize, contentDescription = null)
                }
                IconButton(
                    modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand), onClick = {
                        if (windowState.placement == WindowPlacement.Fullscreen) {
                            windowState.placement = WindowPlacement.Floating
                        } else {
                            windowState.placement = WindowPlacement.Fullscreen
                        }
                    }) {
                    Icon(
                        imageVector = if (windowState.placement != WindowPlacement.Fullscreen) Icons.Default.Maximize else Icons.Outlined.Window,
                        contentDescription = null
                    )
                }
                IconButton(
                    modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand), onClick = {
                        this@TopDecorator.exitApplication()
                    }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null)
                }
            }
        }
        HorizontalDivider()
    }
}