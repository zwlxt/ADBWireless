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
     * Set the port where adbd listens at
     *
     * @param port port
     * @return true when success, otherwise false
     */
    public static boolean setPort(int port) {
        return ShellUtils.execute("setprop service.adb.tcp.port " + port);
    }

    /**
     * Start adbd
     *
     * @return true when success
     */
    public static boolean start() {
        stop();
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
    public static int getStatus() {
        String output = ShellUtils.executeForOutput("sh -c 'netstat -tln | grep 5555'");
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
     * Get wifi IP address
     *
     * @param context
     * @return ip address string
     */
    public static String getAddress(Context context) {
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

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
    }
}
