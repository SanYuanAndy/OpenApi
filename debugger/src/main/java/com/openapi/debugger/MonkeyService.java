package com.openapi.debugger;

import com.openapi.comm.utils.FileUtils;
import com.openapi.comm.utils.JSONParser;
import com.openapi.comm.utils.LogUtil;

import org.json.JSONArray;

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

    public static void run() {
        String strJson = FileUtils.readStringFromFile("/sdcard/tmp/iqy.json");
        List<MonkeyShell.Cmd> cmdList = parseCmd(strJson);
        for (MonkeyShell.Cmd cmd : cmdList) {
            cmd.execute();
        }
    }

    public static List<MonkeyShell.Cmd> parseCmd(String strJson) {
        JSONArray jsonArray = null;
        List<MonkeyShell.Cmd> list = null;
        try {
            jsonArray = new JSONArray(strJson);
            list = JSONParser.parse(MonkeyShell.Cmd.class, jsonArray);
            for (int i = 0; i < list.size(); ++i) {
                LogUtil.e("Shell", "" + list.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}