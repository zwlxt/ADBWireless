package io.github.zwlxt.adbwireless;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.service.quicksettings.TileService;
import android.widget.Toast;

/**
 * Created by quintus on 2018/1/13.
 */

@TargetApi(Build.VERSION_CODES.N)
public class QuickSettingsService extends TileService {

    private Context context;

    @Override
    public void onStartListening() {
        super.onStartListening();
    }

    @Override
    public void onClick() {
        context = getApplicationContext();
        ADBState state = ADBUtils.getState(context);
        if (!state.isConnectedToWifi()) {
            Toast.makeText(context, "WiFi is not connected", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        setStatus(state.getStatus());
    }

    private void setStatus(int status) {
        if (status == 0) {

        } else {
        }
    }
}
