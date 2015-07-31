package com.deshang365.meeting.activity;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.deshang365.meeting.R;

public class UserBlueSignActivity extends Activity {
	private LinearLayout mLlBack;
	private TextView mTvTopical;
	private Handler mHandler;
	private Timer mTimer;
	private Button mBtnBlueSign;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_blue_sign);
		initView();
	}

	private void initView() {
		mTvTopical = (TextView) findViewById(R.id.tv_top_alert_text);
		mTvTopical.setText("蓝牙签到");
		mLlBack = (LinearLayout) findViewById(R.id.ll_top_alert_back);
		mLlBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mBtnBlueSign = (Button) findViewById(R.id.btn_blue_join_sign);
		mBtnBlueSign.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

			}
		});
	}

	/**
	 * 定时发送请求
	 * */
	private void onTimeToUpdate() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

			}
		};
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				mHandler.sendMessage(mHandler.obtainMessage());
			}
		};
		mTimer = new Timer();
		mTimer.schedule(task, 20000, 20000);
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

		}

		@Override
		public void onTick(long millisUntilFinished) {// 计时过程显示

		}
	}
}
