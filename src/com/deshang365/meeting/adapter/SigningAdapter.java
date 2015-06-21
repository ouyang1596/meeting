package com.deshang365.meeting.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.deshang365.meeting.R;
import com.deshang365.meeting.model.GroupMemberInfo;

public class SigningAdapter extends BaseAdapter {
	private Context mContext;
	private List<GroupMemberInfo> mGroupInfoList;

	public SigningAdapter(Context context, List<GroupMemberInfo> groupInfoList) {
		super();
		this.mContext = context;
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

	@Override
	public View getView(int position, View con, ViewGroup parent) {
		ViewHolder vh;
		if (con == null) {
			con = View.inflate(mContext, R.layout.signed_item, null);
			vh = new ViewHolder();
			vh.mTvNickname = (TextView) con.findViewById(R.id.txtv_member_name);
			vh.mTvTime = (TextView) con.findViewById(R.id.txtv_member_time);
			con.setTag(vh);
		} else {
			vh = (ViewHolder) con.getTag();
		}
		vh.mTvNickname.setText(mGroupInfoList.get(position).showname);
		if (!"".equals(mGroupInfoList.get(position).createtime)) {
			vh.mTvTime.setText(mGroupInfoList.get(position).createtime);
			vh.mTvTime.setVisibility(View.VISIBLE);
		} else {
			vh.mTvTime.setVisibility(View.GONE);
		}
		return con;
	}

	class ViewHolder {
		TextView mTvNickname, mTvTime;
	}
}
