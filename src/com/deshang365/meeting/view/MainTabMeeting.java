package com.deshang365.meeting.view;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonNode;

import retrofit.RetrofitError;
import retrofit.client.Response;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.deshang365.meeting.R;
import com.deshang365.meeting.activity.MainActivity;
import com.deshang365.meeting.activity.CreateSignActivity;
import com.deshang365.meeting.activity.SigningActivity;
import com.deshang365.meeting.adapter.SignGroupAdapter;
import com.deshang365.meeting.baselib.MeetingApp;
import com.deshang365.meeting.model.GroupMemberInfo;
import com.deshang365.meeting.network.NetworkReturn;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.network.OnResponse;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class MainTabMeeting extends MainTabViewBase {
	private Context mContext;
	private View mView;
	private MainActivity mMainActivty;
	private PullToRefreshListView mListView;
	private SignGroupAdapter mAdapter;

	public MainTabMeeting(Context context) {
		super(context);
		mContext = context;
		mMainActivty = (MainActivity) context;
		mView = LayoutInflater.from(context).inflate(R.layout.main_tab_meeting, this, true);
		init();
		getGroupList();
	}

	public MainTabMeeting(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mView = LayoutInflater.from(context).inflate(R.layout.main_tab_meeting, this, true);
		init();
		getGroupList();
	}

	@Override
	public void onSwitchTo() {
		super.onSwitchTo();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void init() {
		mListView = (PullToRefreshListView) findViewById(R.id.lv_group_list);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				int mPosition = position - 1;
				GroupMemberInfo groupMemberInfo = mAdapter.getItem(mPosition);
				if (groupMemberInfo.mtype == 0) {
					if ("0".equals(groupMemberInfo.signState)) {
						Intent intent = new Intent(mContext, SigningActivity.class);
						intent.putExtra("meetingid", groupMemberInfo.meetingid);
						intent.putExtra("groupid", groupMemberInfo.group_id);
						intent.putExtra("mtype", groupMemberInfo.mtype);
						intent.putExtra("groupcode", groupMemberInfo.idcard);
						intent.putExtra("showname", groupMemberInfo.showname);
						intent.putExtra("groupname", groupMemberInfo.name);
						intent.putExtra("allow_join", groupMemberInfo.allow_join);
						intent.putExtra("hxgroupid", groupMemberInfo.hxgroupid);
						intent.putExtra("createsigntype", groupMemberInfo.meeting_type);
						mContext.startActivity(intent);
					} else {
						Intent intent = new Intent(mContext, CreateSignActivity.class);
						intent.putExtra("groupname", groupMemberInfo.name);
						intent.putExtra("groupid", groupMemberInfo.group_id);
						intent.putExtra("groupcode", groupMemberInfo.idcard);
						intent.putExtra("showname", groupMemberInfo.showname);
						intent.putExtra("hxgroupid", groupMemberInfo.hxgroupid);
						intent.putExtra("allow_join", groupMemberInfo.allow_join);
						intent.putExtra("mtype", groupMemberInfo.mtype);
						mContext.startActivity(intent);
					}
				}
			}
		});

		mListView.setOnRefreshListener(new OnRefreshListener<ListView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				getGroupList();
			}
		});

		mAdapter = new SignGroupAdapter(mContext);
		mListView.setAdapter(mAdapter);
	}

	private List<GroupMemberInfo> mGroupListTmp;

	public void getGroupList() {
		NewNetwork.getGroupList(new OnResponse<NetworkReturn>("get_group_list_android") {
			@Override
			public void success(NetworkReturn result, Response arg1) {
				super.success(result, arg1);
				mListView.onRefreshComplete();
				if (result.result != 1) {
					Toast.makeText(mContext, result.msg, Toast.LENGTH_SHORT).show();
					return;
				}
				mGroupListTmp = new ArrayList<GroupMemberInfo>();
				JsonNode data = result.data;
				for (int i = 0; i < data.size(); i++) {
					JsonNode item = data.get(i);
					GroupMemberInfo groupInfo = new GroupMemberInfo();
					groupInfo.createtime = item.get("createtime").getValueAsText();
					groupInfo.uid = item.get("uid").getValueAsInt();
					groupInfo.valid = item.get("valid").getValueAsText();
					groupInfo.name = item.get("name").getValueAsText();
					groupInfo.showname = item.get("showname").getValueAsText();
					groupInfo.group_id = item.get("group_id").getValueAsText();
					groupInfo.user_count = item.get("user_count").getValueAsText();
					groupInfo.mtype = item.get("mtype").getValueAsInt();
					groupInfo.idcard = item.get("idcard").getValueAsText();
					groupInfo.hxgroupid = item.get("mob_code").getValueAsText();
					if (item.has("allow_join")) {
						groupInfo.allow_join = item.get("allow_join").getValueAsInt();
					}
					if (item.has("meeting_type")) {
						groupInfo.meeting_type = item.get("meeting_type").getValueAsInt();
					}
					if (item.has("state")) {
						groupInfo.signState = item.get("state").getValueAsText();
					}
					if (item.has("has_sign")) {
						groupInfo.has_sign = item.get("has_sign").getValueAsInt();
					}
					groupInfo.meetingid = item.get("meeting_id").getValueAsText();
					JsonNode members = item.get("members");
					groupInfo.uids = new ArrayList<Integer>();
					for (int j = 0; j < members.size(); j++) {
						groupInfo.uids.add(members.get(j).getValueAsInt());
					}
					mGroupListTmp.add(groupInfo);
				}

				if (mAdapter != null) {
					MeetingApp.setmGroupList(mGroupListTmp);
					if (MeetingApp.userInfo != null) {
						// mAdapter.removeCacheImage(MeetingApp.userInfo.uid);
						mAdapter.notifyDataSetChanged();
					}

					// 更新聊天页面
					// if (MeetingApp.mHxHasLogin) {
					// ((MainTabTalk)
					// mMainActivty.mPageViews.get(1)).setNotifyDataSetChanged();
					// }
					// 刷新MainTabUserInfo群组数量
					MainTabUserInfo mainTabUserInfo = (MainTabUserInfo) mMainActivty.mPageViews.get(2);
					if (mainTabUserInfo != null) {
						mainTabUserInfo.setGroupCount(mGroupListTmp.size());
					}
				}
			}

			@Override
			public void failure(RetrofitError arg0) {
				super.failure(arg0);
				mListView.onRefreshComplete();
				Toast.makeText(mContext, "获取群组列表失败！", 0).show();
			}
		});
	}
}
