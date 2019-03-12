package io.github.zwlxt.adbwireless

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.github.zwlxt.adbwireless.util.WifiUtil
import io.github.zwlxt.adbwireless.util.WirelessAdbUtil

@RequiresApi(api = Build.VERSION_CODES.N)
class WirelessAdbTileService : TileService() {

    inner class UpdateTileBinder : Binder() {
        fun updateTile() {
            updateTileFromAdbState();
        }
    }

    companion object {
        const val PREF_KEY_PORT = "PORT"
    }

    private val NOTIFICATION_CHANNEL_ID = "ADB"
    private val NOTIFICATION_ID = 1

    override fun onClick() {
        super.onClick()

        if (!WifiUtil.isWifiConnected(this)) {
            Toast.makeText(this, getString(R.string.wifi_not_connected), Toast.LENGTH_SHORT)
                    .show();
            return
        }

        val port = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
                .getInt(PREF_KEY_PORT, 5555)
        WirelessAdbUtil.setPort(port)

        if (WirelessAdbUtil.status == WirelessAdbUtil.STATUS_DISABLED) {
            WirelessAdbUtil.setEnable(true)
        } else {
            WirelessAdbUtil.setEnable(false)
        }
        updateTileFromAdbState()
    }

    override fun onTileRemoved() {
        super.onTileRemoved()

        val componentName = ComponentName(this, BootCompletedReceiver::class.java)
        val status = packageManager.getComponentEnabledSetting(componentName)
        if (status == PackageManager.COMPONENT_ENABLED_STATE_ENABLED ||
                status == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) {

            packageManager.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP)
        }
    }

    override fun onTileAdded() {
        super.onTileAdded()

        updateTileFromAdbState()

        val componentName = ComponentName(this, BootCompletedReceiver::class.java)
        val status = packageManager.getComponentEnabledSetting(componentName)
        if (status == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            packageManager.setComponentEnabledSetting(componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
        }
    }

    private fun updateTileFromAdbState() {
        val status = WirelessAdbUtil.status != WirelessAdbUtil.STATUS_DISABLED

        createNotificationChannel()

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_developer_mode_black_24dp)
                .setContentTitle(getString(R.string.app_name))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        val notificationManager = NotificationManagerCompat.from(this)

        if (status) {
            qsTile.apply {
                state = Tile.STATE_ACTIVE
                label = WifiUtil.getIpAddressString()
            }
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        } else {
            qsTile.apply {
                state = Tile.STATE_INACTIVE
                label = getString(R.string.app_name)
            }
            notificationManager.cancel(NOTIFICATION_ID)
        }
        qsTile.updateTile()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = "Control wireless ADB"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}