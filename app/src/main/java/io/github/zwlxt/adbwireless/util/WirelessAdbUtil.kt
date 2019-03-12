package io.github.zwlxt.adbwireless.util


object WirelessAdbUtil {

    var STATUS_DISABLED = 0
    var STATUS_LISTEN = 0x01
    var STATUS_ACTIVE = 0x10

    private val TAG = "WirelessAdbUtil"
    private var port = 5555

    val status: Int
        get() {
            val output = ShellUtil.executeForOutput("sh -c \"netstat -tln | grep ':$port '\"")
                    ?: return STATUS_DISABLED

            var result = 0
            if (output.contains("LISTEN")) {
                result = result or STATUS_LISTEN
            }

            if (output.contains("ESTABLISHED")) {
                result = result or STATUS_ACTIVE
            }

            return result
        }

    fun setEnable(enabled: Boolean): Boolean {
        return if (enabled) {
            ShellUtil.execute(
                    "setprop service.adb.tcp.port $port",
                    "stop adbd",
                    "start adbd")
        } else {
            ShellUtil.execute(
                    "setprop service.adb.tcp.port -1",
                    "stop adbd",
                    "start adbd")
        }
    }

    fun setPort(port: Int) {
        if (port > 0) {
            WirelessAdbUtil.port = port
        }
    }

    fun getPort(): Int {
        val output = ShellUtil.executeForOutput("getprop service.adb.tcp.port")
        try {
            if (output == null) {
                return 0
            }
            port = Integer.parseInt(output)
            return port
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            return 0
        }

    }
}
