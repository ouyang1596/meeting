package com.deshang365.meeting.activity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit.RetrofitError;
import retrofit.client.Response;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deshang365.meeting.R;
import com.deshang365.meeting.baselib.MeetingApp;
import com.deshang365.meeting.network.NetworkReturn;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.network.OnResponse;
import com.tencent.stat.StatService;

public class ChangeDataActivity extends BaseActivity {
	private LinearLayout mLlBack;
	private TextView mTvTopical, mTvSave;
	private EditText mEtvDefaultNickname, mEtvEmail;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_data);
		initView();
	}

	private void initView() {
		mTvTopical = (TextView) findViewById(R.id.tv_top_alert_text);
		mTvTopical.setText("个人资料");
		mLlBack = (LinearLayout) findViewById(R.id.ll_top_alert_back);
		mLlBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mEtvEmail = (EditText) findViewById(R.id.etv_email);
		if (MeetingApp.userInfo != null) {
			mEtvEmail.setText(MeetingApp.userInfo.email);
		}
		mEtvDefaultNickname = (EditText) findViewById(R.id.etv_nickname);
		mEtvDefaultNickname.setText(MeetingApp.userInfo.name);
		mEtvDefaultNickname.setSelection(mEtvDefaultNickname.length());
		mTvSave = (TextView) findViewById(R.id.txtv_what_need);
		mTvSave.setText("保存");
		mTvSave.setVisibility(View.VISIBLE);
		mTvSave.setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				StatService.trackCustomEvent(mContext, "Gnickname", "OK");
				String nickname = mEtvDefaultNickname.getText().toString();
				String email = mEtvEmail.getText().toString();
				if (nickname.length() <= 0) {
					Toast.makeText(mContext, "昵称不能为空", Toast.LENGTH_SHORT).show();
					return;
				}
				if (email.length() <= 0) {
					Toast.makeText(mContext, "邮箱地址不能为空", Toast.LENGTH_SHORT).show();
					return;
				}
				// if (!isMatches(email)) {
				// Toast.makeText(mContext, "请输入正确邮箱！",
				// Toast.LENGTH_SHORT).show();
				// return;
				// }
				showWaitingDialog();
				changeUserInfo(nickname, email);
			}
		});
	}

	public void changeUserInfo(final String nickName, final String email) {
		NewNetwork.changeUserInfo(nickName, email, new OnResponse<NetworkReturn>("change_userinfo_Android") {
			@Override
			public void success(NetworkReturn result, Response arg1) {
				super.success(result, arg1);
				hideWaitingDialog();
				if (result.result != 1) {
					Toast.makeText(mContext, result.msg, Toast.LENGTH_SHORT).show();
					return;
				}
				if (MeetingApp.userInfo != null) {
					MeetingApp.userInfo.name = nickName;
					MeetingApp.userInfo.email = email;
				}
				Toast.makeText(mContext, result.msg, Toast.LENGTH_SHORT).show();
				finish();
			}

			@Override
			public void failure(RetrofitError error) {
				super.failure(error);
				hideWaitingDialog();
				Toast.makeText(mContext, "修改失败", Toast.LENGTH_SHORT).show();
			}
		});

	}

	public boolean isMatches(String str) {
		// 邮箱匹配
		String check = "^([a-z0-9A-Z]+[-_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
		Pattern p = Pattern.compile(check);
		Matcher m = p.matcher(str);
		return m.matches();
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
