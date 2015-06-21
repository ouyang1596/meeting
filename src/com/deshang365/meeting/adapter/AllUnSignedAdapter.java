package com.deshang365.meeting.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.deshang365.meeting.R;
import com.deshang365.meeting.model.GroupMemberInfo;

public class AllUnSignedAdapter extends BaseAdapter {
	private Context mContext;
	private List<GroupMemberInfo> mArrayGroupInfoList;

	public AllUnSignedAdapter(Context mContext, List<GroupMemberInfo> mArrayGroupInfoList) {
		super();
		this.mContext = mContext;
		this.mArrayGroupInfoList = mArrayGroupInfoList;
	}

	@Override
	public int getCount() {
		return mArrayGroupInfoList == null ? 0 : mArrayGroupInfoList.size();
	}

	@Override
	public Object getItem(int position) {
		return mArrayGroupInfoList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View con, ViewGroup parent) {
		ViewHolder vh;
		if (con == null) {
			con = View.inflate(mContext, R.layout.group_un_signed_group_item, null);
			vh = new ViewHolder();
			vh.mTvShowname = (TextView) con.findViewById(R.id.txtv_group_showname);
			vh.mTvUnSIgnedCount = (TextView) con.findViewById(R.id.txtv_group_un_signed_count);
			vh.mTvUnSIgnedCount.setVisibility(View.VISIBLE);
			con.setTag(vh);
		} else {
			vh = (ViewHolder) con.getTag();
		}
		GroupMemberInfo groupMemberInfo = mArrayGroupInfoList.get(position);
		vh.mTvUnSIgnedCount.setText("累计缺席" + groupMemberInfo.absent_count + "次");
		vh.mTvShowname.setText(groupMemberInfo.showname);
		return con;
	}

	class ViewHolder {
		TextView mTvShowname, mTvUnSIgnedCount;
	}
}
