package com.deshang365.meeting.adapter;

import java.util.Date;
import java.util.List;

import ru.biovamp.widget.CircleLayout;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.deshang365.meeting.R;
import com.deshang365.meeting.baselib.MeetingApp;
import com.deshang365.meeting.model.GroupMemberInfo;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.util.MeetingUtils;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.easemob.util.DateUtils;

public class TalkGroupAdapter2 extends ImageLoaderAdapterBase {
	private Context mContext;

	public TalkGroupAdapter2(Context context) {
		super(context);
		this.mContext = context;
	}

	// 把bitmap从内存中移除
	public void lruCacheRemoveKey(String key) {

	}

	@Override
	public int getCount() {
		if (MeetingApp.mHxHasLogin) {
			return MeetingApp.getTalkGroupList() == null ? 0 : MeetingApp.getTalkGroupList().size();
		} else {
			return 0;
		}
	}

	@Override
	public GroupMemberInfo getItem(int position) {
		return MeetingApp.getTalkGroupList().get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View con, ViewGroup parent) {
		ViewHolder vh;
		if (con == null) {
			con = View.inflate(mContext, R.layout.group_item, null);
			vh = new ViewHolder();
			vh.mTvGroupName = (TextView) con.findViewById(R.id.txtv_group_name);
			vh.mClOne = (CircleLayout) con.findViewById(R.id.imgv_groupHead_one);
			vh.mClTwo = (CircleLayout) con.findViewById(R.id.imgv_groupHead_two);
			vh.mClFour = (CircleLayout) con.findViewById(R.id.imgv_groupHead_four);
			vh.mClFive = (CircleLayout) con.findViewById(R.id.imgv_groupHead_five);
			vh.mTvUnReadMsg = (TextView) con.findViewById(R.id.txtv_unread_msg_number);
			vh.mTvCreater = (TextView) con.findViewById(R.id.txtv_idcard_mtype);
			vh.mTvTime = (TextView) con.findViewById(R.id.txtv_time);
			con.setTag(vh);
		} else {
			vh = (ViewHolder) con.getTag();
		}
		GroupMemberInfo info = MeetingApp.getTalkGroupList().get(position);
		List<Integer> uids = info.uids;
		if (uids != null) {
			showImage(vh, uids);
		}
		vh.mTvGroupName.setText(info.name);
		EMConversation conversation = EMChatManager.getInstance().getConversation(info.hxgroupid);
		int unreadMsgCount = conversation.getUnreadMsgCount();
		EMMessage message = conversation.getLastMessage();
		if (message != null) {
			vh.mTvTime.setVisibility(View.VISIBLE);
			vh.mTvTime.setText(DateUtils.getTimestampString(new Date(message.getMsgTime())));
			TextMessageBody messageBody = (TextMessageBody) message.getBody();
			String lastMessage = MeetingUtils.decDESMessage(messageBody.getMessage());
			vh.mTvCreater.setText(lastMessage);
		} else {
			vh.mTvTime.setVisibility(View.GONE);
			vh.mTvCreater.setText("");
		}

		// if (info.mtype == 0) {
		// vh.mTvCreater.setText("创建者");
		// } else {
		// vh.mTvCreater.setText("参与者");
		// }

		if (unreadMsgCount != 0) {
			vh.mTvUnReadMsg.setText("" + unreadMsgCount);
			vh.mTvUnReadMsg.setVisibility(View.VISIBLE);
		} else {
			vh.mTvUnReadMsg.setVisibility(View.GONE);
		}
		return con;
	}

	private void showImage(ViewHolder vh, List<Integer> uids) {
		if (uids.size() == 1) {
			vh.mClOne.setVisibility(View.VISIBLE);
			vh.mClTwo.setVisibility(View.GONE);
			vh.mClFour.setVisibility(View.GONE);
			vh.mClFive.setVisibility(View.GONE);
			for (int i = 0; i < uids.size(); i++) {
				ImageView imgv = (ImageView) vh.mClOne.getChildAt(i);
				String uidString = "" + uids.get(i);
				setImageView(uidString, imgv);
			}
		} else if (uids.size() == 2 || uids.size() == 3) {
			vh.mClOne.setVisibility(View.GONE);
			vh.mClTwo.setVisibility(View.VISIBLE);
			vh.mClFour.setVisibility(View.GONE);
			vh.mClFive.setVisibility(View.GONE);
			for (int i = 0; i < uids.size(); i++) {
				if (i >= 2) {
					break;
				}
				ImageView imgv = (ImageView) vh.mClTwo.getChildAt(i);
				String uidString = "" + uids.get(i);
				setImageView(uidString, imgv);
			}
		} else if (uids.size() == 4) {
			vh.mClOne.setVisibility(View.GONE);
			vh.mClTwo.setVisibility(View.GONE);
			vh.mClFour.setVisibility(View.VISIBLE);
			vh.mClFive.setVisibility(View.GONE);
			for (int i = 0; i < uids.size(); i++) {
				ImageView imgv = (ImageView) vh.mClFour.getChildAt(i);
				String uidString = "" + uids.get(i);
				setImageView(uidString, imgv);
			}
		} else if (uids.size() >= 5) {
			vh.mClOne.setVisibility(View.GONE);
			vh.mClTwo.setVisibility(View.GONE);
			vh.mClFour.setVisibility(View.GONE);
			vh.mClFive.setVisibility(View.VISIBLE);
			for (int i = 0; i < uids.size(); i++) {
				if (i >= 5) {
					break;
				}
				ImageView imgv = (ImageView) vh.mClFive.getChildAt(i);
				String uidString = "" + uids.get(i);
				setImageView(uidString, imgv);
			}
		}
	}

	private void setImageView(String uidString, ImageView imgv) {
		mImageLoader.displayImage(NewNetwork.getAvatarUrl(Integer.valueOf(uidString)), imgv, mOptions);
	}

	class ViewHolder {
		TextView mTvCreater, mTvGroupId, mTvGroupName, mTvUnReadMsg, mTvTime;
		View mVUserSign;
		CircleLayout mClOne, mClTwo, mClFour, mClFive;
	}

	// public void clearList() {
	// mUids.clear();
	// }
}
