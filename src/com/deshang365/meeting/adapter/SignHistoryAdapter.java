package com.deshang365.meeting.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.deshang365.meeting.R;
import com.deshang365.meeting.model.GroupMemberInfo;

public class SignHistoryAdapter extends BaseAdapter {
	private Context mContext;
	private List<GroupMemberInfo> mGroupInfoList;

	public SignHistoryAdapter(Context mContext, List<GroupMemberInfo> groupInfoList) {
		super();
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

	@Override
	public View getView(int position, View con, ViewGroup parent) {
		ViewHolder vh;
		if (con == null) {
			con = View.inflate(mContext, R.layout.sign_history_item, null);
			vh = new ViewHolder();
			vh.mTvItem = (TextView) con.findViewById(R.id.txtv_item_per_sig_history);
			vh.mTvTime = (TextView) con.findViewById(R.id.txtv_time_per_sig_history);
			vh.mTvState = (TextView) con.findViewById(R.id.txtv_state_per_sig_history);
			vh.mImgvNext = (ImageView) con.findViewById(R.id.imgv_next);
			con.setTag(vh);
		} else {
			vh = (ViewHolder) con.getTag();
		}
		vh.mTvItem.setText(position + 1 + "");
		vh.mTvTime.setText(mGroupInfoList.get(position).createtime);
		int state = mGroupInfoList.get(position).state;
		if (state == -1) {
			vh.mTvState.setVisibility(View.GONE);
			vh.mImgvNext.setVisibility(View.VISIBLE);
		} else {
			vh.mTvState.setVisibility(View.VISIBLE);
			vh.mImgvNext.setVisibility(View.GONE);
			if (state == 0) {
				vh.mTvState.setText("正常");
			} else if (state == 1) {
				vh.mTvState.setText("请假");
			} else if (state == 2) {
				vh.mTvState.setText("缺席");
			} else if (state == 3) {
				vh.mTvState.setText("补签");
			}
		}
		return con;
	}

	class ViewHolder {
		TextView mTvItem, mTvTime, mTvState;
		ImageView mImgvNext;
	}
}
