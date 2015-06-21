package com.deshang365.meeting.adapter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.deshang365.meeting.R;
import com.deshang365.meeting.model.GroupMemberInfo;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.view.CircularImageView;

public class ExlSignListAdapter2 extends ExlImageLoaderAdapterBase {
	private static final String Integer = null;
	private Context mContext;
	private List<List<GroupMemberInfo>> mArrayGroupInfoList;
	private String mMeetingId;
	private StringBuilder mChangeState;
	// 保存选中的信息
	private Map<Integer, String> mMapChangeState;
	private int mState;
	private View mViewSigned, mViewUnsigned;
	private boolean mFlagSigned, mFlagUnsigned;
	// checkBox的key
	private final int GROUPPOSITION = R.id.radiobutton_late, CHILDPOSITION = R.id.radiobutton_leave;
	// 一个放LinearLayout,一个放CheckedBox位置
	private final int LINEARLAYOUT = R.id.radiobutton_signed_supplement, CHECKPOSITION = R.id.txtv_nickname_sign_result_unsigned;
	// 对选中的位置进行标记
	private Map mMapTab;
	private boolean isCheckedFlag;

	public ExlSignListAdapter2(Context mContext) {
		super(mContext);
		this.mContext = mContext;
		mViewSigned = View.inflate(mContext, R.layout.sign_result_signed_item, null);
		mViewUnsigned = View.inflate(mContext, R.layout.sign_result_unsigned_item, null);
		mChangeState = new StringBuilder();
		mMapChangeState = new HashMap<Integer, String>();
		mMapTab = new HashMap<Integer, Integer>();
	}

	@Override
	public int getGroupCount() {
		// size=3但只需要前两个
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
		GroupMemberInfo groupMemberInfo = mArrayGroupInfoList.get(mIndex).get(childPosition);
		// 0 代表已完成签到 1代表未完成签到
		if (mIndex == 0) {
			ChildViewHolder cvh;
			if (con == null) {
				con = View.inflate(mContext, R.layout.sign_result_signed_item, null);
				if (mFlagSigned == false) {
					// 用来判断是否是相同的View
					mViewSigned = con;
					mFlagSigned = true;
				}
				cvh = new ChildViewHolder();
				setView1(con, cvh);
			} else if (con.equals(mViewSigned)) {
				cvh = (ChildViewHolder) con.getTag();
			} else {
				con = View.inflate(mContext, R.layout.sign_result_signed_item, null);
				cvh = new ChildViewHolder();
				setView1(con, cvh);
			}
			cvh.mTvCreatetime.setText(groupMemberInfo.createtime);
			cvh.mTvShowname.setText(groupMemberInfo.showname);
			int state = groupMemberInfo.state;// 用户签到状态 0正常 1请假 2缺席 3补签4迟到
			if (state == 0) {
				cvh.mImgvSignState.setImageResource(R.drawable.signed);
			} else if (state == 1) {
				cvh.mImgvSignState.setImageResource(R.drawable.leave);
			} else if (state == 3) {
				cvh.mImgvSignState.setImageResource(R.drawable.sign_supplement);
			} else if (state == 4) {
				cvh.mImgvSignState.setImageResource(R.drawable.late);
			}
			setImageView(cvh, groupMemberInfo);
		} else if (mIndex == 1) {
			ChildViewHolder cvh;
			if (con == null) {
				con = View.inflate(mContext, R.layout.sign_result_unsigned_item, null);
				if (mFlagUnsigned == false) {
					// 用来判断是否是相同的View
					mViewUnsigned = con;
					mFlagUnsigned = true;
				}
				cvh = new ChildViewHolder();
				setView2(mIndex, childPosition, con, cvh);
			} else if (con.equals(mViewUnsigned)) {
				cvh = (ChildViewHolder) con.getTag();
			} else {
				con = View.inflate(mContext, R.layout.sign_result_unsigned_item, null);
				cvh = new ChildViewHolder();
				setView2(mIndex, childPosition, con, cvh);
			}
			Integer position = (Integer) mMapTab.get(childPosition);
			if (position != null) {
				CheckBox checkBox = (CheckBox) cvh.mLlCHecked.getChildAt(position);
				if (checkBox != null) {
					checkBox.setChecked(true);
				}
			}
			cvh.mTvShowname.setText(groupMemberInfo.showname);
			setImageView(cvh, groupMemberInfo);
		}
		return con;
	}

	private void setView2(int groupPosition, int childPosition, View con, ChildViewHolder cvh) {
		cvh.mTvShowname = (TextView) con.findViewById(R.id.txtv_nickname_sign_result_unsigned);
		cvh.mImgvHead = (CircularImageView) con.findViewById(R.id.imgv_sign_result_unsigned);
		cvh.mLlCHecked = (LinearLayout) con.findViewById(R.id.radiogroup_change_sign_result);
		for (int i = 0; i < cvh.mLlCHecked.getChildCount(); i++) {
			CheckBox checkBox = (CheckBox) cvh.mLlCHecked.getChildAt(i);
			checkBox.setTag(LINEARLAYOUT, cvh.mLlCHecked);
			checkBox.setTag(CHECKPOSITION, i);
			checkBox.setTag(CHILDPOSITION, childPosition);
			checkBox.setTag(GROUPPOSITION, groupPosition);
			checkBox.setOnCheckedChangeListener(mCheckedChangeListener);
		}
		// cvh.mLlCHecked.setTag(GROUPPOSITION, groupPosition);
		// cvh.mLlCHecked.setTag(CHILDPOSITION, childPosition);
		// cvh.mRadioGroup.setOnCheckedChangeListener(mCheckedChangeListener);

		con.setTag(cvh);
	}

