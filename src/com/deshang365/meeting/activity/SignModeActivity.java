package com.deshang365.meeting.activity;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.deshang365.meeting.R;
import com.deshang365.meeting.model.Constants;
import com.deshang365.meeting.util.MeetingUtils;

public class SignModeActivity extends BaseActivity {
	private TextView mTvTopical;
	private LinearLayout mLlBack;
	private LinearLayout mLlBluetoothSign, mLlWordSign;
	private ImageView mImgvBluetoothSign, mImgvWordSign;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_mode);
		initView();
	}

	private void initView() {
		mTvTopical = (TextView) findViewById(R.id.tv_top_alert_text);
		mTvTopical.setText("签到方式");
		mLlBack = (LinearLayout) findViewById(R.id.ll_top_alert_back);
		mLlBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mImgvBluetoothSign = (ImageView) findViewById(R.id.imgv_bluetooth_sign_Selected);
		mImgvWordSign = (ImageView) findViewById(R.id.imgv_word_sign_Selected);
		int params = MeetingUtils.getParams();// 获得签到方式
		if (params == 0) {
			mImgvBluetoothSign.setVisibility(View.GONE);
			mImgvWordSign.setVisibility(View.VISIBLE);
		} else if (params == 1) {
			mImgvBluetoothSign.setVisibility(View.VISIBLE);
			mImgvWordSign.setVisibility(View.GONE);
		}
		mLlBluetoothSign = (LinearLayout) findViewById(R.id.ll_sign_mode_bluetooth);
		mLlBluetoothSign.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				MeetingUtils.saveParams(1);
				mImgvBluetoothSign.setVisibility(View.VISIBLE);
				mImgvWordSign.setVisibility(View.GONE);
			}
		});
		mLlWordSign = (LinearLayout) findViewById(R.id.ll_sign_mode_word);
		mLlWordSign.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				MeetingUtils.saveParams(0);
				mImgvBluetoothSign.setVisibility(View.GONE);
				mImgvWordSign.setVisibility(View.VISIBLE);
			}
		});
	}

}
