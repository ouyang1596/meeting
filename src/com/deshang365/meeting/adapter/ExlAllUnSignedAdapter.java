package com.deshang365.meeting.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.deshang365.meeting.R;
import com.deshang365.meeting.model.GroupMemberInfo;
import com.deshang365.meeting.network.NewNetwork;

/**
 * 继承ExlImageLoaderAdapterBase下载图片会比较快
 * */
public class ExlAllUnSignedAdapter extends ExlImageLoaderAdapterBase {
	private Context mContext;
	private List<List<GroupMemberInfo>> mArrayGroupInfoList;

	public ExlAllUnSignedAdapter(Context mContext, List<List<GroupMemberInfo>> arrayGroupInfoList) {
		super(mContext);
		this.mContext = mContext;
		this.mArrayGroupInfoList = arrayGroupInfoList;
	}

	@Override
	public int getGroupCount() {
		return mArrayGroupInfoList == null ? 0 : mArrayGroupInfoList.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		List<GroupMemberInfo> groupInfoList = mArrayGroupInfoList.get(groupPosition);
		// return groupInfoList == null ? 0 : groupInfoList.size();
		return 0;
	}

	@Override
	public List<GroupMemberInfo> getGroup(int groupPosition) {
		return mArrayGroupInfoList.get(groupPosition);
	}

	@Override
	public GroupMemberInfo getChild(int groupPosition, int childPosition) {
		List<GroupMemberInfo> groupInfoList = mArrayGroupInfoList.get(groupPosition);
		GroupMemberInfo groupInfo = groupInfoList.get(childPosition);
		return groupInfo;
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
			gvh.mTvShowname = (TextView) con.findViewById(R.id.txtv_group_showname);
			gvh.mTvUnSIgnedCount = (TextView) con.findViewById(R.id.txtv_group_un_signed_count);
			gvh.mTvUnSIgnedCount.setVisibility(View.VISIBLE);
			gvh.mImgvUpDown = (ImageView) con.findViewById(R.id.imgv_up_down);
			con.setTag(gvh);
		} else {
			gvh = (GroupViewHolder) con.getTag();
		}
		List<GroupMemberInfo> list = mArrayGroupInfoList.get(groupPosition);
		gvh.mTvUnSIgnedCount.setText("累计缺席" + list.size() + "次");
		if (isExpanded) {
			gvh.mImgvUpDown.setImageResource(R.drawable.up);
		} else {
			gvh.mImgvUpDown.setImageResource(R.drawable.down);
		}
		if (!list.isEmpty()) {
			gvh.mTvShowname.setText(list.get(0).showname);
		}
		return con;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View con, ViewGroup parent) {
		ChildViewHolder cvh;
		if (con == null) {
			con = View.inflate(mContext, R.layout.group_un_signed_child_item, null);
			cvh = new ChildViewHolder();
			cvh.mTvCreatetimeData = (TextView) con.findViewById(R.id.txtv_child_createtime_data);
			cvh.mTvCause = (TextView) con.findViewById(R.id.txtv_child_cause);
			cvh.mTvCreatetimeHour = (TextView) con.findViewById(R.id.txtv_child_createtime_hour);
			con.setTag(cvh);
		} else {
			cvh = (ChildViewHolder) con.getTag();
		}
		GroupMemberInfo groupInfo = mArrayGroupInfoList.get(groupPosition).get(childPosition);
		String createtime = groupInfo.createtime;
		String[] split = createtime.split(" ");
		if (split != null && split.length >= 2) {
			String replaceString = split[0].replaceAll("-", "/");
			cvh.mTvCreatetimeData.setText(replaceString);
			cvh.mTvCreatetimeHour.setText(split[1]);
		}
		if (groupInfo.state == 0) {
			cvh.mTvCause.setText("正常");
		} else if (groupInfo.state == 1) {
			cvh.mTvCause.setText("请假");
		} else if (groupInfo.state == 2) {
			cvh.mTvCause.setText("缺席");
		}
		// mImageLoader.displayImage(NewNetwork.getAvatarUrl(uid), image,
		// mOptions);
		return con;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return false;
	}

	class GroupViewHolder {
		TextView mTvShowname, mTvUnSIgnedCount;
		ImageView mImgvUpDown;
	}

	class ChildViewHolder {
		TextView mTvCreatetimeData, mTvCause, mTvCreatetimeHour;

	}
}
