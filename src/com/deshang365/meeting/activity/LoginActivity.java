package com.deshang365.meeting.activity;

import org.codehaus.jackson.JsonNode;
import org.json.JSONObject;

import retrofit.RetrofitError;
import retrofit.client.Response;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.deshang365.meeting.R;
import com.deshang365.meeting.baselib.MeetingApp;
import com.deshang365.meeting.model.Constants;
import com.deshang365.meeting.model.GroupMemberInfo;
import com.deshang365.meeting.model.Network;
import com.deshang365.meeting.model.UserInfo;
import com.deshang365.meeting.network.NetworkReturn;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.network.OnResponse;
import com.deshang365.meeting.network.RetrofitUtils;
import com.deshang365.util.ProfileHelper;
import com.tencent.stat.StatService;

public class LoginActivity extends BaseActivity {

	private EditText mEtUserName;
	private EditText mEtPwd;
	private Button mBtLogin;
	private View mRootLayout;
	private long mExitTime = 0;
	private TextView mTvRegister, mTvForgetPwd;
	private LoginUser mLoginUser;
	private String mGroupname;
	private String mGroupid;
	private int mIsJumpToSign;// 是否跳到签到页面 0跳到 1不需要
	private String mDeshxPwd;
	private String mHxid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		initView();
	}

	private void initView() {
		mGroupname = getIntent().getStringExtra("groupname");
		mGroupid = getIntent().getStringExtra("groupid");
		mIsJumpToSign = getIntent().getIntExtra("isJumpToSign", -1);
		mTvForgetPwd = (TextView) findViewById(R.id.txtv_forget_pwd);
		mTvForgetPwd.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				StatService.trackCustomEvent(mContext, "Fpassword", "OK ");
				startActivity(new Intent(LoginActivity.this, ResetPwdActivity.class));
				finish();
			}
		});

		mRootLayout = findViewById(R.id.ll_login);
		mRootLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mRootLayout.requestFocus();
			}
		});

		mRootLayout.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			}
		});

		mEtUserName = (EditText) findViewById(R.id.edit_username);
		mEtPwd = (EditText) findViewById(R.id.edit_password);
		mTvRegister = (TextView) findViewById(R.id.txtv_register);
		mTvRegister.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
				finish();
			}
		});
		mBtLogin = (Button) findViewById(R.id.btn_login);
		mBtLogin.setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				StatService.trackCustomEvent(mContext, "login", "OK ");
				mLoginUser = new LoginUser();
				mLoginUser.loginName = mEtUserName.getText().toString();
				mLoginUser.loginName.trim();
				if (mLoginUser.loginName.isEmpty()) {
					Toast.makeText(mContext, "请输入账号", Toast.LENGTH_LONG).show();
					return;
				}

				mLoginUser.loginPwd = mEtPwd.getText().toString();
				mLoginUser.loginPwd.trim();
				if (mLoginUser.loginPwd.isEmpty()) {
					Toast.makeText(mContext, "请输入密码", Toast.LENGTH_LONG).show();
					return;
				}
				Log.e("bm", "111:::" + System.currentTimeMillis());
				showWaitingDialog();
				Log.e("bm", "222:::" + System.currentTimeMillis());
				login(mLoginUser);
			}
		});
		int isDelete = getIntent().getIntExtra("isDelete", -1);
		if (isDelete == 0) {
			mEtUserName.setText(MeetingApp.username);
			mEtPwd.setText(MeetingApp.password);
		} else {
			LoginUser loginUser = new LoginUser();
			loginUser.loginName = ProfileHelper.read(mContext, Constants.KEY_REMEMBER_NAME, "");
			mEtUserName.setText(loginUser.loginName);
			loginUser.loginPwd = ProfileHelper.read(mContext, Constants.KEY_REMEMBER_PWD, "");
			mEtPwd.setText(loginUser.loginPwd);
		}
	}

	@Override
	public void onBackPressed() {
		mRootLayout.requestFocus();
		if ((System.currentTimeMillis() - mExitTime) > 2000) {
			Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
			mExitTime = System.currentTimeMillis();
		} else {
			finish();
			System.exit(0);
		}
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

					try {
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
					} catch (Exception e) {
					}

					// XGPushManager.setTag(mContext, result.schoolId);
					ProfileHelper.insertOrUpdate(mContext, Constants.KEY_REMEMBER_NAME, mLoginUser.loginName);
					ProfileHelper.insertOrUpdate(mContext, Constants.KEY_REMEMBER_PWD, mLoginUser.loginPwd);
					ProfileHelper.insertOrUpdate(mContext, Constants.KEY_LOGGEDIN, "1");
					if (mIsJumpToSign == 0) {
						uidInGroup(mGroupid);
						// new UidInGroupAsyn().execute(mGroupid);
					} else {
						Intent intent = new Intent(mContext, MainActivity.class);
						// intent.putExtra("uid", MeetingApp.userInfo.uid);
						// intent.putExtra("hxid", result.hxid);
						startActivity(intent);
					}
					finish();
				} else {
					MeetingApp.hasLogin = false;
					ProfileHelper.insertOrUpdate(mContext, Constants.KEY_LOGGEDIN, "0");
					Toast.makeText(mContext, result.msg, Toast.LENGTH_LONG).show();
				}
			}

			@Override
			public void failure(RetrofitError error) {
				super.failure(error);
				hideWaitingDialog();
				MeetingApp.hasLogin = false;
				ProfileHelper.insertOrUpdate(mContext, Constants.KEY_LOGGEDIN, "0");
				Toast.makeText(mContext, "登录失败", Toast.LENGTH_LONG).show();
			}
		});
	}

	private void uidInGroup(String groupid) {
		NewNetwork.uidInGroup(groupid, new OnResponse<NetworkReturn>("uidingroup_Android") {
			@Override
			public void success(NetworkReturn result, Response arg1) {
				super.success(result, arg1);
				if (result.result == 1) {
					Intent intent = new Intent(LoginActivity.this, UserSignActivity.class);
					intent.putExtra("groupname", mGroupname);
					intent.putExtra("groupid", mGroupid);
					intent.putExtra("backtomain", 1);// UserSignActivity是否需要返回到main
					// 1需要 -1不需要
					startActivity(intent);
				} else if (result.result == -2) {
					GroupMemberInfo groupInfo = new GroupMemberInfo();
					JsonNode data = result.data;
					groupInfo.idcard = data.get("idcard").getValueAsText();
					groupInfo.hxgroupid = data.get("mob_code").getValueAsText();
					groupInfo.name = data.get("name").getValueAsText();
					groupInfo.uid = data.get("idcard").getValueAsInt();

					Intent intent = new Intent(LoginActivity.this, JoinGroupActivity.class);
					intent.putExtra("groupname", mGroupname);
					intent.putExtra("uid", groupInfo.uid);
					intent.putExtra("groupid", mGroupid);
					intent.putExtra("groupcode", groupInfo.idcard);
					intent.putExtra("hxgroupid", groupInfo.hxgroupid);
					intent.putExtra("isJumpToSign", mIsJumpToSign);
					startActivity(intent);
				}
			}

			@Override
			public void failure(RetrofitError error) {
				super.failure(error);
				Toast.makeText(mContext, "获取详情失败", Toast.LENGTH_SHORT).show();
			}
		});
	}

	// class UidInGroupAsyn extends AsyncTask<String, Void, GroupMemberInfo> {
	//
	// @Override
	// protected GroupMemberInfo doInBackground(String... params) {
	// return Network.uidInGroup(params[0]);
	// }
	//
	// @Override
	// protected void onPostExecute(GroupMemberInfo result) {
	// super.onPostExecute(result);
	// if (result == null) {
	// Toast.makeText(mContext, "获取详情失败！", Toast.LENGTH_SHORT).show();
	// return;
	// }
	// if (result.rescode == 1) {
	// Intent intent = new Intent(LoginActivity.this, UserSignActivity.class);
	// intent.putExtra("groupname", mGroupname);
	// intent.putExtra("groupid", mGroupid);
	// intent.putExtra("backtomain", 1);// UserSignActivity是否需要返回到main
	// // 1需要 -1不需要
	// startActivity(intent);
	// } else if (result.rescode == -2) {
	// Intent intent = new Intent(LoginActivity.this, JoinGroupActivity.class);
	// intent.putExtra("groupname", mGroupname);
	// intent.putExtra("uid", result.uid);
	// intent.putExtra("groupid", mGroupid);
	// intent.putExtra("groupcode", result.idcard);
	// intent.putExtra("hxgroupid", result.hxgroupid);
	// intent.putExtra("isJumpToSign", mIsJumpToSign);
	// startActivity(intent);
	// }
	//
	// }
	// }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
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
