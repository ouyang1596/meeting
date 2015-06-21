package com.deshang365.meeting.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.codehaus.jackson.JsonNode;

import retrofit.RetrofitError;
import retrofit.client.Response;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

import com.deshang365.meeting.R;
import com.deshang365.meeting.adapter.GridGroupsAdapter;
import com.deshang365.meeting.baselib.MeetingApp;
import com.deshang365.meeting.model.Constants;
import com.deshang365.meeting.model.GroupMemberInfo;
import com.deshang365.meeting.network.NetworkReturn;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.network.OnResponse;
import com.deshang365.meeting.util.MeetingUtils;
import com.deshang365.meeting.view.MembersGridView;
import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;
import com.tencent.stat.StatAppMonitor;
import com.tencent.stat.StatService;

public class GroupDetailsActivity extends BaseActivity {
	private MembersGridView mGvMember;
	private Button mBtnGroupDismissOrExit;
	private TextView mTvTopical, mTvGroupName, mTvGroupcode, mTvShowname, mTvShare, mTvAllowForbidMemberJoin;
	private LinearLayout mLlBack;
	private RelativeLayout mRelQRCode, mRelGroupName, mRelNickName, mRelSignRecord, mRelAllSignRecord, mRelExportAllSignResult;
	private RelativeLayout mRelForbidJoin, mRelReport;
	private AlertDialog mExportDialog;
	private String mGroupid;
	private String mHxgroupid;
	private int mUid;
	private int mType;
	private View mDeleteExitView;
	private View mExitView;
	private GridGroupsAdapter mAdapter;
	private String mGroupcode;
	private static final String URL_SHARE = "http://www.wlyeah.com/share.html?version=1.4.3.96&clientType=iOS&systemTime=1432347513.43314&token=000c4b03-4e26-449c-b681-82f318c285a3&code=%s&uid=%d";
	private int mAllow_join;// 0允许其他人加入该群组 1不允许其他人加入该群组
	private int mIsAllowjoin;
	private boolean mBoolAllJoin;// true允许其他人加入该群组 false不允许其他人加入该群组

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_member);
		initView();
	}

	@SuppressLint("NewApi")
	private void initView() {
		setExportDialog();
		mAllow_join = getIntent().getIntExtra("allow_join", -1);
		mIsAllowjoin = mAllow_join;
		final String groupname = getIntent().getStringExtra("groupname");
		mGroupid = getIntent().getStringExtra("groupid");
		mGroupcode = getIntent().getStringExtra("groupcode");
		final String showname = getIntent().getStringExtra("showname");
		mType = getIntent().getIntExtra("mtype", -1);
		mHxgroupid = getIntent().getStringExtra("hxgroupid");
		mRelSignRecord = (RelativeLayout) findViewById(R.id.rel_signrecord);
		mRelSignRecord.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mType == 0) {
					Intent intent = new Intent(mContext, CreaterSignRecordActivity.class);
					intent.putExtra("groupid", mGroupid);
					intent.putExtra("groupname", groupname);
					startActivity(intent);
				} else if (mType == 1) {
					Intent intent = new Intent(mContext, JoinerSignRecordActivity.class);
					intent.putExtra("groupid", mGroupid);
					intent.putExtra("groupname", groupname);
					startActivity(intent);
				}

			}
		});
		mRelAllSignRecord = (RelativeLayout) findViewById(R.id.rel_all_signresult);
		mRelAllSignRecord.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, SignAllResultActivity.class);
				intent.putExtra("groupid", mGroupid);
				startActivity(intent);
			}
		});
		mRelExportAllSignResult = (RelativeLayout) findViewById(R.id.rel_export_signresult);
		mRelExportAllSignResult.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mExportDialog.show();
			}
		});
		mRelForbidJoin = (RelativeLayout) findViewById(R.id.rel_forbid_join);
		mRelReport = (RelativeLayout) findViewById(R.id.rel_report);
		mTvTopical = (TextView) findViewById(R.id.tv_top_alert_text);
		mLlBack = (LinearLayout) findViewById(R.id.ll_top_alert_back);
		mLlBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mAllow_join == mIsAllowjoin) {
					finish();
					return;
				}
				showWaitingDialog();
				isCanJoinGroup(mGroupid, mAllow_join, MeetingApp.mVersionName);
			}
		});
		mRelQRCode = (RelativeLayout) findViewById(R.id.rel_QR_code);
		mRelQRCode.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(GroupDetailsActivity.this, QRCodeActivity.class);
				intent.putExtra("groupid", mGroupcode);
				intent.putExtra("groupname", groupname);
				intent.putExtra("uid", mUid);
				intent.putExtra("groupcode", mGroupcode);
				startActivity(intent);
			}
		});
		mRelGroupName = (RelativeLayout) findViewById(R.id.rel_group_name);
		mRelNickName = (RelativeLayout) findViewById(R.id.rel_nickname);
		mRelNickName.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(GroupDetailsActivity.this, ChangeShowNameActivity.class);
				intent.putExtra("showname", showname);
				intent.putExtra("groupid", mGroupid);
				intent.putExtra("uid", MeetingApp.userInfo.uid);
				startActivityForResult(intent, 1);
			}
		});
		mGvMember = (MembersGridView) findViewById(R.id.gv_groupMember);
		showWaitingDialog();
		getGroupMember(mGroupid);
		mTvGroupName = (TextView) findViewById(R.id.txtv_group_name);
		mTvGroupName.setText(groupname);
		mTvGroupcode = (TextView) findViewById(R.id.txtv_group_code);
		mTvGroupcode.setText(mGroupcode);
		mTvShowname = (TextView) findViewById(R.id.txtv_nickname);
		mTvShowname.setText(showname);
		mBtnGroupDismissOrExit = (Button) findViewById(R.id.btn_dismissOrExit);
		if (mType == 0) {
			mRelGroupName.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					StatService.trackCustomEvent(mContext, "ChangeGroupName", "OK");
					Intent intent = new Intent(GroupDetailsActivity.this, ChangeGroupNameActivity.class);
					intent.putExtra("groupname", groupname);
					intent.putExtra("groupid", mGroupid);
					intent.putExtra("uid", MeetingApp.userInfo.uid);
					startActivityForResult(intent, 0);
				}
			});
			mBtnGroupDismissOrExit.setText("解散群组");
			mRelExportAllSignResult.setVisibility(View.VISIBLE);
			mRelAllSignRecord.setVisibility(View.VISIBLE);
			mRelForbidJoin.setVisibility(View.VISIBLE);
		} else if (mType == 1) {
			mBtnGroupDismissOrExit.setText("退出群组");
			mRelExportAllSignResult.setVisibility(View.GONE);
			mRelAllSignRecord.setVisibility(View.GONE);
			mRelForbidJoin.setVisibility(View.GONE);
		}

		mBtnGroupDismissOrExit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if ("解散群组".equals(mBtnGroupDismissOrExit.getText().toString())) {
					StatService.trackCustomEvent(mContext, "DissolveGroup", "OK");
					if (mDeleteExitView == null) {
						deleteExit();
						return;
					}
					mDialog.show();
				} else if ("退出群组".equals(mBtnGroupDismissOrExit.getText().toString())) {
					StatService.trackCustomEvent(mContext, "QuitGroup", "OK");
					if (mExitView == null) {
						exitGroup();
						return;
					}
					mDialog.show();
				}
			}
		});
		mTvShare = (TextView) findViewById(R.id.txtv_what_need);
		mTvShare.setVisibility(View.VISIBLE);
		mTvShare.setText("分享");
		mTvShare.setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
					new ShareAsync().execute();
				} else {
					new ShareAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				}
			}
		});
		mTvAllowForbidMemberJoin = (TextView) findViewById(R.id.txtv_allow_forbid_add_mmeber);
		initAllowJoin();
		mTvAllowForbidMemberJoin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mBoolAllJoin) {
					mTvAllowForbidMemberJoin.setBackgroundResource(R.drawable.turn_off);
					mAllow_join = 0;
					mBoolAllJoin = false;
				} else {
					mTvAllowForbidMemberJoin.setBackgroundResource(R.drawable.turn_on);
					mAllow_join = 1;
					mBoolAllJoin = true;
				}
			}
		});
	}

	private void initAllowJoin() {
		if (mAllow_join == 0) {
			mBoolAllJoin = true;
			mTvAllowForbidMemberJoin.setBackgroundResource(R.drawable.turn_off);
		} else if (mAllow_join == 1) {
			mBoolAllJoin = false;
			mTvAllowForbidMemberJoin.setBackgroundResource(R.drawable.turn_on);
		}
	}

	class ShareAsync extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			return resourceSave();
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result == null) {
				Toast.makeText(mContext, "没有找到图片储存路径！", Toast.LENGTH_SHORT).show();
				return;
			}
			showShare(mGroupcode, result);
		}
	}

	/**
	 * 将资源文件写入储存卡
	 * */
	public String resourceSave() {
		String logoPathString = Constants.ICON_PATH + "logo.png";
		Resources resources = getResources();
		AssetFileDescriptor openRawResource = resources.openRawResourceFd(R.drawable.logo2);
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			fis = openRawResource.createInputStream();
			byte[] buf = new byte[1024];
			fos = new FileOutputStream(new File(logoPathString));
			int len = -1;
			while ((len = fis.read(buf)) != -1) {
				fos.write(buf, 0, len);
			}
			if (fis != null) {
				fis.close();
			}
			if (fos != null) {
				fos.close();
			}
			return logoPathString;
		} catch (IOException e) {
			return null;
		}
	}

	/** 分享 */
	private void showShare(String idcard, String imagePath) {
		ShareSDK.initSDK(this);
		OnekeyShare oks = new OnekeyShare();
		// 关闭sso授权
		oks.disableSSOWhenAuthorize();
		// 分享时Notification的图标和文字 2.5.9以后的版本不调用此方法
		// oks.setNotification(R.drawable.ic_launcher,
		// getString(R.string.app_name));
		// title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
		oks.setTitle(getString(R.string.share));
		// titleUrl是标题的网络链接，仅在人人网和QQ空间使用
		// String urlPathString =
		// "http://www.wlyeah.com/share.html?p=%%7B%%22groupcode%%22%%3A%%22%d%%22%%7D";
		String urlFormat = String.format(URL_SHARE, idcard, MeetingApp.userInfo.uid);
		oks.setTitleUrl(urlFormat);
		// oks.setTitleUrl("http://www.wlyeah.com");
		// text是分享文本，所有平台都需要这个字段
		oks.setText("亲，我正在使用“我来也”，30秒完成一个班的点名签到，特方便，特赞！用群组码" + idcard + "赶快加入！你也可以扫描二维码哟！");
		// imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
		oks.setImagePath(imagePath);// 确保SDcard下面存在此张图片
		// url仅在微信（包括好友和朋友圈）中使用
		oks.setUrl(urlFormat);
		// oks.setUrl("http://www.wlyeah.com");
		// comment是我对这条分享的评论，仅在人人网和QQ空间使用
		// oks.setComment("我是测试评论文本");
		// site是分享此内容的网站名称，仅在QQ空间使用
		// oks.setSite(getString(R.string.app_name));
		// siteUrl是分享此内容的网站地址，仅在QQ空间使用
		// oks.setSiteUrl("http://sharesdk.cn");
		// 启动分享GUI
		oks.show(this);
	}

	private void deleteExit() {
		showDialog();
		mDeleteExitView = View.inflate(mContext, R.layout.exit_dialog, null);
		Button btnDeleteExit = (Button) mDeleteExitView.findViewById(R.id.btn_exit);
		TextView tvDeleteExit = (TextView) mDeleteExitView.findViewById(R.id.txtv_exit);
		tvDeleteExit.setText("解散群组？");
		tvDeleteExit.setVisibility(View.VISIBLE);
		btnDeleteExit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				StatService.trackCustomEvent(mContext, "DSQconfirm", "OK");
				mDialog.cancel();
				new HXGroupDismissAsyn().execute(mHxgroupid);
				showWaitingDialog();
				groupDismiss(mGroupid);
			}
		});
		Button btnCancel = (Button) mDeleteExitView.findViewById(R.id.btn_cancel);
		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				StatService.trackCustomEvent(mContext, "DSQcancel", "OK");
				mDialog.cancel();
			}
		});
		mDialog.show();
		mDialog.getWindow().setContentView(mDeleteExitView);
	}

	private void exitGroup() {
		showDialog();
		mExitView = View.inflate(mContext, R.layout.exit_dialog, null);
		Button btnExit = (Button) mExitView.findViewById(R.id.btn_exit);
		LinearLayout llExit = (LinearLayout) mExitView.findViewById(R.id.ll_delete_exit_group);
		TextView tvExit = (TextView) mExitView.findViewById(R.id.txtv_stop_or_exit);
		tvExit.setText("退出群");
		TextView tvExitDescription = (TextView) mExitView.findViewById(R.id.txtv_stop_or_exit_description);
		tvExitDescription.setText("退出后，您将无法完成签到");
		llExit.setVisibility(View.VISIBLE);
		btnExit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				StatService.trackCustomEvent(mContext, "ConfirmLoginOut", "OK");
				mDialog.cancel();
				new HXGroupExitAsyn().execute(mHxgroupid);
				showWaitingDialog();
				groupExit(mGroupid);
			}
		});
		Button btnCancel = (Button) mExitView.findViewById(R.id.btn_cancel);
		btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				StatService.trackCustomEvent(mContext, "CancelLoginOut", "OK");
				mDialog.cancel();
			}
		});
		mDialog.show();
		mDialog.getWindow().setContentView(mExitView);
	}

	public void getGroupMember(String groupid) {
		NewNetwork.getGroupMember(groupid, new OnResponse<NetworkReturn>("groupmemberlist_Android") {
			@Override
			public void success(NetworkReturn result, Response response) {
				super.success(result, response);
				hideWaitingDialog();
				if (result.result != 1) {
					Toast.makeText(mContext, result.msg, Toast.LENGTH_SHORT).show();
					mTvTopical.setText("群成员（0）");
					return;
				}
				ArrayList<GroupMemberInfo> groupMemberInfos = new ArrayList<GroupMemberInfo>();
				JsonNode data = result.data;
				for (int i = 0; i < data.size(); i++) {
					JsonNode item = data.get(i);
					GroupMemberInfo groupInfo = new GroupMemberInfo();
					groupInfo.showname = item.get("showname").getValueAsText();
					groupInfo.uid = item.get("uid").getValueAsInt();
					groupInfo.avatar = item.get("avatar").getValueAsText();
					groupInfo.hxid = item.get("acode").getValueAsText();
					groupInfo.hxgroupid = item.get("gcode").getValueAsText();
					groupInfo.mobile = item.get("mobile").getValueAsText();
					groupInfo.mtype = item.get("mtype").getValueAsInt();
					groupInfo.group_id = mGroupid;
					groupMemberInfos.add(groupInfo);
				}
				mUid = groupMemberInfos.get(0).uid;
				mTvTopical.setText("群成员（" + groupMemberInfos.size() + "）");
				MeetingUtils.sortChineseList(groupMemberInfos);
				mAdapter = new GridGroupsAdapter(GroupDetailsActivity.this, groupMemberInfos, mType);
				mGvMember.setAdapter(mAdapter);
			}

			@Override
			public void failure(RetrofitError error) {
				super.failure(error);
				hideWaitingDialog();
				mTvTopical.setText("群成员（0）");
			}
		});
	}

	public void groupDismiss(String groupid) {
		NewNetwork.groupDismiss(groupid, new OnResponse<NetworkReturn>("groupdismiss_Android") {
			@Override
			public void success(NetworkReturn result, Response response) {
				super.success(result, response);
				hideWaitingDialog();
				if (result.result == 1) {
					Toast.makeText(getApplication(), "群已解散！", Toast.LENGTH_SHORT).show();
					startActivity(new Intent(GroupDetailsActivity.this, MainActivity.class));
					finish();
				} else {
					Toast.makeText(getApplication(), result.msg, Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void failure(RetrofitError error) {
				super.failure(error);
				hideWaitingDialog();
				Toast.makeText(getApplication(), "群未能成功解散！", Toast.LENGTH_SHORT).show();
			}
		});
	}

	/**
	 * 环信解散群组
	 * */
	class HXGroupDismissAsyn extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			StatAppMonitor monitor = new StatAppMonitor("hxexitAndDeleteGroup_Android");
			long startTime = System.currentTimeMillis();
			try {
				if (params[0] == null) {
					monitor.setReturnCode(StatAppMonitor.FAILURE_RESULT_TYPE);
					return null;
				}
				EMGroupManager.getInstance().exitAndDeleteGroup(params[0]);
				long difftime = System.currentTimeMillis() - startTime;
				monitor.setMillisecondsConsume(difftime);
				monitor.setReturnCode(StatAppMonitor.SUCCESS_RESULT_TYPE);
				return "success";
			} catch (EaseMobException e) {
				long difftime = System.currentTimeMillis() - startTime;
				monitor.setMillisecondsConsume(difftime);
				monitor.setReturnCode(StatAppMonitor.FAILURE_RESULT_TYPE);
				return null;
			} finally {
				StatService.reportAppMonitorStat(MeetingApp.mContext, monitor);
			}
		}
	}

	public void groupExit(String groupid) {
		NewNetwork.groupExit(groupid, new OnResponse<NetworkReturn>("groupexit_Android") {
			@Override
			public void success(NetworkReturn result, Response response) {
				super.success(result, response);
				hideWaitingDialog();
				if (result.result == 1) {
					Toast.makeText(getApplication(), "已退群！", Toast.LENGTH_SHORT).show();
					startActivity(new Intent(GroupDetailsActivity.this, MainActivity.class));
					finish();
				} else {
					Toast.makeText(getApplication(), result.msg, Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void failure(RetrofitError error) {
				super.failure(error);
				hideWaitingDialog();
				Toast.makeText(getApplication(), "未能成功退出群", Toast.LENGTH_SHORT).show();
			}
		});
	}

	/**
	 * 环信退群
	 * */
	class HXGroupExitAsyn extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			StatAppMonitor monitor = new StatAppMonitor("hxexitAndDeleteGroup_Android");
			long startTime = System.currentTimeMillis();
			try {
				if (params[0] == null) {
					monitor.setReturnCode(StatAppMonitor.FAILURE_RESULT_TYPE);
					return null;
				}
				EMGroupManager.getInstance().exitFromGroup(params[0]);
				long difftime = System.currentTimeMillis() - startTime;
				monitor.setMillisecondsConsume(difftime);
				monitor.setReturnCode(StatAppMonitor.SUCCESS_RESULT_TYPE);
				return "success";
			} catch (EaseMobException e) {
				long difftime = System.currentTimeMillis() - startTime;
				monitor.setMillisecondsConsume(difftime);
				monitor.setReturnCode(StatAppMonitor.FAILURE_RESULT_TYPE);
				return null;
			} finally {
				StatService.reportAppMonitorStat(MeetingApp.mContext, monitor);
			}

		}

	}

	private AlertDialog mDialog;

	private void showDialog() {
		if (mDialog == null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			mDialog = builder.create();
		}
	}

	private View mView;

	private void setExportDialog() {
		if (mView == null) {
			mView = View.inflate(mContext, R.layout.dialog_email_item, null);
		}
		final EditText eTvEmail = (EditText) mView.findViewById(R.id.etv_email);
		eTvEmail.setText(MeetingApp.userInfo.email);
		eTvEmail.setSelection(eTvEmail.length());
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle("请输入要导出的邮箱");
		builder.setView(mView).setPositiveButton("确定", new DialogInterface.OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(DialogInterface dialog, int which) {
				StatService.trackCustomEvent(mContext, "Outputtomail", "OK");
				String email = eTvEmail.getText().toString();
				if (email.length() <= 0) {
					Toast.makeText(mContext, "邮箱地址不能为空！", 0).show();
					return;
				}
				showWaitingDialog();
				exportAllSignResult(mGroupid, email);

			}
		}).setNegativeButton("取消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				StatService.trackCustomEvent(mContext, "OutputtomailCancel", "OK");
			}
		});
		mExportDialog = builder.create();

	}

	public void exportAllSignResult(String groupid, String email) {
		NewNetwork.exportAllSignResult(groupid, email, new OnResponse<NetworkReturn>("export_multiple_meeting_results_Android") {
			@Override
			public void success(NetworkReturn result, Response response) {
				super.success(result, response);
				hideWaitingDialog();
				Toast.makeText(getApplication(), result.msg, Toast.LENGTH_SHORT).show();
			}

			@Override
			public void failure(RetrofitError error) {
				super.failure(error);
				hideWaitingDialog();
				Toast.makeText(getApplication(), "导出失败", Toast.LENGTH_SHORT).show();
			}
		});

	}

	private void isCanJoinGroup(String group_id, int is_allow, String app_version) {
		NewNetwork.isCanJoinGroup(group_id, is_allow, app_version, new OnResponse<NetworkReturn>("set_groupentrance_Android") {
			@Override
			public void success(NetworkReturn result, Response arg1) {
				super.success(result, arg1);
				hideWaitingDialog();
				Toast.makeText(mContext, result.msg, Toast.LENGTH_SHORT).show();
				finish();
			}

			@Override
			public void failure(RetrofitError error) {
				super.failure(error);
				hideWaitingDialog();
				Toast.makeText(mContext, "请求失败", Toast.LENGTH_SHORT).show();
				finish();
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == event.KEYCODE_BACK) {
			if (mAllow_join == mIsAllowjoin) {
				return super.onKeyDown(keyCode, event);
			}
			showWaitingDialog();
			isCanJoinGroup(mGroupid, mAllow_join, MeetingApp.mVersionName);
		}
		return super.onKeyDown(keyCode, event);
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
