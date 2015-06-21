package com.deshang365.meeting.activity;

import org.codehaus.jackson.JsonNode;

import retrofit.RetrofitError;
import retrofit.client.Response;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deshang365.meeting.R;
import com.deshang365.meeting.adapter.SignHistoryAdapter;
import com.deshang365.meeting.baselib.BluetoothManager;
import com.deshang365.meeting.baselib.MeetingApp;
import com.deshang365.meeting.network.NetworkReturn;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.network.OnResponse;
import com.deshang365.meeting.service.TimeToUploadService;
import com.deshang365.meeting.util.MeetingUtils;
import com.easemob.chat.EMChatManager;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tencent.stat.StatService;

public class CreateSignActivity extends BaseActivity {
	private TextView mTvTopical, mTvGroupTalk, mTvSignRecord, mTvSetSignMode, mTvSignMode, mTvSignDescription, mTvGroupCode;
	private TextView mTvTimeSub, mTvTimeAdd, mTvTimeData;
	private PullToRefreshListView mLvSignRecord;
	private ImageView mImgvGroupmembers, mImgvSignMode;
	private LinearLayout mLlBack;
	private SignHistoryAdapter mAdapter;
	private ImageView mImgvRondomSignCode;
	private EditText mETvSignCode;
	private Button mBtnWordSign, /* mBtnBlueSign, */mBtnSignResult, mBtnCreateSign;
	private String mGroupid;
	private String mGroupname;
	private String mGroupcode;
	private String mHxgroupid;
	private String mShowname;
	private int mType;
	private View mBlueToothSignedView;
	private AlertDialog mDialog;

