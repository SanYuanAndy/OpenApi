package com.openapi.debugger;

import android.database.Cursor;
import android.os.IBinder;

import com.openapi.comm.utils.CommUtils;
import com.openapi.comm.utils.DnsParser;
import com.openapi.comm.utils.FileUtils;
import com.openapi.comm.utils.LogUtil;
import com.openapi.comm.utils.SQLiteHelper;
import com.openapi.ipc.sdk.IPCProviderSDK;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DnsAnalysisService {
    public static final String TAG = DnsAnalysisService.class.getSimpleName();
    private static final String OLD_DATA_DIR = "/sdcard/whitelist/";
    private static final String HISTORY_DATA_DIR = "/sdcard/whitelist/history/";

    public static class DnsInfo {
        public String pkgName;
        public String dns;

        @Override
        public String toString() {
            return CommUtils.toString(this);
        }
    }

    public static void sendEvent(IFloatingManager manager, String data) {
        try {
            manager.send(0, data.getBytes());
        } catch (Exception e) {

        }
    }

    public static void run() {
        IBinder binder = IPCProviderSDK.getInstance().getService("FloatingManager");
        final IFloatingManager manager = IFloatingManager.Stub.asInterface(binder);
        sendEvent(manager, "解析开始");

        File f = new File("/data/data/com.open.utils.case.all/bdfw.db");
        SQLiteHelper helper = new SQLiteHelper(f.getPath());
        List<String> tables = helper.getTableList();
        CommUtils.println(tables);

        List<String> heads = helper.getHead("bdurllist");
        CommUtils.println(heads);

        final List<DnsInfo> dnsInfoList = new ArrayList<>();
        helper.getTableData("bdurllist", new SQLiteHelper.IQueryCallBack() {
            @Override
            public void onQuery(Cursor cursor) {
                DnsInfo info = new DnsInfo();
                info.pkgName = cursor.getString(0);
                info.dns = cursor.getString(1);
                dnsInfoList.add(info);
            }
        });
        // CommUtils.println(dnsInfoList);

        List<String> dnsList = new ArrayList<>();
        for (DnsInfo info : dnsInfoList) {
            if (!CommUtils.isValidDns(info.dns)) {
                LogUtil.e(TAG, "not valid dns:" + info.dns);
                continue;
            }
            dnsList.add(info.dns);
        }

        Map<String, Set<String>> map = DnsParser.getInstance().parseAllDns(dnsList, 1, 5000, new DnsParser.IProgress() {
            @Override
            public void onProgress(int total, int progress, int loop, int loopIndex) {
                String data = String.format("解析DNS\nLoop:%d/%d\n%d/%d", loopIndex, loop, progress, total);
                sendEvent(manager, data);
            }
        });

        Set<String> historyDnsList = new HashSet<>();
        Set<String> historyIpList = new HashSet<>();
        getOldData(historyDnsList, historyIpList);
        LogUtil.e(TAG, "old size :" + historyDnsList.size() + "," + historyIpList.size());
        getHistoryData(historyDnsList, historyIpList);
        LogUtil.e(TAG, "old size :" + historyDnsList.size() + "," + historyIpList.size());

        List<String> newDnsList = new ArrayList<>();
        List<String> newIpList = new ArrayList<>();
        for (String key : map.keySet()) {
            if (!historyDnsList.contains(key)) {
                if (CommUtils.isIp(key)) {
                    LogUtil.e(TAG, "new dns is ip:" + key);
                    continue;
                }
                LogUtil.e(TAG, "new dns:" + key);
                newDnsList.add(key);
            }
            for (String ip : map.get(key)) {
                if (!historyIpList.contains(ip)) {
                    // LogUtil.e(TAG, "new ip :" + key + "," + ip);
                    newIpList.add(ip);
                }
            }
        }

        List<String[]> change = merge(newDnsList, newIpList);
        FileUtils.writeLines(HISTORY_DATA_DIR + "now.csv", change);

        sendEvent(manager, "解析DNS\n结束");
    }

    public static void getOldData(Set<String> dnsList, Set<String> ipList) {
        FileUtils.readLinesFromFile(OLD_DATA_DIR + "old-dns.csv", 0, dnsList);
        FileUtils.readLinesFromFile(OLD_DATA_DIR + "old-ip.csv", 0, ipList);
    }

    public static void getHistoryData(Set<String> dnsList, Set<String> ipList) {
        File dir = new File(HISTORY_DATA_DIR);
        File[] all = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().startsWith("new-dns-ip") && f.getName().endsWith(".csv");
            }
        });

        if (all == null) {
            return;
        }

        for (File f : all) {
            FileUtils.readLinesFromFile(f.getPath(), 0, dnsList);
            FileUtils.readLinesFromFile(f.getPath(), 1, ipList);
        }
    }

    public static List<String[]> merge(List<String>... dataArray) {
        List<String[]> out = new ArrayList<>();
        int len = 0;
        for (int i = 0; i < dataArray.length; ++i) {
            if (dataArray[i].size() > len) {
                len = dataArray[i].size();
            }
        }

        for (int i = 0; i < len; ++i) {
            String[] row = new String[dataArray.length];
            Arrays.fill(row, "");
            for (int j = 0; j < row.length; ++j) {
                List<String> data = dataArray[j];
                if (i < data.size()) {
                    row[j] = data.get(i);
                }
            }
            out.add(row);
        }

        return out;
    }

//    public static void merge(List<String> one, List<String> two) {
//        int len = one.size() > one.size() ? one.size() : two.size();
//        for (int i = 0; i < len; ++i) {
//            String[] row = new String[2];
//            if (i < one.size()) {
//                row[0] = one.get(i);
//            } else {
//                row[0] = "";
//            }
//
//            if (i < two.size()) {
//                row[1] = two.get(i);
//            } else {
//                row[1] = "";
//            }
//        }
//    }
}
