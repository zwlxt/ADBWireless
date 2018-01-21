package io.github.zwlxt.adbwireless;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by quintus on 2018/1/13.
 */

@TargetApi(Build.VERSION_CODES.N)
public class QuickSettingsService extends TileService {

    private static final String TAG = "QuickSettingsService";
    public static final String PREF_TILE_STATE = "TILE_STATE";

    @Override
    public void onTileAdded() {
        setState(false);
    }

    @Override
    public void onStartListening() {
        // get state from shared preference
        Log.d(TAG, "onStartListening");
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        boolean state = sharedPreferences.getBoolean(PREF_TILE_STATE, false);
        setState(state);
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
            // on
            ADBUtils.start(state.getPort());
            setState(true);
            Log.d(TAG, "onClick: start");
        } else {
            // off
            ADBUtils.stop();
            setState(false);
            Log.d(TAG, "onClick: stop");
        }
    }

    private void setState(boolean state) {
        Tile tile = getQsTile();
        SharedPreferences sharedPreferences
                = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        sharedPreferences.edit()
                .putBoolean(PREF_TILE_STATE, state)
                .apply();
        if (state) {
            tile.setState(Tile.STATE_ACTIVE);
        } else {
            tile.setState(Tile.STATE_INACTIVE);
        }
        tile.updateTile();
    }
}
