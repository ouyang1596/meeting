package com.deshang365.meeting.activity;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonNode;

import retrofit.RetrofitError;
import retrofit.client.Response;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deshang365.meeting.R;
import com.deshang365.meeting.adapter.ExlSignListAdapter;
import com.deshang365.meeting.baselib.MeetingApp;
import com.deshang365.meeting.model.Constants;
import com.deshang365.meeting.model.GroupMemberInfo;
import com.deshang365.meeting.network.NetworkReturn;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.network.OnResponse;
import com.deshang365.meeting.view.CircularImageView;
import com.deshang365.meeting.view.SignedView;
import com.tencent.stat.StatService;

public class SigningActivity extends ImageloaderBaseActivity {
	private TextView mTvTopical, mTvGroupname, mTvIdcard, mTvSignMemberState;
	private Button mBtnStopSign;
	private LinearLayout mLlBack;
	private ExpandableListView mExlistSIgnList;
	private RadioGroup mRgSignList;
	private ImageView mImgvGroupmembers;
	private SignedView mViewAllSigned;
	private RelativeLayout mRelProBar;
	private TextView mTvSignCode;
	private View mStopSign;
	private String mGroupid;
	private String mMeetingid;
	private int mType;// 0是创建者 1是参与者
	private int mSignState;// 签到状态 0是正在签到，1签到已完成
	private AlertDialog mDialog;
	private CircularImageView mImgvGroupHead;
	private String mImagePath = Constants.AVATAR_PATH + MeetingApp.userInfo.uid;
	private RelativeLayout mRelSignList/* , mRelSigned */;
	private RelativeLayout mRelGroupInfo;
	private int mCreateSignType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signing);
		initView();
	}

	@SuppressLint("NewApi")
	private void initView() {
		mViewAllSigned = (SignedView) findViewById(R.id.rel_all_signed);
		final String groupname = getIntent().getStringExtra("groupname");
		final String groupcode = getIntent().getStringExtra("groupcode");
		final int allow_join = getIntent().getIntExtra("allow_join", -1);
		final String hxgroupid = getIntent().getStringExtra("hxgroupid");
		final String showname = getIntent().getStringExtra("showname");
		mMeetingid = getIntent().getStringExtra("meetingid");
		mGroupid = getIntent().getStringExtra("groupid");
		mType = getIntent().getIntExtra("mtype", -1);
		mCreateSignType = getIntent().getIntExtra("createsigntype", -1);
		mRelGroupInfo = (RelativeLayout) findViewById(R.id.rel_group_data);
		mRelProBar = (RelativeLayout) findViewById(R.id.rel_progressbar);
		if (mCreateSignType == 1) {
			mRelGroupInfo.setVisibility(View.GONE);
		} else {
			mRelGroupInfo.setVisibility(View.VISIBLE);
		}
		mRelSignList = (RelativeLayout) findViewById(R.id.rel_sign_list);
		// mRelSigned = (RelativeLayout) findViewById(R.id.rel_all_signed);
		mImgvGroupHead = (CircularImageView) findViewById(R.id.imgv_group_head);
		mImageLoader.displayImage(NewNetwork.getAvatarUrl(MeetingApp.userInfo.uid), mImgvGroupHead, mOptions);
		mTvGroupname = (TextView) findViewById(R.id.txtv_group_name);
		mTvGroupname.setText(groupname);
		mTvIdcard = (TextView) findViewById(R.id.txtv_group_code);
		mTvIdcard.setText(groupcode);
		mBtnStopSign = (Button) findViewById(R.id.btn_stop_sign);
		if (mType == 0) {
			mBtnStopSign.setVisibility(View.VISIBLE);
			mImgvGroupmembers = (ImageView) findViewById(R.id.iv_group_members);
			mImgvGroupmembers.setVisibility(View.VISIBLE);
			mImgvGroupmembers.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					StatService.trackCustomEvent(mContext, "OpenGroup", "OK");
					Intent intent = new Intent(mContext, GroupDetailsActivity.class);
					intent.putExtra("groupname", groupname);
					intent.putExtra("groupcode", groupcode);
					intent.putExtra("groupid", mGroupid);
					intent.putExtra("showname", showname);
					intent.putExtra("mtype", mType);
					intent.putExtra("allow_join", allow_join);
					intent.putExtra("hxgroupid", hxgroupid);
					startActivity(intent);
				}
			});
		} else if (mType == 1) {
			mBtnStopSign.setVisibility(View.INVISIBLE);

		}
		mBtnStopSign.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				stopSign();
			}
		});
		mExlistSIgnList = (ExpandableListView) findViewById(R.id.exl_sign_list);
		mExlistSIgnList.setGroupIndicator(null);
		mLlBack = (LinearLayout) findViewById(R.id.ll_top_alert_back);
		mLlBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mTvSignCode = (TextView) findViewById(R.id.txtv_signcode);
		mTvSignCode.setText("");
		mTvTopical = (TextView) findViewById(R.id.tv_top_alert_text);
		mTvTopical.setText("正在签到");
		mRelProBar.setVisibility(View.VISIBLE);
		getSignList(mGroupid, mMeetingid);
		mRgSignList = (RadioGroup) findViewById(R.id.rg_show_signlist);
		mRgSignList.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (mAdapter == null || mGroupMemberInfoSignList == null) {
					return;
				}
				if (checkedId == R.id.rb_show_un_signed) {
					if (mGroupMemberInfoSignList.get(1).size() == 0) {
						mExlistSIgnList.setVisibility(View.GONE);
						return;
					}
					mExlistSIgnList.setVisibility(View.VISIBLE);
					mAdapter.setShow(1);
				} else if (checkedId == R.id.rb_show_signed) {
					if (mGroupMemberInfoSignList.get(0).size() == 0) {
						mExlistSIgnList.setVisibility(View.GONE);
						mViewAllSigned.setVisibility(View.GONE);
						return;
					}
					mExlistSIgnList.setVisibility(View.VISIBLE);
					mViewAllSigned.setVisibility(View.VISIBLE);
					mAdapter.setShow(0);
				}
			}
		});
		mTvSignMemberState = (TextView) findViewById(R.id.txtv_sign_member_state);
		mTvSignMemberState.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mGroupMemberInfoSignList != null && mGroupMemberInfoSignList.size() >= 3 && mGroupMemberInfoSignList.get(2).size() > 0) {
					Intent intent = new Intent(mContext, LoginSignedActivity.class);
					intent.putExtra("groupid", mGroupid);
					intent.putExtra("meetingid", mMeetingid);
					intent.putExtra("answer", mGroupMemberInfoSignList.get(2).get(0).signcode);
					startActivity(intent);
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
				mRelProBar.setVisibility(View.GONE);
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
				String signcode = groupInfo.signcode;
				mTvSignCode.setText(signcode);
				mGroupMemberInfoSignList = groupMemberInfoLists;
				if (groupMemberInfoLists.get(1).size() <= 0) {
					mExlistSIgnList.setVisibility(View.GONE);
				} else {
					mExlistSIgnList.setVisibility(View.VISIBLE);
				}
				mAdapter = new ExlSignListAdapter(SigningActivity.this, groupMemberInfoLists);
				mExlistSIgnList.setAdapter(mAdapter);
				int groupCount = mAdapter.getGroupCount();
				for (int i = 0; i < groupCount; i++) {
					mExlistSIgnList.expandGroup(i);
				}
			}

			@Override
			public void failure(RetrofitError error) {
				super.failure(error);
				mRelProBar.setVisibility(View.GONE);
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

	private void stopSign() {
		showDialog();
		mStopSign = View.inflate(mContext, R.layout.exit_dialog, null);
		Button btnStopSign = (Button) mStopSign.findViewById(R.id.btn_exit);
		LinearLayout llExit = (LinearLayout) mStopSign.findViewById(R.id.ll_delete_exit_group);
		TextView tvStopSign = (TextView) mStopSign.findViewById(R.id.txtv_stop_or_exit);
		tvStopSign.setText("结束签到");
		TextView tvStopSignDescription = (TextView) mStopSign.findViewById(R.id.txtv_stop_or_exit_description);
		tvStopSignDescription.setText("结束后,群组成员将无法进行签到");
		llExit.setVisibility(View.VISIBLE);
		btnStopSign.setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				mDialog.cancel();
				showWaitingDialog();
				stopSign(mGroupid, mMeetingid);
			}
		});
		Button btnCancel = (Button) mStopSign.findViewById(R.id.btn_cancel);
		btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mDialog.cancel();
			}
		});
		mDialog.show();
		mDialog.getWindow().setContentView(mStopSign);
	}

	private void showDialog() {
		if (mDialog == null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			mDialog = builder.create();
		}
	}

	public void stopSign(String groupid, String meetingid) {
		NewNetwork.stopSign(groupid, meetingid, new OnResponse<NetworkReturn>("end_meeting_Android") {
			@Override
			public void success(NetworkReturn result, Response response) {
				super.success(result, response);
				hideWaitingDialog();
				if (result.result != 1) {
					Toast.makeText(mContext, result.msg, Toast.LENGTH_SHORT).show();
					return;
				}
				// 发送广播，停止定时请求
				Intent receiveIntent = new Intent();
				receiveIntent.setAction("stopreceive");
				receiveIntent.putExtra("receivestate", 0);// 0创建者停止签到1应用推出
				receiveIntent.putExtra("groupid", mGroupid);
				sendBroadcast(receiveIntent);
				Intent intent = new Intent(SigningActivity.this, SignResultActivity.class);
				intent.putExtra("groupid", mGroupid);
				intent.putExtra("meetingid", mMeetingid);
				startActivity(intent);
				finish();
			}

			@Override
			public void failure(RetrofitError error) {
				super.failure(error);
				hideWaitingDialog();
				Toast.makeText(mContext, "请求失败", Toast.LENGTH_SHORT).show();
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
