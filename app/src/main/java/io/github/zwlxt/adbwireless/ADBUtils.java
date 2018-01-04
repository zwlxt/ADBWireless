package io.github.zwlxt.adbwireless;


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
    public static int status() {
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
}
