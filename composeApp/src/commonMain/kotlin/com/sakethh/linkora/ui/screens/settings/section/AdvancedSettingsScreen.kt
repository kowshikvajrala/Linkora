package com.sakethh.linkora.ui.screens.settings.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sakethh.linkora.Localization
import com.sakethh.linkora.preferences.AppPreferenceType
import com.sakethh.linkora.preferences.AppPreferences
import com.sakethh.linkora.utils.Constants
import com.sakethh.linkora.utils.addEdgeToEdgeScaffoldPadding
import com.sakethh.linkora.utils.rememberLocalizedString
import com.sakethh.linkora.di.linkoraViewModel
import com.sakethh.linkora.ui.LocalNavController
import com.sakethh.linkora.ui.navigation.Navigation
import com.sakethh.linkora.ui.screens.settings.SettingsScreenViewModel
import com.sakethh.linkora.ui.screens.settings.common.composables.SettingsSectionScaffold
import com.sakethh.linkora.ui.screens.settings.common.composables.PreferenceTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSettingsScreen() {
    val navController = LocalNavController.current
    val primaryJsoupStringAgent = rememberSaveable(AppPreferences.primaryJsoupUserAgent.value) {
        mutableStateOf(AppPreferences.primaryJsoupUserAgent.value)
    }
    val localizationServerURL = rememberSaveable(AppPreferences.localizationServerURL.value) {
        mutableStateOf(AppPreferences.localizationServerURL.value)
    }
    val isReadOnlyTextFieldForPrimaryUserAgent = rememberSaveable {
        mutableStateOf(true)
    }
    val isReadOnlyTextFieldForLocalizationServer = rememberSaveable {
        mutableStateOf(true)
    }
    val primaryJsoupUserAgentFocusRequester = remember { FocusRequester() }
    val localizationServerTextFieldFocusRequester = remember { FocusRequester() }
    val settingsScreenVM: SettingsScreenViewModel = linkoraViewModel()
    SettingsSectionScaffold(
        topAppBarText = Navigation.Settings.AdvancedSettingsScreen.toString(),
        navController = navController
    ) { paddingValues, topAppBarScrollBehaviour ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().addEdgeToEdgeScaffoldPadding(paddingValues)
                .nestedScroll(topAppBarScrollBehaviour.nestedScrollConnection),
            verticalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            item {
                Spacer(Modifier)
            }
            item {
                PreferenceTextField(
                    textFieldDescText = Localization.Key.UserAgentDesc.rememberLocalizedString(),
                    textFieldLabel = Localization.Key.UserAgent.rememberLocalizedString(),
                    textFieldValue = primaryJsoupStringAgent.value,
                    onResetButtonClick = {
                        settingsScreenVM.changeSettingPreferenceValue(
                            stringPreferencesKey(AppPreferenceType.JSOUP_USER_AGENT.name),
                            Constants.DEFAULT_USER_AGENT
                        )
                        AppPreferences.primaryJsoupUserAgent.value = Constants.DEFAULT_USER_AGENT
                    },
                    onTextFieldValueChange = {
                        primaryJsoupStringAgent.value = it
                    },
                    onConfirmButtonClick = {
                        isReadOnlyTextFieldForPrimaryUserAgent.value =
                            !isReadOnlyTextFieldForPrimaryUserAgent.value
                        if (!isReadOnlyTextFieldForPrimaryUserAgent.value) {
                            primaryJsoupUserAgentFocusRequester.requestFocus()
                        } else {
                            primaryJsoupUserAgentFocusRequester.freeFocus()
                        }
                        if (isReadOnlyTextFieldForPrimaryUserAgent.value) {
                            settingsScreenVM.changeSettingPreferenceValue(
                                stringPreferencesKey(AppPreferenceType.JSOUP_USER_AGENT.name),
                                primaryJsoupStringAgent.value
                            )
                            AppPreferences.primaryJsoupUserAgent.value =
                                primaryJsoupStringAgent.value
                        }
                    },
                    focusRequester = primaryJsoupUserAgentFocusRequester,
                    readonly = isReadOnlyTextFieldForPrimaryUserAgent.value
                )
            }

            item {
                PreferenceTextField(
                    textFieldDescText = Localization.Key.LocalizationServerDesc.rememberLocalizedString(),
                    textFieldLabel = Localization.Key.LocalizationServer.rememberLocalizedString(),
                    textFieldValue = localizationServerURL.value,
                    onResetButtonClick = {
                        settingsScreenVM.changeSettingPreferenceValue(
                            stringPreferencesKey(AppPreferenceType.LOCALIZATION_SERVER_URL.name),
                            Constants.LOCALIZATION_SERVER_URL
                        )
                        AppPreferences.localizationServerURL.value =
                            Constants.LOCALIZATION_SERVER_URL
                    },
                    onTextFieldValueChange = {
                        localizationServerURL.value = it
                    },
                    onConfirmButtonClick = {
                        isReadOnlyTextFieldForLocalizationServer.value =
                            !isReadOnlyTextFieldForLocalizationServer.value
                        if (!isReadOnlyTextFieldForLocalizationServer.value) {
                            localizationServerTextFieldFocusRequester.requestFocus()
                        } else {
                            localizationServerTextFieldFocusRequester.freeFocus()
                        }
                        if (isReadOnlyTextFieldForLocalizationServer.value) {
                            settingsScreenVM.changeSettingPreferenceValue(
                                stringPreferencesKey(AppPreferenceType.LOCALIZATION_SERVER_URL.name),
                                localizationServerURL.value
                            )
                        }
                        AppPreferences.localizationServerURL.value = localizationServerURL.value
                    },
                    focusRequester = localizationServerTextFieldFocusRequester,
                    readonly = isReadOnlyTextFieldForLocalizationServer.value
                )
            }
            item {
                Spacer(Modifier.height(100.dp))
            }
        }
    }
}