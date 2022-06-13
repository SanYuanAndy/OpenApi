package com.openapi.debugger;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.openapi.comm.utils.CommUtils;
import com.openapi.comm.utils.LogUtil;

public class MonkeyShell {
    public static final String TAG = MonkeyShell.class.getSimpleName();

    public static final int CMD_CLICK = 1;
    public static final int CMD_INPUT_TEXT = 2;

    public static final int CMD_VOICE = 7;
    public static final int CMD_RESTART_APP = 8;
    public static final int CMD_BACK = 9;
    public static final int CMD_ENTER = 10;

    public static class Cmd {
        public String name;
        public int type;
        public int x;
        public int y;
        public int alive;
        public String text;
        public String pkgname;
        public String voice;
        public int keycode;

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
                    break;
                case 10:
                    cmd = genEnterCmd();
                    break;
                case 1000:
                    cmd = genKeyEventCmd(keycode);
                    break;
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
        return String.format("am broadcast -a %s --es query %s", action, voice) + " -f 0x01000000";
    }

    public static String genBackCmd() {
        return genKeyEventCmd(4);
    }

    public static String genEnterCmd() {
        return genKeyEventCmd(66);
    }

    public static String genKeyEventCmd(int code) {
        return String.format("input keyevent %d", code);
    }

    public static boolean executeCmd(String cmd, int keepTimeMill) {
        boolean ret = false;
        Runtime runtime = Runtime.getRuntime();
        try {
            LogUtil.e(TAG, cmd);
            runtime.exec(cmd);
            Thread.sleep(keepTimeMill * 1000);
            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
}
