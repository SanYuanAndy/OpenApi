package com.openapi.ipc;

import android.os.MemoryFile;
import android.os.ParcelFileDescriptor;
import com.openapi.comm.utils.CommUtils;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

public class SharedMemoryUtils {

    public static final String TAG = SharedMemoryUtils.class.getSimpleName();

    public static MemoryFile create() {
        MemoryFile memoryFile = null;
        try {
            memoryFile = new MemoryFile("share", 8 * 1024 * 1024);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return memoryFile;
    }

    public static ParcelFileDescriptor getFd(MemoryFile memoryFile) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            Method method = MemoryFile.class.getDeclaredMethod("getFileDescriptor");
            FileDescriptor fd = (FileDescriptor) method.invoke(memoryFile);
            parcelFileDescriptor = ParcelFileDescriptor.dup(fd);
        } catch (Exception e) {

        }
        return parcelFileDescriptor;
    }

    public static void native_pin(FileDescriptor fd, boolean pin) {
        try {
            Method method = MemoryFile.class.getDeclaredMethod("native_pin", new Class<?>[]{FileDescriptor.class, boolean.class});
            method.setAccessible(true);
            method.invoke(MemoryFile.class, fd, pin);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getSize(FileDescriptor fd) {
        int size = 0;
        try {
            Method method = MemoryFile.class.getDeclaredMethod("getSize", new Class<?>[] {FileDescriptor.class});
            size = (int)method.invoke(MemoryFile.class, fd);
        } catch (Exception e) {

        }
        return size;

    }

    public static void write(MemoryFile memoryFile,byte [] data, int offset, int size) {
        write(memoryFile.getOutputStream(), data, offset, size);
    }

    public static void write(OutputStream out, byte [] data, int offset, int size) {
        try {
            out.write(CommUtils.int2Byte(size));
            out.write(data, 0, size);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static byte[] read(FileDescriptor fd) {
        byte[] data = null;
        FileInputStream in = null;
        try {
            in = new FileInputStream(fd);
            byte[] head = new byte[4];
            in.read(head, 0, head.length);
            int len = CommUtils.byte2Int(head);
            data = new byte[len];
            in.read(data, 0, len);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {

                }
            }
        }
        return data;

    }
}
