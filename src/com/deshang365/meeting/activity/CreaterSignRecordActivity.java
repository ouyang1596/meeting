package com.deshang365.meeting.activity;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonNode;

import retrofit.RetrofitError;
import retrofit.client.Response;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deshang365.meeting.R;
import com.deshang365.meeting.adapter.SignHistoryAdapter;
import com.deshang365.meeting.baselib.MeetingApp;
import com.deshang365.meeting.model.GroupMemberInfo;
import com.deshang365.meeting.model.GroupMemberInfoList;
import com.deshang365.meeting.network.NetworkReturn;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.network.OnResponse;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tencent.stat.StatService;

public class CreaterSignRecordActivity extends BaseActivity {
	private LinearLayout mLlBack;
	private TextView mTvTopical;
	private RelativeLayout mRelProBar;
	private PullToRefreshListView mLvGroupSignRecord;
	private String mGroupname;
	private String mGroupid;
	private int mPageCount;
	private List<GroupMemberInfo> mListGroupMemberInfos;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_sign_record);
		initView();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void initView() {
		mListGroupMemberInfos = new ArrayList<GroupMemberInfo>();
		mGroupname = getIntent().getStringExtra("groupname");
		mGroupid = getIntent().getStringExtra("groupid");
		mLlBack = (LinearLayout) findViewById(R.id.ll_top_alert_back);
		mLlBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mRelProBar = (RelativeLayout) findViewById(R.id.rel_progressbar);
		mTvTopical = (TextView) findViewById(R.id.tv_top_alert_text);
		if (mGroupname != null) {
			mTvTopical.setText(mGroupname);
		}
		mLvGroupSignRecord = (PullToRefreshListView) findViewById(R.id.lv_sign_history);
		mLvGroupSignRecord.setOnRefreshListener(new OnRefreshListener2() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase refreshView) {
				initPerSignrecord();
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase refreshView) {
				if (MeetingApp.mVersionName != null) {
					mPageCount += 1;
					getGroupSignRecord(mGroupid, MeetingApp.mVersionName, "" + mPageCount);
				}
			}
		});
		mLvGroupSignRecord.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				position = position - 1;
				StatService.trackCustomEvent(mContext, "onceresult", "OK");
				Intent intent = new Intent(mContext, SignResultActivity.class);
				intent.putExtra("groupid", mGroupid);
				intent.putExtra("meetingid", mAdapter.getItem(position).meetingid);
				startActivity(intent);
			}
		});
		if (MeetingApp.mVersionName != null) {

		}
		mLvGroupSignRecord.setMode(Mode.DISABLED);
		mRelProBar.setVisibility(View.VISIBLE);
		initPerSignrecord();
	}

	private SignHistoryAdapter mAdapter;

	private void getGroupSignRecord(String groupid, String appVersion, String pageCount) {
		NewNetwork.getGroupSignRecord(groupid, appVersion, pageCount,
				new OnResponse<NetworkReturn>("historymeetinglist_group_page_Android") {
					@Override
					public void success(NetworkReturn result, Response arg1) {
						super.success(result, arg1);
						setView();
						if (result.result == 1) {
							GroupMemberInfoList groupMemberInfoList = new GroupMemberInfoList();
							groupMemberInfoList.mGroupMemberInfosList = new ArrayList<GroupMemberInfo>();
							JsonNode data = result.data;
							JsonNode meetingList = data.get("meetinglist");
							for (int i = 0; i < meetingList.size(); i++) {
								JsonNode item = meetingList.get(i);
								GroupMemberInfo groupInfo = new GroupMemberInfo();
								groupInfo.createtime = item.get("createtime").getValueAsText();
								groupInfo.meetingid = item.get("id").getValueAsText();
								groupMemberInfoList.mGroupMemberInfosList.add(groupInfo);
							}
							mListGroupMemberInfos.addAll(groupMemberInfoList.mGroupMemberInfosList);
							if (mAdapter == null) {
								mAdapter = new SignHistoryAdapter(getApplication(), mListGroupMemberInfos);
								mLvGroupSignRecord.setAdapter(mAdapter);
							} else {
								mAdapter.notifyDataSetChanged();
							}
						} else {
							Toast.makeText(mContext, result.msg, Toast.LENGTH_SHORT).show();
						}
					}

					@Override
					public void failure(RetrofitError arg0) {
						super.failure(arg0);
						setView();
						Toast.makeText(mContext, "获得签到记录失败！", Toast.LENGTH_SHORT).show();
					}
				});
	}

	private void setView() {
		if (!mLvGroupSignRecord.isPullToRefreshEnabled()) {
			mLvGroupSignRecord.setMode(Mode.BOTH);
		}
		if (mLvGroupSignRecord.isRefreshing()) {
			mLvGroupSignRecord.onRefreshComplete();
		}
		if (mRelProBar.isShown()) {
			mRelProBar.setVisibility(View.GONE);
		}
	}

	private void initPerSignrecord() {
		mListGroupMemberInfos.clear();
		if (MeetingApp.mVersionName != null) {
			mPageCount = 1;
			getGroupSignRecord(mGroupid, MeetingApp.mVersionName, "" + mPageCount);
		} else {
			setView();
		}
	}
}
