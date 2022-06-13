package com.openapi.debugger;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.openapi.comm.utils.CommUtils;
import com.openapi.comm.utils.LogUtil;

public class MonkeyShell {

    public static class Cmd {
        public String name;
        public int type;
        public int x;
        public int y;
        public int alive;
        public String text;
        public String pkgname;
        public String voice;

        @Override
        public String toString() {
            return CommUtils.toString(this);
        }

        public void execute(Context cxt) {
            String cmd = "";
            switch (type) {
                case 1:
                    cmd = genClickCmd(new ClickPoint(x, y));
                    break;
                case 2:
                    cmd = genTextInputCmd(text);
                    break;
                case 7:
                    cmd = genVoiceCmd(voice);
                    break;
                case 8:
                    cmd = genRestartAppCmd(pkgname, cxt);
                    break;
                case 9:
                    cmd = genBackCmd();
                default:
                    break;
            }

            if (!TextUtils.isEmpty(cmd)) {
                executeCmd(cmd, alive);
            }
        }
    }

    public static class ClickPoint {
        int x;
        int y;
        public ClickPoint(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public static String genClickCmd(ClickPoint point) {
        return String.format("input tap %d %d", point.x, point.y);
    }

    public static String genTextInputCmd(String text) {
        return String.format("input text %s", text);
    }

    public static String genRestartAppCmd(String pkgName, Context cxt) {
        String cmd = "";
        try {
            Intent intent = cxt.getPackageManager().getLaunchIntentForPackage(pkgName);
            ComponentName name = intent.getComponent();
            cmd = String.format("am start -S -n %s/%s", name.getPackageName(), name.getClassName());
        } catch (Exception e) {

        }
        return cmd;
    }

    public static String genVoiceCmd(String voice) {
        String action = "android.intent.action.START_CODRIVER";
        return String.format("am broadcast -a %s --es query %s", action, voice) + "-f 0x01000000";
    }

    public static String genBackCmd() {
        return genKeyEventCmd(4);
    }

    public static String genKeyEventCmd(int code) {
        return String.format("input keyEvent %d", code);
    }

    public static boolean executeCmd(String cmd, int keepTimeMill) {
        boolean ret = false;
        Runtime runtime = Runtime.getRuntime();
        try {
            LogUtil.e("", cmd);
            runtime.exec(cmd);
            Thread.sleep(keepTimeMill);
            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
}
