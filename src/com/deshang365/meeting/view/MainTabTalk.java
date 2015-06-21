package com.deshang365.meeting.view;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deshang365.meeting.R;
import com.deshang365.meeting.activity.MainActivity;
import com.deshang365.meeting.activity.TalkTogetherActivity;
import com.deshang365.meeting.adapter.TalkGroupAdapter2;
import com.deshang365.meeting.baselib.MeetingApp;
import com.deshang365.meeting.model.GroupMemberInfo;
import com.deshang365.meeting.util.MeetingUtils;
import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroupManager;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tencent.stat.StatAppMonitor;
import com.tencent.stat.StatService;

public class MainTabTalk extends MainTabViewBase {
	private Context mContext;
	private View mView;
	private TextView mGroups;
	private PullToRefreshListView mLvGroups;
	private MainActivity mMainActivity;
	private TalkGroupAdapter2 mAdapter;
	private PopupWindow mPop;
	private RelativeLayout mRelProBar;
	private NewMessageBroadcastReceiver msgReceiver;

	public MainTabTalk(Context context) {
		super(context);
		mContext = context;
		mMainActivity = (MainActivity) context;
		mView = LayoutInflater.from(context).inflate(R.layout.main_tab_talk, this, true);
		init();
		registerBroadcast();
		if (MeetingApp.mHxHasLogin) {
			mRelProBar.setVisibility(View.GONE);
			EMGroupManager.getInstance().loadAllGroups();
			EMChatManager.getInstance().loadAllConversations();
			getGroupList();
		} else {
			mLvGroups.setMode(Mode.DISABLED);
			mRelProBar.setVisibility(View.VISIBLE);
			hxLogin(MeetingApp.userInfo.hxid);
		}
	}

	public MainTabTalk(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mMainActivity = (MainActivity) context;
		mView = LayoutInflater.from(context).inflate(R.layout.main_tab_talk, this, true);
		init();
		registerBroadcast();
		if (MeetingApp.mHxHasLogin) {
			getGroupList();
		} else {
			hxLogin(MeetingApp.userInfo.hxid);
		}
	}

	private void hxLogin(String hxid) {
		if (hxid != null && !hxid.isEmpty()) {
			String desHxid = MeetingUtils.getDESHXPwd(hxid);
			if (desHxid != null) {
				doLoginHx(hxid, desHxid);
			} else {
				setViewShowMode();
			}
		} else {
			setViewShowMode();
		}
	}

	private void setViewShowMode() {
		mLvGroups.setMode(Mode.PULL_DOWN_TO_REFRESH);
		if (mLvGroups.isRefreshing()) {
			mLvGroups.onRefreshComplete();
		}
		mRelProBar.setVisibility(View.GONE);
	}

	/**
	 * 环信登陆
	 * */
	private StatAppMonitor mMonitor;
	private long mStartTime;

