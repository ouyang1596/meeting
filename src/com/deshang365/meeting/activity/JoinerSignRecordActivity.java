package com.deshang365.meeting.activity;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonNode;

import retrofit.RetrofitError;
import retrofit.client.Response;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deshang365.meeting.R;
import com.deshang365.meeting.adapter.SignHistoryAdapter;
import com.deshang365.meeting.baselib.MeetingApp;
import com.deshang365.meeting.model.GroupMemberInfo;
import com.deshang365.meeting.network.NetworkReturn;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.network.OnResponse;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class JoinerSignRecordActivity extends BaseActivity {
	private LinearLayout mLlBack;
	private TextView mTvTopical, mTvSignCount, mTvSignAbsenceTimes, mTvSignLeaveTimes;
	private PullToRefreshListView mLvSignRecord;
	private String mGroupname;
	private String mGroupid;
	private RelativeLayout mRelProBar;
	private int mPageCount;
	private List<GroupMemberInfo> mListGroupMemberInfos;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signrecord);
		initView();
	}

	@SuppressWarnings("unchecked")
	private void initView() {
		mListGroupMemberInfos = new ArrayList<GroupMemberInfo>();
		mRelProBar = (RelativeLayout) findViewById(R.id.rel_progressbar);
		mGroupname = getIntent().getStringExtra("groupname");
		mGroupid = getIntent().getStringExtra("groupid");
		mTvTopical = (TextView) findViewById(R.id.tv_top_alert_text);
		mTvSignCount = (TextView) findViewById(R.id.txtv_all_signCount);
		mTvSignAbsenceTimes = (TextView) findViewById(R.id.txtv_sign_absence);
		mTvSignLeaveTimes = (TextView) findViewById(R.id.txtv_sign_leave);
		if (mGroupname != null) {
			mTvTopical.setText(mGroupname);
		}
		mLlBack = (LinearLayout) findViewById(R.id.ll_top_alert_back);
		mLlBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mLvSignRecord = (PullToRefreshListView) findViewById(R.id.lv_sign_history);
		mLvSignRecord.setOnRefreshListener(new OnRefreshListener2() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase refreshView) {
				initPerSignrecord();
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase refreshView) {
				if (MeetingApp.mVersionName != null) {
					mPageCount += 1;
					getPersonalSignRecord(mGroupid, MeetingApp.mVersionName, "" + mPageCount);
				}
			}
		});
		mLvSignRecord.setMode(Mode.DISABLED);
		mRelProBar.setVisibility(View.VISIBLE);
		initPerSignrecord();
	}

	private void initPerSignrecord() {
		mListGroupMemberInfos.clear();
		if (MeetingApp.mVersionName != null) {
			mPageCount = 1;
			getPersonalSignRecord(mGroupid, MeetingApp.mVersionName, "" + mPageCount);
		} else {
			setView();
		}
	}

	private SignHistoryAdapter mAdapter;

	private void getPersonalSignRecord(String groupid, String appVersion, String pageCount) {
		NewNetwork.getPersonalSignRecord(groupid, appVersion, pageCount, new OnResponse<NetworkReturn>(
				"historymeetinglist_group_page_Android") {
			@Override
			public void success(NetworkReturn result, Response arg1) {
				super.success(result, arg1);
				setView();
				if (result.result == 1) {
					JsonNode data = result.data;
					JsonNode meetinglist = data.get("meetinglist");
					int signCount, signNormal = 0, signLeave = 0, signAbsence = 0;
					signCount = data.get("my_meeting_count").getValueAsInt();// 成员加入群组后发生的签到次数
					signAbsence = data.get("absent_count").getValueAsInt();// 缺席次数
					signLeave = data.get("leave_count").getValueAsInt();// 请假次数
					for (int i = 0; i < meetinglist.size(); i++) {
						GroupMemberInfo groupInfo = new GroupMemberInfo();
						JsonNode item = meetinglist.get(i);
						groupInfo.signState = item.get("state").getValueAsText();
						groupInfo.uid = item.get("id").getValueAsInt();
						groupInfo.state = item.get("state").getValueAsInt();
						groupInfo.createtime = item.get("createtime").getValueAsText();
						mListGroupMemberInfos.add(groupInfo);
					}
					setText(signCount, signLeave, signAbsence);
					if (mAdapter == null) {
						mAdapter = new SignHistoryAdapter(getApplication(), mListGroupMemberInfos);
						mLvSignRecord.setAdapter(mAdapter);
					} else {
						mAdapter.notifyDataSetChanged();
					}
				} else if (result.result == -4) {
					Toast.makeText(mContext, result.msg, Toast.LENGTH_SHORT).show();
				} else {
					// mTvSignCount.setText("总共发起0次签到，缺席0次，请假0次");
					// setText(0, 0, 0);
					Toast.makeText(mContext, result.msg, Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void failure(RetrofitError arg0) {
				super.failure(arg0);
				setView();
				setText(0, 0, 0);
				Toast.makeText(mContext, "获得签到记录失败", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void setText(int signCount, int signLeave, int signAbsence) {
		mTvSignCount.setText("" + signCount);
		mTvSignAbsenceTimes.setText("" + signAbsence);
		mTvSignLeaveTimes.setText("" + signLeave);
	}

	private void setView() {
		if (!mLvSignRecord.isPullToRefreshEnabled()) {
			mLvSignRecord.setMode(Mode.BOTH);
		}
		if (mLvSignRecord.isRefreshing()) {
			mLvSignRecord.onRefreshComplete();
		}
		if (mRelProBar.isShown()) {
			mRelProBar.setVisibility(View.GONE);
		}
	}
}
