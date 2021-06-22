package com.openapi.ipc;

import android.os.Build;
import android.os.SharedMemory;

import com.openapi.comm.utils.CommUtils;

import java.nio.ByteBuffer;

public class SharedMemoryWrapper {

    private SharedMemory mSharedMemory;
    private ByteBuffer mMapping;

    private SharedMemoryWrapper(SharedMemory sharedMemory) {
        mSharedMemory = sharedMemory;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            try {
                mMapping = mSharedMemory.mapReadWrite();
            } catch (Exception e) {

            }

        }
    }

    private SharedMemoryWrapper(String name, int size) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            try {
                mSharedMemory = SharedMemory.create(name, size);
                mMapping = mSharedMemory.mapReadWrite();
            } catch (Exception e) {

            }

        }

    }


    public static SharedMemoryWrapper create(String name ,int size) {
        return new SharedMemoryWrapper(name, size);
    }

    public static SharedMemoryWrapper create(SharedMemory sharedMemory) {
        return new SharedMemoryWrapper(sharedMemory);
    }

    public void writeBytes(byte[] buffer, int srcOffset, int destOffset, int count) {
        ByteBuffer mapping = mMapping;
        if (mapping != null) {
            mapping.position(destOffset);
            mapping.put(buffer, srcOffset, count);
        }
    }

    public int readBytes(byte[] buffer, int srcOffset, int destOffset, int count) {
        ByteBuffer mapping = mMapping;
        if (mapping != null) {
            mapping.position(srcOffset);
            mapping.get(buffer, destOffset, count);
        }
        return count;
    }

    public void write(byte[] data, int offset, int size) {
        byte[] head = CommUtils.int2Byte(size);
        writeBytes(head, 0, 0, head.length);
        writeBytes(data, 0, head.length, size);
    }

    public byte[] read() {
        byte[] data = null;
        byte[] head = new byte[4];
        readBytes(head, 0, 0, head.length);
        int len = CommUtils.byte2Int(head);
        data = new byte[len];
        readBytes(data, head.length, 0, len);
        return data;
    }



    public SharedMemory getSharedMemory() {
        return mSharedMemory;
    }

    public void close() {
        ByteBuffer mapping = mMapping;
        mMapping = null;
        if (mapping != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                SharedMemory.unmap(mapping);
            }
        }
        SharedMemory sharedMemory = mSharedMemory;
        mSharedMemory = null;
        if (sharedMemory != null) {
            sharedMemory.close();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
}
