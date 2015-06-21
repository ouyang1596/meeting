package com.deshang365.meeting.adapter;

import java.io.File;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.deshang365.meeting.R;
import com.deshang365.meeting.baselib.ImageHandle;
import com.deshang365.meeting.baselib.MeetingApp;
import com.deshang365.meeting.model.Constants;
import com.deshang365.meeting.model.GroupMemberInfo;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.util.MeetingUtils;
import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.easemob.util.DateUtils;

public class TalkAdapter extends ImageLoaderAdapterBase {
	private EMConversation mDatas;
	private Context mContext;
	private Activity mActivity;
	private String mGroupName;
	private String mGroupId;

	public TalkAdapter(Context context, EMConversation datas, String groupName, String groupId) {
		super(context);
		this.mDatas = datas;
		this.mContext = context;
		mActivity = (Activity) context;
		mGroupName = groupName;
		mGroupId = groupId;
	}

	@Override
	public int getCount() {
		return mDatas == null ? 0 : mDatas.getMsgCount();
	}

	@Override
	public Object getItem(int position) {
		return mDatas.getMessage(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View con, ViewGroup parent) {

		final ViewHolder vh;
		if (con == null) {
			con = View.inflate(mContext, R.layout.group_talk_item, null);
			vh = new ViewHolder();
			vh.relGroupItemLeft = (RelativeLayout) con.findViewById(R.id.rel_groupItem_left);
			vh.mTvGroupNameLeft = (TextView) con.findViewById(R.id.txtv_group_userName_left);
			vh.mTvGroupTimeLeft = (TextView) con.findViewById(R.id.txtv_group_time_left);
			vh.mTvGroupContentLeft = (TextView) con.findViewById(R.id.txtv_group_content_left);
			vh.relGroupItemRight = (RelativeLayout) con.findViewById(R.id.rel_groupItem_right);
			vh.mTvGroupTimeRight = (TextView) con.findViewById(R.id.txtv_group_time_right);
			vh.mTvGroupContentRight = (TextView) con.findViewById(R.id.txtv_group_content_right);
			vh.mProBarSendingRight = (ProgressBar) con.findViewById(R.id.proBar_content_right);
			vh.mImgvFailToSend = (ImageView) con.findViewById(R.id.imgv_content_failToSend_right);
			vh.mImgvHeadRight = (ImageView) con.findViewById(R.id.imgv_head_right);
			vh.mImgvHeadLeft = (ImageView) con.findViewById(R.id.imgv_head_left);
			con.setTag(vh);
		} else {
			vh = (ViewHolder) con.getTag();
		}
		final EMMessage mEMMessage = mDatas.getMessage(position);
		TextMessageBody message = (TextMessageBody) mDatas.getMessage(position).getBody();
		String desMessage = MeetingUtils.decDESMessage(message.getMessage());
		String currentUser = EMChatManager.getInstance().getCurrentUser();
		if (currentUser != null && currentUser.equals(mEMMessage.getFrom())) {
			handleTextMessage(vh, mEMMessage);
			vh.relGroupItemLeft.setVisibility(View.GONE);
			vh.relGroupItemRight.setVisibility(View.VISIBLE);
			showTime(vh.mTvGroupTimeRight, position, vh);
			vh.mTvGroupContentRight.setText(desMessage);
			vh.mImgvFailToSend.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					AlertDialog.Builder reSendDialog = new AlertDialog.Builder(mContext);
					reSendDialog.setTitle("重发").setMessage("确认重发该信息？").setNegativeButton("取消", null)
							.setPositiveButton("确定", new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									sendMessageInBackground(vh, mEMMessage);
								}
							});
					reSendDialog.show();
					return false;
				}
			});
			GroupMemberInfo groupMemberInfo = MeetingApp.sMapGroupMemberInfo.get(mGroupId).get(mEMMessage.getFrom());
			File file = new File(Constants.AVATAR_PATH, "" + groupMemberInfo.uid);
			if (file.exists()) {
				vh.mImgvHeadRight.setImageBitmap(ImageHandle.getLoacalBitmap(Constants.AVATAR_PATH + groupMemberInfo.uid));
			}
		} else {
			vh.relGroupItemLeft.setVisibility(View.VISIBLE);
			vh.relGroupItemRight.setVisibility(View.GONE);
			// new String[] { DBHelper.PRO_MOBILE } 查询的对象 DBHelper.PRO_HXID+
			// "=?", new String[] { mEMMessage.getFrom() } 查询的条件，有多个
			GroupMemberInfo info = MeetingApp.sMapGroupMemberInfo.get(mGroupId).get(mEMMessage.getFrom());
			if (info != null) {
				vh.mTvGroupNameLeft.setText(info.mobile);
			} else {
				vh.mTvGroupNameLeft.setText("");
			}
			showTime(vh.mTvGroupTimeLeft, position, vh);
			vh.mTvGroupContentLeft.setText(desMessage);
			if (info != null) {
				mImageLoader.displayImage(NewNetwork.getAvatarUrl(info.uid), vh.mImgvHeadLeft, mOptions);
			} else {
				vh.mImgvHeadLeft.setImageResource(R.drawable.default_head_portrait);
			}
		}

		return con;
	}

	private void sendMessageInBackground(final ViewHolder vh, EMMessage mEMMessage) {
		vh.mProBarSendingRight.setVisibility(View.VISIBLE);
		vh.mImgvFailToSend.setVisibility(View.GONE);
		EMChatManager.getInstance().sendMessage(mEMMessage, new EMCallBack() {

			@Override
			public void onSuccess() {

				mActivity.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						vh.mProBarSendingRight.setVisibility(View.GONE);
						vh.mImgvFailToSend.setVisibility(View.GONE);
						TalkAdapter.this.notifyDataSetChanged();
					}
				});
			}

			@Override
			public void onProgress(int arg0, String arg1) {

			}

			@Override
			public void onError(int arg0, String arg1) {
				mActivity.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						vh.mProBarSendingRight.setVisibility(View.GONE);
						vh.mImgvFailToSend.setVisibility(View.VISIBLE);
						TalkAdapter.this.notifyDataSetChanged();
					}
				});
			}
		});
	}

	private void handleTextMessage(ViewHolder vh, EMMessage mEMMessage) {
		switch (mEMMessage.status) {
		case SUCCESS:
			vh.mProBarSendingRight.setVisibility(View.GONE);
			vh.mImgvFailToSend.setVisibility(View.GONE);
			break;
		case FAIL:
			vh.mProBarSendingRight.setVisibility(View.GONE);
			vh.mImgvFailToSend.setVisibility(View.VISIBLE);
			break;
		case INPROGRESS:
			vh.mProBarSendingRight.setVisibility(View.VISIBLE);
			vh.mImgvFailToSend.setVisibility(View.GONE);
			break;
		default:

			break;
		}
	}

	private void showTime(TextView tv, int position, ViewHolder vh) {
		if (position == 0) {
			tv.setText(DateUtils.getTimestampString(new Date(mDatas.getMessage(position).getMsgTime())));
			tv.setVisibility(View.VISIBLE);
		} else {
			// 两条消息时间离得如果稍长，显示时间
			if (DateUtils.isCloseEnough(mDatas.getMessage(position).getMsgTime(), mDatas.getMessage(position - 1).getMsgTime())) {
				tv.setVisibility(View.INVISIBLE);
			} else {
				tv.setText(DateUtils.getTimestampString(new Date(mDatas.getMessage(position).getMsgTime())));
				tv.setVisibility(View.VISIBLE);
			}
		}
	}

	class ViewHolder {
		TextView mTvGroupTimeRight, mTvGroupContentRight, mTvGroupNameLeft, mTvGroupTimeLeft, mTvGroupContentLeft;
		ImageView mImgvHeadRight, mImgvHeadLeft, mImgvFailToSend;
		RelativeLayout relGroupItemRight, relGroupItemLeft;
		ProgressBar mProBarSendingRight;
	}
}
