package com.deshang365.meeting.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.deshang365.meeting.R;
import com.deshang365.meeting.model.GroupMemberInfo;

public class ExlSignListAdapter extends BaseExpandableListAdapter {
	private Context mContext;
	private List<List<GroupMemberInfo>> mArrayGroupInfoList;
	/**
	 * 1表示未完成签到0表示已完成签到
	 * */
	private int mIndex = 1;

	public ExlSignListAdapter(Context mContext, List<List<GroupMemberInfo>> arrayGroupInfoList) {
		super();
		this.mContext = mContext;
		this.mArrayGroupInfoList = arrayGroupInfoList;
	}

	@Override
	public int getGroupCount() {
		// return mArrayGroupInfoList == null ? 0 : 2;
		return mArrayGroupInfoList == null ? 0 : 1;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		if (mIndex == 0) {
			return mArrayGroupInfoList.get(mIndex).size();
		} else {
			return mArrayGroupInfoList.get(mIndex).size();
		}

	}

	@Override
	public Object getGroup(int groupPosition) {
		return mArrayGroupInfoList.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		if (mIndex == 0) {
			return mArrayGroupInfoList.get(mIndex).get(childPosition);
		} else {
			return mArrayGroupInfoList.get(mIndex).get(childPosition);
		}
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View con, ViewGroup parent) {
		GroupViewHolder gvh;
		if (con == null) {
			con = View.inflate(mContext, R.layout.group_un_signed_group_item, null);
			gvh = new GroupViewHolder();
			// gvh.mTvSignCount = (TextView)
			// con.findViewById(R.id.txtv_group_showname);
			// gvh.mImgvUpDown = (ImageView)
			// con.findViewById(R.id.imgv_up_down);
			gvh.mRelGroup = (RelativeLayout) con.findViewById(R.id.rel_group);
			con.setTag(gvh);
		} else {
			gvh = (GroupViewHolder) con.getTag();
		}
		gvh.mRelGroup.setVisibility(View.GONE);
		// if (isExpanded) {
		// gvh.mImgvUpDown.setImageResource(R.drawable.up);
		// } else {
		// gvh.mImgvUpDown.setImageResource(R.drawable.down);
		// }
		// if (groupPosition == 0) {
		//
		// gvh.mRelGroup.setBackgroundResource(R.color.new_blue);
		// gvh.mTvSignCount.setText(mArrayGroupInfoList.get(groupPosition).size()
		// + "人已完成签到");
		// } else if (groupPosition == 1) {
		// gvh.mRelGroup.setBackgroundResource(R.color.new_orange_yellow);
		// gvh.mTvSignCount.setText(mArrayGroupInfoList.get(groupPosition).size()
		// + "人未完成签到");
		// }
		return con;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View con, ViewGroup parent) {
		ChildViewHolder cvh;
		if (con == null) {
			con = View.inflate(mContext, R.layout.signed_item, null);
			cvh = new ChildViewHolder();
			cvh.mTvCreatetime = (TextView) con.findViewById(R.id.txtv_member_time);
			cvh.mTvShowname = (TextView) con.findViewById(R.id.txtv_member_name);
			cvh.mImgvState = (ImageView) con.findViewById(R.id.imgv_group_sign_state);
			con.setTag(cvh);
		} else {
			cvh = (ChildViewHolder) con.getTag();
		}
		GroupMemberInfo groupMemberInfo = mArrayGroupInfoList.get(mIndex).get(childPosition);
		if (groupMemberInfo == null) {
			return con;
		}
		if (mIndex == 0) {
			cvh.mImgvState.setVisibility(View.VISIBLE);
			int state = groupMemberInfo.state;// 用户签到状态 0正常 1请假 2缺席 3补签4迟到
			if (state == 0) {
				cvh.mImgvState.setImageResource(R.drawable.signed);
			} else if (state == 1) {
				cvh.mImgvState.setImageResource(R.drawable.leave);
			} else if (state == 3) {
				cvh.mImgvState.setImageResource(R.drawable.sign_supplement);
			} else if (state == 4) {
				cvh.mImgvState.setImageResource(R.drawable.late);
			}
		} else {
			cvh.mImgvState.setVisibility(View.GONE);
		}
		cvh.mTvCreatetime.setText(groupMemberInfo.createtime);
		cvh.mTvShowname.setText(groupMemberInfo.showname);
		return con;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {

		return false;
	}

	public void setShow(int index) {
		mIndex = index;
		notifyDataSetChanged();
	}

	class GroupViewHolder {
		TextView mTvSignCount;
		ImageView mImgvUpDown;
		RelativeLayout mRelGroup;
	}

	class ChildViewHolder {
		TextView mTvCreatetime, mTvShowname;
		ImageView mImgvState;
	}
}
