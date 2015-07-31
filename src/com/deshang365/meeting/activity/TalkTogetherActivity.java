package com.deshang365.meeting.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;

import retrofit.RetrofitError;
import retrofit.client.Response;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deshang365.meeting.R;
import com.deshang365.meeting.adapter.TalkAdapter;
import com.deshang365.meeting.baselib.MeetingApp;
import com.deshang365.meeting.model.GroupMemberInfo;
import com.deshang365.meeting.network.NetworkReturn;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.network.OnResponse;
import com.deshang365.meeting.util.MeetingUtils;
import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.TextMessageBody;
import com.tencent.stat.StatService;

public class TalkTogetherActivity extends BaseActivity {
	private TextView mTvGroup, title;
	private EditText mEtvMessage;
	private Button mBtnSend;
	private ListView mLvMessage;
	private String mHxgroupid;
	private ImageView mImgvGroupMember;
	private RelativeLayout mRelWhole;
	public static TalkTogetherActivity mTalkTogetherActivityInstance;
	private LinearLayout mLlBack;
	InputMethodManager mImm;
	private EMConversation mConversation;
	private TalkAdapter mAdapter;
	private NewMessageBroadcastReceiver mMsgReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_talk_together);
		initView();
	}

	private void registerBroadcast() {
		mMsgReceiver = new NewMessageBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter(EMChatManager.getInstance().getNewMessageBroadcastAction());
		intentFilter.setPriority(3);
		registerReceiver(mMsgReceiver, intentFilter);
	}

	private void getMessage() {
		if (mHxgroupid != null) {
			EMGroup emGroup = EMGroupManager.getInstance().getGroup(mHxgroupid);
			if (emGroup != null) {
				String enGroupname = emGroup.getGroupName();
				if (enGroupname != null) {
					String groupname = MeetingUtils.decDESGroup(enGroupname);
					mConversation = EMChatManager.getInstance().getConversation(mHxgroupid);
					mAdapter = new TalkAdapter(this, mConversation, groupname, emGroup.getGroupId());
					mLvMessage.setAdapter(mAdapter);
					mLvMessage.setSelection(mLvMessage.getCount() - 1);
				}
			}
		}
		registerBroadcast();
	}

	private void initView() {
		mLlBack = (LinearLayout) findViewById(R.id.ll_top_alert_back);
		mLlBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mTalkTogetherActivityInstance = this;
		title = (TextView) findViewById(R.id.tv_top_alert_text);
		final String groupName = getIntent().getStringExtra("groupname");
		title.setText(groupName);
		mHxgroupid = getIntent().getStringExtra("mobcode");
		final int allow_join = getIntent().getIntExtra("allow_join", -1);
		final String showName = getIntent().getStringExtra("showname");
		final String groupid = getIntent().getStringExtra("groupid");
		final String groupcode = getIntent().getStringExtra("groupcode");
		final int mtype = getIntent().getIntExtra("mtype", -1);
		mRelWhole = (RelativeLayout) findViewById(R.id.rel_whole);
		mImgvGroupMember = (ImageView) findViewById(R.id.imgv_what_need);
		mImgvGroupMember.setVisibility(View.VISIBLE);
		mImgvGroupMember.setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				StatService.trackCustomEvent(mContext, "OpenGroup", "OK");
				Intent intent = new Intent(TalkTogetherActivity.this, GroupDetailsActivity.class);
				intent.putExtra("groupid", groupid);
				intent.putExtra("groupname", groupName);
				intent.putExtra("showname", showName);
				intent.putExtra("groupcode", groupcode);
				intent.putExtra("hxgroupid", mHxgroupid);
				intent.putExtra("allow_join", allow_join);
				intent.putExtra("mtype", mtype);
				TalkTogetherActivity.this.startActivity(intent);
			}
		});
		mLvMessage = (ListView) findViewById(R.id.lv_showMessage);
		mLvMessage.setDividerHeight(0);
		getMembersByHxGroupId(mHxgroupid);
		mEtvMessage = (EditText) findViewById(R.id.etxt_message);
		mEtvMessage.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mLvMessage.setSelection(mLvMessage.getCount() - 1);
				return false;
			}
		});
		mBtnSend = (Button) findViewById(R.id.btn_send);
		mBtnSend.setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				sendMessage();
			}
		});
		mLvMessage.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (mImm == null) {
					mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				}
				mImm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				return false;
			}
		});
	}

	private class NewMessageBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 消息id
			String msgId = intent.getStringExtra("msgid");
			// 发消息的人的username(userid)
			String msgFrom = intent.getStringExtra("from");
			// 消息类型，文本，图片，语音消息等,这里返回的值为msg.type.ordinal()。
			// 所以消息type实际为是enum类型
			int msgType = intent.getIntExtra("type", 0);
			Log.d("main", "new message id:" + msgId + " from:" + msgFrom + " type:" + msgType);
			// 更方便的方法是通过msgId直接获取整个message
			EMMessage message = EMChatManager.getInstance().getMessage(msgId);
			if (mAdapter != null) {
				mAdapter.notifyDataSetChanged();
			}
			mLvMessage.setSelection(mLvMessage.getCount() - 1);
		}
	}

	/**
	 * 发送消息
	 * */
	private void sendMessage() {
		StatService.trackCustomEvent(mContext, "Setmessage", "OK");
		String content = mEtvMessage.getText().toString();
		if (content.length() <= 0) {
			Toast.makeText(getApplication(), "消息不能为空！", 0).show();
			return;
		}
		String desMessage = MeetingUtils.getDESMessage(content);
		uploadMessage(mHxgroupid, content);
		EMMessage message = EMMessage.createSendMessage(EMMessage.Type.TXT);
		message.setChatType(ChatType.GroupChat);
		TextMessageBody txtBody = new TextMessageBody(desMessage);
		// 设置消息body
		message.addBody(txtBody);
		// 设置要发给谁,用户username或者群聊groupid
		message.setReceipt(mHxgroupid);
		// 把messgage加到conversation中
		if (mConversation != null && mAdapter != null) {
			mConversation.addMessage(message);
			mAdapter.notifyDataSetChanged();
		}
		mLvMessage.setSelection(mLvMessage.getCount() - 1);
		// 发送消息
		EMChatManager.getInstance().sendMessage(message, new EMCallBack() {

			@Override
			public void onError(int arg0, final String message) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (mAdapter != null) {
							mAdapter.notifyDataSetChanged();
						}
						mLvMessage.setSelection(mLvMessage.getCount() - 1);
					}
				});
			}

			@Override
			public void onProgress(int arg0, String arg1) {

			}

			@Override
			public void onSuccess() {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (mAdapter != null) {
							mAdapter.notifyDataSetChanged();
						}
						mLvMessage.setSelection(mLvMessage.getCount() - 1);
					}
				});

			}
		});
		mEtvMessage.setText("");
	}

	private void uploadMessage(String mob_code, String message) {
		NewNetwork.uploadMessage(mob_code, message, new OnResponse<NetworkReturn>("bak_msg_Android") {
			@Override
			public void success(NetworkReturn result, Response arg1) {
				super.success(result, arg1);
			}

			@Override
			public void failure(RetrofitError error) {
				super.failure(error);
			}
		});
	}

	private void getMembersByHxGroupId(final String hxGroupId) {
		NewNetwork.getMembersByHxGroupId(hxGroupId, new OnResponse<NetworkReturn>("groupdetail_bymobcode_Android") {
			@Override
			public void success(NetworkReturn result, Response arg1) {
				super.success(result, arg1);
				if (result.result != 1) {
					Toast.makeText(mContext, result.msg, Toast.LENGTH_SHORT).show();
					return;
				}
				ArrayList<GroupMemberInfo> groupMemberInfos = new ArrayList<GroupMemberInfo>();
				JsonNode data = result.data;
				for (int i = 0; i < data.size(); i++) {
					JsonNode object = data.get(i);
					GroupMemberInfo groupinfo = new GroupMemberInfo();
					groupinfo.mobile = object.get("showname").getValueAsText();
					groupinfo.hxid = object.get("mob_code").getValueAsText();
					groupinfo.uid = object.get("uid").getValueAsInt();
					groupMemberInfos.add(groupinfo);
				}
				List<Integer> datas = new ArrayList<Integer>();
				if (groupMemberInfos != null) {
					Map<String, GroupMemberInfo> memInfoMap = new HashMap<String, GroupMemberInfo>();
					for (int n = 0; n < groupMemberInfos.size(); n++) {
						GroupMemberInfo info = groupMemberInfos.get(n);
						memInfoMap.put(info.hxid, info);
						datas.add(groupMemberInfos.get(n).uid);
					}
					MeetingApp.sMapGroupMemberInfo.put(hxGroupId, memInfoMap);
				}
				getMessage();
			}

			@Override
			public void failure(RetrofitError error) {
				super.failure(error);
				Toast.makeText(mContext, "获取聊天记录失败", Toast.LENGTH_SHORT).show();
			}
		});
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
		mTalkTogetherActivityInstance = null;
		if (mMsgReceiver != null) {
			unregisterReceiver(mMsgReceiver);
			mMsgReceiver = null;
		}
	}

}
