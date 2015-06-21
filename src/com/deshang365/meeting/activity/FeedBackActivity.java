package com.deshang365.meeting.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deshang365.meeting.R;

public class FeedBackActivity extends BaseActivity {
	private LinearLayout mLlBack;
	private TextView mTvTopical, mTvSend;
	private EditText mEtvMessage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feed_back);
		initView();
	}

	private void initView() {
		mTvTopical = (TextView) findViewById(R.id.tv_top_alert_text);
		mTvTopical.setText("意见反馈");
		mLlBack = (LinearLayout) findViewById(R.id.ll_top_alert_back);
		mLlBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mEtvMessage = (EditText) findViewById(R.id.etv_message);
		mTvSend = (TextView) findViewById(R.id.txtv_what_need);
		mTvSend.setText("发送");
		mTvSend.setVisibility(View.VISIBLE);
		mTvSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String message = mEtvMessage.getText().toString();
				if (message.length() <= 0) {
					Toast.makeText(mContext, "您还未写下意见", Toast.LENGTH_SHORT).show();
					return;
				}
			}
		});
	}
}
