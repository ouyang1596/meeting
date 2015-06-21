package com.deshang365.meeting.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import retrofit.RetrofitError;
import retrofit.client.Response;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.deshang365.meeting.baselib.BluetoothManager;
import com.deshang365.meeting.network.NetworkReturn;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.network.OnResponse;
import com.deshang365.util.Installation;

public class TimeToUploadService extends Service {
    private List<String> mHasUploadBluetoothList;
    private String mCurMeetingId = "";
    private BluetoothManager mBluetoothManager;

    /**
     * start可以被多次调用
     * */
    @Override
    public void onStart(Intent intent, int startId) {
        mBluetoothManager = BluetoothManager.getInstance();
        super.onStart(intent, startId);
        if (intent == null) {
            return;
        }
        if (mHasUploadBluetoothList == null) {
            mHasUploadBluetoothList = new ArrayList<String>();
        }
        String meetingid = intent.getStringExtra("meetingid");
        if (meetingid == null) {
            return;
        }
        if (!meetingid.equals(mCurMeetingId)) {
            mHasUploadBluetoothList.clear();
            mCurMeetingId = intent.getStringExtra("meetingid");
        }
        new Async(intent.getStringExtra("groupid"));
    }

    class Async {
        private Handler mHandler;
        private Timer mTimer;
        private String mGroupid;
        StopReceive mStopReceive;
        TimeCount mTimeCount;

        public Async(String groupid) {
            mStopReceive = new StopReceive();
            IntentFilter intentFilter = new IntentFilter("stopreceive");
            registerReceiver(mStopReceive, intentFilter);
            mGroupid = groupid;
            startTimer();
            if (mGroupid.equals(mCurMeetingId)) {
                mTimeCount = new TimeCount(10 * 60 * 1000, 1000);
            } else {
                mTimeCount = new TimeCount(2 * 60 * 1000, 1000);
            }
            mTimeCount.start();
        }

        /**
         * 获取蓝牙搜索列表数据
         * */
        private String getBluetoothInfoStr() {
            Map<String, String> unuploadBluetoothMap = mBluetoothManager.mUnUploadBluetoothMap;
            if (unuploadBluetoothMap != null && unuploadBluetoothMap.size() > 0) {
                StringBuilder builder = new StringBuilder();
                Set<Entry<String, String>> entrySet = unuploadBluetoothMap.entrySet();
                for (Entry<String, String> entry : entrySet) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    String format = "{\"uid\":\"" + value + "\",\"addr\":\"" + key + "\"},";
                    // 不包含加入，包含不加入
                    if (!mHasUploadBluetoothList.contains(key)) {
                        mHasUploadBluetoothList.add(key);
                        builder.append(format);
                    }
                    // builder.append(format);
                }
                String builderString = builder.toString();
                if (builderString != null && builderString.length() > 0) {
                    String substring = builderString.substring(0, builderString.length() - 1);
                    return "\"bluetooths\":[" + substring + "]";
                }
            }
            return "";
        }

        private void uploadBlueToothsInfo() {
            Log.i("bm", "----------" + mGroupid + "-------------");
            String blueToothInfoStr = getBluetoothInfoStr();
            if (blueToothInfoStr.isEmpty()) {
                return;
            }
            String macAddress = BluetoothManager.getInstance().getMacAddress();
            NewNetwork.uploadBlueToothInfo(mGroupid, Installation.id(getApplicationContext()), blueToothInfoStr,
                    macAddress, new OnResponse<NetworkReturn>("upload_bluetooth_info_android") {
                        @Override
                        public void success(NetworkReturn arg0, Response arg1) {
                            super.success(arg0, arg1);
                            if (arg0.result != 1) {
                                mHasUploadBluetoothList.clear();
                            }
                        }

                        @Override
                        public void failure(RetrofitError arg0) {
                            super.failure(arg0);
                             mHasUploadBluetoothList.clear();
                        }
                    });
        }

        /**
         * 定时发送请求
         * */
        private void startTimer() {
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    uploadBlueToothsInfo();
                }
            };
            TimerTask task = new TimerTask() {

                @Override
                public void run() {
                    mHandler.sendMessage(mHandler.obtainMessage());
                }
            };
            mTimer = new Timer();
            mTimer.schedule(task, 0, 20000);
        }

        /**
         * 倒计时类
         * */
        class TimeCount extends CountDownTimer {
            public TimeCount(long millisInFuture, long countDownInterval) {
                super(millisInFuture, countDownInterval);// 参数依次为总时长,和计时的时间间隔
            }

            @Override
            public void onFinish() {// 计时完毕时触发
                mTimer.cancel();
            }

            @Override
            public void onTick(long millisUntilFinished) {// 计时过程显示
                // mBtnResend.setText(millisUntilFinished / 1000 + "秒");
            }
        }

        /**
         * 停止发送请求活动
         * */
        class StopReceive extends BroadcastReceiver {

            @Override
            public void onReceive(Context context, Intent intent) {
                int signerType = intent.getIntExtra("receivestate", -1);// 0创建者停止签到1应用推出
                // 创建者停止签到，结束单个对应receive
                if (signerType == 0) {
                    String groupid = intent.getStringExtra("groupid");
                    if (groupid != null && groupid.equals(mGroupid)) {
                        cancelAction();
                    }
                    // 应用退出的时候,结束所有的recerive
                } else if (signerType == 1) {
                    cancelAction();
                    stopSelf();
                }
            }

            private void cancelAction() {
                mTimer.cancel();
                mTimeCount.cancel();
                unregisterReceiver(mStopReceive);
                mCurMeetingId = "";
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("bm", "=======destroy");
    }

}
