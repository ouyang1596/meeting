package com.deshang365.meeting.activity;

import retrofit.RetrofitError;
import retrofit.client.Response;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deshang365.meeting.R;
import com.deshang365.meeting.baselib.MeetingApp;
import com.deshang365.meeting.model.Network;
import com.deshang365.meeting.model.NetworkReturnBase;
import com.deshang365.meeting.network.NetworkReturn;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.network.OnResponse;

public class ChangeShowNameActivity extends BaseActivity {
	private TextView mTvTopical;
	private LinearLayout mLlBack;
	private TextView mTvSave;
	private EditText mEtvNewName;
	private String mShowname;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_show_name);
		initView();
	}

	private void initView() {
		mEtvNewName = (EditText) findViewById(R.id.etv_new_name);
		final String groupid = getIntent().getStringExtra("groupid");
		int uid = getIntent().getIntExtra("uid", -1);
		mShowname = getIntent().getStringExtra("showname");
		mEtvNewName.setText(mShowname);
		mEtvNewName.setSelection(mEtvNewName.length());
		mTvSave = (TextView) findViewById(R.id.txtv_what_need);
		mTvSave.setText("保存");
		mTvSave.setVisibility(View.VISIBLE);
		mTvSave.setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				String name = mEtvNewName.getText().toString().trim();
				if (name.length() <= 0) {
					Toast.makeText(ChangeShowNameActivity.this, "名称不能为空！", Toast.LENGTH_SHORT).show();
					return;
				}
				if (mShowname.equals(name)) {
					Toast.makeText(ChangeShowNameActivity.this, "名称与原来相同！", Toast.LENGTH_SHORT).show();
					return;
				}
				showWaitingDialog();
				setShowName(groupid, "" + MeetingApp.userInfo.uid, name);
				// if (Build.VERSION.SDK_INT <=
				// Build.VERSION_CODES.GINGERBREAD_MR1) {
				// new UpdateShowNameAsyn().execute(groupid, "" +
				// MeetingApp.userInfo.uid, name);
				// } else {
				// new
				// UpdateShowNameAsyn().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
				// groupid, "" + MeetingApp.userInfo.uid, name);
				// }

			}
		});
		mTvTopical = (TextView) findViewById(R.id.tv_top_alert_text);
		mTvTopical.setText("更改群组昵称");
		mLlBack = (LinearLayout) findViewById(R.id.ll_top_alert_back);
		mLlBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	public void setShowName(String groupid, String uid, String showname) {
		NewNetwork.setShowName(groupid, uid, showname, new OnResponse<NetworkReturn>("updateshowname_Android") {
			@Override
			public void success(NetworkReturn result, Response response) {
				super.success(result, response);
				hideWaitingDialog();
				if (result.result == 1) {
					startActivity(new Intent(ChangeShowNameActivity.this, MainActivity.class));
				} else {
					Toast.makeText(ChangeShowNameActivity.this, result.msg, Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void failure(RetrofitError error) {
				super.failure(error);
				hideWaitingDialog();
				Toast.makeText(mContext, "修改失败", Toast.LENGTH_SHORT).show();
			}
		});

	}

	// class UpdateShowNameAsyn extends AsyncTask<String, Void,
	// NetworkReturnBase> {
	//
	// @Override
	// protected NetworkReturnBase doInBackground(String... params) {
	// return Network.updateShowName(params[0], params[1], params[2]);
	// }
	//
	// @Override
	// protected void onPostExecute(NetworkReturnBase result) {
	// super.onPostExecute(result);
	// hideWaitingDialog();
	// if (result == null) {
	// Toast.makeText(mContext, "修改失败！", Toast.LENGTH_SHORT).show();
	// return;
	// }
	// if (result.rescode == 1) {
	// startActivity(new Intent(ChangeShowNameActivity.this,
	// MainActivity.class));
	// } else {
	// Toast.makeText(ChangeShowNameActivity.this, result.message,
	// Toast.LENGTH_SHORT).show();
	// }
	//
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
