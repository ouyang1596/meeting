package com.deshang365.meeting.activity;

import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.jackson.JsonNode;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit.RetrofitError;
import retrofit.client.Response;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deshang365.meeting.R;
import com.deshang365.meeting.baselib.MeetingApp;
import com.deshang365.meeting.model.GroupMemberInfo;
import com.deshang365.meeting.model.Network;
import com.deshang365.meeting.network.NetworkReturn;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.network.OnResponse;
import com.deshang365.meeting.util.MeetingUtils;
import com.deshang365.meeting.util.RGBLuminanceSource;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.tencent.stat.StatAppMonitor;
import com.tencent.stat.StatService;
import com.zxing.activity.CaptureActivity;

public class GroupsActivity extends BaseActivity {
	private EditText mEtvCreateGroupName, mEtvCreateGroupNickname, mEtvJoinGroupcode;
	private Button mBtnCreatGroup, mBtnJoinGroup;
	private RelativeLayout mRelCreateGroups;
	private LinearLayout mLlJoinGroups, mLlScan, mLlNearSignGroups;
	private TextView mTvTopical, mQrcodePic;
	private LinearLayout mLlBack;
	private String mHxGroupId;
	private Bitmap mScanBitmap;
	private int mIsScanOrGroupcode = -1;// 0 群组码加群 1扫描加群
	private int REQUESTCODE_SCAN = 0;
	private int REQUESTCODE_ALBUM = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_groups);
		initView();
	}

	private GroupMemberInfo res;

	private void initView() {
		mLlNearSignGroups = (LinearLayout) findViewById(R.id.ll_near_sign_group);
		mLlNearSignGroups.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				StatService.trackCustomEvent(mContext, "Ngroup", "OK");
				Intent intent = new Intent(GroupsActivity.this, NearSignGroupsActivity.class);
				startActivity(intent);
				finish();
			}
		});
		mLlBack = (LinearLayout) findViewById(R.id.ll_top_alert_back);
		mLlBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mTvTopical = (TextView) findViewById(R.id.tv_top_alert_text);
		mRelCreateGroups = (RelativeLayout) findViewById(R.id.rel_createGroups);
		mEtvCreateGroupName = (EditText) findViewById(R.id.etv_groupName);
		mEtvCreateGroupName.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String editable = mEtvCreateGroupName.getText().toString();
				String str = stringFilter(editable.toString());
				if (!editable.equals(str)) {
					mEtvCreateGroupName.setText(str);
					// 设置新的光标所在位置
					mEtvCreateGroupName.setSelection(str.length());
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});
		mEtvCreateGroupNickname = (EditText) findViewById(R.id.etv_group_nickname);
		if (MeetingApp.userInfo != null) {
			mEtvCreateGroupNickname.setText(MeetingApp.userInfo.name);
		}
		mEtvCreateGroupNickname.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String editable = mEtvCreateGroupNickname.getText().toString();
				String str = stringFilter(editable.toString());
				if (!editable.equals(str)) {
					mEtvCreateGroupNickname.setText(str);
					// 设置新的光标所在位置
					mEtvCreateGroupNickname.setSelection(str.length());
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});
		mBtnCreatGroup = (Button) findViewById(R.id.btn_creatGroups);
		mBtnCreatGroup.setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				StatService.trackCustomEvent(mContext, "Cgroup", "OK");
				final String groupName = mEtvCreateGroupName.getText().toString();
				final String groupNickname = mEtvCreateGroupNickname.getText().toString();
				if ("".equals(groupName)) {
					Toast.makeText(getApplication(), "请输入群组名称", 0).show();
					return;
				}
				if ("".equals(groupNickname)) {
					Toast.makeText(getApplication(), "请输入群组昵称", 0).show();
					return;
				}
				showWaitingDialog();
				if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
					new CreatHXGroupsTasks().execute(groupName, "...");
				} else {
					new CreatHXGroupsTasks().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, groupName, "...");
				}

			}
		});
		mBtnJoinGroup = (Button) findViewById(R.id.btn_joinGroups);
		mLlJoinGroups = (LinearLayout) findViewById(R.id.rel_joinGroups);
		mEtvJoinGroupcode = (EditText) findViewById(R.id.etv_group_code);
		mEtvJoinGroupcode.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.length() <= 0) {
					mBtnJoinGroup.setVisibility(View.GONE);
				} else {
					mBtnJoinGroup.setVisibility(View.VISIBLE);
				}

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		mLlScan = (LinearLayout) findViewById(R.id.ll_scan);
		mLlScan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				StatService.trackCustomEvent(mContext, "Scan", "OK");
				mIsScanOrGroupcode = 1;
				Intent intent = new Intent(GroupsActivity.this, CaptureActivity.class);
				startActivityForResult(intent, 0);
			}
		});
		mBtnJoinGroup.setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				StatService.trackCustomEvent(mContext, "JusedNumber", "OK");
				String idcard = mEtvJoinGroupcode.getText().toString();
				if ("".equals(idcard)) {
					Toast.makeText(getApplication(), "请输入群组码", 0).show();
					return;
				}
				mIsScanOrGroupcode = 0;
				showWaitingDialog();
				getGroupInfo(idcard);
			}
		});
		int value = getIntent().getIntExtra("groups", 0);
		if (value == 1) {
			mRelCreateGroups.setVisibility(View.VISIBLE);
			mLlJoinGroups.setVisibility(View.GONE);
			mTvTopical.setText("创建群组");
		} else if (value == 2) {
			mRelCreateGroups.setVisibility(View.GONE);
			mLlJoinGroups.setVisibility(View.VISIBLE);
			mTvTopical.setText("加入群组");
		}
		mQrcodePic = (TextView) findViewById(R.id.txtv_what_need);
		mQrcodePic.setVisibility(View.VISIBLE);
		mQrcodePic.setText("相册二维码");
		mQrcodePic.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				StatService.trackCustomEvent(mContext, "2code", "OK");
				Intent picIntent = new Intent(Intent.ACTION_GET_CONTENT);
				picIntent.setType("image/*");
				startActivityForResult(picIntent, REQUESTCODE_ALBUM);
			}
		});
	}

	@SuppressLint("NewApi")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// 处理扫描结果
		if (requestCode == REQUESTCODE_SCAN) {
			if (data == null) {
				return;
			}
			Bundle bundle = data.getExtras();
			String resulString = bundle.getString("result");
			jsonUtil(resulString);
		} else if (requestCode == REQUESTCODE_ALBUM) {
			if (data == null) {
				return;
			}
			Uri photoUri = data.getData();
			String absolutePath = MeetingUtils.getRealFilePath(this, photoUri);
			if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
				new ScanningImageAsyn().execute(absolutePath);
			} else {
				new ScanningImageAsyn().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, absolutePath);
			}
		}
	}

	class ScanningImageAsyn extends AsyncTask<String, Void, Result> {

		@Override
		protected Result doInBackground(String... params) {
			return scanningImage(params[0]);
		}

		@Override
		protected void onPostExecute(Result result) {
			super.onPostExecute(result);
			if (result != null) {
				String resulString = result.toString();
				jsonUtil(resulString);
			} else {
				Toast.makeText(mContext, "扫面失败", Toast.LENGTH_SHORT).show();
			}
		}

	}

	@SuppressLint("NewApi")
	private void jsonUtil(String resulString) {
		try {
			String substring = resulString.substring(8);
			JSONObject object = new JSONObject(substring);
			String groupid = object.getString("data");
			showWaitingDialog();
			getGroupInfo(groupid);
			// if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1)
			// {
			// new GetGroupInfoTask().execute(groupid);
			// } else {
			// new
			// GetGroupInfoTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
			// groupid);
			// }

		} catch (JSONException e) {
			Toast.makeText(mContext, "扫面失败", Toast.LENGTH_SHORT).show();
			return;
		}
	}

	/**
	 * 环信建群
	 * */
	class CreatHXGroupsTasks extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String a[] = {};
			StatAppMonitor monitor = new StatAppMonitor("hxcreatePublicGroup_Android");
			long startTime = System.currentTimeMillis();
			try {
				String groupname = MeetingUtils.getDESGroup(params[0]);
				String groupdes = MeetingUtils.getDESGroup(params[1]);
				EMGroup mEMGroup = EMGroupManager.getInstance().createPublicGroup(groupname, groupdes, a, false);
				mHxGroupId = mEMGroup.getGroupId();
				long difftime = System.currentTimeMillis() - startTime;
				monitor.setMillisecondsConsume(difftime);
				monitor.setReturnCode(StatAppMonitor.SUCCESS_RESULT_TYPE);
				StatService.reportAppMonitorStat(MeetingApp.mContext, monitor);
				return "success";
			} catch (EaseMobException e) {
				long difftime = System.currentTimeMillis() - startTime;
				monitor.setMillisecondsConsume(difftime);
				monitor.setReturnCode(StatAppMonitor.FAILURE_RESULT_TYPE);
				return e.getLocalizedMessage();
			} finally {
				StatService.reportAppMonitorStat(MeetingApp.mContext, monitor);
			}

		}

		@SuppressLint("NewApi")
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if ("success".equals(result)) {
				createGroups(mEtvCreateGroupName.getText().toString(), mEtvCreateGroupNickname.getText().toString(), mHxGroupId);

			} else {
				hideWaitingDialog();
				Toast.makeText(GroupsActivity.this, "创建失败！", 1).show();
			}
		}
	}

	private void createGroups(String groupname, final String showname, String mobcode) {
		NewNetwork.createGroup(groupname, showname, mobcode, new OnResponse<NetworkReturn>("create_group_android") {
			@Override
			public void success(NetworkReturn result, retrofit.client.Response arg1) {
				super.success(result, arg1);
				hideWaitingDialog();
				if (result.result != 1) {
					Toast.makeText(GroupsActivity.this, result.msg, 0).show();
					return;

				}
				JsonNode object = result.data;
				GroupMemberInfo groupInfo = new GroupMemberInfo();
				groupInfo.idcard = object.get("idcard").getValueAsText();
				groupInfo.name = object.get("name").getValueAsText();
				groupInfo.mtype = object.get("mtype").getValueAsInt();
				groupInfo.group_id = object.get("group_id").getValueAsText();
				Toast.makeText(GroupsActivity.this, "创建成功", 0).show();
				Intent intent = new Intent(GroupsActivity.this, CompleteGroupActivity.class);
				intent.putExtra("idcard", groupInfo.idcard);
				intent.putExtra("groupname", groupInfo.name);
				intent.putExtra("hxgroupid", mHxGroupId);
				intent.putExtra("rescode", 1);
				intent.putExtra("groupid", groupInfo.group_id);
				intent.putExtra("showname", showname);
				startActivity(intent);
				finish();
			};

			@SuppressLint("NewApi")
			@Override
			public void failure(RetrofitError arg0) {
				super.failure(arg0);
				hideWaitingDialog();
				if (mHxGroupId != null) {
					if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
						new DeleteHXGroupAsyn().execute(mHxGroupId);
					} else {
						new DeleteHXGroupAsyn().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mHxGroupId);
					}

				}
				Toast.makeText(GroupsActivity.this, "创建失败！", 0).show();
			}
		});
	}

	public void getGroupInfo(final String idcard) {
		NewNetwork.getGroupInfo(idcard, new OnResponse<NetworkReturn>("groupinfo_byidcard_Android") {
			@Override
			public void success(NetworkReturn result, Response response) {
				super.success(result, response);
				hideWaitingDialog();
				if (result.result != 1) {
					Toast.makeText(GroupsActivity.this, result.msg, 0).show();
					return;
				}
				GroupMemberInfo groupInfo = new GroupMemberInfo();
				JsonNode object = result.data;
				// JSONObject object = jsonObject.getJSONObject("data");
				groupInfo.name = object.get("name").getValueAsText();
				groupInfo.group_id = object.get("id").getValueAsText();
				groupInfo.hxgroupid = object.get("mob_code").getValueAsText();
				groupInfo.uid = object.get("uid").getValueAsInt();
				Intent intent = new Intent(GroupsActivity.this, JoinGroupActivity.class);
				intent.putExtra("groupid", groupInfo.group_id);
				intent.putExtra("groupname", groupInfo.name);
				intent.putExtra("hxgroupid", groupInfo.hxgroupid);
				intent.putExtra("uid", groupInfo.uid);
				if (mIsScanOrGroupcode == 0) {
					intent.putExtra("groupcode", mEtvJoinGroupcode.getText().toString());
				} else if (mIsScanOrGroupcode == 1) {
					intent.putExtra("groupcode", idcard);
				}

				startActivity(intent);
				finish();
			}

			@Override
			public void failure(RetrofitError error) {
				super.failure(error);
				hideWaitingDialog();
				Toast.makeText(GroupsActivity.this, "获取信息失败", 0).show();
			}
		});
	}

	/**
	 * 环信删群
	 * */
	class DeleteHXGroupAsyn extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			StatAppMonitor monitor = new StatAppMonitor("hxexitAndDeleteGroup_Android");
			long startTime = System.currentTimeMillis();
			try {
				EMGroupManager.getInstance().exitAndDeleteGroup(params[0]);
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

	public String stringFilter(String str) {
		// 只允许字母、数字、汉字和_
		String regEx = "[^a-zA-Z0-9\u4E00-\u9FA5_]";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(str);
		return m.replaceAll("").trim();
	}

	/**
	 * 扫描相册二维码
	 * */
	private Result scanningImage(String path) {
		if (TextUtils.isEmpty(path)) {
			return null;

		}
		// DecodeHintType 和EncodeHintType
		Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
		hints.put(DecodeHintType.CHARACTER_SET, "utf-8"); // 设置二维码内容的编码
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true; // 先获取原大小
		mScanBitmap = BitmapFactory.decodeFile(path, options);
		options.inJustDecodeBounds = false; // 获取新的大小

		int sampleSize = (int) (options.outHeight / (float) 200);

		if (sampleSize <= 0)
			sampleSize = 1;
		options.inSampleSize = sampleSize;
		mScanBitmap = BitmapFactory.decodeFile(path, options);

		RGBLuminanceSource source = new RGBLuminanceSource(mScanBitmap);
		BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
		QRCodeReader reader = new QRCodeReader();
		try {
			return reader.decode(bitmap1, hints);
		} catch (NotFoundException e) {
		} catch (ChecksumException e) {
		} catch (FormatException e) {
		}

		return null;

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
