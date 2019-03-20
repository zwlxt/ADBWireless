package io.github.zwlxt.adbwireless

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
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

    companion object {
        const val PREF_KEY_PORT = "port"
    }

    private val notificationChannelId = "ADB"
    private val notificationId = 1
    private var port = 5555

    override fun onClick() {
        super.onClick()

        if (!WifiUtil.isWifiConnected(this)) {
            Toast.makeText(this, getString(R.string.wifi_not_connected), Toast.LENGTH_SHORT)
                    .show()
            return
        }

        port = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
                .getInt(PREF_KEY_PORT, 5555)
        WirelessAdbUtil.setPort(port)

        if (WirelessAdbUtil.status == WirelessAdbUtil.STATUS_DISABLED) {
            WirelessAdbUtil.setEnable(true)
        } else {
            WirelessAdbUtil.setEnable(false)
        }
        updateTileFromAdbState()
    }

    override fun onTileAdded() {
        super.onTileAdded()
        updateTileFromAdbState()
    }

    private fun updateTileFromAdbState() {
        val status = WirelessAdbUtil.status != WirelessAdbUtil.STATUS_DISABLED
        val ipAddress = WifiUtil.getIpAddressString()
        createNotificationChannel()

        val settingIntent = Intent(this, SettingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val settingPendingIntent = PendingIntent.getActivity(this,
                0, settingIntent, 0)
        val notificationBuilder = NotificationCompat.Builder(this, notificationChannelId)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.ic_developer_mode_black_24dp)
                .setContentTitle(getString(R.string.adb_is_running))
                .setContentText(getString(R.string.at_address)
                        .format(ipAddress, port))
                .setAutoCancel(false)
                .setContentIntent(settingPendingIntent)

        val notificationManager = NotificationManagerCompat.from(this)

        if (status) {
            qsTile.apply {
                state = Tile.STATE_ACTIVE
                label = "$ipAddress\n:$port"
            }
            notificationManager.notify(notificationId, notificationBuilder.build())
        } else {
            qsTile.apply {
                state = Tile.STATE_INACTIVE
                label = getString(R.string.app_name)
            }
            notificationManager.cancel(notificationId)
        }
        qsTile.updateTile()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(notificationChannelId, name, importance).apply {
                description = "Control wireless ADB"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        TileService.requestListeningState(this,
                ComponentName(this, WirelessAdbTileService::class.java))
        return super.onBind(intent)
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileFromAdbState()
    }
}