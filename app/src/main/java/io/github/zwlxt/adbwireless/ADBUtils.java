package io.github.zwlxt.adbwireless;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.Locale;

/**
 * Created by Hex on 2018/1/2.
 */

public class ADBUtils {

    private static final String TAG = "ADBUtils";

    /**
     * Start adbd
     *
     * @return true when success
     */
    public static boolean start(int port) {
        stop();
        ShellUtils.execute("setprop service.adb.tcp.port " + port);
        return ShellUtils.execute("start adbd");
    }

    /**
     * Stop adbd
     *
     * @return true when success
     */
    public static boolean stop() {
        return ShellUtils.execute("stop adbd");
    }

    /**
     * Status of adbd
     *
     * @return 0 -> disabled; 1 -> listening; 2 -> connection is active; 3 -> 1 + 2
     */
    private static int getStatus(int port) {
        String output = ShellUtils.executeForOutput("sh -c 'netstat -tln | grep :" + port + "'");
        if (output == null)
            return 0;

        int result = 0;
        if (output.contains("LISTEN"))
            result += 1;

        if (output.contains("ESTABLISHED"))
            result += 2;

        return result;
    }

    /**
     * Get port number predefined in adb
     *
     * @return port number
     */
    private static int getPort() {
        String output = ShellUtils.executeForOutput("getprop service.adb.tcp.port");
        try {
            return Integer.parseInt(output);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Get wifi IP address
     *
     * @param context context
     * @return ip address string
     */
    private static String getAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null) {
            return null;
        }
        int ipAddress = wifiInfo.getIpAddress();
        return String.format(Locale.getDefault(),
                "%d.%d.%d.%d",
                (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff),
                (ipAddress >> 24 & 0xff)); // https://stackoverflow.com/a/8796997/8817846
    }

    /**
     * Check wifi connectivity status
     *
     * @param context context
     * @return true when connected to wifi
     */
    private static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
    }

    public static ADBState getState(Context context) {
        int port = ADBUtils.getPort();
        int status = ADBUtils.getStatus(port);
        String ipAddress;
        boolean connectedToWifi;
        if (ADBUtils.isWifiConnected(context)) {
            ipAddress = ADBUtils.getAddress(context);
            connectedToWifi = true;
        } else {
            ipAddress = context.getString(R.string.wifi_not_connected);
            connectedToWifi = false;
        }
        ADBState state = new ADBState();
        state.setAddress(ipAddress);
        state.setConnectedToWifi(connectedToWifi);
        state.setStatus(status);
        state.setPort(port);
        return state;
    }
}
