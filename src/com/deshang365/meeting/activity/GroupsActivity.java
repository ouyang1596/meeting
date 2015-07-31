package com.deshang365.meeting.activity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.jackson.JsonNode;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit.RetrofitError;
import retrofit.client.Response;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deshang365.meeting.R;
import com.deshang365.meeting.baselib.MeetingApp;
import com.deshang365.meeting.model.GroupMemberInfo;
import com.deshang365.meeting.network.NetworkReturn;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.network.OnResponse;
import com.deshang365.meeting.util.MeetingUtils;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;
import com.tencent.stat.StatAppMonitor;
import com.tencent.stat.StatService;
import com.zxing.activity.CaptureActivity;

public class GroupsActivity extends BaseActivity {
	private EditText mEtvCreateGroupName, mEtvCreateGroupNickname, mEtvJoinGroupcode;
	private Button mBtnCreatGroup, mBtnJoinGroup;
	private LinearLayout mRelCreateGroups;
	private LinearLayout mLlJoinGroups, mLlScan, mLlNearSignGroups;
	private TextView mTvTopical;
	private LinearLayout mLlBack;
	private String mHxGroupId;
	// private int mIsScanOrGroupcode = -1;// 0 群组码加群 1扫描加群
	private int REQUESTCODE_SCAN = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_groups);
		initView();
	}

	private GroupMemberInfo res;

	private void initView() {
		mLlNearSignGroups = (LinearLayout) findViewById(R.id.ll_near_sign_group);
		mLlNearSignGroups.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				StatService.trackCustomEvent(mContext, "Ngroup", "OK");
				Intent intent = new Intent(GroupsActivity.this, NearSignGroupsActivity.class);
				startActivity(intent);
				finish();
			}
		});
		mLlBack = (LinearLayout) findViewById(R.id.ll_top_alert_back);
		mLlBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mTvTopical = (TextView) findViewById(R.id.tv_top_alert_text);
		mRelCreateGroups = (LinearLayout) findViewById(R.id.rel_createGroups);
		mEtvCreateGroupName = (EditText) findViewById(R.id.etv_groupName);
		mEtvCreateGroupName.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String editable = mEtvCreateGroupName.getText().toString();
				String str = stringFilter(editable.toString());
				if (!editable.equals(str)) {
					mEtvCreateGroupName.setText(str);
					// 设置新的光标所在位置
					mEtvCreateGroupName.setSelection(str.length());
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		mEtvCreateGroupNickname = (EditText) findViewById(R.id.etv_group_nickname);
		if (MeetingApp.userInfo != null) {
			mEtvCreateGroupNickname.setText(MeetingApp.userInfo.name);
		}
		mEtvCreateGroupNickname.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String editable = mEtvCreateGroupNickname.getText().toString();
				String str = stringFilter(editable.toString());
				if (!editable.equals(str)) {
					mEtvCreateGroupNickname.setText(str);
					// 设置新的光标所在位置
					mEtvCreateGroupNickname.setSelection(str.length());
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		mBtnCreatGroup = (Button) findViewById(R.id.btn_creatGroups);
		mBtnCreatGroup.setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				StatService.trackCustomEvent(mContext, "Cgroup", "OK");
				final String groupName = mEtvCreateGroupName.getText().toString();
				final String groupNickname = mEtvCreateGroupNickname.getText().toString();
				if ("".equals(groupName)) {
					Toast.makeText(getApplication(), "请输入群组名称", 0).show();
					return;
				}
				if ("".equals(groupNickname)) {
					Toast.makeText(getApplication(), "请输入群组昵称", 0).show();
					return;
				}
				showWaitingDialog();
				if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
					new CreatHXGroupsTasks().execute(groupName, "...");
				} else {
					new CreatHXGroupsTasks().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, groupName, "...");
				}

			}
		});
		mBtnJoinGroup = (Button) findViewById(R.id.btn_joinGroups);
		mLlJoinGroups = (LinearLayout) findViewById(R.id.rel_joinGroups);
		mEtvJoinGroupcode = (EditText) findViewById(R.id.etv_group_code);
		mEtvJoinGroupcode.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.length() <= 0) {
					mBtnJoinGroup.setVisibility(View.GONE);
				} else {
					mBtnJoinGroup.setVisibility(View.VISIBLE);
				}

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		mLlScan = (LinearLayout) findViewById(R.id.ll_scan);
		mLlScan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				StatService.trackCustomEvent(mContext, "Scan", "OK");
				// mIsScanOrGroupcode = 1;
				Intent intent = new Intent(GroupsActivity.this, CaptureActivity.class);
				startActivityForResult(intent, 0);
			}
		});
		mBtnJoinGroup.setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				StatService.trackCustomEvent(mContext, "JusedNumber", "OK");
				String groupcode = mEtvJoinGroupcode.getText().toString();
				if ("".equals(groupcode)) {
					Toast.makeText(getApplication(), "请输入群组码", 0).show();
					return;
				}
				// mIsScanOrGroupcode = 0;
				showWaitingDialog();
				getGroupInfo(groupcode);
			}
		});
		int value = getIntent().getIntExtra("groups", 0);
		if (value == 1) {
			mRelCreateGroups.setVisibility(View.VISIBLE);
			mLlJoinGroups.setVisibility(View.GONE);
			mTvTopical.setText("创建群组");
		} else if (value == 2) {
			mRelCreateGroups.setVisibility(View.GONE);
			mLlJoinGroups.setVisibility(View.VISIBLE);
			mTvTopical.setText("加入群组");
		}

	}

	@SuppressLint("NewApi")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// 处理扫描结果
		if (requestCode == REQUESTCODE_SCAN) {
			if (data == null) {
				return;
			}
			Bundle bundle = data.getExtras();
			String resulString = bundle.getString("result");
			jsonUtil(resulString);
		}
	}

	@SuppressLint("NewApi")
	private void jsonUtil(String resulString) {
		try {
			String substring = resulString.substring(8);
			JSONObject object = new JSONObject(substring);
			String groupcode = object.getString("data");
			showWaitingDialog();
			getGroupInfo(groupcode);
		} catch (JSONException e) {
			Toast.makeText(mContext, "扫面失败", Toast.LENGTH_SHORT).show();
			return;
		}
	}

	/**
	 * 环信建群
	 * */
	class CreatHXGroupsTasks extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String a[] = {};
			StatAppMonitor monitor = new StatAppMonitor("hxcreatePublicGroup_Android");
			long startTime = System.currentTimeMillis();
			try {
				String groupname = MeetingUtils.getDESGroup(params[0]);
				String groupdes = MeetingUtils.getDESGroup(params[1]);
				EMGroup mEMGroup = EMGroupManager.getInstance().createPublicGroup(groupname, groupdes, a, false);
				mHxGroupId = mEMGroup.getGroupId();
				long difftime = System.currentTimeMillis() - startTime;
				monitor.setMillisecondsConsume(difftime);
				monitor.setReturnCode(StatAppMonitor.SUCCESS_RESULT_TYPE);
				StatService.reportAppMonitorStat(MeetingApp.mContext, monitor);
				return "success";
			} catch (EaseMobException e) {
				long difftime = System.currentTimeMillis() - startTime;
				monitor.setMillisecondsConsume(difftime);
				monitor.setReturnCode(StatAppMonitor.FAILURE_RESULT_TYPE);
				return e.getLocalizedMessage();
			} finally {
				StatService.reportAppMonitorStat(MeetingApp.mContext, monitor);
			}

		}

		@SuppressLint("NewApi")
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if ("success".equals(result)) {
				createGroups(mEtvCreateGroupName.getText().toString(), mEtvCreateGroupNickname.getText().toString(), mHxGroupId);

			} else {
				hideWaitingDialog();
				Toast.makeText(GroupsActivity.this, "创建失败", 1).show();
			}
		}
	}

	private void createGroups(String groupname, final String showname, String mobcode) {
		NewNetwork.createGroup(groupname, showname, mobcode, new OnResponse<NetworkReturn>("create_group_android") {
			@Override
			public void success(NetworkReturn result, retrofit.client.Response arg1) {
				super.success(result, arg1);
				hideWaitingDialog();
				if (result.result != 1) {
					Toast.makeText(GroupsActivity.this, result.msg, 0).show();
					return;
				}
				JsonNode object = result.data;
				GroupMemberInfo groupInfo = new GroupMemberInfo();
				groupInfo.idcard = object.get("idcard").getValueAsText();
				groupInfo.name = object.get("name").getValueAsText();
				groupInfo.mtype = object.get("mtype").getValueAsInt();
				groupInfo.group_id = object.get("group_id").getValueAsText();
				Toast.makeText(GroupsActivity.this, "创建成功", 0).show();
				Intent intent = new Intent(GroupsActivity.this, CompleteGroupActivity.class);
				intent.putExtra("idcard", groupInfo.idcard);
				intent.putExtra("groupname", groupInfo.name);
				intent.putExtra("hxgroupid", mHxGroupId);
				intent.putExtra("rescode", 1);
				intent.putExtra("groupid", groupInfo.group_id);
				intent.putExtra("showname", showname);
				startActivity(intent);
				finish();
			};

			@SuppressLint("NewApi")
			@Override
			public void failure(RetrofitError arg0) {
				super.failure(arg0);
				hideWaitingDialog();
				if (mHxGroupId != null) {
					if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
						new DeleteHXGroupAsyn().execute(mHxGroupId);
					} else {
						new DeleteHXGroupAsyn().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mHxGroupId);
					}
				}
				Toast.makeText(GroupsActivity.this, "创建失败！", 0).show();
			}
		});
	}

	// 一律使用群组码加群
	public void getGroupInfo(final String groupcode) {
		NewNetwork.getGroupInfo(groupcode, new OnResponse<NetworkReturn>("groupinfo_byidcard_Android") {
			@Override
			public void success(NetworkReturn result, Response response) {
				super.success(result, response);
				hideWaitingDialog();
				if (result.result != 1) {
					Toast.makeText(GroupsActivity.this, result.msg, 0).show();
					return;
				}
				GroupMemberInfo groupInfo = new GroupMemberInfo();
				JsonNode object = result.data;
				groupInfo.name = object.get("name").getValueAsText();
				groupInfo.group_id = object.get("id").getValueAsText();
				groupInfo.hxgroupid = object.get("mob_code").getValueAsText();
				groupInfo.uid = object.get("uid").getValueAsInt();
				Intent intent = new Intent(GroupsActivity.this, JoinGroupActivity.class);
				intent.putExtra("groupid", groupInfo.group_id);
				intent.putExtra("groupname", groupInfo.name);
				intent.putExtra("hxgroupid", groupInfo.hxgroupid);
				intent.putExtra("uid", groupInfo.uid);
				intent.putExtra("groupcode", groupcode);
				startActivity(intent);
				finish();
			}

			@Override
			public void failure(RetrofitError error) {
				super.failure(error);
				hideWaitingDialog();
				Toast.makeText(GroupsActivity.this, "获取信息失败", 0).show();
			}
		});
	}

	/**
	 * 环信删群
	 * */
	class DeleteHXGroupAsyn extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			StatAppMonitor monitor = new StatAppMonitor("hxexitAndDeleteGroup_Android");
			long startTime = System.currentTimeMillis();
			try {
				EMGroupManager.getInstance().exitAndDeleteGroup(params[0]);
				long difftime = System.currentTimeMillis() - startTime;
				monitor.setMillisecondsConsume(difftime);
				monitor.setReturnCode(StatAppMonitor.SUCCESS_RESULT_TYPE);
				return null;
			} catch (EaseMobException e) {
				long difftime = System.currentTimeMillis() - startTime;
				monitor.setMillisecondsConsume(difftime);
				monitor.setReturnCode(StatAppMonitor.FAILURE_RESULT_TYPE);
				return null;
			} finally {
				StatService.reportAppMonitorStat(MeetingApp.mContext, monitor);
			}

		}
	}

	public String stringFilter(String str) {
		// 只允许字母、数字、汉字和_
		String regEx = "[^a-zA-Z0-9\u4E00-\u9FA5_]";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(str);
		return m.replaceAll("").trim();
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
