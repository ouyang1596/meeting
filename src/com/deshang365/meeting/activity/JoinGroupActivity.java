package com.deshang365.meeting.activity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit.RetrofitError;
import retrofit.client.Response;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deshang365.meeting.R;
import com.deshang365.meeting.baselib.MeetingApp;
import com.deshang365.meeting.network.NetworkReturn;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.network.OnResponse;
import com.deshang365.meeting.util.MeetingUtils;
import com.deshang365.meeting.view.CircularImageView;
import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;
import com.tencent.stat.StatAppMonitor;
import com.tencent.stat.StatService;

public class JoinGroupActivity extends ImageloaderBaseActivity {
	private EditText mEtvNickName;
	private Button mBtnJoin;
	private TextView mTvGroupName, mTvGroupCode, mTvTopical;
	private LinearLayout mLlBack;
	private CircularImageView mImgvHead;
	private String mIdcard;
	private String mGroupname;
	private String mGroupid;
	private String mHxgroupid;
	private int mUid;
	private int mIsJumpToSign;// 是否跳到签到页面 0跳到 1不需要

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_join_group);
		if (MeetingApp.mHxHasLogin) {
			initView();
		} else {
			hxLogin(MeetingApp.userInfo.hxid);
		}
	}

	private void initView() {
		mIsJumpToSign = getIntent().getIntExtra("isJumpToSign", -1);
		mGroupname = getIntent().getStringExtra("groupname");
		mGroupid = getIntent().getStringExtra("groupid");
		mUid = getIntent().getIntExtra("uid", -1);
		mHxgroupid = getIntent().getStringExtra("hxgroupid");
		mIdcard = getIntent().getStringExtra("groupcode");
		mTvTopical = (TextView) findViewById(R.id.tv_top_alert_text);
		mTvTopical.setText(mGroupname);
		mImgvHead = (CircularImageView) findViewById(R.id.imgv_join_group_head);
		mLlBack = (LinearLayout) findViewById(R.id.ll_top_alert_back);
		mLlBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mTvGroupName = (TextView) findViewById(R.id.txtv_group_name);
		mTvGroupCode = (TextView) findViewById(R.id.txtv_group_code);
		mEtvNickName = (EditText) findViewById(R.id.etv_group_nickname);
		mEtvNickName.setText(MeetingApp.userInfo.name);
		mEtvNickName.setSelection(mEtvNickName.length());
		mEtvNickName.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String editable = mEtvNickName.getText().toString();
				String str = stringFilter(editable.toString());
				if (!editable.equals(str)) {
					mEtvNickName.setText(str);
					// 设置新的光标所在位置
					mEtvNickName.setSelection(str.length());
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		mBtnJoin = (Button) findViewById(R.id.btn_join);
		setImageView();
		mTvGroupName.setText(mGroupname);
		mTvGroupCode.setText(mIdcard);
		mBtnJoin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				StatService.trackCustomEvent(mContext, "Jgroup", "OK");
				String nickname = mEtvNickName.getText().toString();
				if ("".equals(nickname)) {
					Toast.makeText(getApplication(), "群组昵称没写", 0).show();
					return;
				}
				showWaitingDialog();
				new JoinHXGroupTasks().execute(mHxgroupid);
			}
		});
	}

	private void hxLogin(String hxid) {
		if (hxid != null && !hxid.isEmpty()) {
			String desHxid = MeetingUtils.getDESHXPwd(hxid);
			if (desHxid != null) {
				doLoginHx(hxid, desHxid);
			} else {
				Toast.makeText(mContext, "登录失败！", 0).show();
			}
		}
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
				runOnUiThread(mLoginSucc);
				long difftime = System.currentTimeMillis() - mStartTime;
				mMonitor.setMillisecondsConsume(difftime);
				mMonitor.setReturnCode(StatAppMonitor.SUCCESS_RESULT_TYPE);
				StatService.reportAppMonitorStat(MeetingApp.mContext, mMonitor);
				try {
					EMGroupManager.getInstance().getGroupsFromServer();
				} catch (EaseMobException e) {

				}
			}

			@Override
			public void onProgress(int arg0, String arg1) {

			}

			@Override
			public void onError(int arg0, final String message) {
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(mContext, "登陆失败！" + message, Toast.LENGTH_SHORT).show();
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
			initView();
		}
	};

	private void setImageView() {
		mImageLoader.displayImage(NewNetwork.getAvatarUrl(mUid), mImgvHead, mOptions);
	}

	/**
	 * 环信加群
	 * */
	class JoinHXGroupTasks extends AsyncTask<String, Void, EaseMobException> {

		@Override
		protected EaseMobException doInBackground(String... params) {
			StatAppMonitor monitor = new StatAppMonitor("hxjoinGroup_Android");
			long startTime = System.currentTimeMillis();
			try {
				EMGroupManager.getInstance().joinGroup(params[0]);
				long difftime = System.currentTimeMillis() - startTime;
				monitor.setMillisecondsConsume(difftime);
				monitor.setReturnCode(StatAppMonitor.SUCCESS_RESULT_TYPE);
				return null;
			} catch (EaseMobException e) {
				Log.e("bm", "fail" + e.getLocalizedMessage());
				long difftime = System.currentTimeMillis() - startTime;
				monitor.setMillisecondsConsume(difftime);
				monitor.setReturnCode(StatAppMonitor.FAILURE_RESULT_TYPE);
				return e;
			} finally {
				StatService.reportAppMonitorStat(MeetingApp.mContext, monitor);
			}
		}

		@SuppressLint("NewApi")
		@Override
		protected void onPostExecute(EaseMobException e) {
			super.onPostExecute(e);
			if (e == null) {
				joinGroup(mEtvNickName.getText().toString(), mGroupid);
			} else {
				hideWaitingDialog();
				Toast.makeText(getApplication(), "加群失败！", 0).show();
			}
		}
	}

	public void joinGroup(String nickname, String groupid) {
		NewNetwork.joinGroup(nickname, groupid, new OnResponse<NetworkReturn>("groupuseradd_Android") {
			@Override
			public void success(NetworkReturn result, Response response) {
				super.success(result, response);
				hideWaitingDialog();
				if (result.result == 1) {
					Toast.makeText(getApplication(), "加入成功", 0).show();
					if (mIsJumpToSign == 0) {
						Intent intent = new Intent(JoinGroupActivity.this, UserSignActivity.class);
						intent.putExtra("groupname", mGroupname);
						intent.putExtra("groupid", mGroupid);
						intent.putExtra("backtomain", 1);// UserSignActivity是否需要返回到main
															// 1需要 -1不需要
						startActivity(intent);
					} else {
						Intent intent = new Intent(JoinGroupActivity.this, CompleteGroupActivity.class);
						intent.putExtra("groupname", mGroupname);
						intent.putExtra("idcard", mIdcard);
						intent.putExtra("groupid", mGroupid);
						intent.putExtra("rescode", 2);
						intent.putExtra("uid", mUid);
						intent.putExtra("hxgroupid", mHxgroupid);
						startActivity(intent);
					}
					finish();
				} else if (result.result == -2) {
					Toast.makeText(getApplication(), result.msg, 0).show();
				} else {
					if (mHxgroupid != null) {
						new ExitHXGroupAsyn().execute(mHxgroupid);
					}
					Toast.makeText(getApplication(), result.msg, 0).show();

				}
			}

			@Override
			public void failure(RetrofitError error) {
				super.failure(error);
				hideWaitingDialog();
				if (mHxgroupid != null) {
					new ExitHXGroupAsyn().execute(mHxgroupid);
				}
				Toast.makeText(JoinGroupActivity.this, "加群失败！", 0).show();
			}
		});
	}

	// class JoinGroupTasks extends AsyncTask<String, Void, NetworkReturnBase> {
	//
	// @Override
	// protected NetworkReturnBase doInBackground(String... params) {
	// return Network.joinGroup(params[0], params[1]);
	// }
	//
	// @Override
	// protected void onPostExecute(NetworkReturnBase result) {
	// super.onPostExecute(result);
	// hideWaitingDialog();
	// if (result == null) {
	// if (mHxgroupid != null) {
	// new ExitHXGroupAsyn().execute(mHxgroupid);
	// }
	// Toast.makeText(JoinGroupActivity.this, "加群失败！", 0).show();
	// return;
	// }
	// if (result.rescode == 1) {
	// Toast.makeText(getApplication(), "加入成功", 0).show();
	// if (mIsJumpToSign == 0) {
	// Intent intent = new Intent(JoinGroupActivity.this,
	// UserSignActivity.class);
	// intent.putExtra("groupname", mGroupname);
	// intent.putExtra("groupid", mGroupid);
	// intent.putExtra("backtomain", 1);// UserSignActivity是否需要返回到main
	// // 1需要 -1不需要
	// startActivity(intent);
	// } else {
	// Intent intent = new Intent(JoinGroupActivity.this,
	// CompleteGroupActivity.class);
	// intent.putExtra("groupname", mGroupname);
	// intent.putExtra("idcard", mIdcard);
	// intent.putExtra("rescode", 2);
	// intent.putExtra("uid", mUid);
	// intent.putExtra("hxgroupid", mHxgroupid);
	// startActivity(intent);
	// }
	// finish();
	// } else if (result.rescode == -2) {
	// Toast.makeText(getApplication(), result.message, 0).show();
	// } else {
	// if (mHxgroupid != null) {
	// new ExitHXGroupAsyn().execute(mHxgroupid);
	// }
	// Toast.makeText(getApplication(), result.message, 0).show();
	//
	// }
	// }
	// }

	public String stringFilter(String str) {
		// 只允许字母、数字、汉字和_
		String regEx = "[^a-zA-Z0-9\u4E00-\u9FA5_]";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(str);
		return m.replaceAll("").trim();
	}

	/**
	 * 本服务器加群失败，环信退群
	 * */
	class ExitHXGroupAsyn extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			StatAppMonitor monitor = new StatAppMonitor("hxexitFromGroup_Android");
			long startTime = System.currentTimeMillis();
			try {
				EMGroupManager.getInstance().exitFromGroup(params[0]);
				long difftime = System.currentTimeMillis() - startTime;
				monitor.setMillisecondsConsume(difftime);
				monitor.setReturnCode(StatAppMonitor.SUCCESS_RESULT_TYPE);
				return null;
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

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
}
