package com.deshang365.meeting.activity;

import retrofit.RetrofitError;
import retrofit.client.Response;
import android.annotation.SuppressLint;
import android.content.Intent;
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

public class ChangeGroupNameActivity extends BaseActivity {
	private TextView mTvTopical;
	private LinearLayout mLlBack;
	private TextView mTvSave;
	private EditText mEtvNewName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_name);
		initView();
	}

	private String mGroupname;

	private void initView() {
		mEtvNewName = (EditText) findViewById(R.id.etv_new_name);
		final String groupid = getIntent().getStringExtra("groupid");
		int uid = getIntent().getIntExtra("uid", -1);
		mGroupname = getIntent().getStringExtra("groupname");
		mEtvNewName.setText(mGroupname);
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
					Toast.makeText(ChangeGroupNameActivity.this, "请输入名称", Toast.LENGTH_SHORT).show();
					return;
				}
				if (mGroupname.equals(name)) {
					Toast.makeText(ChangeGroupNameActivity.this, "名称与原来相同", Toast.LENGTH_SHORT).show();
					return;
				}
				showWaitingDialog();
				setGroupName(groupid, "" + MeetingApp.userInfo.uid, name);
			}
		});
		mTvTopical = (TextView) findViewById(R.id.tv_top_alert_text);
		mTvTopical.setText("更改群组名称");
		mLlBack = (LinearLayout) findViewById(R.id.ll_top_alert_back);
		mLlBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	public void setGroupName(String groupid, String uid, String groupname) {
		NewNetwork.setGroupName(groupid, uid, groupname, new OnResponse<NetworkReturn>("updategroupname_Android") {
			@Override
			public void success(NetworkReturn result, Response response) {
				super.success(result, response);
				hideWaitingDialog();
				if (result.result == 1) {
					startActivity(new Intent(ChangeGroupNameActivity.this, MainActivity.class));
				} else {
					Toast.makeText(ChangeGroupNameActivity.this, result.msg, Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void failure(RetrofitError error) {
				super.failure(error);
				hideWaitingDialog();
				Toast.makeText(ChangeGroupNameActivity.this, "修改失败", Toast.LENGTH_SHORT).show();
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
