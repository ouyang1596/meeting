package com.deshang365.meeting.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.deshang365.meeting.R;
import com.deshang365.meeting.model.AbsentDetail.AbsentDetailItem;

public class AbsentDetailAdapter extends BaseAdapter {
	private Context mContext;
	private List<AbsentDetailItem> mAbsentDetail;

	public AbsentDetailAdapter(Context context, List<AbsentDetailItem> absentDetail) {
		mContext = context;
		mAbsentDetail = absentDetail;

	}

	@Override
	public int getCount() {
		return mAbsentDetail == null ? 0 : mAbsentDetail.size();
	}

	@Override
	public AbsentDetailItem getItem(int position) {

		return mAbsentDetail.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View con, ViewGroup parent) {
		ViewHolder vh;
		if (con == null) {
			con = View.inflate(mContext, R.layout.group_un_signed_child_item, null);
			vh = new ViewHolder();
			vh.mTvDate = (TextView) con.findViewById(R.id.item_txtv_date);
			vh.mTvTime = (TextView) con.findViewById(R.id.item_txtv_time);
			vh.mTvState = (TextView) con.findViewById(R.id.item_txtv_state);
			con.setTag(vh);
		} else {
			vh = (ViewHolder) con.getTag();
		}
		AbsentDetailItem absentDetailItem = mAbsentDetail.get(position);
		vh.mTvDate.setText(absentDetailItem.getMeeting_date());
		vh.mTvTime.setText(absentDetailItem.getMeeting_time());
		int state = absentDetailItem.getState();
		if (state == 0) {
			vh.mTvState.setText("正常");
		} else if (state == 1) {
			vh.mTvState.setText("请假");
		} else if (state == 2) {
			vh.mTvState.setText("缺席");
		}
		return con;
	}

	class ViewHolder {
		TextView mTvDate, mTvTime, mTvState;
	}

}
