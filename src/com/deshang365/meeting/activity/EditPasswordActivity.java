package com.deshang365.meeting.activity;

import retrofit.RetrofitError;
import retrofit.client.Response;
import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deshang365.meeting.R;
import com.deshang365.meeting.model.Network;
import com.deshang365.meeting.model.NetworkReturnBase;
import com.deshang365.meeting.network.NetworkReturn;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.network.OnResponse;

public class EditPasswordActivity extends BaseActivity {
	private EditText mEtvOriginalPwd, mEtvNewPwd, mEtvEnsureNewPwd;
	private Button mBtnEnsure;
	private LinearLayout mLlBack;
	private TextView mTvTopical;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_password);
		initView();
	}

	private void initView() {
		mTvTopical = (TextView) findViewById(R.id.tv_top_alert_text);
		mTvTopical.setText("修改密码");
		mLlBack = (LinearLayout) findViewById(R.id.ll_top_alert_back);
		mLlBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		final String password = getIntent().getStringExtra("password");
		final String username = getIntent().getStringExtra("username");
		mEtvOriginalPwd = (EditText) findViewById(R.id.etv_original_password);
		mEtvNewPwd = (EditText) findViewById(R.id.etv_new_password);
		mEtvEnsureNewPwd = (EditText) findViewById(R.id.etv_ensure_new_password);
		mBtnEnsure = (Button) findViewById(R.id.btn_ensure);
		mBtnEnsure.setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				final String originalPwd = mEtvOriginalPwd.getText().toString();
				final String newPwd = mEtvNewPwd.getText().toString();
				final String ensureNewPwd = mEtvEnsureNewPwd.getText().toString();
				if ("".equals(originalPwd)) {
					Toast.makeText(EditPasswordActivity.this, "请输入原密码！", 0).show();
					return;
				}
				if ("".equals(newPwd)) {
					Toast.makeText(EditPasswordActivity.this, "请输入新密码！", 0).show();
					return;
				}
				if ("".equals(ensureNewPwd)) {
					Toast.makeText(EditPasswordActivity.this, "请再次输入新密码！", 0).show();
					return;
				}
				if (!password.equals(originalPwd)) {
					Toast.makeText(EditPasswordActivity.this, "原密码输入错误！", 0).show();
					mEtvOriginalPwd.setText("");
					return;
				}
				if (!newPwd.equals(ensureNewPwd)) {
					Toast.makeText(EditPasswordActivity.this, "两次输入的密码不一致！", 0).show();
					mEtvNewPwd.setText("");
					mEtvEnsureNewPwd.setText("");
					return;
				}
				showWaitingDialog();
				setPassword(originalPwd, newPwd, ensureNewPwd);
				// if (Build.VERSION.SDK_INT <=
				// Build.VERSION_CODES.GINGERBREAD_MR1) {
				// new SetPasswordAsyn().execute(originalPwd, newPwd,
				// ensureNewPwd);
				// } else {
				// new
				// SetPasswordAsyn().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
				// originalPwd, newPwd, ensureNewPwd);
				// }

			}
		});
	}

	public void setPassword(String originalPwd, String newPwd, String ensureNewPwd) {
		NewNetwork.setPassword(originalPwd, newPwd, ensureNewPwd, new OnResponse<NetworkReturn>("change_pwd_Android") {
			@Override
			public void success(NetworkReturn result, Response response) {
				super.success(result, response);
				hideWaitingDialog();
				if (result.result == 1) {
					finish();
				}
				Toast.makeText(getApplication(), result.msg, Toast.LENGTH_SHORT).show();
			}

			@Override
			public void failure(RetrofitError error) {
				super.failure(error);
				hideWaitingDialog();
				Toast.makeText(getApplication(), "密码修改失败！", Toast.LENGTH_SHORT).show();
			}
		});
	}

	// class SetPasswordAsyn extends AsyncTask<String, Void, NetworkReturnBase>
	// {
	//
	// @Override
	// protected NetworkReturnBase doInBackground(String... params) {
	// return Network.setPassword(params[0], params[1], params[2]);
	//
	// }
	//
	// @Override
	// protected void onPostExecute(NetworkReturnBase result) {
	// super.onPostExecute(result);
	// hideWaitingDialog();
	// if (result == null) {
	// Toast.makeText(getApplication(), "密码修改失败！", Toast.LENGTH_SHORT).show();
	// return;
	// }
	// if (result.rescode == 1) {
	// finish();
	// }
	// Toast.makeText(getApplication(), result.message,
	// Toast.LENGTH_SHORT).show();
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
