package com.deshang365.meeting.baselib;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.BaseAdapter;

import com.deshang365.meeting.R;
import com.deshang365.meeting.activity.MainActivity;
import com.deshang365.meeting.adapter.SignGroupAdapter;
import com.deshang365.meeting.adapter.TalkGroupAdapter2;
import com.deshang365.meeting.baselib.InitLocation.OnLocationListener;
import com.deshang365.meeting.model.Constants;
import com.deshang365.meeting.model.GroupMemberInfo;
import com.deshang365.meeting.model.UserInfo;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.util.MeetingUtils;
import com.easemob.EMConnectionListener;
import com.easemob.EMError;
import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMChatOptions;
import com.easemob.chat.EMMessage;
import com.easemob.chat.OnMessageNotifyListener;
import com.easemob.util.NetUtils;
import com.tencent.stat.StatConfig;
import com.tencent.stat.StatService;

public class MeetingApp extends Application implements EMConnectionListener {
	public static Context mContext;
	public static UserInfo userInfo;
	public static boolean hasLogin;
	public static boolean isFromLaunch;
	public static String favoriteId;
	public static boolean newPostSuccess;
	public static String username;
	public static String password;
	public static Map<String, Map<String, GroupMemberInfo>> sMapGroupMemberInfo;

	private static List<GroupMemberInfo> mSignGroupList;
	private static List<GroupMemberInfo> mTalkGroupList;
	public static List<BaseAdapter> mGroupListAdapters;
	public static Intent mIntentToTimeToUploadService;
	public static Boolean mHxHasLogin = false;
	public static String mVersionName = "-1";// 版本号
	public static SharedPreferences mParamsSharePrefreces;

	public static void loginOut() {
		hasLogin = false;
		userInfo = null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;
		NewNetwork.Init(mContext);
		BluetoothManager.init(mContext);
		sMapGroupMemberInfo = new HashMap<String, Map<String, GroupMemberInfo>>();
		mGroupListAdapters = new ArrayList<BaseAdapter>();
		mParamsSharePrefreces = getSharedPreferences(Constants.KEY_PARAMS, 0);
		setNotification();
		StatConfig.setDebugEnable(true);
		StatService.trackCustomEvent(this, "onCreate", "");
		int pid = android.os.Process.myPid();
		String processAppName = getAppName(pid);
		// 如果app启用了远程的service，此application:onCreate会被调用2次
		// 为了防止环信SDK被初始化2次，加此判断会保证SDK被初始化1次
		// 默认的app会在以包名为默认的process name下运行，如果查到的process name不是app的process
		// name就立即返回
		if (processAppName == null || !processAppName.equalsIgnoreCase("com.deshang365.meeting")) {
			return;
		}
		// 环信SDK默认是自动登录的
		EMChat.getInstance().setAutoLogin(false);
		EMChat.getInstance().init(mContext);
		EMChat.getInstance().setDebugMode(true);
		EMChatManager.getInstance().addConnectionListener(this);
		initAppData();
		initLocation();
		try {
			mVersionName = getVersionName();
		} catch (Exception e) {
			mVersionName = "-1";
		}
		mTalkGroupList = new ArrayList<GroupMemberInfo>();
	}

	private void setNotification() {
		EMChatOptions chatOptions = EMChatManager.getInstance().getChatOptions();

		chatOptions.setNotifyText(new OnMessageNotifyListener() {

			@Override
			public int onSetSmallIcon(EMMessage arg0) {
				return R.drawable.notification;
			}

			@Override
			public String onSetNotificationTitle(EMMessage message) {

				return null;
			}

			@Override
			public String onNewMessageNotify(EMMessage message) {

				return "一个朋友发来了一条信息";
			}

			@Override
			public String onLatestMessageNotify(EMMessage message, int fromUsersNum, int messageNum) {
				return fromUsersNum + "个联系人，发来了" + EMChatManager.getInstance().getUnreadMsgsCount() + "条消息";
			}
		});

	}

	private InitLocation mLocation;
	private static List<Double> sArrayLat = new ArrayList<Double>();
	private static List<Double> sArrayLng = new ArrayList<Double>();

	private void initLocation() {
		mLocation = new InitLocation(this);
		mLocation.setOnLocationListener(new OnLocationListener() {

			@Override
			public void setLocation(double lat, double lng) {
				setLat(lat);
				setLng(lng);
			}
		});

	}

	public static double getLat(int index) {
		double lat = 0;
		if (sArrayLat.size() > index) {
			lat = sArrayLat.get(index);
		} else if (sArrayLat.size() > 0) {
			lat = sArrayLat.get(0);
		}

		return lat;
	}

	public static void setLat(double lat) {
		if (sArrayLat.size() >= 3) {
			sArrayLat.remove(2);
		}
		sArrayLat.add(0, lat);
	}

