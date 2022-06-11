package com.openapi.comm.utils;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DnsParser {
    public static DnsParser sInstance = new DnsParser();

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

    public Map<String, Set<String>> parseAllDns(List<String> dnsList, int repeatCnt, int durationMill) {
        Map<String, Set<String>> dnsIpInfoMap = new HashMap<>();
        for (int i = 0;;) {

            for (String dns : dnsList) {
                Set<String> ipSet = null;
                if (!dnsIpInfoMap.containsKey(dns)) {
                    ipSet = new HashSet<>();
                    dnsIpInfoMap.put(dns, ipSet);
                }
                ipSet = dnsIpInfoMap.get(dns);

                List<String> ipList = parseDns(dns);
                for (String ip : ipList) {
                    ipSet.add(ip);
                }
            }

            if (i >= repeatCnt -1) {
                break;
            }
            i++;
            LogUtil.e("checkWhitelist", i + "/" + repeatCnt);
            try {
                Thread.sleep(durationMill);
            } catch (Exception e) {

            }
        }

        return dnsIpInfoMap;
    }
}
