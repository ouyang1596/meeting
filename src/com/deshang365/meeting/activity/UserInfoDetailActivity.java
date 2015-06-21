package com.deshang365.meeting.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.deshang365.meeting.R;
import com.deshang365.meeting.baselib.ImageHandle;
import com.deshang365.meeting.baselib.MeetingApp;
import com.deshang365.meeting.model.Constants;

public class UserInfoDetailActivity extends BaseActivity {
	private TextView mTextView;
	private ImageView mAvatar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_info_detail);

		mAvatar = (ImageView) findViewById(R.id.iv_user_head);
		// 暂时报错
		Bitmap bitmap = ImageHandle.getLoacalBitmap(Constants.AVATAR_PATH + MeetingApp.userInfo.uid);
		if (bitmap != null) {
			mAvatar.setImageBitmap(bitmap);
		}

		mTextView = (TextView) findViewById(R.id.tv_top_alert_text);
		mTextView.setText("个人资料");
		mTextView = (TextView) findViewById(R.id.tv_user_nickname);
		mTextView.setText(MeetingApp.userInfo.name);
		// mTextView = (TextView) findViewById(R.id.tv_user_realname);
		// mTextView.setText(AppData.userInfo.cname);
		mTextView = (TextView) findViewById(R.id.tv_user_major);
		mTextView.setText(MeetingApp.userInfo.major);
		mTextView = (TextView) findViewById(R.id.tv_user_department);
		mTextView.setText(MeetingApp.userInfo.department);
		mTextView = (TextView) findViewById(R.id.tv_user_slogan);
		mTextView.setText(MeetingApp.userInfo.slogan);
		mTextView = (TextView) findViewById(R.id.tv_user_mobile);
		mTextView.setText(MeetingApp.userInfo.mobile);
		mTextView = (TextView) findViewById(R.id.tv_user_email);
		mTextView.setText(MeetingApp.userInfo.email);

		final View backView = findViewById(R.id.ll_top_alert_back);
		backView.setVisibility(View.VISIBLE);
		backView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				backView.setVisibility(View.INVISIBLE);
				finish();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
}
