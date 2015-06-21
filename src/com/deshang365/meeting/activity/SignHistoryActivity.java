package com.deshang365.meeting.activity;

import java.util.Timer;
import java.util.TimerTask;

import org.codehaus.jackson.JsonNode;

import retrofit.RetrofitError;
import retrofit.client.Response;
import android.R.integer;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
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

public class SignHistoryActivity extends BaseActivity {
	private TextView mTvTopical, mTvSignCount, mTvIsSignIng, mTvSigncode, mTvGroupTalk, mTvSignRecord;
	private Button mBtnUserSign;
	private PullToRefreshListView mLvSignHistory;
	private LinearLayout mLlBack;
	private ImageView mImgvGroupmembers, mImgvToSign;
	private String mGroupid;
	private Handler mHandler;
	private EditText mETvSignCode;
	private Timer mTimer;
	private String mCreaterUid;
	private String ANDROID_ID;
	private String signingMeetingid;
	private TimeCount mTimeCount;
	private int pageCount;// pageCount必须从一开始
	private int mSignTimes = 0;
	private String mAnswer;
	/**
	 * 正在签到的群组会返回一个has_sign字段表示用户是否签过到 1:用户已签到 0:用户未签到
	 * */
	private int mHasSign;
	private String mSignState;
	private View mView;
	private int mMeetingType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_history);
		ANDROID_ID = MeetingUtils.getAndroidID(mContext);
		initView();
	}

	@SuppressWarnings("unchecked")
	private void initView() {
		mHasSign = getIntent().getIntExtra("hassign", -1);
		mSignState = getIntent().getStringExtra("signstate");
		final int allow_join = getIntent().getIntExtra("allow_join", -1);
		final String groupname = getIntent().getStringExtra("groupname");
		final String groupcode = getIntent().getStringExtra("groupcode");
		final String showname = getIntent().getStringExtra("showname");
		final String hxgroupid = getIntent().getStringExtra("hxgroupid");
		final int mtype = getIntent().getIntExtra("mtype", -1);
		mGroupid = getIntent().getStringExtra("groupid");
		signingMeetingid = getIntent().getStringExtra("meetingid");
		mMeetingType = getIntent().getIntExtra("meetingtype", -1);
		mTvSigncode = (TextView) findViewById(R.id.txtv_groupcode);
		mTvSigncode.setText(groupcode);
		mLlBack = (LinearLayout) findViewById(R.id.ll_top_alert_back);
		mLlBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mTvIsSignIng = (TextView) findViewById(R.id.txtv_issigning);
		mTvTopical = (TextView) findViewById(R.id.tv_top_alert_text);
		mTvTopical.setText(groupname);
		mTvGroupTalk = (TextView) findViewById(R.id.txtv_grouptalk);
		mTvGroupTalk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, TalkTogetherActivity.class);
				intent.putExtra("mobcode", hxgroupid);
				intent.putExtra("groupName", groupname);
				startActivity(intent);
				EMChatManager.getInstance().getConversation(hxgroupid).resetUnreadMsgCount();
			}
		});
		mTvSignRecord = (TextView) findViewById(R.id.txtv_signrecord);
		mTvSignRecord.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, JoinerSignRecordActivity.class);
				intent.putExtra("groupname", groupname);
				intent.putExtra("groupid", mGroupid);
				startActivity(intent);
			}
		});

		mBtnUserSign = (Button) findViewById(R.id.btn_tosign);
		mBtnUserSign.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mMeetingType == 0) {
					initDialog();
					if (mView == null) {
						mView = View.inflate(mContext, R.layout.join_word_sign, null);
					}
					mETvSignCode = (EditText) mView.findViewById(R.id.etv_signcode);
					mETvSignCode.addTextChangedListener(onTextWatcher);
					Button mBtnEnsure = (Button) mView.findViewById(R.id.btn_ensure);
					mBtnEnsure.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							mAnswer = mETvSignCode.getText().toString();
							if (mAnswer.length() <= 0) {
								Toast.makeText(mContext, "请输入签到码", Toast.LENGTH_SHORT).show();
								return;
							}
							mDialog.cancel();
							showWaitingDialog();
							Log.e("bm", "mAnswer==" + mAnswer);
							userSign(mAnswer);
						}
					});
					// 为了能显示输入法
					mDialog.setView(new EditText(mContext));
					mDialog.show();
					mDialog.getWindow().setContentView(mView);
				} else {
					BluetoothManager bluetoothManager = BluetoothManager.getInstance();
					bluetoothManager.startBluetooth();
					if (!bluetoothManager.isDiscoverable() && bluetoothManager.openDiscoverable(0)) {
						bluetoothManager.openDiscoverableAsyn(SignHistoryActivity.this);
					}

					Intent serviceIntent = new Intent(mContext, TimeToUploadService.class);
					serviceIntent.putExtra("groupid", mGroupid);
					serviceIntent.putExtra("meetingid", signingMeetingid);
					startService(serviceIntent);
					showWaitingDialog();
					startTimer();
					if (mTimeCount == null) {
						mTimeCount = new TimeCount(60000, 1000);
						mTimeCount.start();
					}
				}
			}
		});
		mImgvGroupmembers = (ImageView) findViewById(R.id.iv_group_members);
		mImgvGroupmembers.setVisibility(View.VISIBLE);
		mImgvToSign = (ImageView) findViewById(R.id.imgv_tosign);
		mImgvGroupmembers.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				StatService.trackCustomEvent(mContext, "OpenGroup", "OK");
				Intent intent = new Intent(SignHistoryActivity.this, GroupDetailsActivity.class);
				intent.putExtra("groupname", groupname);
				intent.putExtra("groupcode", groupcode);
				intent.putExtra("groupid", mGroupid);
				intent.putExtra("showname", showname);
				intent.putExtra("allow_join", allow_join);
				intent.putExtra("mtype", mtype);
				intent.putExtra("hxgroupid", hxgroupid);
				startActivity(intent);
			}
		});
		if ("0".equals(mSignState)) {
			if (mHasSign == 1) {
				mBtnUserSign.setEnabled(false);
				mBtnUserSign.setBackgroundResource(R.drawable.invalid);
				mTvIsSignIng.setVisibility(View.GONE);
				mBtnUserSign.setText("已成功签到");
			} else if (mHasSign == 0) {
				mBtnUserSign.setEnabled(true);
				mBtnUserSign.setBackgroundResource(R.drawable.btn_blue_bg_radius_selector);
				mTvIsSignIng.setVisibility(View.VISIBLE);
				mBtnUserSign.setText("立即签到");
			}
		} else {
			mBtnUserSign.setEnabled(false);
			mBtnUserSign.setBackgroundResource(R.drawable.invalid);
			mTvIsSignIng.setVisibility(View.GONE);
			mBtnUserSign.setText("暂未发起签到");
		}
		if (MeetingApp.mVersionName != null) {
			isSigning(mGroupid, MeetingApp.mVersionName);
		} else {
			isSigning(mGroupid, "-1");
		}
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

		}

		@Override
		public void afterTextChanged(Editable s) {
		}
	};
	private AlertDialog mDialog;

	private void initDialog() {
		if (mDialog == null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			mDialog = builder.create();
			mDialog.setCanceledOnTouchOutside(true);
		}
	}

	private void bluetoothJoinSign() {
		NewNetwork.bluetoothJoinSign(mGroupid, ANDROID_ID, new OnResponse<NetworkReturn>("bluetooth_sign_android") {
			@Override
			public void success(NetworkReturn result, Response arg1) {
				super.success(result, arg1);
				if (result.result == 1) {
					hideWaitingDialog();
					mTimer.cancel();
					mTimeCount.cancel();
					mTimeCount = null;
					mTimer = null;
					Intent intent = new Intent(mContext, LoginSignedActivity.class);
					intent.putExtra("groupid", mGroupid);
					intent.putExtra("answer", "");
					if (signingMeetingid != null) {
						intent.putExtra("meetingid", signingMeetingid);
					}
					startActivity(intent);
					finish();
				}
			}

			@Override
			public void failure(RetrofitError arg0) {
				super.failure(arg0);
			}
		});
	}

	private void isSigning(String group_id, String app_version) {
		NewNetwork.isSigning(group_id, "0", app_version, new OnResponse<NetworkReturn>("issign_v1_Android") {
			@Override
			public void success(NetworkReturn result, Response arg1) {
				super.success(result, arg1);
				if (result.result == 1) {
					// {"meeting":{"state":0,"meeting_id":2273},"stateCode":3}
					JsonNode data = result.data;
					JsonNode meetingInfoJson = data.get("meeting");
					int state = data.get("stateCode").getValueAsInt();
					switch (state) {
					case 1:
						mBtnUserSign.setEnabled(false);
						mBtnUserSign.setBackgroundResource(R.drawable.invalid);
						mTvIsSignIng.setVisibility(View.GONE);
						mBtnUserSign.setText("暂未发起签到");
						break;
					case 2:
						mBtnUserSign.setEnabled(false);
						mBtnUserSign.setBackgroundResource(R.drawable.invalid);
						mTvIsSignIng.setVisibility(View.GONE);
						mBtnUserSign.setText("已成功签到");
						break;
					case 3:
						mBtnUserSign.setEnabled(true);
						mBtnUserSign.setBackgroundResource(R.drawable.btn_blue_bg_radius_selector);
						mTvIsSignIng.setVisibility(View.VISIBLE);
						mBtnUserSign.setText("立即签到");
						mMeetingType = meetingInfoJson.get("mtype").getValueAsInt();
						break;
					default:
						break;
					}
				}
			}

			@Override
			public void failure(RetrofitError error) {
				super.failure(error);
			}
		});
	}

	private void userSign(String answer) {
		NewNetwork.userSign(mGroupid, answer, MeetingApp.getLat(mSignTimes), MeetingApp.getLng(mSignTimes), ANDROID_ID,
				new OnResponse<NetworkReturn>("sign_android") {
					@Override
					public void success(NetworkReturn result, Response arg1) {
						super.success(result, arg1);
						JsonNode data = result.data;
						String meetingId = "";
						if (data.has("meeting_id")) {
							meetingId = data.get("meeting_id").getValueAsText();
						}
						if (result.result == 1) {
							Intent intent = new Intent(mContext, LoginSignedActivity.class);
							intent.putExtra("groupid", mGroupid);
							intent.putExtra("answer", mAnswer);
							intent.putExtra("meetingid", meetingId);
							intent.putExtra("backtomain", 1);// UserSignActivity是否需要返回到main
							// 1需要返回，-1不需要返回
							startActivity(intent);
							finish();
						} else if (result.result == -5) {
							if (mSignTimes < 2) {
								mSignTimes++;
								userSign(mAnswer);
							} else {
								hideWaitingDialog();
								Toast.makeText(mContext, "不在附近", Toast.LENGTH_SHORT).show();
							}
						} else {
							hideWaitingDialog();
							Toast.makeText(mContext, result.msg, Toast.LENGTH_SHORT).show();
						}
					}

					@Override
					public void failure(RetrofitError arg0) {
						super.failure(arg0);
						hideWaitingDialog();
						Toast.makeText(mContext, "签到失败", Toast.LENGTH_SHORT).show();
						mSignTimes = 0;
					}
				});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// 网上说是RESULT_OK,但试了一下，返回值是RESULT_FIRST_USER
		Log.e("bm", "resultCode==" + resultCode);
		if (requestCode == BluetoothManager.REQUEST_DISCOVERABLE && resultCode != RESULT_FIRST_USER) {
			Toast.makeText(mContext, "为保证签到成功，请打开蓝牙，并设置可被发现", Toast.LENGTH_SHORT).show();
		}
	}

	// class GetPesonalSignRecord extends AsyncTask<String, Void,
	// GroupMemberInfoList> {
	//
	// @Override
	// protected GroupMemberInfoList doInBackground(String... params) {
	// return Network.getPesonalSignRecord(params[0], params[1], params[2]);
	//
	// }
	//
	// @Override
	// protected void onPostExecute(GroupMemberInfoList result) {
	// super.onPostExecute(result);
	// hideWaitingDialog();
	// mLvSignHistory.onRefreshComplete();
	// if (result == null) {
	// Toast.makeText(SignHistoryActivity.this, "获取个人签到记录失败！",
	// Toast.LENGTH_SHORT).show();
	// mTvSignCount.setText("总共发起0次签到，缺席0次，请假0次");
	// return;
	// }
	// if (result.rescode == 1) {
	// List<GroupMemberInfo> list = result.mGroupMemberInfosList;
	// int signCount, signNormal = 0, signLeave = 0, signAbsence = 0;
	// signCount = list.size();
	// for (int i = 0; i < list.size(); i++) {
	// if (list.get(i).state == 0) {
	// signNormal += 1;
	// } else if (list.get(i).state == 1) {
	// signLeave += 1;
	// } else if (list.get(i).state == 2) {
	// signAbsence += 1;
	// }
	// }
	// mTvSignCount.setText("总共发起" + signCount + "次签到，缺席" + signAbsence + "次，请假"
	// + signLeave + "次");
	// SignHistoryAdapter adapter = new SignHistoryAdapter(getApplication(),
	// list);
	// mLvSignHistory.setAdapter(adapter);
	// } else {
	// mTvSignCount.setText("总共发起0次签到，缺席0次，请假0次");
	// Toast.makeText(SignHistoryActivity.this, result.message,
	// Toast.LENGTH_SHORT).show();
	// }
	// }
	// }

	// class IsSigningAsyn extends AsyncTask<String, Void, UserToSign> {
	//
	// @Override
	// protected UserToSign doInBackground(String... params) {
	// return Network.isSigning(params[0], params[1]);
	// }
	//
	// @Override
	// protected void onPostExecute(UserToSign result) {
	// super.onPostExecute(result);
	// // hideWaitingDialog();
	// // mTvIsSignIng.setVisibility(View.VISIBLE);
	// // mBtnUserSign.setVisibility(View.VISIBLE);
	// // if (result == 1) {
	// // mTvIsSignIng.setText("已经签过");
	// // mBtnUserSign.setEnabled(false);
	// // mBtnUserSign.setBackgroundResource(R.drawable.invalid);
	// // } else if (result == 0) {
	// // mTvIsSignIng.setText("正在签到中");
	// // mBtnUserSign.setEnabled(true);
	// // mBtnUserSign.setBackgroundResource(R.drawable.orange_yellow_radius);
	// // } else if (result == -4) {
	// // mTvIsSignIng.setText("暂未发起签到");
	// // mBtnUserSign.setEnabled(false);
	// // mBtnUserSign.setBackgroundResource(R.drawable.invalid);
	// // } else {
	// // mTvIsSignIng.setText("暂未发起签到或已经签过");
	// // mBtnUserSign.setEnabled(false);
	// // mBtnUserSign.setBackgroundResource(R.drawable.invalid);
	// // }
	// // new GetPesonalSignRecord().execute(mGroupid);
	// }
	// }

	/**
	 * 定时发送请求
	 * */
	private void startTimer() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				bluetoothJoinSign();
			}
		};
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				mHandler.sendMessage(mHandler.obtainMessage());
			}
		};
		if (mTimer == null) {
			mTimer = new Timer();
			mTimer.schedule(task, 2000, 20000);
		}
	}

	/**
	 * 倒计时类
	 * */
	class TimeCount extends CountDownTimer {
		public TimeCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);// 参数依次为总时长,和计时的时间间隔
		}

		@Override
		public void onFinish() {// 计时完毕时触发
			hideWaitingDialog();
			mTimer.cancel();
			mTimer = null;
			cancel();
			mTimeCount = null;
			Toast.makeText(mContext, "签到失败", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onTick(long millisUntilFinished) {// 计时过程显示
			// mBtnResend.setText(millisUntilFinished / 1000 + "秒");
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mWaitingDialog = null;
	}
}
