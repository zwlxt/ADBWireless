package io.github.zwlxt.adbwireless;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;

/**
 * Created by quintus on 2018/1/13.
 */

@TargetApi(Build.VERSION_CODES.N)
public class QuickSettingsService extends TileService {

    private static final String TAG = "QuickSettingsService";
    public static final String PREF_PORT = "port";
    private final String NOTIF_CHAN = "Adb status";
    private final int NOTIF_ID = 1;
    private final int NOTIF_CODE_INFO = 1;
    private final int NOTIF_CODE_CLOSE = 2;
    private final int NOTIF_CODE_RESTART = 3;
    private final String NOTIF_ACTION_CLOSE = "action_close";
    private final String NOTIF_ACTION_RESTART = "action_restart";
    private ADBState adbState;
    private int port;

    @Override
    public void onTileAdded() {
        setTileState(false);
    }

    @Override
    public void onStartListening() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean closeCommand = intent.getBooleanExtra(NOTIF_ACTION_CLOSE, false);
        if (closeCommand) {
            controlADB(false);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onClick() {
        controlADB(getTileState() != Tile.STATE_ACTIVE);
    }

    private void controlADB(boolean newState) {
        Context context = getApplicationContext();
        adbState = ADBUtils.getState(context);
        if (!adbState.isConnectedToWifi()) {
            Toast.makeText(context, "WiFi is not connected", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        SharedPreferences sharedPreferences
                = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        port = sharedPreferences.getInt(PREF_PORT, 5555);

        if (newState) {
            // on
            ADBUtils.start(port);
            Log.d(TAG, "ADB: start");
            setTileState(true);
            setNotification(true, adbState);
        } else {
            // off
            ADBUtils.stop();
            Log.d(TAG, "ADB: stop");
            setTileState(false);
            setNotification(false, null);
        }
    }

    private void setTileState(boolean tileState) {
        Tile tile = getQsTile();
        if (tileState) {
            tile.setLabel(getString(R.string.adb_is_running));
            tile.setState(Tile.STATE_ACTIVE);
        } else {
            tile.setLabel(getString(R.string.app_name));
            tile.setState(Tile.STATE_INACTIVE);
        }
        tile.updateTile();
    }

    private int getTileState() {
        Tile tile = getQsTile();
        return tile.getState();
    }

    private void setNotification(boolean show, ADBState state) {
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null)
            return;
        if (!show) {
            notificationManager.cancel(NOTIF_ID);
            return;
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIF_CHAN);
        Intent intent = new Intent(this, InfoDialog.class)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(InfoDialog.INTENT_EXTRA, adbState);
        PendingIntent infoDialogPendingIntent = PendingIntent.getActivity(this,
                NOTIF_CODE_INFO, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(infoDialogPendingIntent)
                .setSmallIcon(R.drawable.ic_developer_mode_black_24dp)
                .setContentTitle(getString(R.string.adb_is_running))
                .setContentText(String.format(Locale.getDefault(),
                        getString(R.string.at_address), state.getAddress(), port))
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        Intent closeIntent = new Intent(this, QuickSettingsService.class)
                .putExtra(NOTIF_ACTION_CLOSE, true);
        PendingIntent closePendingIntent = PendingIntent.getService(this,
                NOTIF_CODE_CLOSE, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(R.drawable.ic_close_black_24dp, getString(R.string.close), closePendingIntent);

        Intent restartIntent = new Intent(this, QuickSettingsService.class)
                .putExtra(NOTIF_ACTION_CLOSE, true);
        PendingIntent restartPendingIntent = PendingIntent.getService(this,
                NOTIF_CODE_CLOSE, restartIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(R.drawable.ic_refresh_black_24dp, getString(R.string.restart), restartPendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIF_CHAN, NOTIF_CHAN,
                    NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        notificationManager.notify(NOTIF_ID, notification);
    }
}
