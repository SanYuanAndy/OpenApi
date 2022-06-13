package com.example.myapplication;

import android.widget.Toast;

import com.openapi.comm.utils.DnsParser;
import com.openapi.comm.utils.HttpManager;
import com.openapi.comm.utils.LogUtil;
import com.openapi.comm.utils.WorkHandler;
import com.openapi.debugger.ActionAdapter;
import com.openapi.debugger.DebuggerActivity;
import com.openapi.debugger.DnsAnalysisService;
import com.openapi.debugger.MonkeyService;

import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DemoCaseActivity extends DebuggerActivity {

    @Override
    protected void init() {
        addAction(new ActionAdapter.Action("显示Toast") {
            @Override
            public boolean invoke() {
                Toast.makeText(getBaseContext(), "invoke", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        addAction(new ActionAdapter.Action("第二个接口") {
            @Override
            public boolean invoke() {
                Toast.makeText(getBaseContext(), "invoke", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        addAction(new ActionAdapter.Action("Toast测试") {
            @Override
            public boolean invoke() {
                Toast.makeText(getBaseContext(), "invoke", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        addAction(new ActionAdapter.Action("Http") {
            @Override
            public boolean invoke() {
                HttpManager.getInstance().request("http://customer.cx580.com/", new HttpManager.RequestCallBack() {
                    @Override
                    public void onError(int code, String errMsg) {
                        LogUtil.e("checkWhitelist onError", "code:" + code + ", errMsg:" + errMsg);
                    }

                    @Override
                    public void onSuccess(int code, String bodyString) {
                        LogUtil.e("checkWhitelist onSuccess", "code:" + code + ", body:" + bodyString);
                    }

                });
                return false;
            }
        });

        addAction(new ActionAdapter.Action("monkey") {
            @Override
            public boolean invoke() {
                WorkHandler.runBgThread(new Runnable() {
                    @Override
                    public void run() {
                        MonkeyService.run(getApplication());
                    }
                }, 0);
                return false;
            }
        });

        addAction(new ActionAdapter.Action("getDnsAndConnect") {
            @Override
            public boolean invoke() {
                WorkHandler.runBgThread(new Runnable() {
                    @Override
                    public void run() {
                        checkWhitelist();
                    }
                }, 0);
                return false;
            }
        });

        addAction(new ActionAdapter.Action("parseDns") {
            @Override
            public boolean invoke() {
                WorkHandler.runBgThread(new Runnable() {
                    @Override
                    public void run() {
                        List<String> dnsList = new ArrayList<>();
                        dnsList.add("download.ali.xmcdn.com");
                        dnsList.add("oss.cx580.com");
                        dnsList.add("oss.cx580.com.w.kunlunaq.com");
                        dnsList.add("customer.cx580.com");
                        dnsList.add("violation-bapi.cx580.com");
                        dnsList.add("nlchshjapi.cx580.com");
                        Map<String, Set<String>> map = DnsParser.getInstance().parseAllDns(dnsList, 10, 5000, null);
                        for (String key : map.keySet()) {
                            for (String ip : map.get(key)) {
                                LogUtil.e("checkWhitelist", key + "," + ip);

                            }
                        }
                    }
                }, 0);
                return false;
            }
        });

        addAction(new ActionAdapter.Action("parseDB") {
            @Override
            public boolean invoke() {
                WorkHandler.runBgThread(new Runnable() {
                    @Override
                    public void run() {
                        DnsAnalysisService.run(getApplication());
                    }
                }, 0);
                return false;
            }
        });

    }

    public static String getDebugLabel() {
        return "DemoCaseActivity";
    }

    private static void checkWhitelist() {
        try {
            InetAddress[] remotes = InetAddress.getAllByName("download.ali.xmcdn.com");
            for (InetAddress remote : remotes) {
                LogUtil.e("checkWhitelist", "address:" + remote);
                try {
                    Socket socket = new Socket(remote.getHostAddress(), 80);
                    LogUtil.e("checkWhitelist", "connect");
                } catch (Exception e) {
                    LogUtil.e("checkWhitelist", e.toString() + ":" + remote.getHostAddress());
                }
            }
        } catch (Exception e) {
            LogUtil.e("checkWhitelist", e.toString());
            e.printStackTrace();
        }
    }



}
