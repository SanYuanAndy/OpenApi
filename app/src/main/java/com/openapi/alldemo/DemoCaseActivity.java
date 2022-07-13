package com.openapi.alldemo;

import android.content.ContentResolver;
import android.content.Intent;
import android.provider.Settings;
import android.widget.Toast;

import com.openapi.comm.mail.Mail;
import com.openapi.comm.mail.MailBox;
import com.openapi.comm.mail.MailBoxConf;
import com.openapi.comm.ui.CommDialog;
import com.openapi.comm.utils.FileUtils;
import com.openapi.comm.utils.HttpManager;
import com.openapi.comm.utils.JSONParser;
import com.openapi.comm.utils.LogUtil;
import com.openapi.comm.utils.WorkHandler;
import com.openapi.debugger.ActionAdapter;
import com.openapi.debugger.DebuggerActivity;
import com.openapi.debugger.MonkeyService;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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


        addAction(new ActionAdapter.Action("Http") {
            @Override
            public boolean invoke() {
                HttpManager.getInstance().request("http://www.baidu.com/", new HttpManager.RequestCallBack() {
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

        addAction(new ActionAdapter.Action("打开邮箱") {
            @Override
            public boolean invoke() {
                WorkHandler.runBgThread(new Runnable() {
                    @Override
                    public void run() {
                        String strMailJson = FileUtils.readStringFromFile("/sdcard/mail.json");
                        Mail mail = JSONParser.parseDeep(Mail.class, strMailJson);
                        MailBoxConf conf = MailBoxConf.createImapConf(mail);
                        MailBox.getBox().init(getBaseContext(), conf, new MailBox.INewMailListener() {
                            @Override
                            public void onNewMailReceived(String subject, String sender) {
                                LogUtil.d("XXX", "onNewMailReceive:" + subject + "," + sender);
                            }
                        });
                    }
                }, 0);
                return false;
            }
        });
        addAction(new ActionAdapter.Action("拉取邮件") {
            @Override
            public boolean invoke() {
                WorkHandler.runBgThread(new Runnable() {
                    @Override
                    public void run() {
                        MailBox.getBox().fetchAll();
                    }
                }, 0);
                return false;
            }
        });

        addAction(new ActionAdapter.Action("重新打开邮箱") {
            @Override
            public boolean invoke() {
                WorkHandler.runBgThread(new Runnable() {
                    @Override
                    public void run() {
                        MailBox.getBox().restart();
                    }
                }, 0);
                return false;
            }
        });

        addAction(new ActionAdapter.Action("关闭邮箱") {
            @Override
            public boolean invoke() {
                WorkHandler.runBgThread(new Runnable() {
                    @Override
                    public void run() {
                        MailBox.getBox().close();
                    }
                }, 0);
                return false;
            }
        });
    }


    public static String getDebugLabel() {
        return "DemoCaseActivity";
    }


    private void sendMail() {
        String strMailJson = FileUtils.readStringFromFile("/sdcard/mail.json");
        Mail mail = JSONParser.parseDeep(Mail.class, strMailJson);

        List<String> attachNames = new ArrayList<>();
        attachNames.add("/sdcard/mail/111.txt");
        attachNames.add("/sdcard/mail/222.txt");
        mail.setAttachFileNames(attachNames);
        mail.setSubject("111");
        mail.setContent("333");
        mail.setAttachPassword("12345678");
        boolean ret = mail.send(getApplicationContext());
        LogUtil.e("Mail", "ret=" + ret);
    }



}
