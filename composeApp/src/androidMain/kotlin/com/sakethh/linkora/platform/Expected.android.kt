package com.sakethh.linkora.platform

import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import com.sakethh.linkora.domain.Platform
import com.sakethh.linkora.ui.AppVM
import com.sakethh.linkora.ui.LocalNavController
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.utils.isTablet
import kotlinx.coroutines.launch

actual val showFollowSystemThemeOption: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
actual val platform: @Composable () -> Platform = {
    if (isTablet(LocalConfiguration.current) || LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) Platform.Android.Tablet else Platform.Android.Mobile
}
actual val BUILD_FLAVOUR: String = platform.toString()

actual val showDynamicThemingOption: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@Composable
actual fun PlatformSpecificBackHandler(init: () -> Unit) {
    val navController = LocalNavController.current
    val coroutineScope = rememberCoroutineScope()
    BackHandler(onBack = {
        if (AppVM.isMainFabRotated.value) {
            AppVM.isMainFabRotated.value = false
        } else if (navController.previousBackStackEntry == null) {
            coroutineScope.launch {
                UIEvent.pushUIEvent(UIEvent.Type.MinimizeTheApp)
            }
        } else {
            init()
        }
    })
}

actual fun platformSpecificLogging(string: String) {
    Log.d("Linkora Log", string)
}