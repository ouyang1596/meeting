package com.deshang365.meeting.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.biovamp.widget.CircleLayout;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deshang365.meeting.R;
import com.deshang365.meeting.baselib.BluetoothManager;
import com.deshang365.meeting.baselib.InitLocation;
import com.deshang365.meeting.baselib.InitLocation.OnLocationListener;
import com.deshang365.meeting.baselib.MeetingApp;
import com.deshang365.meeting.model.Constants;
import com.deshang365.meeting.model.GroupMemberInfo;
import com.deshang365.meeting.model.Network;
import com.deshang365.meeting.model.UserInfo;
import com.deshang365.meeting.model.VersionInfo;
import com.deshang365.meeting.network.NetworkReturn;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.network.OnResponse;
import com.deshang365.meeting.network.RetrofitUtils;
import com.deshang365.meeting.service.DownloadListenService;
import com.deshang365.meeting.util.MeetingUtils;
import com.deshang365.meeting.view.CustomViewPager;
import com.deshang365.meeting.view.MainTabMeeting;
import com.deshang365.meeting.view.MainTabTalk;
import com.deshang365.meeting.view.MainTabUserInfo;
import com.deshang365.meeting.view.MainTabViewBase;
import com.deshang365.meeting.view.TabButton;
import com.deshang365.util.ProfileHelper;
import com.easemob.EMCallBack;
import com.easemob.EMConnectionListener;
import com.easemob.EMError;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMGroupManager;
import com.easemob.util.EMLog;
import com.easemob.util.NetUtils;
import com.tencent.stat.StatAppMonitor;

public class MainActivity extends BaseActivity implements EMConnectionListener {
	public static MainActivity instance = null;
	public View mBackView;
	public TextView mTitle, mTvUnreadMsgCount;
	private CustomViewPager mViewPager;
	private PackageInfo mPackageInfo;
	private TabButton[] mBottomTabs;
	private IsShowLoadingReceive mIsShowLoadingReceive;
	/**
	 * 环信未登录时显示
	 * */
	private ProgressBar mPbLoading;
	private ImageView mImgvGroupsAdd, mImgvTipCancel;
	private long mExitTime = 0;
	private int mCurPage;
	private PopupWindow mPop;
	public ArrayList<MainTabViewBase> mPageViews = new ArrayList<MainTabViewBase>();
	public static MainActivity mMainActivityInstance;
	private RelativeLayout mRelTopAlert, mRelTip;
	private View mView;
	private CircleLayout mCirlay;
	private boolean mIsConflictDialogShow;
	public String mPhotoPathToRemember;
	private String mDeshxPwd;
	private String mHxid;
	private MainTabUserInfo mMainTabUserInfo;
	private final int REQUESTCODE_TAKEPHOTOS = 1;
	private final int REQUESTCODE_ALBUM = 2;

