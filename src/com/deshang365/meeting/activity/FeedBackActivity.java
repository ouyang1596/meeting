package com.deshang365.meeting.activity;

import retrofit.RetrofitError;
import retrofit.client.Response;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deshang365.meeting.R;
import com.deshang365.meeting.network.NetworkReturn;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.network.OnResponse;

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
		mTvSend.setText("提交");
		mTvSend.setVisibility(View.VISIBLE);
		mTvSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String message = mEtvMessage.getText().toString();
				if (message.length() <= 0) {
					Toast.makeText(mContext, "您还未写下意见", Toast.LENGTH_SHORT).show();
					return;
				}
				showWaitingDialog();
				feedBack(message);
			}
		});
	}

	private void feedBack(String message) {
		NewNetwork.feedBack(message, new OnResponse<NetworkReturn>("send_feed_Android") {
			@Override
			public void success(NetworkReturn result, Response arg1) {
				super.success(result, arg1);
				hideWaitingDialog();
				if (result.result == 1) {
					finish();
				}
				Toast.makeText(mContext, result.msg, Toast.LENGTH_SHORT).show();
			}

			@Override
			public void failure(RetrofitError error) {
				super.failure(error);
				hideWaitingDialog();
				Toast.makeText(mContext, "上传失败", Toast.LENGTH_SHORT).show();
			}
		});
	}
}
