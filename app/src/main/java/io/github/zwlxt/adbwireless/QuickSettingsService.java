package io.github.zwlxt.adbwireless;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

/**
 * Created by quintus on 2018/1/13.
 */

@TargetApi(Build.VERSION_CODES.N)
public class QuickSettingsService extends TileService {

    private TileServiceBinder tileServiceBinder = new TileServiceBinder();

    @Override
    public void onTileAdded() {
        setState(false);
    }

    @Override
    public void onClick() {
        Context context = getApplicationContext();
        ADBState state = ADBUtils.getState(context);
        if (!state.isConnectedToWifi()) {
            Toast.makeText(context, "WiFi is not connected", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        if (state.getState() == 0) {
            ADBUtils.start(state.getPort());
            setState(false);
        } else {
            ADBUtils.stop();
            setState(true);
        }
    }

    private void setState(boolean state) {
        Tile tile = getQsTile();
        if (state) {
            tile.setState(Tile.STATE_INACTIVE);
        } else {
            tile.setState(Tile.STATE_ACTIVE);
        }
        tile.updateTile();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return tileServiceBinder;
    }

    public class TileServiceBinder extends Binder {
        public void setState(boolean state) {
            QuickSettingsService.this.setState(state);
        }
    }
}
