package io.github.zwlxt.adbwireless

import android.content.*
import android.os.IBinder
import android.util.Log

class BootCompletedReceiver : BroadcastReceiver() {

    private val tileServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {

        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val updateTileBinder = service as WirelessAdbTileService.UpdateTileBinder
            updateTileBinder.updateTile()
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val serviceIntent = Intent(context, WirelessAdbTileService::class.java)
        context?.bindService(serviceIntent, tileServiceConnection, Context.BIND_AUTO_CREATE)
    }
}