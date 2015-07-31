package com.deshang365.meeting.activity;

import org.codehaus.jackson.JsonNode;

import retrofit.RetrofitError;
import retrofit.client.Response;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.deshang365.meeting.network.NetworkReturn;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.network.OnResponse;
import com.deshang365.util.Installation;

public class UserSignActivity extends BaseActivity {
	private EditText mEtvUserSign;
	private TextView mTvTopical;
	private Button mBtnEnsure;
	private LinearLayout mLlBack;
	private String mGroupid;
	private String mGroupname;
	private String mAnswer;
	private int mSignTimes = 0;
	// 显示错误对话框
	private AlertDialog mDialog;
	private int mBackToMain;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_sign);
		initView();
	}

	private void initView() {
		mBackToMain = getIntent().getIntExtra("backtomain", -1);
		Log.w("bm", "backToMain == " + mBackToMain);
		mLlBack = (LinearLayout) findViewById(R.id.ll_top_alert_back);
		mLlBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mBackToMain == 1) {
					Intent intent = new Intent(mContext, MainActivity.class);
					startActivity(intent);
					return;
				}
				finish();
			}
		});
		mGroupname = getIntent().getStringExtra("groupname");
		mGroupid = getIntent().getStringExtra("groupid");
		mTvTopical = (TextView) findViewById(R.id.tv_top_alert_text);
		mTvTopical.setText("参与签到");
		mEtvUserSign = (EditText) findViewById(R.id.etv_user_sign);
		mEtvUserSign.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.length() > 4) {
					mEtvUserSign.setText(mEtvUserSign.getText().toString().substring(0, 4));
					mEtvUserSign.setSelection(4);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		mBtnEnsure = (Button) findViewById(R.id.btn_ensure);
		mBtnEnsure.setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				mAnswer = mEtvUserSign.getText().toString();
				if ("".equals(mAnswer)) {
					Toast.makeText(getApplication(), "签到码没写！", 0).show();
					return;
				}

				showWaitingDialog();
				userSign(mAnswer);
			}
		});
	}

	private void userSign(String answer) {
		NewNetwork.userSign(mGroupid, answer, MeetingApp.getLat(mSignTimes), MeetingApp.getLng(mSignTimes), Installation.id(mContext),
				new OnResponse<NetworkReturn>("sign_android") {
					@Override
					public void success(NetworkReturn result, Response arg1) {
						super.success(result, arg1);
						hideWaitingDialog();
						JsonNode data = result.data;
						String meetingId = "";
						if (data.has("meeting_id")) {
							meetingId = data.get("meeting_id").getValueAsText();
						}

						if (result.result == 1) {
							Intent intent = new Intent(mContext, LoginSignedActivity.class);
							intent.putExtra("groupid", mGroupid);
							intent.putExtra("answer", mAnswer);
							intent.putExtra("meetingid", meetingId);
							intent.putExtra("backtomain", 1);// UserSignActivity是否需要返回到main
							// 1需要返回，-1不需要返回
							startActivity(intent);
							finish();
						} else if (result.result == -5) {
							initDialog();
							String text = "";
							if (result.result == -5) {
								if (mSignTimes < 2) {
									mSignTimes++;
									userSign(mAnswer);
									return;
								} else {
									text = "不在附近！";
								}
							} else {
								text = result.msg;
							}
							View mExitView = View.inflate(mContext, R.layout.sign_wrong_dialog, null);
							Button btnExit = (Button) mExitView.findViewById(R.id.btn_ensure);
							btnExit.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									mDialog.cancel();
								}
							});
							TextView tvExit = (TextView) mExitView.findViewById(R.id.txtv_wrong);
							tvExit.setText(text);
							mDialog.show();
							mDialog.getWindow().setContentView(mExitView);

							if (mSignTimes < 2) {
								mSignTimes++;
								userSign(mAnswer);
							} else {
								Toast.makeText(mContext, "不在附近", Toast.LENGTH_SHORT).show();
							}
						} else {
							Toast.makeText(mContext, result.msg, Toast.LENGTH_SHORT).show();
						}
					}

					@Override
					public void failure(RetrofitError arg0) {
						super.failure(arg0);
						mSignTimes = 0;
						hideWaitingDialog();
						initDialog();
						View mExitView = View.inflate(mContext, R.layout.sign_wrong_dialog, null);
						Button btnExit = (Button) mExitView.findViewById(R.id.btn_ensure);
						btnExit.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								mDialog.cancel();
							}
						});
						TextView tvExit = (TextView) mExitView.findViewById(R.id.txtv_wrong);
						tvExit.setText("签到失败！");
						mDialog.show();
						mDialog.getWindow().setContentView(mExitView);
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

	// private ProgressDialog mSignWaitingDialog;
	//
	// public void showSignWaitingDialog() {
	// if (mSignWaitingDialog == null) {
	// mSignWaitingDialog = ProgressDialog.show(mContext, "", "正在签到...", true,
	// false);
	// } else if (!mSignWaitingDialog.isShowing()) {
	// mSignWaitingDialog.show();
	// }
	// }
	//
	// public void hideSignWaitingDialog() {
	// if (mSignWaitingDialog != null) {
	// mSignWaitingDialog.dismiss();
	// }
	// }

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
}
