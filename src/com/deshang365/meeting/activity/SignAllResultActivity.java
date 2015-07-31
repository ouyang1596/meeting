package com.deshang365.meeting.activity;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonNode;

import retrofit.RetrofitError;
import retrofit.client.Response;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.deshang365.meeting.R;
import com.deshang365.meeting.adapter.AbsentDetailAdapter;
import com.deshang365.meeting.adapter.AllUnSignedAdapter;
import com.deshang365.meeting.adapter.AllUnSignedAdapter.VH;
import com.deshang365.meeting.baselib.MeetingApp;
import com.deshang365.meeting.model.AbsentDetail;
import com.deshang365.meeting.model.GroupMemberInfo;
import com.deshang365.meeting.model.GroupMemberInfoList;
import com.deshang365.meeting.network.NetworkReturn;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.network.OnResponse;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tencent.stat.StatService;

public class SignAllResultActivity extends BaseActivity {
	private Button mBtnExportResult;
	private PullToRefreshListView mExListUnSigned;
	private LinearLayout mLlBack;
	private RelativeLayout mRelProBar;
	private TextView mTvTopical, mSignCount;
	private AlertDialog mExportDialog;
	private String mGroupid;
	private AllUnSignedAdapter mAdapter;
	private int mPageCount;
	private List<GroupMemberInfo> mListGroupMemberInfos;
	private View mExportView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_all_result);
		initView();
	}

	@SuppressWarnings("unchecked")
	@SuppressLint("NewApi")
	private void initView() {
		mListGroupMemberInfos = new ArrayList<GroupMemberInfo>();
		mGroupid = getIntent().getStringExtra("groupid");
		mSignCount = (TextView) findViewById(R.id.txtv_sign_count);
		mTvTopical = (TextView) findViewById(R.id.tv_top_alert_text);
		mTvTopical.setText("签到结果");
		mLlBack = (LinearLayout) findViewById(R.id.ll_top_alert_back);
		mLlBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mRelProBar = (RelativeLayout) findViewById(R.id.rel_progressbar);
		mBtnExportResult = (Button) findViewById(R.id.btn_export_result);
		mBtnExportResult.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				initDialog();
				showExportDialog();
			}

		});
		mExListUnSigned = (PullToRefreshListView) findViewById(R.id.exlist_un_signed_member);
		mExListUnSigned.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				position = position - 1;
				if (mAdapter == null) {
					return;
				}
				int uid = mAdapter.getItem(position).uid;
				VH vh = (VH) view.getTag();
				AbsentDetailAdapter adapter = (AbsentDetailAdapter) vh.mLvAbsentDetail.getAdapter();
				if (adapter != null) {
					setExpand(position);
					mAdapter.notifyDataSetChanged();
					return;
				}
				showWaitingDialog();
				getAbsentDetail(position, mGroupid, uid, MeetingApp.mVersionCode);
			}
		});
		mExListUnSigned.setOnRefreshListener(new OnRefreshListener2() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase refreshView) {
				if (mAdapter != null) {
					mAdapter.clear();
				}
				initList();
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase refreshView) {
				mPageCount += 1;
				getAbsentResult(mGroupid, MeetingApp.mVersionCode, "" + mPageCount);
			}
		});
		mRelProBar.setVisibility(View.VISIBLE);
		mExListUnSigned.setMode(Mode.DISABLED);
		initList();
	}

	private void getAbsentDetail(final int position, String groupid, int uid, String app_Version) {
		NewNetwork.getAbsentDetail(groupid, uid, app_Version, new OnResponse<NetworkReturn>("check_absent_details_Android") {
			@Override
			public void success(NetworkReturn result, Response arg1) {
				super.success(result, arg1);
				hideWaitingDialog();
				if (result.result != 1) {
					Toast.makeText(getApplication(), result.msg, Toast.LENGTH_SHORT).show();
					return;
				}
				JsonNode data = result.data;
				AbsentDetail absentDetail = JSON.parseObject(data.toString(), AbsentDetail.class);
				AbsentDetailAdapter adapter = new AbsentDetailAdapter(mContext, absentDetail.getAbsent_list());
				setExpand(position);
				mAdapter.setAdpater(position, adapter);
				mAdapter.notifyDataSetChanged();
			}

			@Override
			public void failure(RetrofitError error) {
				super.failure(error);
				hideWaitingDialog();
				Toast.makeText(getApplication(), "请求失败", Toast.LENGTH_SHORT).show();
			}
		});
	}

	/**
	 * 设置是否展开列表
	 * */
	private void setExpand(int position) {
		Integer isExpanded = mAdapter.get(position);
		if (isExpanded != null) {
			if (isExpanded == 0) {
				mAdapter.set(position, 1);
			} else {
				mAdapter.set(position, 0);
			}
		}
	}

	private void initList() {
		mListGroupMemberInfos.clear();
		mPageCount = 1;
		getAbsentResult(mGroupid, MeetingApp.mVersionCode, "" + mPageCount);
	}

	private void setView() {
		if (!mExListUnSigned.isPullToRefreshEnabled()) {
			mExListUnSigned.setMode(Mode.BOTH);
		}
		if (mExListUnSigned.isRefreshing()) {
			mExListUnSigned.onRefreshComplete();
		}
		if (mRelProBar.isShown()) {
			mRelProBar.setVisibility(View.GONE);
		}
	}

	public void getAbsentResult(String groupid, String appVersion, String pageCount) {
		NewNetwork.getAbsentResult(groupid, appVersion, pageCount, new OnResponse<NetworkReturn>("absent_results_Android") {
			@Override
			public void success(NetworkReturn result, Response response) {
				super.success(result, response);
				setView();
				if (result.result != 1) {
					Toast.makeText(SignAllResultActivity.this, result.msg, Toast.LENGTH_SHORT).show();
					return;
				}
				GroupMemberInfoList groupMemberInfoList = new GroupMemberInfoList();
				JsonNode data = result.data;
				JsonNode object = data.get("results");
				if (data.has("meeting_count")) {
					groupMemberInfoList.meeting_count = data.get("meeting_count").getValueAsInt();
				}
				groupMemberInfoList.mGroupMemberInfosList = new ArrayList<GroupMemberInfo>();
				for (int i = 0; i < object.size(); i++) {
					JsonNode item = object.get(i);
					GroupMemberInfo groupMemberInfo = new GroupMemberInfo();
					groupMemberInfo.absent_count = item.get("absent_count").getValueAsInt();
					groupMemberInfo.uid = item.get("uid").getValueAsInt();
					groupMemberInfo.showname = item.get("showname").getValueAsText();
					groupMemberInfoList.mGroupMemberInfosList.add(groupMemberInfo);
				}
				if (groupMemberInfoList.meeting_count > 0) {
					mSignCount.setText("" + groupMemberInfoList.meeting_count);
				}
				mListGroupMemberInfos.addAll(groupMemberInfoList.mGroupMemberInfosList);
				if (mAdapter == null) {
					mAdapter = new AllUnSignedAdapter(SignAllResultActivity.this, mListGroupMemberInfos);
					mExListUnSigned.setAdapter(mAdapter);
				} else {
					mAdapter.notifyDataSetChanged();
				}
			}

			@Override
			public void failure(RetrofitError error) {
				super.failure(error);
				setView();
				Toast.makeText(SignAllResultActivity.this, "获取信息失败", Toast.LENGTH_SHORT).show();
			}
		});

	}

	public void exportAllSignResult(String groupid, String email) {
		NewNetwork.exportAllSignResult(groupid, email, new OnResponse<NetworkReturn>("export_multiple_meeting_results_Android") {
			@Override
			public void success(NetworkReturn result, Response response) {
				super.success(result, response);
				hideWaitingDialog();
				Toast.makeText(getApplication(), result.msg, Toast.LENGTH_SHORT).show();
			}

			@Override
			public void failure(RetrofitError error) {
				super.failure(error);
				hideWaitingDialog();
				Toast.makeText(getApplication(), "导出失败", Toast.LENGTH_SHORT).show();
			}
		});

	}

	private AlertDialog mDialog;

	private void initDialog() {
		if (mDialog == null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			mDialog = builder.create();
			mDialog.setCanceledOnTouchOutside(true);
		}
	}

	private void showExportDialog() {
		if (mExportView == null) {
			mExportView = View.inflate(mContext, R.layout.export_dialog, null);

			final EditText eTvEmail = (EditText) mExportView.findViewById(R.id.etv_email);
			eTvEmail.setText(MeetingApp.userInfo.email);
			Button btnEnsure = (Button) mExportView.findViewById(R.id.btn_ensure);
			btnEnsure.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					StatService.trackCustomEvent(SignAllResultActivity.this, "Outputtomail", "OK");
					String email = eTvEmail.getText().toString();
					if (email.length() <= 0) {
						Toast.makeText(mContext, "邮箱地址不能为空！", 0).show();
						return;
					}
					mDialog.cancel();
					showWaitingDialog();
					exportAllSignResult(mGroupid, email);
				}
			});
			Button btnCancel = (Button) mExportView.findViewById(R.id.btn_cancel);
			btnCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mDialog.cancel();
					StatService.trackCustomEvent(mContext, "OutputtomailCancel", "OK");
				}
			});
			// 为了能显示输入法
			mDialog.setView(new EditText(mContext));
		}
		mDialog.show();
		mDialog.getWindow().setContentView(mExportView);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
}
