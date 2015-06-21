package com.deshang365.meeting.adapter;

import java.util.List;

import retrofit.RetrofitError;
import retrofit.client.Response;

import android.R.integer;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deshang365.meeting.R;
import com.deshang365.meeting.model.GroupMemberInfo;
import com.deshang365.meeting.model.Network;
import com.deshang365.meeting.model.NetworkReturnBase;
import com.deshang365.meeting.network.NetworkReturn;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.network.OnResponse;
import com.deshang365.meeting.view.CircularImageView;
import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;
import com.tencent.stat.StatAppMonitor;
import com.tencent.stat.StatService;

public class GridGroupsAdapter extends ImageLoaderAdapterBase {
	private Context mContext;
	private List<GroupMemberInfo> mGroups;
	private boolean mIsDelete;
	private int mType;
	private boolean isFirst = true;

	public GridGroupsAdapter(Context context, List<GroupMemberInfo> groups, int mtype) {
		super(context);
		this.mContext = context;
		this.mGroups = groups;
		this.mType = mtype;
		if (mtype == 0) {
			GroupMemberInfo groupInfo = new GroupMemberInfo();
			groupInfo.endFlag = true;
			groups.add(groupInfo);
		}
	}

	@Override
	public int getCount() {
		return mGroups == null ? 0 : mGroups.size();
	}

	@Override
	public GroupMemberInfo getItem(int position) {
		return mGroups.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View con, ViewGroup parent) {
		ViewHolder vh;
		if (con == null) {
			con = View.inflate(mContext, R.layout.group_member_item, null);
			vh = new ViewHolder();
			vh.mRelGroupMember = (RelativeLayout) con.findViewById(R.id.rel_groupMemember);
			vh.mRelGroupMemberEnd = (RelativeLayout) con.findViewById(R.id.rel_groupMemember_end);
			vh.mTvShowname = (TextView) con.findViewById(R.id.txtv_groupMember);
			vh.mImgvHead = (CircularImageView) con.findViewById(R.id.imgv_memberHead);
			vh.mImgvDelete = (CircularImageView) con.findViewById(R.id.imgv_memberHead_end);
			vh.mImgvDeleteMember = (ImageView) con.findViewById(R.id.imgv_delete);
			con.setTag(vh);
		} else {
			vh = (ViewHolder) con.getTag();
		}
		final GroupMemberInfo memberInfo = mGroups.get(position);
		if (memberInfo.endFlag == false) {
			vh.mRelGroupMember.setVisibility(View.VISIBLE);
			vh.mRelGroupMemberEnd.setVisibility(View.GONE);
			vh.mTvShowname.setText(memberInfo.showname);
			setImageView(memberInfo.uid, vh);
			if (memberInfo.mtype == 1) {
				if (mIsDelete == true) {
					vh.mImgvDeleteMember.setVisibility(View.VISIBLE);
					vh.mImgvDeleteMember.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							StatService.trackCustomEvent(mContext, "DeleteMember", "OK");
							new HxDeleteMemberAsyn(position, memberInfo.group_id, "" + memberInfo.uid, memberInfo.hxgroupid,
									memberInfo.hxid).execute();
						}
					});
				} else {
					vh.mImgvDeleteMember.setVisibility(View.GONE);
				}
			} else {
				vh.mImgvDeleteMember.setVisibility(View.GONE);
			}

		} else {
			vh.mRelGroupMember.setVisibility(View.GONE);
			vh.mRelGroupMemberEnd.setVisibility(View.VISIBLE);
			vh.mRelGroupMemberEnd.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mIsDelete == false) {
						mIsDelete = true;
					} else {
						mIsDelete = false;
					}
					isFirst = false;
					notifyDataSetChanged();
				}
			});
		}

		return con;
	}

	// public boolean isDelete() {
	// return mIsDelete;
	// }

	public void setDeleteFlag(boolean isDelete) {
		this.mIsDelete = isDelete;
	}

	private void setImageView(int uid, ViewHolder vh) {
		mImageLoader.getDiskCache().remove(NewNetwork.getAvatarUrl(uid));
		mImageLoader.displayImage(NewNetwork.getAvatarUrl(uid), vh.mImgvHead, mOptions);
	}

	class ViewHolder {
		TextView mTvShowname;
		CircularImageView mImgvHead, mImgvDelete;
		RelativeLayout mRelGroupMember, mRelGroupMemberEnd;
		ImageView mImgvDeleteMember;
	}

	class HxDeleteMemberAsyn extends AsyncTask<String, Void, String> {
		private String groupid;
		private String uid;
		private String hxGroupid;
		private String hxid;
		private int position;

		public HxDeleteMemberAsyn(int position, String groupid, String uid, String hxGroupid, String hxid) {
			this.groupid = groupid;
			this.uid = uid;
			this.hxGroupid = hxGroupid;
			this.hxid = hxid;
			this.position = position;
		}

		@Override
		protected String doInBackground(String... params) {
			StatAppMonitor monitor = new StatAppMonitor("hxremoveUserFromGroup_Android");
			long startTime = System.currentTimeMillis();
			try {
				EMGroupManager.getInstance().removeUserFromGroup(hxGroupid, hxid);
				long difftime = System.currentTimeMillis() - startTime;
				monitor.setMillisecondsConsume(difftime);
			} catch (EaseMobException e) {
				monitor.setReturnCode(StatAppMonitor.FAILURE_RESULT_TYPE);
				return "fail";
			}
			monitor.setReturnCode(StatAppMonitor.SUCCESS_RESULT_TYPE);
			return "success";
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if ("success".equals(result)) {
				deleteMember(position, groupid, uid);
			} else {
				Toast.makeText(mContext, "删除群成员失败", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void deleteMember(final int position, String groupid, String uid) {
		NewNetwork.deleteMember(groupid, uid, new OnResponse<NetworkReturn>("memberdelete_Android") {
			@Override
			public void success(NetworkReturn result, Response arg1) {
				super.success(result, arg1);
				if (result.result == 1) {
					mGroups.remove(position);
					notifyDataSetChanged();
				} else {
					Toast.makeText(mContext, result.msg, Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void failure(RetrofitError error) {
				super.failure(error);
				Toast.makeText(mContext, "删除群成员失败", Toast.LENGTH_SHORT).show();
			}
		});

	}

	// class DeleteMemberAsyn extends AsyncTask<String, Void, NetworkReturnBase>
	// {
	// private int position;
	//
	// private DeleteMemberAsyn(int position) {
	// this.position = position;
	// }
	//
	// @Override
	// protected NetworkReturnBase doInBackground(String... params) {
	// return Network.deleteMember(params[0], params[1]);
	//
	// }
	//
	// @Override
	// protected void onPostExecute(NetworkReturnBase result) {
	// super.onPostExecute(result);
	// if (result == null) {
	// Toast.makeText(mContext, "删除群成员失败！", Toast.LENGTH_SHORT).show();
	// return;
	// }
	// if (result.rescode == 1) {
	// mGroups.remove(position);
	// notifyDataSetChanged();
	// } else {
	// Toast.makeText(mContext, result.message, Toast.LENGTH_SHORT).show();
	// }
	// }
	// }
}
