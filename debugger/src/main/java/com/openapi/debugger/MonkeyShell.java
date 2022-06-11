package com.openapi.debugger;

import android.text.TextUtils;

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

        @Override
        public String toString() {
            return "Cmd{" +
                    "name='" + name + '\'' +
                    ", type=" + type +
                    ", x=" + x +
                    ", y=" + y +
                    ", alive=" + alive +
                    '}';
        }

        public void execute() {
            String cmd = "";
            switch (type) {
                case 1:
                    cmd = genClickCmd(new ClickPoint(x, y));
                    break;
                case 2:
                    cmd = genTextInputCmd(text);
                    break;
                case 8:
                    cmd = genRestartAppCmd(pkgname);
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

    public static String genRestartAppCmd(String pkgname) {
        return String.format("killall %s", pkgname, pkgname);
    }

    public static String genBackCmd() {
        return genKeyeventCmd(4);
    }

    public static String genKeyeventCmd(int code) {
        return String.format("input keyevent %d", code);
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
