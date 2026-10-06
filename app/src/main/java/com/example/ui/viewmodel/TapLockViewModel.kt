package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PreferencesManager
import com.example.service.TapLockAccessibilityService
import com.example.service.TapLockOverlayService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TapLockUiState(
    val isAccessibilityEnabled: Boolean = false,
    val canDrawOverlays: Boolean = false,
    val isServiceActive: Boolean = false
)

class TapLockViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context get() = getApplication<Application>().applicationContext
    private val preferencesManager = PreferencesManager(context)

    private val _permissions = MutableStateFlow(
        Pair(
            TapLockAccessibilityService.isAccessibilityServiceEnabled(context),
            Settings.canDrawOverlays(context)
        )
    )

    val uiState: StateFlow<TapLockUiState> = combine(
        _permissions,
        preferencesManager.isServiceEnabled
    ) { perms, isEnabled ->
        TapLockUiState(
            isAccessibilityEnabled = perms.first,
            canDrawOverlays = perms.second,
            isServiceActive = isEnabled && perms.first && perms.second
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TapLockUiState()
    )

    fun refreshPermissions() {
        _permissions.update {
            Pair(
                TapLockAccessibilityService.isAccessibilityServiceEnabled(context),
                Settings.canDrawOverlays(context)
            )
        }
    }

    fun toggleService(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled && !Settings.canDrawOverlays(context)) {
                return@launch
            }
            preferencesManager.setServiceEnabled(enabled)
            if (enabled) {
                TapLockOverlayService.start(context)
            } else {
                TapLockOverlayService.stop(context)
            }
        }
    }

    fun instantLockScreen(): Boolean {
        return TapLockAccessibilityService.lockScreen(context)
    }
}
