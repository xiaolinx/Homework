package com.xiaolin.homework;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.TelephonyManager;

import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;

// 录音的service
public class RecorderService extends Service {

    private static final String ACTION_IN = "android.intent.action.PHONE_STATE";
    private static final String ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL";
    private static final String EXTRA_PHONE_NUMBER = "android.intent..extra.PHONE_NUMBER";

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_OUT)) {
                phone = intent.getStringExtra(EXTRA_PHONE_NUMBER);
            } else {
                String stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                phone = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

                int state = 0;
                if (TelephonyManager.EXTRA_STATE_IDLE.equals(stateStr)) {
                    state = TelephonyManager.CALL_STATE_IDLE;
                } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(stateStr)) {
                    state = TelephonyManager.CALL_STATE_OFFHOOK;
                } else if (TelephonyManager.EXTRA_STATE_RINGING .equals(stateStr)) {
                    state = TelephonyManager.CALL_STATE_RINGING;
                }
                onCallStateChanged(state);
            }
        }
    };

    private RecordUtil recordUtil;

    private int lastState;
    private boolean isIncoming;
    private String phone;

    @Override
    public void onCreate() {
        super.onCreate();

        recordUtil = new RecordUtil();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_IN);
        filter.addAction(ACTION_OUT);
        registerReceiver(receiver, filter);

        startTimer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(receiver);
    }

    private void startTimer() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                recordUtil.startRecord(null);
                stopTimerRecord();
            }
        }, 0, 10 * 60 * 1000);
    }

    // 定时录音开始一段时间后，停止录音
    private void stopTimerRecord() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                recordUtil.stopRecord();
            }
        }, 10 * 1000); //只录10秒
    }

    private void onCallStateChanged(int state) {
        if (lastState == state) {
            return;
        }

        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false;

                    recordUtil.startRecord(phone);
                } else {
                    isIncoming = true;

                    recordUtil.startRecord(phone);
                }
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    //Ring but no pickup-  a miss
                } else if (isIncoming) {
                    recordUtil.stopRecord();
                } else {
                    recordUtil.stopRecord();
                }
                break;
        }

        lastState = state;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