	private OnCheckedChangeListener mCheckedChangeListener = new OnCheckedChangeListener() {
		// tag用来标记上一个的选中状态
		private int tag = -1;
		// 判断是不是同一个item
		private int mPosition = -1;

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			// 标记checked被选中的位置
			int index = -1;
			CheckBox checkBox = (CheckBox) buttonView;
			int checkedId = checkBox.getId();
			LinearLayout ll = (LinearLayout) checkBox.getTag(LINEARLAYOUT);
			int childPosition = (Integer) checkBox.getTag(CHILDPOSITION);
			int groupPosition = (Integer) checkBox.getTag(GROUPPOSITION);
			GroupMemberInfo groupMemberInfo = mArrayGroupInfoList.get(groupPosition).get(childPosition);
			// tag如果不在同一个item则为-1，避免不同的item相互影响
			if (childPosition != mPosition) {
				tag = -1;
				mPosition = childPosition;
				// 将所有未点击的状态设置为false，因为点击别的item再回来点击原来的item时，找不到tag
				for (int i = 0; i < ll.getChildCount(); i++) {
					CheckBox checkBox1 = (CheckBox) ll.getChildAt(i);
					if (!checkBox.equals(checkBox1)) {
						checkBox1.setChecked(false);
					}
				}

			}
			if (isChecked) {
				if (tag != -1) {
					CheckBox checkBoxBefore = (CheckBox) ll.getChildAt(tag);
					if (!checkBoxBefore.equals(checkBox)) {
						checkBoxBefore.setChecked(false);
					}
				}
				tag = (Integer) checkBox.getTag(CHECKPOSITION);
				String data = null;
				if (checkedId == R.id.radiobutton_late) {
					mState = 4;// 迟到
					index = 0;
				} else if (checkedId == R.id.radiobutton_leave) {
					mState = 1;// 请假
					index = 1;
				} else if (checkedId == R.id.radiobutton_signed_supplement) {
					mState = 3;// 补签
					index = 2;
				}
				mMapTab.put(childPosition, index);
				data = formData(groupMemberInfo);
				if (mMapChangeState.containsKey(childPosition)) {
					mMapChangeState.put(childPosition, data);
				} else {
					mMapChangeState.put(childPosition, data);
				}
			} else {
				boolean hasSelected = false;
				for (int i = 0; i < ll.getChildCount(); i++) {
					CheckBox checkBox1 = (CheckBox) ll.getChildAt(i);
					if (checkBox1.isChecked()) {
						hasSelected = true;
						break;
					}
				}
				// 如果一个item中三个checked都没别选中则将这个item数据清除
				if (!hasSelected) {
					mMapChangeState.remove(childPosition);
					mMapTab.remove(childPosition);
				}
			}
			// 是否显示确认修改按钮
			if (mOnCheckedListener != null) {
				mOnCheckedListener.onChecked(mMapChangeState.size());
			}

		}
	};

	private void setView1(View con, ChildViewHolder cvh) {
		cvh.mTvCreatetime = (TextView) con.findViewById(R.id.txtv_createtime_sign_result_signed);
		cvh.mTvShowname = (TextView) con.findViewById(R.id.txtv_nickname_sign_result_signed);
		cvh.mImgvHead = (CircularImageView) con.findViewById(R.id.imgv_sign_result_signed);
		cvh.mImgvSignState = (ImageView) con.findViewById(R.id.imgv_group_sign_state);
		con.setTag(cvh);
	}

	public void clearMapTab() {
		if (mMapTab != null) {
			mMapTab.clear();
		}
	}

	/**
	 * 1表示未完成签到0表示已完成签到
	 * */
	private int mIndex = 1;

	public void setShow(int index) {
		mIndex = index;
		notifyDataSetChanged();
	}

	private void setImageView(ChildViewHolder cvh, GroupMemberInfo groupMemberInfo) {
		mImageLoader.displayImage(NewNetwork.getAvatarUrl(groupMemberInfo.uid), cvh.mImgvHead, mOptions);
	}

	private String formData(GroupMemberInfo groupMemberInfo) {
		String data;
		data = String.format("{\"meeting_id\":\"%s\",\"user_id\":\"%d\",\"state\":\"%d\"}", mMeetingId, groupMemberInfo.uid, mState);
		return data;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

	public interface OnCheckedListener {
		public void onChecked(int mapSize);
	}

	private OnCheckedListener mOnCheckedListener;

	/** 创建一个接口回调 */
	public void setOnCheckedListener(OnCheckedListener onCheckedListener) {
		mOnCheckedListener = onCheckedListener;
	};

	public void setGroupMemberInfoList(List<List<GroupMemberInfo>> arrayGroupInfoList, String meetingid) {
		mArrayGroupInfoList = arrayGroupInfoList;
		mMeetingId = meetingid;
		mMapChangeState.clear();
	}

	public StringBuilder getparams() {
		mChangeState.delete(0, mChangeState.length());
		Set<Integer> keySet = mMapChangeState.keySet();
		Iterator<Integer> iterator = keySet.iterator();
		while (iterator.hasNext()) {
			Integer key = iterator.next();
			mChangeState.append(mMapChangeState.get(key) + ",");
		}
		if (mChangeState.length() > 0) {
			mChangeState.deleteCharAt(mChangeState.length() - 1);
		}
		return mChangeState;
	}

	class GroupViewHolder {
		TextView mTvSignCount;
		ImageView mImgvUpDown;
		RelativeLayout mRelGroup;
	}

	class ChildViewHolder {
		TextView mTvCreatetime, mTvShowname;
		CircularImageView mImgvHead;
		ImageView mImgvSignState;
		LinearLayout mLlCHecked;

	}
}
