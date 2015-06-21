package com.deshang365.meeting.adapter;

import java.util.List;

import com.deshang365.meeting.R;
import com.deshang365.meeting.model.MeetingRecordsItem;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MyRecUnSignedAdapter extends BaseAdapter {
	private Context mContext;
	private List<MeetingRecordsItem> datas;

	public MyRecUnSignedAdapter(Context mContext, List<MeetingRecordsItem> datas) {
		this.mContext = mContext;
		this.datas = datas;
	}

	@Override
	public int getCount() {
		return datas == null ? 0 : datas.size();
	}

	@Override
	public Object getItem(int position) {
		return datas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View con, ViewGroup parent) {
		ViewHolder vh;
		if (con == null) {
			con = View.inflate(mContext, R.layout.my_sign_record_item, null);
			vh = new ViewHolder();
			vh.mTvGroupName = (TextView) con.findViewById(R.id.txtv_group_name);
			vh.mTvCreateTime = (TextView) con.findViewById(R.id.txtv_create_time);
			con.setTag(vh);
		} else {
			vh = (ViewHolder) con.getTag();
		}
		MeetingRecordsItem meetingRecordsItem = datas.get(position);
		vh.mTvGroupName.setText(meetingRecordsItem.getName());
		vh.mTvCreateTime.setText(meetingRecordsItem.getCreatetime());
		return con;
	}

	class ViewHolder {
		TextView mTvGroupName, mTvCreateTime;
	}
}
