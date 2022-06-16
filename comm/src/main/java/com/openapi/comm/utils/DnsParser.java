package com.openapi.comm.utils;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DnsParser {
    public static final String TAG = DnsParser.class.getSimpleName();

    public static DnsParser sInstance = new DnsParser();

    public interface IProgress {
        int CODE_OK = 0;
        int CODE_INTERRUPTED = -1000;

        int onProgress(int total, int progress, int loop, int loopIndex);
    }

    private DnsParser() {

    }

    public static DnsParser getInstance() {
        return sInstance;
    }

    public List<String> parseDns(String domain) {
        List<String> ipList = new ArrayList<>();
        try {
            InetAddress[] remotes = InetAddress.getAllByName(domain);
            for (InetAddress remote : remotes) {
                ipList.add(remote.getHostAddress());
            }
        } catch (Exception e) {

        }
        return ipList;
    }

    public Map<String, Set<String>> parseAllDns(List<String> dnsList,
                                                int repeatCnt, int durationMill,
                                                IProgress progressCallBack) {
        Map<String, Set<String>> dnsIpInfoMap = new HashMap<>();
        if (progressCallBack == null) {
            return dnsIpInfoMap;
        }


        for (int i = 0;;) {

            for (int j = 0; j < dnsList.size(); ++j) {
                long begin = System.currentTimeMillis();
                String dns = dnsList.get(j);
                Set<String> ipSet = null;
                if (!dnsIpInfoMap.containsKey(dns)) {
                    ipSet = new HashSet<>();
                    dnsIpInfoMap.put(dns, ipSet);
                }
                ipSet = dnsIpInfoMap.get(dns);

                List<String> ipList = parseDns(dns);

                if (System.currentTimeMillis() - begin > 2000) {
                    LogUtil.e(TAG, "cost too much time:" + dns);
                }
                for (String ip : ipList) {
                    ipSet.add(ip);
                }

                int code = progressCallBack.onProgress(dnsList.size(), j + 1, repeatCnt, i + 1);
                if (code == IProgress.CODE_INTERRUPTED) {
                    i = repeatCnt;
                    break;
                }
            }

            if (i >= repeatCnt -1) {
                break;
            }
            i++;
            synchronized (progressCallBack) {
                try {
                    progressCallBack.wait(durationMill);
                } catch (Exception e) {
                    break;
                }
            }
        }

        return dnsIpInfoMap;
    }

    public void interrupt(IProgress progress) {
        if (progress != null) {
            synchronized (progress) {
                progress.notifyAll();
            }
        }
    }
}
