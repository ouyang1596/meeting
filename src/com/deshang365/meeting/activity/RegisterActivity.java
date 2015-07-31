package com.deshang365.meeting.activity;

import org.codehaus.jackson.JsonNode;

import retrofit.RetrofitError;
import retrofit.client.Response;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
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
import com.deshang365.meeting.util.MeetingUtils;
import com.deshang365.util.ProfileHelper;
import com.easemob.chat.EMChatManager;
import com.easemob.exceptions.EaseMobException;
import com.tencent.stat.StatAppMonitor;
import com.tencent.stat.StatService;

public class RegisterActivity extends BaseActivity {
	private EditText mEtvPhoneNumber, mEtvPassward;
	private Button mBtnRegister;
	private LinearLayout mLlBack;
	private TextView mTvTopical;
	private ProgressDialog mWaitDialog;
	private LoginUser mLoginUser;
	private Dialog mPd;
	private String mDeshxPwd;
	private String mHxid;
	private String mMd5hxid;
	private String mPhonenumber;
	private View mRegisteredView;
	private String mAuth;// 短信验证码
	private final int REQUESTCODE_REGISTER = 10;
	private final int RESCODE_IDENTIFYCODE = 1;
	private final int SUCCESS = 0;// 环信注册成功
	private final int FAIL = -1;// 环信注册失败
	private AlertDialog mDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		initView();

	}

	private void initView() {
		mTvTopical = (TextView) findViewById(R.id.tv_top_alert_text);
		mTvTopical.setText("注册");
		mLlBack = (LinearLayout) findViewById(R.id.ll_top_alert_back);
		mLlBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(mContext, LoginActivity.class));
				finish();
			}
		});
		mEtvPhoneNumber = (EditText) findViewById(R.id.etv_register_phone_number);
		mEtvPassward = (EditText) findViewById(R.id.etv_register_passward);
		mBtnRegister = (Button) findViewById(R.id.btn_register);
		mBtnRegister.setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				StatService.trackCustomEvent(mContext, "register", "OK ");
				mPhonenumber = mEtvPhoneNumber.getText().toString();
				if (mPhonenumber.length() <= 0) {
					Toast.makeText(getApplication(), "请输入手机号码！", 0).show();
					return;
				}
				if (mPhonenumber.length() != 11) {
					Toast.makeText(getApplication(), "手机号码输入不正确，请重新输入！", 0).show();
					mEtvPhoneNumber.setText("");
					return;
				}
				if (mEtvPassward.length() <= 0) {
					Toast.makeText(getApplication(), "请输入密码！", 0).show();
					return;
				}
				showWaitingDialog();
				isRegistered(mPhonenumber);
				// if (Build.VERSION.SDK_INT <=
				// Build.VERSION_CODES.GINGERBREAD_MR1) {
				// new IsRegisterAsyn().execute(mPhonenumber);
				// } else {
				// new
				// IsRegisterAsyn().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
				// mPhonenumber);
				// }

			}
		});
	}

	@SuppressLint("NewApi")
	private void registerUser() {
		mMd5hxid = MeetingUtils.getMD5HXID();
		String deshxPwd = MeetingUtils.getDESHXPwd(mMd5hxid);
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
			new HXRegisterAsyn().execute(mMd5hxid, deshxPwd);
		} else {
			new HXRegisterAsyn().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mMd5hxid, deshxPwd);
		}

	}

	class HXRegisterAsyn extends AsyncTask<String, Void, Integer> {

		@Override
		protected Integer doInBackground(String... params) {
			StatAppMonitor monitor = new StatAppMonitor("hxcreateAccountOnServer_Android");
			long startTime = System.currentTimeMillis();
			try {
				EMChatManager.getInstance().createAccountOnServer(params[0], params[1]);
				long difftime = System.currentTimeMillis() - startTime;
				monitor.setMillisecondsConsume(difftime);
				monitor.setReturnCode(StatAppMonitor.SUCCESS_RESULT_TYPE);
				return SUCCESS;
			} catch (EaseMobException e) {
				monitor.setReturnCode(StatAppMonitor.FAILURE_RESULT_TYPE);
				return FAIL;
			} finally {
				StatService.reportAppMonitorStat(MeetingApp.mContext, monitor);
			}

		}

		@SuppressLint("NewApi")
		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			if (result == SUCCESS) {
				register(mEtvPhoneNumber.getText().toString(), mEtvPassward.getText().toString(), mMd5hxid);
			} else if (result == FAIL) {
				hideWaitingDialog();
				Toast.makeText(getApplication(), "注册失败！", 0).show();
			}
		}
	}

	private void register(String userName, String password, String hxid) {
		NewNetwork.register(userName, password, hxid, new OnResponse<NetworkReturn>("register_android") {
			@Override
			public void success(NetworkReturn result, Response arg1) {
				super.success(result, arg1);

				if (result.result == 1) {
					mLoginUser = new LoginUser();
					mLoginUser.loginName = mEtvPhoneNumber.getText().toString();
					mLoginUser.loginName.trim();
					if (mLoginUser.loginName.isEmpty()) {
						Toast.makeText(RegisterActivity.this, "请输入账号", Toast.LENGTH_LONG).show();
						return;
					}
					mLoginUser.loginPwd = mEtvPassward.getText().toString();
					mLoginUser.loginPwd.trim();
					if (mLoginUser.loginPwd.isEmpty()) {
						Toast.makeText(RegisterActivity.this, "请输入密码", Toast.LENGTH_LONG).show();
						return;
					}
					login(mLoginUser);
				} else {
					Toast.makeText(RegisterActivity.this, result.msg, Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void failure(RetrofitError arg0) {
				super.failure(arg0);
				Toast.makeText(getApplication(), "注册失败！", 0).show();
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

				hideWaitingDialog();
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
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESCODE_IDENTIFYCODE) {
			showWaitingDialog();
			registerUser();
		}
	}

	private static final int UNREGISTER = 2;
	private static final int REGISTERED = 1;

	private void isRegistered(String mobile) {
		NewNetwork.isRegister(mobile, new OnResponse<NetworkReturn>("user_exists_Android") {
			@Override
			public void success(NetworkReturn result, Response arg1) {
				super.success(result, arg1);
				int rescode = result.result;
				if (rescode == UNREGISTER) {
					sendToSMS(mPhonenumber);
				} else if (rescode == REGISTERED) {
					hideWaitingDialog();
					changeState();
				} else {
					hideWaitingDialog();
					Toast.makeText(mContext, result.msg, Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void failure(RetrofitError error) {
				super.failure(error);
				hideWaitingDialog();
				Toast.makeText(mContext, "网络错误", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void sendToSMS(String mobile) {
		NewNetwork.sendToSMS(mobile, new OnResponse<NetworkReturn>("send_xsms_Android") {
			@Override
			public void success(NetworkReturn result, Response arg1) {
				super.success(result, arg1);
				hideWaitingDialog();
				if (result.result == 1) {
					UserInfo userInfo = new UserInfo();
					JsonNode data = result.data;
					userInfo.auth = data.get("auth").getValueAsText();
					mAuth = userInfo.auth;
					Intent intent = new Intent(mContext, IdentifyCodeActivity.class);
					intent.putExtra("auth", mAuth);
					intent.putExtra("mobile", mPhonenumber);
					startActivityForResult(intent, REQUESTCODE_REGISTER);
					Toast.makeText(mContext, result.msg, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(mContext, result.msg, Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void failure(RetrofitError error) {
				super.failure(error);
				hideWaitingDialog();
				Toast.makeText(mContext, "短信发送失败", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void initDialog() {
		if (mDialog == null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			mDialog = builder.create();
			mDialog.setCanceledOnTouchOutside(true);
		}
	}

	private void changeState() {
		initDialog();
		if (mRegisteredView == null) {
			mRegisteredView = View.inflate(mContext, R.layout.exit_dialog, null);
		}
		Button btnLogin = (Button) mRegisteredView.findViewById(R.id.btn_exit);
		btnLogin.setText("登陆");
		TextView tvChangeState = (TextView) mRegisteredView.findViewById(R.id.tv_exit);
		tvChangeState.setText("该手机号已被注册");
		tvChangeState.setVisibility(View.VISIBLE);
		btnLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mDialog.cancel();
				startActivity(new Intent(mContext, LoginActivity.class));
				finish();
			}
		});
		Button btnCancel = (Button) mRegisteredView.findViewById(R.id.btn_cancel);
		btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mDialog.cancel();
			}
		});
		mDialog.show();
		mDialog.getWindow().setContentView(mRegisteredView);
	}

	public void onResume() {
		super.onResume();
	}

	public void onPause() {
		super.onPause();
	}

}