	public void doLoginHx(String userName, String userPassward) {
		mMonitor = new StatAppMonitor("hxlogin_Android");
		mStartTime = System.currentTimeMillis();
		EMChatManager.getInstance().login(userName, userPassward, new EMCallBack() {

			@Override
			public void onSuccess() {
				MeetingApp.mHxHasLogin = true;
				EMGroupManager.getInstance().loadAllGroups();
				EMChatManager.getInstance().loadAllConversations();
				mMainActivity.runOnUiThread(mLoginSucc);
				long difftime = System.currentTimeMillis() - mStartTime;
				mMonitor.setMillisecondsConsume(difftime);
				mMonitor.setReturnCode(StatAppMonitor.SUCCESS_RESULT_TYPE);
				StatService.reportAppMonitorStat(MeetingApp.mContext, mMonitor);
			}

			@Override
			public void onProgress(int arg0, String arg1) {

			}

			@Override
			public void onError(int arg0, final String message) {
				mMainActivity.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						mLvGroups.setMode(Mode.PULL_DOWN_TO_REFRESH);
						mRelProBar.setVisibility(View.GONE);
						if (mLvGroups != null) {
							mLvGroups.onRefreshComplete();
						}
						long difftime = System.currentTimeMillis() - mStartTime;
						mMonitor.setMillisecondsConsume(difftime);
						mMonitor.setReturnCode(StatAppMonitor.FAILURE_RESULT_TYPE);
						StatService.reportAppMonitorStat(MeetingApp.mContext, mMonitor);
					}

				});
			}
		});
	}

	private Runnable mLoginSucc = new Runnable() {

		@Override
		public void run() {
			mLvGroups.setMode(Mode.PULL_DOWN_TO_REFRESH);
			mRelProBar.setVisibility(View.GONE);
			if (mLvGroups != null) {
				mLvGroups.onRefreshComplete();
			}
			getGroupList();
		}
	};

	private void registerBroadcast() {
		// 注册一个接收消息的BroadcastReceiver
		msgReceiver = new NewMessageBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter(EMChatManager.getInstance().getNewMessageBroadcastAction());
		intentFilter.setPriority(3);
		mContext.registerReceiver(msgReceiver, intentFilter);

	}

	@Override
	public void onSwitchTo() {
		super.onSwitchTo();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mContext.unregisterReceiver(msgReceiver);
	}

	private void init() {
		mRelProBar = (RelativeLayout) findViewById(R.id.rel_progressbar);
		mLvGroups = (PullToRefreshListView) findViewById(R.id.lv_getAllGroups);
		// 本地加载群列表
		mAdapter = new TalkGroupAdapter2(mContext);
		mLvGroups.setAdapter(mAdapter);
		mLvGroups.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				StatService.trackCustomEvent(mContext, "Gchat", "OK");
				if (mAdapter == null) {
					return;
				}
				GroupMemberInfo info = mAdapter.getItem(position - 1);
				Intent intent = new Intent(mContext, TalkTogetherActivity.class);
				intent.putExtra("mobcode", info.hxgroupid);
				intent.putExtra("groupid", info.group_id);
				intent.putExtra("groupname", info.name);
				intent.putExtra("showname", info.showname);
				intent.putExtra("groupcode", info.idcard);
				intent.putExtra("mtype", info.mtype);
				intent.putExtra("allow_join", info.allow_join);
				mContext.startActivity(intent);
				EMChatManager.getInstance().getConversation(info.hxgroupid).resetUnreadMsgCount();
				mMainActivity.updataUnreadTv();
			}
		});
		// MeetingApp.mGroupListAdapters.add(mAdapter);
		mLvGroups.setOnRefreshListener(new OnRefreshListener<ListView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				if (MeetingApp.mHxHasLogin) {
					getGroupList();
				} else {
					hxLogin(MeetingApp.userInfo.hxid);
				}
			}
		});
	}

	@SuppressLint("NewApi")
	public void getGroupList() {
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
			new GetEMGroupListAsyn().execute();
		} else {
			new GetEMGroupListAsyn().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}

	private class GetEMGroupListAsyn extends AsyncTask<String, Void, Integer> {
		private int isSuccess;// 1成功-1失败

		@Override
		protected Integer doInBackground(String... params) {
			StatAppMonitor monitor = new StatAppMonitor("hxgetGroupsFromServer_Android");
			long startTime = System.currentTimeMillis();
			try {
				EMGroupManager.getInstance().getGroupsFromServer();
				long difftime = System.currentTimeMillis() - startTime;
				monitor.setMillisecondsConsume(difftime);
				monitor.setReturnCode(StatAppMonitor.SUCCESS_RESULT_TYPE);
				isSuccess = 1;
				return isSuccess;
			} catch (Exception e) {
				monitor.setReturnCode(StatAppMonitor.FAILURE_RESULT_TYPE);
				isSuccess = -1;
				return isSuccess;
			} finally {
				StatService.reportAppMonitorStat(MeetingApp.mContext, monitor);
			}
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			if (mLvGroups.isRefreshing()) {
				mLvGroups.onRefreshComplete();
			}
			if (result == 1) {
				setNotifyDataSetChanged();
			} else {
				Toast.makeText(mContext, "获取群组列表失败！", 0).show();
				MeetingApp.mHxHasLogin = false;
				setNotifyDataSetChanged();
			}
		}
	}

	// 接受新消息的广播
	private class NewMessageBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (mAdapter != null) {
				mAdapter.notifyDataSetChanged();
			}
			mMainActivity.updataUnreadTv();
		}

	}

	public void setNotifyDataSetChanged() {
		if (MeetingApp.userInfo != null && mAdapter != null && mLvGroups != null && MeetingApp.getGroupList() != null) {
			mAdapter.lruCacheRemoveKey("" + MeetingApp.userInfo.uid);
			mAdapter.notifyDataSetChanged();
			mMainActivity.updataUnreadTv();
		}
	}

}
