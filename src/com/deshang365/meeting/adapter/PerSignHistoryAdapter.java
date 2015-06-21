package com.deshang365.meeting.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.deshang365.meeting.R;
import com.deshang365.meeting.model.GroupMemberInfo;

public class PerSignHistoryAdapter extends BaseAdapter {
	private Context mContext;
	private List<GroupMemberInfo> mGroupInfoList;

	public PerSignHistoryAdapter(Context mContext, List<GroupMemberInfo> groupInfoList) {
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
			con = View.inflate(mContext, R.layout.per_sign_history_item, null);
			vh = new ViewHolder();
			vh.mTvItem = (TextView) con.findViewById(R.id.txtv_item_per_sig_history);
			vh.mTvTime = (TextView) con.findViewById(R.id.txtv_time_per_sig_history);
			vh.mTvState = (TextView) con.findViewById(R.id.txtv_state_per_sig_history);
			con.setTag(vh);
		} else {
			vh = (ViewHolder) con.getTag();
		}
		vh.mTvItem.setText(position + 1 + "");
		vh.mTvTime.setText(mGroupInfoList.get(position).createtime);
		int state = mGroupInfoList.get(position).state;
		if (state == -1) {
			vh.mTvState.setVisibility(View.GONE);
		} else {
			vh.mTvState.setVisibility(View.VISIBLE);
			if (state == 0) {
				vh.mTvState.setText("正常");
			} else if (state == 1) {
				vh.mTvState.setText("请假");
			} else if (state == 2) {
				vh.mTvState.setText("缺席");
			}
		}

		return con;
	}

	class ViewHolder {
		TextView mTvItem, mTvTime, mTvState;
	}
}
