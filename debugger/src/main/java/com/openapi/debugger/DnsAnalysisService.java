package com.openapi.debugger;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.openapi.comm.utils.CommUtils;
import com.openapi.comm.utils.DnsParser;
import com.openapi.comm.utils.FileUtils;
import com.openapi.comm.utils.LogUtil;
import com.openapi.comm.utils.SQLiteHelper;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DnsAnalysisService {
    public static final String TAG = DnsAnalysisService.class.getSimpleName();
    private static final String ROOT_DIR = "/sdcard/whitelist/";
    private static final String OLD_DATA_DIR = ROOT_DIR;
    private static final String HISTORY_DATA_DIR = ROOT_DIR + "history/";

    public static class DnsInfo {
        public String pkgName;
        public String dns;

        @Override
        public String toString() {
            return CommUtils.toString(this);
        }
    }

    public static void run(final Context cxt) {
        UIManager.getInstance().sendFloatingText("解析开始");

        File f = new File(cxt.getApplicationInfo().dataDir + "/bdfw.db");
        SQLiteHelper helper = new SQLiteHelper(f.getPath());
        List<String> tables = helper.getTableList();
        CommUtils.println(tables);

        List<String> heads = helper.getHead("bdurllist");
        CommUtils.println(heads);

        final List<DnsInfo> dnsInfoList = new ArrayList<>();
        final String prefix = CommUtils.getMetaValue(cxt, "dns_filter_pkg_name_prefix");
        LogUtil.e(TAG, "prefix:" + prefix);
        helper.getTableData("bdurllist", new SQLiteHelper.IQueryCallBack() {
            @Override
            public void onQuery(Cursor cursor) {
                DnsInfo info = new DnsInfo();
                info.pkgName = cursor.getString(0);
                info.dns = cursor.getString(1);
                if (!TextUtils.isEmpty(prefix) && !info.pkgName.startsWith(prefix)) {
                    LogUtil.e(TAG, "is not "  + prefix + ":"+ info.pkgName + "," + info.dns);
                    return;
                }
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

        Map<String, Set<String>> map = DnsParser.getInstance().parseAllDns(dnsList, 3, 5000, new DnsParser.IProgress() {
            @Override
            public int onProgress(int total, int progress, int loop, int loopIndex) {
                String data = String.format("解析DNS (%d/%d) %d/%d", loopIndex, loop, progress, total);
                UIManager.getInstance().sendFloatingText(data);
                return 0;
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

        List<String[]> change = CommUtils.merge(newDnsList, newIpList);
        FileUtils.writeLines(HISTORY_DATA_DIR + "now.csv", change);

        UIManager.getInstance().
                sendFloatingText(
                        String.format("解析结束, 新增域名%d个, 新增IP%d个", newDnsList.size(), newIpList.size()));
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
}
