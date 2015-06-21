package com.deshang365.meeting.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.codehaus.jackson.JsonNode;
import org.json.JSONArray;
import org.json.JSONObject;

import retrofit.RetrofitError;
import retrofit.client.Response;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deshang365.meeting.R;
import com.deshang365.meeting.baselib.MeetingApp;
import com.deshang365.meeting.model.GroupMemberInfo;
import com.deshang365.meeting.model.GroupMemberInfoList;
import com.deshang365.meeting.model.Network;
import com.deshang365.meeting.network.NetworkReturn;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.network.OnResponse;
import com.deshang365.meeting.view.ImageViewButton;
import com.tencent.stat.StatService;

public class NearSignGroupsActivity extends ImageloaderBaseActivity {
	private TextView mTvExit;
	private RelativeLayout mRelNearSignGroups;
	private Handler mHandler;
	private Timer mTimer;
	private TimerTask mTask;
	private ImageView mImgvFlush;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_near_sign_groups);
		initView();
	}

	private void initView() {
		mTvExit = (TextView) findViewById(R.id.txtv_exit);
		mTvExit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mRelNearSignGroups = (RelativeLayout) findViewById(R.id.rel_login_near_sign_groups);
		getNearSIgnGroups();
		mImgvFlush = (ImageView) findViewById(R.id.imgv_flush);
		mImgvFlush.setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				StatService.trackCustomEvent(mContext, "RefreshNB", "OK");
				if (MeetingApp.getLat(0) == 0 || MeetingApp.getLng(0) == 0) {
					Toast.makeText(getApplication(), "定位失败，无法获取群组", 0).show();
				} else {
					Toast.makeText(getApplication(), "正在刷新，请稍等...", 0).show();
					getNearSignGroups(MeetingApp.getLat(0), MeetingApp.getLng(0));
				}
			}
		});
	}

	@SuppressLint("NewApi")
	private void getNearSIgnGroups() {
		if (MeetingApp.getLat(0) == 0 || MeetingApp.getLng(0) == 0) {
			Toast.makeText(getApplication(), "定位失败，无法获取群组", 0).show();
		} else {
			showWaitingDialog();
			getNearSignGroups(MeetingApp.getLat(0), MeetingApp.getLng(0));
		}
	}

	private void onTimeToUpdate() {
		mHandler = new Handler() {
			@SuppressLint("NewApi")
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				showWaitingDialog();
				getNearSignGroups(MeetingApp.getLat(0), MeetingApp.getLng(0));
			}
		};
		mTask = new TimerTask() {

			@Override
			public void run() {
				mHandler.sendMessage(mHandler.obtainMessage());
			}
		};
		mTimer = new Timer();
		mTimer.schedule(mTask, 30000, 30000);
	}

	private List<GroupMemberInfo> mGroupMemberInfos;

	private void getNearSignGroups(double lat, double lng) {
		NewNetwork.getNearSignGroups(lat, lng, new OnResponse<NetworkReturn>("near_groups_Android") {

			@Override
			public void success(NetworkReturn result, Response arg1) {
				super.success(result, arg1);
				hideWaitingDialog();
				if (result.result != 1) {
					Toast.makeText(mContext, result.msg, Toast.LENGTH_SHORT).show();
					return;
				}
				mGroupMemberInfos = new ArrayList<GroupMemberInfo>();
				JsonNode data = result.data;
				for (int i = 0; i < data.size(); i++) {
					GroupMemberInfo groupInfo = new GroupMemberInfo();
					JsonNode object = data.get(i);
					groupInfo.group_id = object.get("group_id").getValueAsText();
					groupInfo.meetingid = object.get("meeting_id").getValueAsText();
					groupInfo.idcard = object.get("idcard").getValueAsText();
					groupInfo.hxgroupid = object.get("mob_code").getValueAsText();
					groupInfo.showname = object.get("name").getValueAsText();
					groupInfo.name = object.get("group_name").getValueAsText();
					groupInfo.uid = object.get("uid").getValueAsInt();
					mGroupMemberInfos.add(groupInfo);
				}
				for (int i = 0; i < mRelNearSignGroups.getChildCount(); i++) {
					ImageViewButton mImgvB = (ImageViewButton) mRelNearSignGroups.getChildAt(i);
					mImgvB.setTag(i);
					mImgvB.setOnClickListener(mGourpHeadClickListener);
					if (i < mGroupMemberInfos.size()) {
						mImgvB.setTextViewText(mGroupMemberInfos.get(i).name);
						mImageLoader.displayImage(NewNetwork.getAvatarUrl(mGroupMemberInfos.get(i).uid), mImgvB.mImgv, mOptions);
					}
				}

			}

			@Override
			public void failure(RetrofitError error) {
				super.failure(error);
				hideWaitingDialog();
				Toast.makeText(mContext, "获取信息失败", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private OnClickListener mGourpHeadClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int j = (Integer) v.getTag();
			if (j < mGroupMemberInfos.size()) {
				Intent intent = new Intent(mContext, JoinGroupActivity.class);
				intent.putExtra("groupcode", mGroupMemberInfos.get(j).idcard);
				intent.putExtra("groupname", mGroupMemberInfos.get(j).name);
				intent.putExtra("groupid", mGroupMemberInfos.get(j).group_id);
				intent.putExtra("hxgroupid", mGroupMemberInfos.get(j).hxgroupid);
				intent.putExtra("groupname", mGroupMemberInfos.get(j).name);
				intent.putExtra("uid", mGroupMemberInfos.get(j).uid);
				startActivity(intent);
			}

		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		onTimeToUpdate();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mTask.cancel();
		mTimer.cancel();
	}
}
