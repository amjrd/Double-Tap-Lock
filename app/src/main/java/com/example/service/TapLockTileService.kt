package com.example.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.example.MainActivity

class TapLockTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val isEnabled = TapLockAccessibilityService.isAccessibilityServiceEnabled(this)

        tile.state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(com.example.R.string.tile_name)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = if (isEnabled) "Pixel 8 جاهز" else "يتطلب تفعيل الإذن"
        }
        tile.updateTile()
    }

    override fun onClick() {
        super.onClick()
        val isEnabled = TapLockAccessibilityService.isAccessibilityServiceEnabled(this)

        if (isEnabled) {
            TapLockAccessibilityService.lockScreen(this)
        } else {
            // Guide user to enable accessibility service
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("OPEN_ACCESSIBILITY_GUIDE", true)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                startActivityAndCollapse(pendingIntent)
            } else {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(intent)
            }
        }
    }
}
