package com.openapi.comm.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class FileUtils {
    public static String readStringFromFile(String path) {
        StringBuilder content = new StringBuilder();
        File f = new File(path);
        if (!f.exists()) {
            return content.toString();
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            while(true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                content.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (reader != null) {
            try {
                reader.close();
            } catch (Exception e) {

            }
        }
        return content.toString();

    }
}
