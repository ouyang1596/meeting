package com.deshang365.meeting.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.deshang365.meeting.R;
import com.deshang365.meeting.model.GroupMemberInfo;
import com.deshang365.meeting.view.AbsentDetailListView;

public class AllUnSignedAdapter extends BaseAdapter {
	private Context mContext;
	private List<GroupMemberInfo> mArrayGroupInfoList;
	private Map<Integer, Integer> mIsExpanded;// 1展开0关闭
	private Map<Integer, BaseAdapter> mAdapterTabs;

	public AllUnSignedAdapter(Context mContext, List<GroupMemberInfo> mArrayGroupInfoList) {
		super();
		this.mContext = mContext;
		this.mArrayGroupInfoList = mArrayGroupInfoList;
		mIsExpanded = new HashMap<Integer, Integer>();
		mAdapterTabs = new HashMap<Integer, BaseAdapter>();
	}

	@Override
	public int getCount() {
		return mArrayGroupInfoList == null ? 0 : mArrayGroupInfoList.size();
	}

	@Override
	public GroupMemberInfo getItem(int position) {
		return mArrayGroupInfoList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View con, ViewGroup parent) {
		Log.i("bm", "==" + position);
		// 初始化，默认item是不展开的
		if (mIsExpanded.get(position) == null) {
			mIsExpanded.put(position, 0);
		}
		final VH vh;
		if (con == null) {
			con = View.inflate(mContext, R.layout.group_un_signed_group_item, null);
			vh = new VH();
			vh.mTvShowname = (TextView) con.findViewById(R.id.txtv_group_showname);
			vh.mRelGroup = (RelativeLayout) con.findViewById(R.id.rel_group);
			vh.mTvUnSIgnedCount = (TextView) con.findViewById(R.id.txtv_group_un_signed_count);
			vh.mLvAbsentDetail = (AbsentDetailListView) con.findViewById(R.id.child_item);
			vh.mImgvUpOrDown = (ImageView) con.findViewById(R.id.imgv_up_down);
			vh.mTvUnSIgnedCount.setVisibility(View.VISIBLE);
			con.setTag(vh);
		} else {
			vh = (VH) con.getTag();
		}
		// 将adapter标记为属于哪一个item的listView从而避免错位
		AbsentDetailAdapter absentDetailAdapter = (AbsentDetailAdapter) mAdapterTabs.get(position);
		if (absentDetailAdapter != null) {
			vh.mLvAbsentDetail.setAdapter(absentDetailAdapter);
		} else {
			vh.mLvAbsentDetail.setAdapter(null);
		}
		GroupMemberInfo groupMemberInfo = mArrayGroupInfoList.get(position);
		vh.mTvUnSIgnedCount.setText("累计缺席" + groupMemberInfo.absent_count + "次");
		vh.mTvShowname.setText(groupMemberInfo.showname);
		if (mIsExpanded.get(position) == 1) {
			vh.mImgvUpOrDown.setImageResource(R.drawable.up);
			vh.mLvAbsentDetail.setVisibility(View.VISIBLE);
		} else {
			vh.mImgvUpOrDown.setImageResource(R.drawable.down);
			vh.mLvAbsentDetail.setVisibility(View.GONE);
		}
		return con;
	}

	/**
	 * 给是否展开做一个标记
	 * */
	public void set(int key, int value) {
		mIsExpanded.put(key, value);
	}

	/**
	 * 获得是否展开的标记
	 * */
	public Integer get(int key) {
		return mIsExpanded.get(key);
	}

	/**
	 * 清除所有是否展开的标记
	 * */
	public void clear() {
		mIsExpanded.clear();
	}

	public void setAdpater(int key, BaseAdapter value) {
		mAdapterTabs.put(key, value);
	}

	public class VH {
		TextView mTvShowname, mTvUnSIgnedCount;
		RelativeLayout mRelGroup;
		ImageView mImgvUpOrDown;
		public AbsentDetailListView mLvAbsentDetail;
	}
}