	public static double getLng(int index) {
		double lng = 0;
		if (sArrayLng.size() > index) {
			lng = sArrayLng.get(index);
		} else if (sArrayLng.size() > 0) {
			lng = sArrayLng.get(0);
		}

		return lng;
	}

	public static void setLng(double lng) {
		if (sArrayLng.size() >= 3) {
			sArrayLng.remove(2);
		}
		sArrayLng.add(0, lng);
	}

	// public static void setGroupList(List<GroupMemberInfo> groupList) {
	// mSignGroupList = groupList;
	// for (BaseAdapter adapter : mGroupListAdapters) {
	// if (adapter != null) {
	// if (adapter instanceof SignGroupAdapter) {
	// ((SignGroupAdapter) adapter).removeCacheImage(MeetingApp.userInfo.uid);
	// } else if (adapter instanceof TalkGroupAdapter2) {
	// ((TalkGroupAdapter2) adapter).removeCacheImage(MeetingApp.userInfo.uid);
	// }
	// adapter.notifyDataSetChanged();
	// }
	// }
	// }

	public static void setmGroupList(List<GroupMemberInfo> groupList) {
		mSignGroupList = groupList;
		mTalkGroupList.clear();
		mTalkGroupList.addAll(mSignGroupList);
		MeetingUtils.sortList(mTalkGroupList);
	}

	public static List<GroupMemberInfo> getGroupList() {
		return mSignGroupList;
	}

	public static List<GroupMemberInfo> getTalkGroupList() {
		return mTalkGroupList;
	}

	@Override
	public void onConnected() {

	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			Intent intent = new Intent(getApplicationContext(), MainActivity.class);
			intent.putExtra("conflict", true);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(intent);
		};
	};

	@Override
	public void onDisconnected(final int error) {
		if (error == EMError.USER_REMOVED) {
			// 显示帐号已经被移除
		} else if (error == EMError.CONNECTION_CONFLICT) {
			// 显示帐号在其他设备登陆
			Message message = Message.obtain();
			handler.sendMessage(message);
		} else {
			if (NetUtils.hasNetwork(mContext)) {
				// 连接不到聊天服务器
			} else {
				// 当前网络不可用，请检查网络设置
			}

		}
	}

	/** 环信SDK方法 获取processAppName */
	private String getAppName(int pID) {
		String processName = null;
		ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
		List l = am.getRunningAppProcesses();
		Iterator i = l.iterator();
		PackageManager pm = this.getPackageManager();
		while (i.hasNext()) {
			ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
			try {
				if (info.pid == pID) {
					CharSequence c = pm.getApplicationLabel(pm.getApplicationInfo(info.processName, PackageManager.GET_META_DATA));
					processName = info.processName;
					return processName;
				}
			} catch (Exception e) {
			}
		}
		return processName;
	}

	private String getVersionName() throws Exception {
		// 获取packagemanager的实例
		PackageManager packageManager = getPackageManager();
		// getPackageName()是你当前类的包名，0代表是获取版本信息
		PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), 0);
		String version = packInfo.versionName;
		return version;
	}

	private void initAppData() {
		hasLogin = false;
		isFromLaunch = false;

		File file = new File(Constants.DOWNLOAD_PATH);
		if (!file.exists()) {
			file.mkdirs();
		}
		file = new File(Constants.QRCODE_PATH);
		if (!file.exists()) {
			file.mkdirs();
		}
		file = new File(Constants.PICTURES_ROOT_PATH);
		if (!file.exists()) {
			file.mkdirs();
		}

		file = new File(Constants.AVATAR_PATH);
		if (!file.exists()) {
			file.mkdirs();
		}

		file = new File(Constants.COURSE_COVER_PATH);
		if (!file.exists()) {
			file.mkdirs();
		}

		file = new File(Constants.FORUM_PIC_PATH);
		if (!file.exists()) {
			file.mkdirs();
		}

		file = new File(Constants.POST_PHOTO_PATH);
		if (!file.exists()) {
			file.mkdirs();
		}

		file = new File(Constants.TOPIC_PATH);
		if (!file.exists()) {
			file.mkdirs();
		}

		file = new File(Constants.QUESTION_PHOTO_PATH);
		if (!file.exists()) {
			file.mkdirs();
		}

		file = new File(Constants.LECTURE_PHOTO_PATH);
		if (!file.exists()) {
			file.mkdirs();
		}

		file = new File(Constants.GAME_PHOTO_PATH);
		if (!file.exists()) {
			file.mkdirs();
		}

		file = new File(Constants.ICON_PATH);
		if (!file.exists()) {
			file.mkdirs();
		}

		file = new File(Constants.BUILDING_PATH);
		if (!file.exists()) {
			file.mkdirs();
		}
		// CrashHandler crashHandler = CrashHandler.getInstance();
		// crashHandler.init(getApplicationContext());

	}

}
