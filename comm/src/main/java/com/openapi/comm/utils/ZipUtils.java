package com.openapi.comm.utils;

import android.text.TextUtils;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import java.io.File;
import java.util.List;

public class ZipUtils {
    public static boolean zip(List<String> srcFilePathList, String destZipFilePath, String password) {
        boolean ret = false;
        ZipFile zipFile = new ZipFile(destZipFilePath);
        ZipParameters params = new ZipParameters();
        params.setCompressionMethod(CompressionMethod.DEFLATE);
        params.setCompressionLevel(CompressionLevel.NORMAL);

        if(!TextUtils.isEmpty(password)){
            params.setEncryptFiles(true);
            params.setEncryptionMethod(EncryptionMethod.AES);
            params.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);
            zipFile.setPassword(password.toCharArray());
        }
        for (String srcFilePath : srcFilePathList) {
            File srcFile = new File(srcFilePath);
            try {
                if (srcFile.isDirectory()) {
                    zipFile.addFolder(srcFile, params);
                } else {
                    zipFile.addFile(srcFile, params);
                }
                ret = true;
            } catch (Exception e) {
                ret = false;
                e.printStackTrace();
                break;
            }
        }

        return ret;
    }
}
