package com.deshang365.meeting.adapter;

import java.util.List;

import ru.biovamp.widget.CircleLayout;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.deshang365.meeting.R;
import com.deshang365.meeting.baselib.MeetingApp;
import com.deshang365.meeting.model.GroupMemberInfo;
import com.deshang365.meeting.network.NewNetwork;

public class SignGroupAdapter extends ImageLoaderAdapterBase {
	private Context mContext;
	// 保证第一次都是去网络上加载图片
	// private List<Integer> mUids = new ArrayList<Integer>();
	private LruCache<String, Bitmap> mImageCache;

	public SignGroupAdapter(Context context) {
		super(context);
		this.mContext = context;
	}

	@Override
	public int getCount() {
		return MeetingApp.getGroupList() == null ? 0 : MeetingApp.getGroupList().size();
	}

	@Override
	public GroupMemberInfo getItem(int position) {
		return MeetingApp.getGroupList().get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View con, ViewGroup parent) {
		ViewHolder vh;
		if (con == null) {
			con = View.inflate(mContext, R.layout.sign_group_item, null);
			vh = new ViewHolder();
			vh.mImgvCreaterorJoiner = (ImageView) con.findViewById(R.id.imgv_group_creater);
			vh.mImgvSigning = (ImageView) con.findViewById(R.id.imgv_group_signing);
			vh.mTvGroupName = (TextView) con.findViewById(R.id.txtv_group_name);
			vh.mTvGroupId = (TextView) con.findViewById(R.id.txtv_group_code);
			vh.mRelSIgnGroupItem = (RelativeLayout) con.findViewById(R.id.rel_sign_group_item);
			vh.mClOne = (CircleLayout) con.findViewById(R.id.imgv_groupHead_one);
			vh.mClTwo = (CircleLayout) con.findViewById(R.id.imgv_groupHead_two);
			vh.mClFour = (CircleLayout) con.findViewById(R.id.imgv_groupHead_four);
			vh.mClFive = (CircleLayout) con.findViewById(R.id.imgv_groupHead_five);
			con.setTag(vh);
		} else {
			vh = (ViewHolder) con.getTag();
		}

		GroupMemberInfo info = MeetingApp.getGroupList().get(position);
		List<Integer> uids = info.uids;
		if (uids != null) {
			showImage(vh, uids);
		}

		vh.mTvGroupName.setText(info.name);
		vh.mTvGroupId.setText(info.idcard);
		if (info.mtype == 0) {
			vh.mImgvCreaterorJoiner.setImageResource(R.drawable.creater);
		} else {
			vh.mImgvCreaterorJoiner.setImageResource(R.drawable.joiner);
		}
		if ("0".equals(info.signState)) {
			vh.mImgvSigning.setVisibility(View.VISIBLE);
			if (info.has_sign == 1) {
				vh.mImgvSigning.setImageResource(R.drawable.meeting_signed);
			} else {
				vh.mImgvSigning.setImageResource(R.drawable.signing);
			}
		} else {
			vh.mImgvSigning.setVisibility(View.GONE);
		}
		return con;
	}

	private void showImage(ViewHolder vh, List<Integer> uids) {
		if (uids.size() == 1) {
			vh.mClOne.setVisibility(View.VISIBLE);
			vh.mClTwo.setVisibility(View.GONE);
			vh.mClFour.setVisibility(View.GONE);
			vh.mClFive.setVisibility(View.GONE);
			for (int i = 0; i < uids.size(); i++) {
				ImageView imgv = (ImageView) vh.mClOne.getChildAt(i);
				String uidString = "" + uids.get(i);
				setImageView(uidString, imgv);
			}
		} else if (uids.size() == 2 || uids.size() == 3) {
			vh.mClOne.setVisibility(View.GONE);
			vh.mClTwo.setVisibility(View.VISIBLE);
			vh.mClFour.setVisibility(View.GONE);
			vh.mClFive.setVisibility(View.GONE);
			for (int i = 0; i < uids.size(); i++) {
				if (i >= 2) {
					break;
				}
				ImageView imgv = (ImageView) vh.mClTwo.getChildAt(i);
				String uidString = "" + uids.get(i);
				setImageView(uidString, imgv);
			}
		} else if (uids.size() == 4) {
			vh.mClOne.setVisibility(View.GONE);
			vh.mClTwo.setVisibility(View.GONE);
			vh.mClFour.setVisibility(View.VISIBLE);
			vh.mClFive.setVisibility(View.GONE);
			for (int i = 0; i < uids.size(); i++) {
				ImageView imgv = (ImageView) vh.mClFour.getChildAt(i);
				String uidString = "" + uids.get(i);
				setImageView(uidString, imgv);
			}
		} else if (uids.size() >= 5) {
			vh.mClOne.setVisibility(View.GONE);
			vh.mClTwo.setVisibility(View.GONE);
			vh.mClFour.setVisibility(View.GONE);
			vh.mClFive.setVisibility(View.VISIBLE);
			for (int i = 0; i < uids.size(); i++) {
				if (i >= 5) {
					break;
				}
				ImageView imgv = (ImageView) vh.mClFive.getChildAt(i);
				String uidString = "" + uids.get(i);
				setImageView(uidString, imgv);
			}
		}
	}

	private void setImageView(String uidString, ImageView imgv) {
		mImageLoader.displayImage(NewNetwork.getAvatarUrl(Integer.valueOf(uidString)), imgv, mOptions);
	}

	class ViewHolder {
		TextView mTvGroupId, mTvGroupName;
		// View mVUserSign;
		CircleLayout mClOne, mClTwo, mClFour, mClFive;
		ImageView mImgvCreaterorJoiner, mImgvSigning;
		RelativeLayout mRelSIgnGroupItem;
	}

}
