// ILocationManager.aidl
package com.openapi.ipc;

interface ILocationManager {

    void start();

    double[] getLastLocation();

}
