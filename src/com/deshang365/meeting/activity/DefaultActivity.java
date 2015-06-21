package com.deshang365.meeting.activity;

import org.codehaus.jackson.JsonNode;

import retrofit.RetrofitError;
import retrofit.client.Response;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.deshang365.meeting.R;
import com.deshang365.meeting.baselib.MeetingApp;
import com.deshang365.meeting.model.Constants;
import com.deshang365.meeting.model.Network;
import com.deshang365.meeting.model.UserInfo;
import com.deshang365.meeting.network.NetworkReturn;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.network.OnResponse;
import com.deshang365.meeting.network.RetrofitUtils;
import com.deshang365.util.ProfileHelper;

public class DefaultActivity extends BaseActivity {
	private ImageView mImgvNavigation;
	private String deshxPwd;
	private String hxid;
	private LoginUser mLoginUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_default);
		initView();

	}

	@SuppressLint("NewApi")
	private void initView() {
		mImgvNavigation = (ImageView) findViewById(R.id.imgv_navigation);
		mLoginUser = new LoginUser();
		mLoginUser.loginName = ProfileHelper.read(mContext, Constants.KEY_REMEMBER_NAME, "");
		mLoginUser.loginPwd = ProfileHelper.read(mContext, Constants.KEY_REMEMBER_PWD, "");
		if (!("".equals(mLoginUser.loginName)) && !("".equals(mLoginUser.loginPwd))) {
			login(mLoginUser);
			return;
		}
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
			new IntentAsyn().execute();
		} else {
			new IntentAsyn().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}

	}

	class IntentAsyn extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {

			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (Network.getmLoginCookieString() != null) {
				startActivity(new Intent(DefaultActivity.this, MainActivity.class));
				finish();
				return;
			}
			DefaultActivity.this.startActivity(new Intent(DefaultActivity.this, UnLoginActivity.class));
			finish();
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
                if (result.result == 1) {
                    RetrofitUtils.setCookies(arg1);
                    MeetingApp.hasLogin = true;
                    
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
                        // TODO: handle exception
                    }

                    // XGPushManager.setTag(mContext, result.schoolId);
                    ProfileHelper.insertOrUpdate(mContext, Constants.KEY_REMEMBER_NAME, mLoginUser.loginName);
                    ProfileHelper.insertOrUpdate(mContext, Constants.KEY_REMEMBER_PWD, mLoginUser.loginPwd);
                    ProfileHelper.insertOrUpdate(mContext, Constants.KEY_LOGGEDIN, "1");
                    Intent intent = new Intent(DefaultActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    MeetingApp.hasLogin = false;
                    ProfileHelper.insertOrUpdate(mContext, Constants.KEY_LOGGEDIN, "0");
                    Toast.makeText(mContext, result.msg, Toast.LENGTH_LONG).show();
                    startActivity(new Intent(DefaultActivity.this, LoginActivity.class));
                    finish();
                }
            }
            
            @Override
            public void failure(RetrofitError arg0) {
                super.failure(arg0);
                MeetingApp.hasLogin = false;
                ProfileHelper.insertOrUpdate(mContext, Constants.KEY_LOGGEDIN, "0");
                Toast.makeText(mContext, "登录失败", Toast.LENGTH_LONG).show();
                startActivity(new Intent(DefaultActivity.this, LoginActivity.class));
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
