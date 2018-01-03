package io.github.zwlxt.adbwireless;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Hex on 2018/1/3.
 */

public class ShellUtils {

    private static final String TAG = "ShellUtils";

    /**
     * Execute command
     * @param command command and argument
     * @return true when success
     */
    public static boolean execute(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            return process.exitValue() == 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Execute command and read output
     * @param command command and argument
     * @return output
     */
    public static String executeForOutput(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                Log.d(TAG, "executeForOutput: " + line);
                stringBuilder.append(line);
            }
            reader.close();
            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
