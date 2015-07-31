package com.deshang365.meeting.activity;

import org.codehaus.jackson.JsonNode;

import retrofit.RetrofitError;
import retrofit.client.Response;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
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

public class IdentifyCodeActivity extends BaseActivity {
	private LinearLayout mLlBack;
	private TextView mTvTopical, mTvMobile;
	private EditText mEtvIdentyfiCode;
	private Button mBtnNext, mBtnResend;
	private String mAuth;
	private int RESCODE_IDENTIFYCODE = 1;// 注册的返回码
	private String mMobile;
	private TimeCount mTimeCount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_identify_code);
		initView();
	}

	private void initView() {
		mAuth = getIntent().getStringExtra("auth");
		mMobile = getIntent().getStringExtra("mobile");
		mTvTopical = (TextView) findViewById(R.id.tv_top_alert_text);
		mTvTopical.setText("验证码");
		mLlBack = (LinearLayout) findViewById(R.id.ll_top_alert_back);
		mLlBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mTvMobile = (TextView) findViewById(R.id.txtv_mobile);
		mTvMobile.setText(mMobile);
		mEtvIdentyfiCode = (EditText) findViewById(R.id.etv_identyfi_code);
		mBtnNext = (Button) findViewById(R.id.btn_next);
		mBtnNext.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String indentifycode = mEtvIdentyfiCode.getText().toString();
				Log.i("bm", "===" + indentifycode);
				if (indentifycode.length() <= 0) {
					Toast.makeText(mContext, "请输入验证码！", Toast.LENGTH_SHORT).show();
					return;
				}
				if (indentifycode.equals(mAuth)) {
					setResult(RESCODE_IDENTIFYCODE);
					finish();
				} else {
					Toast.makeText(mContext, "验证码输入错误！", Toast.LENGTH_SHORT).show();
				}
			}
		});
		mBtnResend = (Button) findViewById(R.id.btn_resend);
		mBtnResend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showWaitingDialog();
				sendToSMS(mMobile);
				timeCountDown();
			}
		});
		timeCountDown();
	}

	public void timeCountDown() {
		mBtnResend.setBackgroundResource(R.drawable.new_black_three_radius_bg);
		mBtnResend.setEnabled(false);
		if (mTimeCount == null) {
			mTimeCount = new TimeCount(60000, 1000);
		}
		mTimeCount.start();
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

	/**
	 * 倒计时类
	 * */
	class TimeCount extends CountDownTimer {
		public TimeCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);// 参数依次为总时长,和计时的时间间隔
		}

		@Override
		public void onFinish() {// 计时完毕时触发
			mBtnResend.setText("重新发送");
			mBtnResend.setBackgroundResource(R.drawable.btn_orange_default);
			mBtnResend.setEnabled(true);
		}

		@Override
		public void onTick(long millisUntilFinished) {// 计时过程显示
			// checking.setClickable(false);
			// checking.setText(millisUntilFinished / 1000 + "秒");
			mBtnResend.setText(millisUntilFinished / 1000 + "秒");
		}
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
