package io.github.zwlxt.adbwireless.util

import android.util.Log

import java.io.BufferedReader
import java.io.InputStreamReader


object ShellUtil {

    private val TAG = "ShellUtil"

    /**
     * Execute command
     *
     * @param command command and argument
     * @return true when success
     */
    fun execute(command: String): Boolean {
        try {
            val process = Runtime.getRuntime().exec("su -c $command")
            process.waitFor()
            return process.exitValue() == 0
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    /**
     * Execute command and read output
     *
     * @param command command and argument
     * @return output
     */
    fun executeForOutput(command: String): String? {
        try {
            val process = Runtime.getRuntime().exec("su -c $command")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val stringBuilder = StringBuilder()
            for (line in reader.lines()) {
                Log.d(TAG, "executeForOutput: $line")
                stringBuilder.append(line)
            }
            process.waitFor()
            reader.close()
            return stringBuilder.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    fun execute(vararg commands: String): Boolean {
        for (command in commands) {
            val result = execute(command)
            if (!result) {
                return false
            }
        }
        return true
    }
}