	// private boolean isFirstGetList = true;// 避免重复刷新列表，导致crash

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState != null && savedInstanceState.containsKey("userInfo")) {
			super.onCreate(savedInstanceState);
			// setContentView(R.layout.activity_default);
			String loginName = savedInstanceState.getString("user_name");
			String loginPwd = savedInstanceState.getString("user_pwd");
			if (savedInstanceState.containsKey("image_path")) {
				String imagePath = savedInstanceState.getString("image_path");
				new InitHelp().init(loginName, loginPwd, imagePath);
			} else {
				new InitHelp().init(loginName, loginPwd, null);
			}
			return;
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		checkToUpdata();
		// 启动activity时不自动弹出软键盘
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		instance = this;
		initView();
	}

	private void checkToUpdata() {
		try {
			mPackageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
		} catch (NameNotFoundException e) {
		}
		String deviceId = ((TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
		checkNewVersion();
	}

	private void initView() {
		registerLoading();
		mView = findViewById(R.id.view_shadow);
		mView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mView.setVisibility(View.GONE);
			}
		});
		mRelTip = (RelativeLayout) findViewById(R.id.rel_tip);
		mImgvTipCancel = (ImageView) findViewById(R.id.imgv_tip_cancel);
		if (MeetingUtils.getParams(Constants.KEY_TIP_SHOW) == 0) {
			mRelTip.setVisibility(View.VISIBLE);
			// 设置点击事件，避免被覆盖的事件获取焦点
			mRelTip.setOnClickListener(null);
			MeetingUtils.saveParams(Constants.KEY_TIP_SHOW, 1);
		} else {
			mRelTip.setVisibility(View.GONE);
		}
		mImgvTipCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mRelTip.setVisibility(View.GONE);
			}
		});
		mRelTopAlert = (RelativeLayout) findViewById(R.id.public_top_alert);
		mMainActivityInstance = this;
		mTvUnreadMsgCount = (TextView) findViewById(R.id.txtv_tb_unread_msg_number);
		updataUnreadTv();
		final View view = View.inflate(mContext, R.layout.groups_popup_item, null);
		LinearLayout creatGroups = (LinearLayout) view.findViewById(R.id.ll_create_groups);
		creatGroups.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mPop.dismiss();
				mView.setVisibility(View.GONE);
				mContext.startActivity(new Intent(mContext, GroupsActivity.class).putExtra("groups", 1));
			}
		});
		LinearLayout joinGroups = (LinearLayout) view.findViewById(R.id.ll_join_groups);
		joinGroups.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mPop.dismiss();
				mView.setVisibility(View.GONE);
				mContext.startActivity(new Intent(mContext, GroupsActivity.class).putExtra("groups", 2));
			}
		});
		mViewPager = (CustomViewPager) findViewById(R.id.vp_main);
		mBackView = findViewById(R.id.ll_top_alert_back);
		mTitle = (TextView) findViewById(R.id.tv_top_alert_text);
		mPbLoading = (ProgressBar) findViewById(R.id.pb_loading);
		mImgvGroupsAdd = (ImageView) findViewById(R.id.imgv_what_need);
		mImgvGroupsAdd.setImageResource(R.drawable.btn_add_imgv_selector);
		mImgvGroupsAdd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mPop == null) {
					mPop = new PopupWindow(view, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					mPop.setBackgroundDrawable(new ColorDrawable(0xffffff));
					mPop.setOutsideTouchable(true);
					mPop.showAsDropDown(mRelTopAlert, mRelTopAlert.getWidth() - 216 - 10, 0);
					mView.setVisibility(View.VISIBLE);
					return;
				}
				mPop.showAsDropDown(mRelTopAlert, mRelTopAlert.getWidth() - 216 - 10, 0);
				mView.setVisibility(View.VISIBLE);
			}
		});
		isShowGroupImgv();
		// 添加MainTab，MainTab必须继承自MainTabViewBase
		mPageViews.add(new MainTabMeeting(this));
		mPageViews.add(new MainTabTalk(this));
		mPageViews.add(new MainTabUserInfo(this));
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setScanScroll(false);
		mBottomTabs = new TabButton[3];
		mBottomTabs[0] = (TabButton) findViewById(R.id.tb_meeting);
		mBottomTabs[0].setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// mImgvGroupsAdd.setVisibility(View.VISIBLE);
				isShowGroupImgv();
				goTo(0);
				for (TabButton button : mBottomTabs) {
					if (button.getId() != v.getId()) {
						button.setSelected(false);
					}
				}
				if (!mBottomTabs[0].isSelected()) {
					mBottomTabs[0].setSelected(true);
				}
			}
		});
		mBottomTabs[1] = (TabButton) findViewById(R.id.tb_talk);
		mBottomTabs[1].setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mImgvGroupsAdd.setVisibility(View.GONE);
				mPbLoading.setVisibility(View.GONE);
				goTo(1);
				for (TabButton button : mBottomTabs) {
					if (button.getId() != v.getId()) {
						button.setSelected(false);
					}
				}
				if (!mBottomTabs[1].isSelected()) {
					mBottomTabs[1].setSelected(true);
				}
			}
		});

		mBottomTabs[2] = (TabButton) findViewById(R.id.tb_user_info);
		mBottomTabs[2].setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mImgvGroupsAdd.setVisibility(View.GONE);
				mPbLoading.setVisibility(View.GONE);
				goTo(2);
				for (TabButton button : mBottomTabs) {
					if (button.getId() != v.getId()) {
						button.setSelected(false);
					}
				}
				if (!mBottomTabs[2].isSelected()) {
					mBottomTabs[2].setSelected(true);
				}
			}
		});

		for (int i = 0; i < mBottomTabs.length; i++) {
			mBottomTabs[i].setSelected(false);
		}
		mBottomTabs[0].setSelected(true);
		// mImgvGroupsAdd.setVisibility(View.VISIBLE);
		isShowGroupImgv();
		goTo(0);
		int code = getIntent().getIntExtra("code", -1);
		if (code == 1) {
			mBottomTabs[1].setSelected(true);
			mImgvGroupsAdd.setVisibility(View.GONE);
			mPbLoading.setVisibility(View.GONE);
			goTo(1);
		}
		EMChatManager.getInstance().addConnectionListener(this);
	}

	private void registerLoading() {
		mIsShowLoadingReceive = new IsShowLoadingReceive();
		IntentFilter intentFilter = new IntentFilter("show_loading");
		registerReceiver(mIsShowLoadingReceive, intentFilter);
	}

	private void isShowGroupImgv() {
		if (MeetingApp.mHxHasLogin) {
			mPbLoading.setVisibility(View.GONE);
			mImgvGroupsAdd.setVisibility(View.VISIBLE);
		} else {
			mPbLoading.setVisibility(View.VISIBLE);
			mImgvGroupsAdd.setVisibility(View.GONE);
		}
	}

	private int curBtn;

	public void goTo(int index) {
		curBtn = index;
		mViewPager.setCurrentItem(index);
		mBackView.setVisibility(View.INVISIBLE);
		switch (mViewPager.getCurrentItem()) {
		case 0:
			mTitle.setText("签到");
			break;
		case 1:
			mTitle.setText("群聊");
			break;
		case 2:
			mTitle.setText("我");
			break;
		default:
			break;
		}
		mCurPage = index;
		if (mPageViews != null && index < mPageViews.size() && index >= 0) {
			mPageViews.get(index).onSwitchTo();
		}
		EMChatManager.getInstance().addConnectionListener(this);
	}

	@Override
	public void onBackPressed() {
		// super.onBackPressed();
		if ((System.currentTimeMillis() - mExitTime) > 2000) {
			Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
			mExitTime = System.currentTimeMillis();
		} else {
			MeetingApp.mHxHasLogin = false;
			EMChatManager.getInstance().logout();// 此方法为同步方法
			finish();
			System.exit(0);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mViewPager == null) {
			return;
		}
		goTo(mCurPage);
		if (getIntent().getBooleanExtra("conflict", false) || mIsConflictDialogShow == true) {
			return;
		}
		if (MeetingApp.mHxHasLogin) {
			((MainTabMeeting) mPageViews.get(0)).getGroupList();
			updataUnreadTv();
		}
	}

	// 填充ViewPager的数据适配器
	PagerAdapter mPagerAdapter = new PagerAdapter() {

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public int getCount() {
			if (mPageViews == null) {
				return 0;
			} else {
				return mPageViews.size();
			}
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			if (mPageViews != null && position < mPageViews.size() && position >= 0) {
				((ViewPager) container).removeView(mPageViews.get(position));
			}
		}

		@Override
		public Object instantiateItem(View container, int position) {
			if (mPageViews != null && position < mPageViews.size() && position >= 0) {
				((ViewPager) container).addView(mPageViews.get(position));
				return mPageViews.get(position);
			} else {
				return null;
			}
		}
	};

	/**
	 * 刷新未读消息总数
	 * */
	public void updataUnreadTv() {
		int unreadMsgsCountTotal = 0;
		// EMChatManager.getInstance().getUnreadMsgsCount();
		List<GroupMemberInfo> memberInfos = MeetingApp.getGroupList();
		if (memberInfos == null) {
			return;
		}
		for (GroupMemberInfo info : memberInfos) {
			EMConversation conversation = EMChatManager.getInstance().getConversation(info.hxgroupid);
			if (conversation != null) {
				unreadMsgsCountTotal += conversation.getUnreadMsgCount();
			}
		}

		if (unreadMsgsCountTotal != 0) {
			mTvUnreadMsgCount.setText("" + unreadMsgsCountTotal);
			mTvUnreadMsgCount.setVisibility(View.VISIBLE);
		} else {
			mTvUnreadMsgCount.setVisibility(View.GONE);
		}
	}

	public void onSaveInstanceState(Bundle outState) {
		outState.putSerializable("userInfo", MeetingApp.userInfo);
		outState.putString("user_name", MeetingApp.username);
		outState.putString("user_pwd", MeetingApp.password);
		if (mPhotoPathToRemember != null) {
			outState.putString("image_path", mPhotoPathToRemember);
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (mPageViews.size() < 3) {
			return;
		}
		mMainTabUserInfo = (MainTabUserInfo) mPageViews.get(2);
		if (requestCode == REQUESTCODE_TAKEPHOTOS) {
			String absolutePath = mMainTabUserInfo.getImageTakephotoPathString();
			uploadFile(absolutePath);
		} else if (requestCode == REQUESTCODE_ALBUM) {
			if (data == null) {
				return;
			}
			Uri photoUri = data.getData();
			String absolutePath = MeetingUtils.getRealFilePath(this, photoUri);
			uploadFile(absolutePath);
		}
	}

	@SuppressLint("NewApi")
	private void uploadFile(String absolutePath) {
		if (absolutePath == null) {
			return;
		}

		File file = new File(absolutePath);
		if (!file.exists()) {
			return;
		}
		int len = MeetingUtils.fileLength(absolutePath);
		if (len <= 0) {
			return;
		}

		Bitmap scaleBitmap = null;
		if (len > 10 * 1024) {
			String scaleFilePath = Constants.SCALE_IMAGE + "/" + MeetingApp.userInfo.uid;
			// String scaleFilePath = Constants.AVATAR_PATH +
			// MeetingApp.userInfo.uid;
			scaleBitmap = MeetingUtils.scalePic(absolutePath);
			MeetingUtils.saveBitmap(scaleFilePath, scaleBitmap);
			file = new File(scaleFilePath);
		} else {
			scaleBitmap = BitmapFactory.decodeFile(absolutePath);
		}
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
			new UpLoadFileAsyn().execute(file);
		} else {
			new UpLoadFileAsyn().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, file);
		}

		if (mMainTabUserInfo != null) {
			mMainTabUserInfo.getmImgvHead().setImageBitmap(scaleBitmap);
		}
		mPhotoPathToRemember = null;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		for (MainTabViewBase view : mPageViews) {
			view.onDestroy();
		}
		mMainActivityInstance = null;
		if (mConflictBuilder != null) {
			mConflictBuilder.create().dismiss();
			mConflictBuilder = null;
		}
		MeetingApp.mHxHasLogin = false;

		BluetoothManager.unInit(mContext);
		if (mIsShowLoadingReceive != null) {
			unregisterReceiver(mIsShowLoadingReceive);
		}
	}

	class UpLoadFileAsyn extends AsyncTask<File, Void, String> {

		@Override
		protected String doInBackground(File... params) {
			return Network.uploadFile(params[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result == null) {
				return;
			}
			try {
				JSONObject object = new JSONObject(result);
				int code = object.getInt("result");
				if (code == 1) {
					if (mMainTabUserInfo != null) {
						mMainTabUserInfo.getRemoveImageCache(MeetingApp.userInfo.uid);
						mMainTabUserInfo.getPhoto();
					}
				} else if (code == -2) {
					Toast.makeText(getApplication(), "上传失败！", Toast.LENGTH_LONG).show();
				} else if (code == -1) {
					Toast.makeText(getApplication(), "未登录！", Toast.LENGTH_LONG).show();
				}
			} catch (JSONException e) {
				Toast.makeText(getApplication(), "解析出错！", Toast.LENGTH_LONG).show();
				return;
			}
		}
	}

	@Override
	public void onConnected() {

	}

	@Override
	public void onDisconnected(final int error) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (error == EMError.USER_REMOVED) {
					// 显示帐号已经被移除
				} else if (error == EMError.CONNECTION_CONFLICT) {
					// 显示帐号在其他设备登陆
					if (!getIntent().getBooleanExtra("conflict", false)) {
						showConflictDialog();
					}
				} else {
					if (NetUtils.hasNetwork(MainActivity.this)) {
						// 连接不到聊天服务器
					} else {
						// 当前网络不可用，请检查网络设置
					}
				}
			}
		});

	}

	private android.app.AlertDialog.Builder mConflictBuilder;

	/**
	 * 显示帐号在别处登录dialog
	 */
	private void showConflictDialog() {
		mIsConflictDialogShow = true;
		EMChatManager.getInstance().logout(null);
		MeetingApp.userInfo = null;
		MeetingApp.hasLogin = false;
		if (!MainActivity.this.isFinishing()) {
			// clear up global variables
			try {
				if (mConflictBuilder == null) {
					mConflictBuilder = new android.app.AlertDialog.Builder(MainActivity.this);
				}
				mConflictBuilder.setTitle("下线通知");
				mConflictBuilder.setMessage("同一账号已在其他设备登陆");
				mConflictBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						mConflictBuilder = null;
						finish();
						startActivity(new Intent(MainActivity.this, LoginActivity.class));
					}
				});
				mConflictBuilder.setCancelable(false);
				mConflictBuilder.create().show();
			} catch (Exception e) {
				EMLog.e("bm", "---------color conflictBuilder error" + e.getMessage());
			}

		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (getIntent().getBooleanExtra("conflict", false) && !mIsConflictDialogShow) {
			showConflictDialog();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	private void checkNewVersion() {
		NewNetwork.getUpdatedVersion(mContext, new OnResponse<VersionInfo>("check_new_version") {
			@Override
			public void success(VersionInfo result, Response arg1) {
				super.success(result, arg1);
				if (mPackageInfo == null || result.versionCode <= mPackageInfo.versionCode) {
					return;
				}
				LayoutInflater factory = LayoutInflater.from(mContext);
				View mUpdateInfo = factory.inflate(R.layout.update_info, null);
				TextView mUpdatedVersionName = (TextView) mUpdateInfo.findViewById(R.id.updated_version_name);
				mUpdatedVersionName.setText(result.versionName);
				TextView mUpdatedFileSize = (TextView) mUpdateInfo.findViewById(R.id.updated_file_size);
				mUpdatedFileSize.setText(result.fileSize);
				TextView mUpdatedContent = (TextView) mUpdateInfo.findViewById(R.id.updated_content);
				mUpdatedContent.setText(result.content);
				final String downloadUrl = result.downloadURL;
				new AlertDialog.Builder(mContext).setTitle("发现新版本").setView(mUpdateInfo)
						.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// 启动下载服务
								Intent intent = new Intent();
								intent.putExtra("url", downloadUrl);
								intent.setClass(mContext, DownloadListenService.class);
								intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								mContext.startService(intent);
							}
						}).setNegativeButton("以后再说", null).show();
			}

			@Override
			public void failure(RetrofitError arg0) {
				super.failure(arg0);
			}
		});
	}

	public class InitHelp {
		private String mImagePath;
		private InitLocation mLocation;
		private LoginUser mLoginUser;

		public void init(String name, String pwd, String imagePath) {
			initUser(name, pwd);
			mImagePath = imagePath;
		}

		private void initUser(String name, String pwd) {
			mLoginUser = new LoginUser();
			mLoginUser.loginName = name;
			mLoginUser.loginPwd = pwd;
			if (!("".equals(mLoginUser.loginName)) && !("".equals(mLoginUser.loginPwd))) {
				initLocation();
				login(mLoginUser);
			}
		}

		private void initLocation() {
			mLocation = new InitLocation(MainActivity.this);
			mLocation.setOnLocationListener(new OnLocationListener() {

				@Override
				public void setLocation(double lat, double lng) {
					MeetingApp.setLat(lat);
					MeetingApp.setLng(lng);
					// 只保留一个进行周期性调用监听接口
					mLocation.removeLocation();
				}
			});
		}

		private class LoginUser {
			String loginName;
			String loginPwd;
		}

		private void login(LoginUser user) {
			MeetingApp.username = user.loginName;
			MeetingApp.password = user.loginPwd;
			NewNetwork.login(user.loginName, user.loginPwd, new OnResponse<NetworkReturn>("login_android") {
				@Override
				public void success(NetworkReturn result, Response arg1) {
					super.success(result, arg1);
					if (result.result == 1) {
						MeetingApp.hasLogin = true;
						RetrofitUtils.setCookies(arg1);
						JsonNode rsData = result.data;
						MeetingApp.userInfo = new UserInfo();

						try {
							MeetingApp.userInfo.uid = rsData.get("uid").getValueAsInt();
							Log.i("bm", "uid==" + MeetingApp.userInfo.uid);
							if (rsData.has("email")) {
								MeetingApp.userInfo.email = rsData.get("email").getValueAsText();
							}
							if (rsData.has("username")) {
								MeetingApp.userInfo.name = rsData.get("username").getValueAsText();
							}
							if (rsData.has("mob_code")) {
								MeetingApp.userInfo.hxid = rsData.get("mob_code").getValueAsText();
							}
						} catch (Exception e) {
						}
						hxLogin(MeetingApp.userInfo.hxid);
						uploadFile(mImagePath);
					} else {
						MeetingApp.hasLogin = false;
						ProfileHelper.insertOrUpdate(mContext, Constants.KEY_LOGGEDIN, "0");
						Toast.makeText(mContext, result.msg, Toast.LENGTH_LONG).show();
						startActivity(new Intent(MainActivity.this, LoginActivity.class));
						finish();
					}
				}

				@Override
				public void failure(RetrofitError arg0) {
					super.failure(arg0);
					MeetingApp.hasLogin = false;
					Toast.makeText(mContext, "登录失败", Toast.LENGTH_LONG).show();
					startActivity(new Intent(MainActivity.this, LoginActivity.class));
					finish();
				}
			});
		}

		private void hxLogin(String hxid) {
			if (hxid != null) {
				mHxid = hxid;
				if (!"".equals(mHxid)) {
					mDeshxPwd = MeetingUtils.getDESHXPwd(mHxid);
				}
				if (mHxid != null && mDeshxPwd != null) {
					hxLogin(mHxid, mDeshxPwd);
				} else {
					Toast.makeText(getApplication(), "登录失败！", Toast.LENGTH_LONG).show();
					startActivity(new Intent(MainActivity.this, LoginActivity.class));
					finish();
				}
			}
		}

		/**
		 * 环信登陆
		 * */
		public void hxLogin(String userName, String userPassward) {
			final StatAppMonitor monitor = new StatAppMonitor("hxcreatePublicGroup_Android");
			final long startTime = System.currentTimeMillis();
			EMChatManager.getInstance().login(userName, userPassward, new EMCallBack() {

				@Override
				public void onSuccess() {
					EMGroupManager.getInstance().loadAllGroups();
					EMChatManager.getInstance().loadAllConversations();
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							Intent intent = new Intent(MainActivity.this, MainActivity.class);
							startActivity(intent);
							finish();
							long difftime = System.currentTimeMillis() - startTime;
							monitor.setMillisecondsConsume(difftime);
							monitor.setReturnCode(StatAppMonitor.SUCCESS_RESULT_TYPE);
						}
					});
				}

				@Override
				public void onProgress(int arg0, String arg1) {

				}

				@Override
				public void onError(int arg0, final String message) {
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(getApplicationContext(), "登陆失败！" + message, Toast.LENGTH_SHORT).show();
							long difftime = System.currentTimeMillis() - startTime;
							monitor.setMillisecondsConsume(difftime);
							monitor.setReturnCode(StatAppMonitor.SUCCESS_RESULT_TYPE);
						}
					});
				}
			});
		}
	}

	private class IsShowLoadingReceive extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (curBtn == 0) {
				mImgvGroupsAdd.setVisibility(View.VISIBLE);
				mPbLoading.setVisibility(View.GONE);
			}
		}
	}
}
