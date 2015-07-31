package com.deshang365.meeting.activity;

import org.codehaus.jackson.JsonNode;

import retrofit.RetrofitError;
import retrofit.client.Response;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deshang365.meeting.R;
import com.deshang365.meeting.model.UserInfo;
import com.deshang365.meeting.network.NetworkReturn;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.network.OnResponse;

public class ResetPwdActivity extends BaseActivity {
	private LinearLayout mLlBack;
	private TextView mTvTopical;
	private EditText mEtvPhonenumber;
	private Button mBtnNext;
	private Dialog mPd;
	private String mPhonenumber;
	private final int REQUESTCODE_RESETPWD = 11;
	private final int RESCODE_IDENTIFYCODE = 1;

	public void onResume() {
		super.onResume();
	}

	public void onPause() {
		super.onPause();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reset_pwd);
		initView();

	}

	private void initView() {
		mTvTopical = (TextView) findViewById(R.id.tv_top_alert_text);
		mTvTopical.setText("重置密码");
		mLlBack = (LinearLayout) findViewById(R.id.ll_top_alert_back);
		mLlBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(mContext, LoginActivity.class));
				finish();
			}
		});
		mEtvPhonenumber = (EditText) findViewById(R.id.etxt_phonenumber);
		mBtnNext = (Button) findViewById(R.id.btn_next);
		mBtnNext.setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				mPhonenumber = mEtvPhonenumber.getText().toString();
				if (mPhonenumber.length() <= 0) {
					Toast.makeText(getApplication(), "请输入电话号码！", 0).show();
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

	private static final int REGISTERED = 1;

	/**
	 * 判断用户是否注册
	 * */
	private void isRegistered(String mobile) {
		NewNetwork.isRegister(mobile, new OnResponse<NetworkReturn>("user_exists_Android") {
			@Override
			public void success(NetworkReturn result, Response arg1) {
				super.success(result, arg1);
				if (result.result == REGISTERED) {
					sendToSMS(mPhonenumber);
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
					Intent intent = new Intent(ResetPwdActivity.this, IdentifyCodeActivity.class);
					intent.putExtra("mobile", mPhonenumber);
					intent.putExtra("auth", userInfo.auth);
					startActivityForResult(intent, REQUESTCODE_RESETPWD);
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESCODE_IDENTIFYCODE) {
			submitUserInformation();
		}
	}

	private void submitUserInformation() {
		Intent intent = new Intent(ResetPwdActivity.this, SetPwdActivity.class);
		intent.putExtra("mobile", mPhonenumber);
		startActivity(intent);
		finish();
	}

}
