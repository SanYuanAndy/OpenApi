package com.openapi.ipc;

import android.os.MemoryFile;
import android.os.ParcelFileDescriptor;
import com.openapi.comm.utils.CommUtils;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

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
        ByteBuffer byteBuffer = ByteBuffer.allocate(4 + size);
        byteBuffer.put(CommUtils.int2Byte(size));
        byteBuffer.put(data, offset, size);
        try {
            memoryFile.getOutputStream().write(byteBuffer.array());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void write(FileDescriptor fd,byte [] data, int offset, int size) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(fd);
            ByteBuffer byteBuffer = ByteBuffer.allocate(4 + size);
            byteBuffer.put(CommUtils.int2Byte(size));
            byteBuffer.put(data, offset, size);
            out.write(byteBuffer.array());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {

                }
            }
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
