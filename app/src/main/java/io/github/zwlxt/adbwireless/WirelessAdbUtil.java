package io.github.zwlxt.adbwireless;


public class WirelessAdbUtil {

    public static int STATUS_DISABLED = 0;
    public static int STATUS_LISTEN = 0x01;
    public static int STATUS_ACTIVE = 0x10;

    private static final String TAG = "WirelessAdbUtil";
    private static int port = 5555;

    public static boolean setEnable(boolean enabled) {
        if (enabled) {
            return ShellUtils.execute(
                    "setprop service.adb.tcp.port " + port,
                    "stop adbd",
                    "start adbd");
        } else {
            return ShellUtils.execute(
                    "setprop service.adb.tcp.port -1",
                    "stop adbd",
                    "start adbd");
        }
    }

    public static int getStatus() {
        String output = ShellUtils.executeForOutput("sh -c 'netstat -tln | grep :" + port + "'");
        if (output == null) {
            return STATUS_DISABLED;
        }

        int result = 0;
        if (output.contains("LISTEN")) {
            result |= STATUS_LISTEN;
        }

        if (output.contains("ESTABLISHED")) {
            result |= STATUS_ACTIVE;
        }

        return result;
    }

    public static void setPort(int port) {
        if (port > 0) {
            WirelessAdbUtil.port = port;
        }
    }

    public static int getPort() {
        String output = ShellUtils.executeForOutput("getprop service.adb.tcp.port");
        try {
            if (output == null) {
                return 0;
            }
            port = Integer.parseInt(output);
            return port;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
