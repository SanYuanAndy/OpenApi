package com.openapi.debugger;

import android.content.Context;

import com.openapi.comm.utils.FileUtils;
import com.openapi.comm.utils.JSONParser;
import com.openapi.comm.utils.LogUtil;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class MonkeyService {
    static String test = "[\n" +
            "       {\"name\":\"\", \"type\":1, \"x\":1099,\"y\":245,\"alive\":1},\n" +
            "       {\"name\":\"\", \"type\":0}\n" +
            "       ]";
    /*[
       {"name":"", "type":1, "x":1099,"y":245, "alive":1},
       {"name":"", "type":0}
       ]
     */

    public static void run(Context context) {
        String strJson = FileUtils.readStringFromFile("/sdcard/tmp/iqy.json");
        List<MonkeyShell.Cmd> cmdList = parseCmd(strJson);
        for (MonkeyShell.Cmd cmd : cmdList) {
            cmd.execute(context.getApplicationContext());
        }
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