	private int mCreateSignType;// 签到类型 1蓝牙签到，0口令签到
	private int mPageCount;
	private View mView;
	private int mTimeData;
	private int mAllow_join;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_record_history);
		initView();
	}

	@SuppressWarnings("unchecked")
	@SuppressLint("NewApi")
	private void initView() {
		mAllow_join = getIntent().getIntExtra("allow_join", -1);
		mGroupname = getIntent().getStringExtra("groupname");
		mGroupid = getIntent().getStringExtra("groupid");
		mGroupcode = getIntent().getStringExtra("groupcode");
		mHxgroupid = getIntent().getStringExtra("hxgroupid");
		mShowname = getIntent().getStringExtra("showname");
		mType = getIntent().getIntExtra("mtype", -1);
		mLlBack = (LinearLayout) findViewById(R.id.ll_top_alert_back);
		mLlBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mTvTimeData = (TextView) findViewById(R.id.txtv_time_data);
		mTvTimeSub = (TextView) findViewById(R.id.txtv_time_sub);
		mTvTimeSub.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mTimeData == 0) {
					mTvTimeData.setText("0");
					return;
				}
				mTimeData -= 1;
				mTvTimeData.setText("" + mTimeData);

			}
		});
		mTvTimeAdd = (TextView) findViewById(R.id.txtv_time_add);
		mTvTimeAdd.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mTimeData += 1;
				mTvTimeData.setText("" + mTimeData);
			}
		});
		mImgvSignMode = (ImageView) findViewById(R.id.imgv_signmode);
		mImgvSignMode.setImageResource(R.drawable.word_sign);
		mTvSignDescription = (TextView) findViewById(R.id.txtv_tag_signdescription);
		mTvSignMode = (TextView) findViewById(R.id.txtv_signmode);
		mTvSignMode.setText("口令签到");
		mTvSignDescription.setText("请告知参与者签到码以便完成其签到");
		mTvSignMode.setTextColor(Color.parseColor("#00b26d"));
		mTvSetSignMode = (TextView) findViewById(R.id.txtv_set_signmode);
		getSignMode();
		mTvSetSignMode.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mCreateSignType == 0) {
					setSignMode(1, "蓝牙签到", R.drawable.bluetooth_sign, "请告知参与者打开蓝牙以便完成其签到", "#ff961d");
				} else {
					setSignMode(0, "口令签到", R.drawable.word_sign, "请告知参与者签到码以便完成其签到", "#00b26d");
				}
			}

		});
		mTvGroupCode = (TextView) findViewById(R.id.txtv_groupcode);
		mTvGroupCode.setText("" + mGroupcode);
		mTvTopical = (TextView) findViewById(R.id.tv_top_alert_text);
		mTvTopical.setText(mGroupname);
		mTvGroupTalk = (TextView) findViewById(R.id.txtv_grouptalk);
		mTvGroupTalk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mHxgroupid != null) {
					Intent intent = new Intent(mContext, TalkTogetherActivity.class);
					intent.putExtra("mobcode", mHxgroupid);
					intent.putExtra("groupName", mGroupname);
					startActivity(intent);
					EMChatManager.getInstance().getConversation(mHxgroupid).resetUnreadMsgCount();
				}
			}
		});
		mTvSignRecord = (TextView) findViewById(R.id.txtv_signrecord);
		mTvSignRecord.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, CreaterSignRecordActivity.class);
				intent.putExtra("groupname", mGroupname);
				intent.putExtra("groupid", mGroupid);
				startActivity(intent);
			}
		});
		mBtnCreateSign = (Button) findViewById(R.id.btn_createsign);
		mBtnCreateSign.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mCreateSignType == 0) {
					initDialog();
					if (mView == null) {
						mView = View.inflate(mContext, R.layout.create_word_sign, null);
					}
					mETvSignCode = (EditText) mView.findViewById(R.id.etv_signcode);
					mETvSignCode.setText("" + randomSignCode());
					mImgvRondomSignCode = (ImageView) mView.findViewById(R.id.imgv_random);
					mImgvRondomSignCode.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							int random = randomSignCode();
							mETvSignCode.setText("" + random);
						}

					});
					mETvSignCode.addTextChangedListener(onTextWatcher);
					Button mBtnEnsure = (Button) mView.findViewById(R.id.btn_ensure);
					mBtnEnsure.setOnClickListener(new OnClickListener() {

						@SuppressLint("NewApi")
						@Override
						public void onClick(View v) {
							String signcode = mETvSignCode.getText().toString();
							if (signcode.length() <= 0) {
								Toast.makeText(mContext, "请输入签到码", Toast.LENGTH_SHORT).show();
								return;
							}
							mDialog.cancel();
							showWaitingDialog();
							createSign(mGroupid, signcode, MeetingApp.getLat(0), MeetingApp.getLng(0), mTimeData);

						}
					});
					// 为了能显示输入法
					mDialog.setView(new EditText(mContext));
					mDialog.show();
					mDialog.getWindow().setContentView(mView);
				} else if (mCreateSignType == 1) {
					BluetoothManager bluetoothManager = BluetoothManager.getInstance();
					bluetoothManager.startBluetooth();
					if (bluetoothManager.isDiscoverable() || bluetoothManager.openDiscoverable(0)) {
						Intent serviceIntent = new Intent(mContext, TimeToUploadService.class);
						serviceIntent.putExtra("groupid", mGroupid);
						serviceIntent.putExtra("meetingid", mGroupid);
						startService(serviceIntent);
						showWaitingDialog();
						createBlueSign(mGroupid, mTimeData);
					} else {
						bluetoothManager.openDiscoverableAsyn(CreateSignActivity.this);
					}
				}
			}
		});
		mImgvGroupmembers = (ImageView) findViewById(R.id.iv_group_members);
		mImgvGroupmembers.setVisibility(View.VISIBLE);
		mImgvGroupmembers.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				StatService.trackCustomEvent(mContext, "OpenGroup", "OK");
				Intent intent = new Intent(CreateSignActivity.this, GroupDetailsActivity.class);
				intent.putExtra("groupname", mGroupname);
				intent.putExtra("groupcode", mGroupcode);
				intent.putExtra("groupid", mGroupid);
				intent.putExtra("showname", mShowname);
				intent.putExtra("allow_join", mAllow_join);
				intent.putExtra("mtype", mType);
				intent.putExtra("hxgroupid", mHxgroupid);
				startActivity(intent);
			}
		});

	}

	private void getSignMode() {
		int signMode = MeetingUtils.getParams();
		if (signMode == 0) {
			setSignMode(0, "口令签到", R.drawable.word_sign, "请告知参与者签到码以便完成其签到", "#00b26d");
		} else if (signMode == 1) {
			setSignMode(1, "蓝牙签到", R.drawable.bluetooth_sign, "请告知参与者打开蓝牙以便完成其签到", "#ff961d");
		}
	}

	private void setSignMode(int signType, String signMode, int picSrc, String description, String color) {
		mCreateSignType = signType;
		mTvSignMode.setText(signMode);
		mImgvSignMode.setImageResource(picSrc);
		mTvSignDescription.setText(description);
		mTvSignMode.setTextColor(Color.parseColor(color));
	}

	private int randomSignCode() {
		int random = (int) (Math.random() * 10000);
		if (random <= 1000 || random >= 10000) {
			random = 1000;
		}
		return random;
	}

	private void createBlueSign(String groupId, int meeting_time) {
		NewNetwork.createBlueSign(groupId, meeting_time, new OnResponse<NetworkReturn>("bluetooth_meetingadd_Android") {
			@Override
			public void success(NetworkReturn arg0, Response arg1) {
				super.success(arg0, arg1);
				hideWaitingDialog();
				if (arg0.result != 1) {
					Toast.makeText(mContext, arg0.msg, Toast.LENGTH_SHORT).show();
					return;
				}

				JsonNode object = arg0.data;
				String meetingId = object.get("meeting_id").getValueAsText();
				Intent intent = new Intent(mContext, SigningActivity.class);
				intent.putExtra("meetingid", meetingId);
				intent.putExtra("groupid", mGroupid);
				intent.putExtra("groupcode", mGroupcode);
				intent.putExtra("groupname", mGroupname);
				intent.putExtra("showname", mShowname);
				intent.putExtra("hxgroupid", mHxgroupid);
				intent.putExtra("mtype", mType);
				intent.putExtra("createsigntype", 1);// 1蓝牙签到
				startActivity(intent);
				finish();
			}

			@Override
			public void failure(RetrofitError arg0) {
				super.failure(arg0);
				hideWaitingDialog();
				Toast.makeText(mContext, "发起失败！", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void createSign(String groupId, String signCode, double lat, double lng, int meeting_time) {
		NewNetwork.createSign(groupId, signCode, lat, lng, meeting_time, new OnResponse<NetworkReturn>("create_sign_android") {
			@Override
			public void success(NetworkReturn result, Response arg1) {
				super.success(result, arg1);
				hideWaitingDialog();
				if (result.result != 1) {
					Toast.makeText(getApplication(), result.msg, Toast.LENGTH_SHORT).show();
					return;
				}
				Toast.makeText(getApplication(), "签到已发起", 0).show();
				JsonNode object = result.data;
				String meetingid = object.get("meeting_id").getValueAsText();
				Intent intent = new Intent(mContext, SigningActivity.class);
				intent.putExtra("meetingid", meetingid);
				intent.putExtra("groupid", mGroupid);
				intent.putExtra("groupcode", mGroupcode);
				intent.putExtra("groupname", mGroupname);
				intent.putExtra("showname", mShowname);
				intent.putExtra("hxgroupid", mHxgroupid);
				intent.putExtra("mtype", mType);
				intent.putExtra("createsigntype", 0);// 0口令签到
				startActivity(intent);
				finish();
			}

			@Override
			public void failure(RetrofitError arg0) {
				super.failure(arg0);
				hideWaitingDialog();
				Toast.makeText(getApplication(), "未能成功发起签到", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private TextWatcher onTextWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (s.length() > 4) {
				mETvSignCode.setText(mETvSignCode.getText().toString().substring(0, 4));
				mETvSignCode.setSelection(4);
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
	};

	private void initDialog() {
		if (mDialog == null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			mDialog = builder.create();
			mDialog.setCanceledOnTouchOutside(true);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// 网上说是RESULT_OK,但试了一下，返回值是RESULT_FIRST_USER
		Log.e("bm", "resultCode==" + resultCode);
		if (requestCode == BluetoothManager.REQUEST_DISCOVERABLE && resultCode == RESULT_FIRST_USER) {
			Intent serviceIntent = new Intent(mContext, TimeToUploadService.class);
			serviceIntent.putExtra("groupid", mGroupid);
			serviceIntent.putExtra("meetingid", mGroupid);
			showWaitingDialog();
			startService(serviceIntent);
			showWaitingDialog();
			createBlueSign(mGroupid, mTimeData);
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

}
