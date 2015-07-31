package com.deshang365.meeting.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.codehaus.jackson.JsonNode;

import retrofit.RetrofitError;
import retrofit.client.Response;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deshang365.meeting.R;
import com.deshang365.meeting.baselib.MeetingApp;
import com.deshang365.meeting.model.GroupMemberInfo;
import com.deshang365.meeting.network.NetworkReturn;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.network.OnResponse;
import com.deshang365.meeting.view.ImageViewButton;
import com.tencent.stat.StatService;

public class UnLoginActivity extends ImageloaderBaseActivity {
	private TextView mTvLogin;
	private RelativeLayout mRelNearSignGroups;
	private int mIsJumpToSign;// 是否跳转到签到页面 0跳转 1不跳转
	private Handler mHandler;
	private Timer mTimer;
	private ImageView mImgvFlush;
	private ImageView mImgvAnimation;
	private AnimationDrawable mAnimationDrawable;

	// private int backState;// 是否直接返回 0直接返回 1需要点两次

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_un_login);
		initView();
	}

	@SuppressLint("NewApi")
	private void initView() {
		mRelNearSignGroups = (RelativeLayout) findViewById(R.id.rel_imgv_un_login_sign);
		mTvLogin = (TextView) findViewById(R.id.txtv_login);
		mTvLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(UnLoginActivity.this, LoginActivity.class));
				finish();
			}
		});
		// showWaitingDialog();
		getNearSignGroups(MeetingApp.getLat(0), MeetingApp.getLng(0));
		// if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
		// new GetNearSignGroupsUnloginAsyn().execute(MeetingApp.getLat(0),
		// MeetingApp.getLng(0));
		// } else {
		// new GetNearSignGroupsUnloginAsyn()
		// .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
		// MeetingApp.getLat(0), MeetingApp.getLng(0));
		// }
		onTimeToUpdate();
		mImgvAnimation = (ImageView) findViewById(R.id.imgv_animation);
		mImgvFlush = (ImageView) findViewById(R.id.imgv_flush);
		mImgvFlush.setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				StatService.trackCustomEvent(mContext, "RefreshNB", "OK");
				Toast.makeText(getApplication(), "正在刷新，请稍等...", 0).show();
				getNearSignGroups(MeetingApp.getLat(0), MeetingApp.getLng(0));
			}
		});
	}

	private void startAnimation() {
		if (mAnimationDrawable == null) {
			mAnimationDrawable = (AnimationDrawable) mImgvAnimation.getDrawable();
		}
		mAnimationDrawable.start();
	}

	private void onTimeToUpdate() {
		mHandler = new Handler() {
			@SuppressLint("NewApi")
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				// showWaitingDialog();
				getNearSignGroups(MeetingApp.getLat(0), MeetingApp.getLng(0));
			}
		};
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				mHandler.sendMessage(mHandler.obtainMessage());
			}
		};
		mTimer = new Timer();
		mTimer.schedule(task, 30000, 30000);

	}

	private List<GroupMemberInfo> mGroupMemberInfos;

	private void getNearSignGroups(double lat, double lng) {
		NewNetwork.getNearSignGroups(lat, lng, new OnResponse<NetworkReturn>("near_groups_Android") {

			@Override
			public void success(NetworkReturn result, Response arg1) {
				super.success(result, arg1);
				// hideWaitingDialog();
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
				// hideWaitingDialog();
				Toast.makeText(mContext, "获取信息失败", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private OnClickListener mGourpHeadClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int j = (Integer) v.getTag();
			if (j < mGroupMemberInfos.size()) {
				GroupMemberInfo groupMemberInfo = mGroupMemberInfos.get(j);
				Intent intent = new Intent(UnLoginActivity.this, LoginActivity.class);
				intent.putExtra("groupname", groupMemberInfo.name);
				intent.putExtra("groupid", groupMemberInfo.group_id);
				intent.putExtra("isJumpToSign", 0);
				startActivity(intent);
				finish();
			}

		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == event.KEYCODE_BACK) {
			finish();
			System.exit(0);
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onResume() {
		super.onResume();
		startAnimation();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mTimer.cancel();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mAnimationDrawable.stop();
		mAnimationDrawable = null;
	}
}
