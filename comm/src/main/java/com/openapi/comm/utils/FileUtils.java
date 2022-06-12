package com.openapi.comm.utils;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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
    public static void writeLines(String path, List<String[]> data) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path)));
            for (String[] row : data) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < row.length; ++i) {
                    sb.append(row[i]);
                    if (i < row.length - 1) {
                        sb.append(",");
                    }
                }
                writer.write(sb.toString());
                writer.write("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (writer != null) {
            try {
                writer.close();
            } catch (Exception e) {

            }
        }
    }

    public static List<String[]> readLinesFromFile(String path) {
        List<String[]> data = new ArrayList<>();
        File f = new File(path);
        if (!f.exists()) {
            return data;
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            while(true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                line = line.replace("\n", "");
                data.add(line.split(","));
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
        return data;

    }

    public static void readLinesFromFile(String path, int col, Collection<String> out) {
        List<String[]> data = readLinesFromFile(path);
        for (int i = 0; i < data.size(); ++i) {
            String[] row = data.get(i);
            if (row == null) {
                continue;
            }
            if (row.length > col) {
                if (!TextUtils.isEmpty(row[col])) {
                    out.add(row[col].trim());
                }
            }
        }
    }
}
