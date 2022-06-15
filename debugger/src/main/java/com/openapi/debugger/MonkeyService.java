package com.openapi.debugger;

import android.content.Context;

import com.openapi.comm.utils.FileUtils;
import com.openapi.comm.utils.JSONParser;
import com.openapi.comm.utils.LogUtil;

import org.json.JSONArray;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class MonkeyService {
    public static final String TAG = MonkeyService.class.getSimpleName();

    private static boolean isRunning = false;
    private static boolean isStopped = false;

    private static Thread sWorkThread = null;

    public static void stop() {
        if (!isStopped) {
            isStopped = true;
            Thread t = sWorkThread;
            if (t != null && t.getState() == Thread.State.TIMED_WAITING) {
                t.interrupt();
            }
        }
    }

    public static boolean isRunning() {
        return isRunning;
    }

    public static void run(Context context, String monkeyDir) {
        if (isRunning) {
            LogUtil.e(TAG, "is Running");
            return;
        }
        isRunning = true;
        isStopped = false;
        sWorkThread = Thread.currentThread();

        File root = new File(monkeyDir);
        File[] all = root.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().endsWith(".json");
            }
        });
        for (File f : all) {
            if (isStopped) {
                break;
            }
            String strJson = FileUtils.readStringFromFile(f.getPath());
            List<MonkeyShell.Cmd> cmdList = parseCmd(strJson);
            for (MonkeyShell.Cmd cmd : cmdList) {
                if (isStopped) {
                    break;
                }
                UIManager.getInstance().sendFloatingText(cmd.name);
                cmd.execute(context.getApplicationContext());
            }
        }
        UIManager.getInstance().sendFloatingText("运行结束");
        isStopped = true;
        isRunning = false;
        sWorkThread = null;
    }

    public static List<MonkeyShell.Cmd> parseCmd(String strJson) {
        JSONArray jsonArray = null;
        List<MonkeyShell.Cmd> list = new ArrayList<>();
        try {
            jsonArray = new JSONArray(strJson);
            list = JSONParser.parse(MonkeyShell.Cmd.class, jsonArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
