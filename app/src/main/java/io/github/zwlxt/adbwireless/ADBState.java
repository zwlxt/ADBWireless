package io.github.zwlxt.adbwireless;

import java.io.Serializable;

/**
 * Created by Hex on 2018/1/8.
 */

public class ADBState implements Serializable {

    private int status;

    private boolean connectedToWifi;

    private String address;

    private int port;

    public int getState() {
        return status;
    }

    public ADBState setStatus(int status) {
        this.status = status;
        return this;
    }

    public boolean isConnectedToWifi() {
        return connectedToWifi;
    }

    public ADBState setConnectedToWifi(boolean connectedToWifi) {
        this.connectedToWifi = connectedToWifi;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public ADBState setAddress(String address) {
        this.address = address;
        return this;
    }

    public int getPort() {
        return port;
    }

    public ADBState setPort(int port) {
        this.port = port;
        return this;
    }
}
