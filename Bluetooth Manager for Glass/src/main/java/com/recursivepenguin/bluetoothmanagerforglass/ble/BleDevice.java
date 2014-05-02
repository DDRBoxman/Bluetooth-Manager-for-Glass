package com.recursivepenguin.bluetoothmanagerforglass.ble;

import android.bluetooth.BluetoothDevice;

public class BleDevice {
    BluetoothDevice device;
    int rssi;

    public BleDevice(BluetoothDevice device, int rssi) {
        this.device = device;
        this.rssi = rssi;
    }

    public boolean equals(Object o) {
        if (o instanceof BleDevice) {
            BleDevice toCompare = (BleDevice) o;
            return this.device.equals(toCompare.device);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return device.hashCode();
    }

    public String getName() {
        return device.getName();
    }

    public String getAddress() {
        return device.getAddress();
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public int getRssi() {
        return rssi;
    }
}
