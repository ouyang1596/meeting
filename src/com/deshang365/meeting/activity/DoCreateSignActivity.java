package com.deshang365.meeting.activity;

import org.codehaus.jackson.JsonNode;
import org.json.JSONObject;

import retrofit.RetrofitError;
import retrofit.client.Response;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deshang365.meeting.R;
import com.deshang365.meeting.baselib.BluetoothManager;
import com.deshang365.meeting.baselib.MeetingApp;
import com.deshang365.meeting.model.GroupMemberInfo;
import com.deshang365.meeting.model.Network;
import com.deshang365.meeting.network.NetworkReturn;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.network.OnResponse;
import com.deshang365.meeting.service.TimeToUploadService;

public class DoCreateSignActivity extends BaseActivity {
	private EditText mEtvSignCode;
	private Button mBtnwordSignEnsure, mBtnBluetoothSignEnsure;
	private TextView mTvTopical;
	private LinearLayout mLlBack;
	private ImageView mImgvRondomSignCode;
	private TextView mTvChangeMode;
	private RelativeLayout mRelWordSign, mRelBluetoothSign;
	private String mGroupname;
	private String mGroupid;
	private int mtype;
	private String mGroupcode;
	private static final int REQUEST_DISCOVERABLE = 0;
	private String mShowname;
	private String mHxGroupid;
	private int mSignMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign);
		initView();
		// startBluetooth();
	}

	private void initView() {
		mSignMode = getIntent().getIntExtra("signmode", -1);
		mShowname = getIntent().getStringExtra("showname");
		mHxGroupid = getIntent().getStringExtra("hxgroupid");
		mGroupname = getIntent().getStringExtra("groupname");
		mGroupid = getIntent().getStringExtra("groupid");
		mtype = getIntent().getIntExtra("mtype", -1);
		mGroupcode = getIntent().getStringExtra("groupcode");
		mRelWordSign = (RelativeLayout) findViewById(R.id.rel_word_sign);
		mRelBluetoothSign = (RelativeLayout) findViewById(R.id.rel_bluetooth_sign);
		if (mSignMode == 0) {
			mRelWordSign.setVisibility(View.VISIBLE);
			mRelBluetoothSign.setVisibility(View.GONE);
		} else {
			mRelWordSign.setVisibility(View.GONE);
			mRelBluetoothSign.setVisibility(View.VISIBLE);
		}
		mLlBack = (LinearLayout) findViewById(R.id.ll_top_alert_back);
		mLlBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mTvTopical = (TextView) findViewById(R.id.tv_top_alert_text);
		if (mRelWordSign.isShown()) {
			mTvTopical.setText("口令签到");
		} else {
			mTvTopical.setText("蓝牙签到");
		}
		mEtvSignCode = (EditText) findViewById(R.id.etv_sign);
		mEtvSignCode.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.length() > 4) {
					mEtvSignCode.setText(mEtvSignCode.getText().toString().substring(0, 4));
					mEtvSignCode.setSelection(4);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		mBtnwordSignEnsure = (Button) findViewById(R.id.btn_ensure);
		mBtnwordSignEnsure.setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				if ("".equals(mEtvSignCode.getText().toString())) {
					Toast.makeText(getApplication(), "请输入签到码", 0).show();
					return;
				}

				// createSign(mGroupid, mEtvSignCode.getText().toString(),
				// MeetingApp.getLat(0), MeetingApp.getLng(0));
			}
		});
		mBtnBluetoothSignEnsure = (Button) findViewById(R.id.btn_bluetooth_ensure);
		mBtnBluetoothSignEnsure.setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				BluetoothManager bluetoothManager = BluetoothManager.getInstance();
				bluetoothManager.startBluetooth();
				if (!bluetoothManager.openDiscoverable(0)) {
					bluetoothManager.openDiscoverableAsyn(DoCreateSignActivity.this);
				} else {
					Intent serviceIntent = new Intent(mContext, TimeToUploadService.class);
					serviceIntent.putExtra("groupid", mGroupid);
					serviceIntent.putExtra("meetingid", mGroupid);
					startService(serviceIntent);
					// createBlueSign(mGroupid);
				}
			}
		});
		mTvChangeMode = (TextView) findViewById(R.id.txtv_what_need);
		mTvChangeMode.setText("切换模式");
		mTvChangeMode.setVisibility(View.VISIBLE);
		mTvChangeMode.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mRelWordSign.isShown()) {
					mRelWordSign.setVisibility(View.GONE);
					mRelBluetoothSign.setVisibility(View.VISIBLE);
					mTvTopical.setText("蓝牙签到");
				} else {
					mRelWordSign.setVisibility(View.VISIBLE);
					mRelBluetoothSign.setVisibility(View.GONE);
					mTvTopical.setText("口令签到");
				}

			}
		});
		mImgvRondomSignCode = (ImageView) findViewById(R.id.imgv_etv_word_sign);
		mImgvRondomSignCode.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int random = (int) (Math.random() * 10000);
				if (random <= 1000 || random >= 10000) {
					random = 1000;
				}
				mEtvSignCode.setText("" + random);

			}
		});
	}

	// private void createBlueSign(String groupId) {
	// NewNetwork.createBlueSign(groupId, new
	// OnResponse<NetworkReturn>("bluetooth_meetingadd_Android") {
	// @Override
	// public void success(NetworkReturn arg0, Response arg1) {
	// super.success(arg0, arg1);
	// if (arg0.result != 1) {
	// Toast.makeText(mContext, arg0.msg, Toast.LENGTH_SHORT).show();
	// return;
	// }
	//
	// JsonNode object = arg0.data;
	// String meetingId = object.get("meeting_id").getValueAsText();
	//
	// Intent intent = new Intent(mContext, SigningActivity.class);
	// intent.putExtra("meetingid", meetingId);
	// intent.putExtra("groupid", mGroupid);
	// intent.putExtra("groupcode", mGroupcode);
	// intent.putExtra("groupname", mGroupname);
	// intent.putExtra("showname", mShowname);
	// intent.putExtra("hxgroupid", mHxGroupid);
	// intent.putExtra("mtype", mtype);
	// intent.putExtra("createsigntype", 1);// 1蓝牙签到
	// startActivity(intent);
	// finish();
	// }
	//
	// @Override
	// public void failure(RetrofitError arg0) {
	// super.failure(arg0);
	// Toast.makeText(mContext, "发起失败！", Toast.LENGTH_SHORT).show();
	// }
	// });
	// }

	// private void createSign(String groupId, String signCode, double lat,
	// double lng) {
	// NewNetwork.createSign(groupId, signCode, lat, lng,new
	// OnResponse<NetworkReturn>("create_sign_android") {
	// @Override
	// public void success(NetworkReturn result, Response arg1) {
	// super.success(result, arg1);
	// if (result.result != 1) {
	// Toast.makeText(getApplication(), result.msg, Toast.LENGTH_SHORT).show();
	// return;
	// }
	// Toast.makeText(getApplication(), "签到已发起", 0).show();
	//
	// JsonNode object = result.data;
	// String meetingid = object.get("meeting_id").getValueAsText();
	// Intent intent = new Intent(mContext, SigningActivity.class);
	// intent.putExtra("meetingid", meetingid);
	// intent.putExtra("groupid", mGroupid);
	// intent.putExtra("groupcode", mGroupcode);
	// intent.putExtra("groupname", mGroupname);
	// intent.putExtra("showname", mShowname);
	// intent.putExtra("hxgroupid", mHxGroupid);
	// intent.putExtra("mtype", mtype);
	// intent.putExtra("createsigntype", 0);// 0口令签到
	// startActivity(intent);
	// finish();
	// }
	//
	// @Override
	// public void failure(RetrofitError arg0) {
	// super.failure(arg0);
	// Toast.makeText(getApplication(), "未能成功发起签到", Toast.LENGTH_SHORT).show();
	// }
	// });
	// }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// 网上说是RESULT_OK,但试了一下，返回值是RESULT_FIRST_USER
		if (requestCode == REQUEST_DISCOVERABLE && resultCode == RESULT_FIRST_USER) {
			Intent serviceIntent = new Intent(mContext, TimeToUploadService.class);
			serviceIntent.putExtra("groupid", mGroupid);
			serviceIntent.putExtra("meetingid", mGroupid);
			startService(serviceIntent);
			// createBlueSign(mGroupid);
		} else {
			Toast.makeText(mContext, "发起签到失败，请先打开蓝牙，并设置可被发现！", Toast.LENGTH_SHORT).show();
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
