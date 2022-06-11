// IFloatingManager.aidl
package com.openapi.debugger;

interface IFloatingManager {
    void send(in int cmd, in byte[] data);
}