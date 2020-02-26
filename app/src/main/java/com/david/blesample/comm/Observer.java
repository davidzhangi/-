package com.david.blesample.comm;


import com.david.blueconnection.data.BleDevice;

public interface Observer {

    void disConnected(BleDevice bleDevice);
}
