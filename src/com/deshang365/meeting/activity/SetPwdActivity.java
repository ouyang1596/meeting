package com.deshang365.meeting.activity;

import org.codehaus.jackson.JsonNode;

import retrofit.RetrofitError;
import retrofit.client.Response;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deshang365.meeting.R;
import com.deshang365.meeting.baselib.MeetingApp;
import com.deshang365.meeting.model.Constants;
import com.deshang365.meeting.model.UserInfo;
import com.deshang365.meeting.network.NetworkReturn;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.network.OnResponse;
import com.deshang365.meeting.network.RetrofitUtils;
import com.deshang365.util.ProfileHelper;
import com.tencent.stat.StatService;

public class SetPwdActivity extends BaseActivity {
	private LinearLayout mLlBack;
	private TextView mTvTopical, mTvPhonenumber;
	private EditText mEtxtNewPwd;
	private Button mBtnEnsure;
	private LoginUser mLoginUser;
	private String mPhonenumber;
	private String mDeshxPwd;
	private String mHxid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_pwd);
		initView();
	}

	private void initView() {
		mPhonenumber = getIntent().getStringExtra("mobile");
		mTvPhonenumber = (TextView) findViewById(R.id.txtv_phonenumber);
		mTvPhonenumber.setText("手机号码：" + mPhonenumber);
		mTvTopical = (TextView) findViewById(R.id.tv_top_alert_text);
		mTvTopical.setText("设置密码");
		mLlBack = (LinearLayout) findViewById(R.id.ll_top_alert_back);
		mLlBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		mEtxtNewPwd = (EditText) findViewById(R.id.etxt_new_pwd);

		mBtnEnsure = (Button) findViewById(R.id.btn_ensure);
		mBtnEnsure.setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				StatService.trackCustomEvent(mContext, "Cpassword", "OK ");
				if (mEtxtNewPwd.length() <= 0) {
					Toast.makeText(getApplication(), "请输入电话号码！", 0).show();
					return;
				}
				showWaitingDialog();
				setNewPwd(mPhonenumber, mEtxtNewPwd.getText().toString());
			}
		});
	}

	private void setNewPwd(String mobile, String password) {
		NewNetwork.setNewPwd(mobile, password, new OnResponse<NetworkReturn>("fchange_pwd_Android") {
			@Override
			public void success(NetworkReturn result, Response arg1) {
				super.success(result, arg1);
				hideWaitingDialog();
				if (result.result == 1) {
					mLoginUser = new LoginUser();
					mLoginUser.loginName = mPhonenumber;
					mLoginUser.loginName.trim();
					mLoginUser.loginPwd = mEtxtNewPwd.getText().toString();
					mLoginUser.loginPwd.trim();
					login(mLoginUser);
				} else {
					finish();
					Toast.makeText(getApplication(), result.msg, Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void failure(RetrofitError error) {
				super.failure(error);
				hideWaitingDialog();
				Toast.makeText(getApplication(), "密码设置失败", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private class LoginUser {
		String loginName;
		String loginPwd;
	}

	private void login(LoginUser user) {
		MeetingApp.username = user.loginName;
		MeetingApp.password = user.loginPwd;

		NewNetwork.login(user.loginName, user.loginPwd, new OnResponse<NetworkReturn>("login_android") {
			@Override
			public void success(NetworkReturn result, Response arg1) {
				super.success(result, arg1);
				hideWaitingDialog();
				if (result.result == 1) {
					MeetingApp.hasLogin = true;
					RetrofitUtils.setCookies(arg1);
					JsonNode rsData = result.data;
					MeetingApp.userInfo = new UserInfo();

					MeetingApp.userInfo.uid = rsData.get("uid").getValueAsInt();
					Log.i("bm", "uid==" + MeetingApp.userInfo.uid);
					if (rsData.has("email")) {
						MeetingApp.userInfo.email = rsData.get("email").getValueAsText();
					}
					if (rsData.has("username")) {
						MeetingApp.userInfo.name = rsData.get("username").getValueAsText();
					}
					if (rsData.has("mob_code")) {
						MeetingApp.userInfo.hxid = rsData.get("mob_code").getValueAsText();
					}

					ProfileHelper.insertOrUpdate(mContext, Constants.KEY_REMEMBER_NAME, mLoginUser.loginName);
					ProfileHelper.insertOrUpdate(mContext, Constants.KEY_REMEMBER_PWD, mLoginUser.loginPwd);
					ProfileHelper.insertOrUpdate(mContext, Constants.KEY_LOGGEDIN, "1");
					Intent intent = new Intent(mContext, MainActivity.class);
					startActivity(intent);
					finish();
				} else {
					MeetingApp.hasLogin = false;
					ProfileHelper.insertOrUpdate(mContext, Constants.KEY_LOGGEDIN, "0");
					Toast.makeText(mContext, result.msg, Toast.LENGTH_LONG).show();
					startActivity(new Intent(mContext, LoginActivity.class));
					finish();
				}
			}

			@Override
			public void failure(RetrofitError arg0) {
				hideWaitingDialog();
				super.failure(arg0);
				MeetingApp.hasLogin = false;
				ProfileHelper.insertOrUpdate(mContext, Constants.KEY_LOGGEDIN, "0");
				Toast.makeText(mContext, "登录失败", Toast.LENGTH_LONG).show();
				startActivity(new Intent(mContext, LoginActivity.class));
				finish();
			}
		});

		showWaitingDialog();
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
