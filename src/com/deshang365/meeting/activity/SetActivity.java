package com.deshang365.meeting.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.deshang365.meeting.R;
import com.deshang365.meeting.util.MeetingUtils;

public class SetActivity extends BaseActivity {
	private TextView mTvTopical, mTvSignMode;
	private LinearLayout mLlBack;
	private LinearLayout mLlSignMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_email);
		initView();
	}

	private void initView() {
		mTvTopical = (TextView) findViewById(R.id.tv_top_alert_text);
		mTvTopical.setText("设置");
		mLlBack = (LinearLayout) findViewById(R.id.ll_top_alert_back);
		mLlBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mTvSignMode = (TextView) findViewById(R.id.txtv_sign_mode);
		mLlSignMode = (LinearLayout) findViewById(R.id.ll_sign_mode);
		mLlSignMode.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(mContext, SignModeActivity.class));
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		int signMode = MeetingUtils.getParams();
		if (signMode == 0) {
			mTvSignMode.setText("口令签到");
		} else if (signMode == 1) {
			mTvSignMode.setText("蓝牙签到");
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

}
