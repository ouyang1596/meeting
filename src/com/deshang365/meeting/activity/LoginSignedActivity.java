package com.deshang365.meeting.activity;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.json.JSONArray;
import org.json.JSONObject;

import retrofit.RetrofitError;
import retrofit.client.Response;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.deshang365.meeting.R;
import com.deshang365.meeting.adapter.ExlSignListAdapter;
import com.deshang365.meeting.baselib.MeetingApp;
import com.deshang365.meeting.model.GroupMemberInfo;
import com.deshang365.meeting.model.GroupMemberInfoList;
import com.deshang365.meeting.model.Network;
import com.deshang365.meeting.network.NetworkReturn;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.network.OnResponse;
import com.deshang365.meeting.view.SignedView;

public class LoginSignedActivity extends BaseActivity {
	private TextView mTvTopical, mTvBack;
	private ExpandableListView mLvSigned;
	private RadioGroup mRgSignList;
	private SignedView mViewAllSigned;
	private LinearLayout mLlBack;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login_signed);
		initView();
	}

	@SuppressLint("NewApi")
	private void initView() {
		final int backToMain = getIntent().getIntExtra("backtomain", -1);
		final String groupid = getIntent().getStringExtra("groupid");
		String answer = getIntent().getStringExtra("answer");
		final String meetingid = getIntent().getStringExtra("meetingid");
		mLlBack = (LinearLayout) findViewById(R.id.ll_top_alert_back);
		mLlBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (backToMain == 1) {
					Intent intent = new Intent(mContext, MainActivity.class);
					startActivity(intent);
					return;
				}
				finish();
			}
		});
		mLvSigned = (ExpandableListView) findViewById(R.id.lv_signed);
		mTvTopical = (TextView) findViewById(R.id.tv_top_alert_text);
		mTvTopical.setText("完成签到");
		mViewAllSigned = (SignedView) findViewById(R.id.rel_all_signed);
		showWaitingDialog();
		getSignList(groupid, meetingid);
		mTvBack = (TextView) findViewById(R.id.txtv_what_need);
		mTvBack.setText("返回首页");
		mTvBack.setVisibility(View.VISIBLE);
		mTvBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LoginSignedActivity.this, MainActivity.class);
				intent.putExtra("uid", MeetingApp.userInfo.uid);
				startActivity(intent);
				finish();
			}
		});
		mRgSignList = (RadioGroup) findViewById(R.id.rg_show_signlist);
		mRgSignList.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (mAdapter == null || mGroupMemberInfoSignList == null) {
					return;
				}
				if (checkedId == R.id.rb_show_un_signed) {
					if (mGroupMemberInfoSignList.get(1).size() == 0) {
						mLvSigned.setVisibility(View.GONE);
						mViewAllSigned.setVisibility(View.VISIBLE);
						return;
					}
					mLvSigned.setVisibility(View.VISIBLE);
					mAdapter.setShow(1);
				} else if (checkedId == R.id.rb_show_signed) {
					if (mGroupMemberInfoSignList.get(0).size() == 0) {
						mLvSigned.setVisibility(View.GONE);
						mViewAllSigned.setVisibility(View.GONE);
						return;
					}
					mLvSigned.setVisibility(View.VISIBLE);
					mAdapter.setShow(0);
				}
			}
		});
	}

	private ExlSignListAdapter mAdapter;
	private List<List<GroupMemberInfo>> mGroupMemberInfoSignList;

	public void getSignList(String groupid, String meetingid) {
		NewNetwork.getSignList(groupid, meetingid, new OnResponse<NetworkReturn>("meetinglist_Android") {
			@Override
			public void success(NetworkReturn result, Response response) {
				super.success(result, response);
				hideWaitingDialog();
				if (result.result != 1) {
					Toast.makeText(mContext, result.msg, Toast.LENGTH_SHORT).show();
					return;
				}
				ArrayList<List<GroupMemberInfo>> groupMemberInfoLists = new ArrayList<List<GroupMemberInfo>>();
				JsonNode data = result.data;
				JsonNode joiners = data.get("joiners");
				List<GroupMemberInfo> joinersGroupInfoList = jsonData(joiners);
				groupMemberInfoLists.add(joinersGroupInfoList);
				JsonNode absenters = data.get("absenters");
				List<GroupMemberInfo> absentersGroupInfoList = jsonData(absenters);
				groupMemberInfoLists.add(absentersGroupInfoList);
				GroupMemberInfo groupInfo = new GroupMemberInfo();
				groupInfo.signcode = data.get("sign_code").getValueAsText();
				mGroupMemberInfoSignList = groupMemberInfoLists;
				if (groupMemberInfoLists.get(1).size() <= 0) {
					mLvSigned.setVisibility(View.GONE);
				} else {
					mLvSigned.setVisibility(View.VISIBLE);
				}
				mAdapter = new ExlSignListAdapter(mContext, groupMemberInfoLists);
				mLvSigned.setAdapter(mAdapter);
				int groupCount = mAdapter.getGroupCount();
				for (int i = 0; i < groupCount; i++) {
					mLvSigned.expandGroup(i);
				}
			}

			@Override
			public void failure(RetrofitError error) {
				super.failure(error);
				hideWaitingDialog();
				Toast.makeText(mContext, "获取签到列表失败", Toast.LENGTH_SHORT).show();
			}

		});

	}

	private List<GroupMemberInfo> jsonData(JsonNode absenters) {
		List<GroupMemberInfo> absentersGroupInfoList = new ArrayList<GroupMemberInfo>();
		for (int j = 0; j < absenters.size(); j++) {
			JsonNode jsonItem = absenters.get(j);
			GroupMemberInfo absentersGroupInfo = new GroupMemberInfo();
			if (jsonItem.has("createtime")) {
				absentersGroupInfo.createtime = jsonItem.get("createtime").getValueAsText();
			}
			if (jsonItem.has("showname")) {
				absentersGroupInfo.showname = jsonItem.get("showname").getValueAsText();
			}
			absentersGroupInfo.uid = jsonItem.get("uid").getValueAsInt();
			if (jsonItem.has("state")) {
				absentersGroupInfo.state = jsonItem.get("state").getValueAsInt();
			}
			absentersGroupInfoList.add(absentersGroupInfo);
		}
		return absentersGroupInfoList;
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
