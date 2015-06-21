package com.deshang365.meeting.adapter;

import java.io.File;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.deshang365.meeting.R;
import com.deshang365.meeting.baselib.ImageHandle;
import com.deshang365.meeting.baselib.MeetingApp;
import com.deshang365.meeting.model.Constants;
import com.deshang365.meeting.model.GroupMemberInfo;
import com.deshang365.meeting.model.Network;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.util.MeetingUtils;

public class UnSignedAdapter extends ImageLoaderAdapterBase {
	private Context mContext;
	private List<GroupMemberInfo> mGroupInfoList;

	public UnSignedAdapter(Context mContext, List<GroupMemberInfo> groupInfoList) {
		super(mContext);
		this.mContext = mContext;
		this.mGroupInfoList = groupInfoList;
	}

	@Override
	public int getCount() {
		return mGroupInfoList == null ? 0 : mGroupInfoList.size();
	}

	@Override
	public GroupMemberInfo getItem(int position) {
		return mGroupInfoList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("NewApi")
	@Override
	public View getView(int position, View con, ViewGroup parent) {
		ViewHolder vh;
		if (con == null) {
			con = View.inflate(mContext, R.layout.group_member_item, null);
			vh = new ViewHolder();
			vh.mTvName = (TextView) con.findViewById(R.id.txtv_groupMember);
			vh.mImgvHead = (ImageView) con.findViewById(R.id.imgv_memberHead);
			vh.mTvState = (TextView) con.findViewById(R.id.txtv_state);
			con.setTag(vh);
		} else {
			vh = (ViewHolder) con.getTag();
		}
		GroupMemberInfo groupInfo = mGroupInfoList.get(position);
		vh.mTvName.setText(groupInfo.showname);
		vh.mTvState.setVisibility(View.VISIBLE);
		if (groupInfo.state == 1) {
			vh.mTvState.setText("请假");
		} else if (groupInfo.state == 2) {
			vh.mTvState.setText("缺席");
		} else {
			vh.mTvState.setVisibility(View.GONE);
		}
		mImageLoader.displayImage(NewNetwork.getAvatarUrl(groupInfo.uid), vh.mImgvHead, mOptions);
		return con;
	}

	class ViewHolder {
		TextView mTvName, mTvState;
		ImageView mImgvHead;
	}

}
