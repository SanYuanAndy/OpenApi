package com.openapi.alldemo;

import android.content.ContentResolver;
import android.content.Intent;
import android.provider.Settings;
import android.widget.Toast;

import com.openapi.alldemo.R;
import com.openapi.comm.mail.Mail;
import com.openapi.comm.mail.MailBox;
import com.openapi.comm.ui.CommDialog;
import com.openapi.comm.utils.DnsParser;
import com.openapi.comm.utils.FileUtils;
import com.openapi.comm.utils.HttpManager;
import com.openapi.comm.utils.JSONParser;
import com.openapi.comm.utils.LogUtil;
import com.openapi.comm.utils.WorkHandler;
import com.openapi.comm.utils.ZipUtils;
import com.openapi.debugger.ActionAdapter;
import com.openapi.debugger.DebuggerActivity;
import com.openapi.debugger.DnsAnalysisService;
import com.openapi.debugger.MonkeyService;

import org.json.JSONObject;

import java.io.File;
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

                if (MonkeyService.isRunning()) {
                    WorkHandler.showToast("正在运行中");
                    return true;
                }

                WorkHandler.runBgThread(new Runnable() {
                    @Override
                    public void run() {
                        MonkeyService.run(getApplication(), "/sdcard/whitelist/monkey/");
                    }
                }, 0);
                return false;
            }
        });

        addAction(new ActionAdapter.Action("stopMonkey") {
            @Override
            public boolean invoke() {
                if (MonkeyService.isRunning()) {
                    MonkeyService.stop();
                }
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

        addAction(new ActionAdapter.Action("弹窗") {
            @Override
            public boolean invoke() {
                CommDialog dialog = new CommDialog.Builder(DemoCaseActivity.this).
                        layoutId(R.layout.alert).title("注意").content("此操作不可恢复,确定要执行此操作吗?").
                        callBack(new CommDialog.DialogCallBack() {
                    @Override
                    public void onClickOk() {
                        WorkHandler.showToast("确认");
                    }
                }).
                        build();
                dialog.show();
                return false;
            }
        });

        addAction(new ActionAdapter.Action("关闭语音权限") {
            @Override
            public boolean invoke() {
                setVoiceState(false, "当前语音不可用");
                return false;
            }
        });

        addAction(new ActionAdapter.Action("打开语音权限") {
            @Override
            public boolean invoke() {
                setVoiceState(true, "");
                return false;
            }
        });

        addAction(new ActionAdapter.Action("懂你模式广播") {
            @Override
            public boolean invoke() {
                Intent intent = new Intent("com.baidu.duerosauto.scenemode_controler.call");
                intent.putExtra("cmd", "open_mode_do_not_disturb");
                sendBroadcast(intent);
                return false;
            }
        });

        addAction(new ActionAdapter.Action("发送邮件") {
            @Override
            public boolean invoke() {
                WorkHandler.runBgThread(new Runnable() {
                    @Override
                    public void run() {
                        sendMail();
                    }
                }, 0);
                return false;
            }
        });

        addAction(new ActionAdapter.Action("接收邮件") {
            @Override
            public boolean invoke() {
                WorkHandler.runBgThread(new Runnable() {
                    @Override
                    public void run() {
                        new MailBox();
                    }
                }, 0);
                return false;
            }
        });
    }

    private void setVoiceState(boolean enable, String tts) {
        ContentResolver resolver = getContentResolver();
        JSONObject json = new JSONObject();
        try {
            json.put("vrState", enable);
            json.put("tts", tts);
        } catch (Exception e) {

        }
        Settings.Global.putString(resolver, "voice.permission.enable", json.toString());
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

    private void sendMail() {
        String strMailJson = FileUtils.readStringFromFile("/sdcard/mail.json");
        Mail mail = JSONParser.parseDeep(Mail.class, strMailJson);

        List<String> attachNames = new ArrayList<>();
        attachNames.add("/sdcard/whitelist/history/new-dns-ip-2022-07-04.csv");
        attachNames.add("/sdcard/whitelist/history/new-dns-ip-2022-06-09.csv");

        mail.setAttachFileNames(attachNames);
        // mail.setAttachPassword("12345678");
        boolean ret = mail.send(getApplicationContext());
        LogUtil.e("Mail", "ret=" + ret);
    }



}
