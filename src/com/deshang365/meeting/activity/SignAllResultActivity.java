package com.deshang365.meeting.activity;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonNode;

import retrofit.RetrofitError;
import retrofit.client.Response;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deshang365.meeting.R;
import com.deshang365.meeting.adapter.AllUnSignedAdapter;
import com.deshang365.meeting.baselib.MeetingApp;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_all_result);
		initView();
	}

	@SuppressWarnings("unchecked")
	@SuppressLint("NewApi")
	private void initView() {
		setExportDialog();
		mListGroupMemberInfos = new ArrayList<GroupMemberInfo>();
		// final int signcount = getIntent().getIntExtra("signcount", 0);
		mGroupid = getIntent().getStringExtra("groupid");
		mSignCount = (TextView) findViewById(R.id.txtv_sign_count);
		// mSignCount.setText("本群组共发起" + signcount + "次签到");
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
				mExportDialog.show();
			}

		});
		mExListUnSigned = (PullToRefreshListView) findViewById(R.id.exlist_un_signed_member);
		mExListUnSigned.setOnRefreshListener(new OnRefreshListener2() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase refreshView) {
				initList();

			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase refreshView) {
				mPageCount += 1;
				getAbsentResult(mGroupid, MeetingApp.mVersionName, "" + mPageCount);

			}
		});
		// mExListUnSigned.setGroupIndicator(null);
		mRelProBar.setVisibility(View.VISIBLE);
		mExListUnSigned.setMode(Mode.DISABLED);
		initList();
	}

	private void initList() {
		mListGroupMemberInfos.clear();
		if (MeetingApp.mVersionName != null) {
			mPageCount = 1;
			getAbsentResult(mGroupid, MeetingApp.mVersionName, "" + mPageCount);
		} else {
			getAbsentResult(mGroupid, "-1", "" + mPageCount);
		}
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

	private View mView;

	private void setExportDialog() {
		if (mView == null) {
			mView = View.inflate(SignAllResultActivity.this, R.layout.dialog_email_item, null);
		}
		final EditText eTvEmail = (EditText) mView.findViewById(R.id.etv_email);
		eTvEmail.setText(MeetingApp.userInfo.email);
		eTvEmail.setSelection(eTvEmail.length());
		AlertDialog.Builder builder = new AlertDialog.Builder(SignAllResultActivity.this);
		builder.setTitle("请输入要导出的邮箱");
		builder.setView(mView).setPositiveButton("确定", new DialogInterface.OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(DialogInterface dialog, int which) {
				StatService.trackCustomEvent(mContext, "Outputtomail", "OK");
				String email = eTvEmail.getText().toString();
				if (email.length() <= 0) {
					Toast.makeText(SignAllResultActivity.this, "邮箱地址不能为空！", 0).show();
					return;
				}
				showWaitingDialog();
				exportAllSignResult(mGroupid, email);

			}
		}).setNegativeButton("取消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				StatService.trackCustomEvent(mContext, "OutputtomailCancel", "OK");
			}
		});
		mExportDialog = builder.create();
	}

	public void getAbsentResult(String groupid, String appVersion, String pageCount) {
		NewNetwork.getAbsentResult(groupid, appVersion, pageCount, new OnResponse<NetworkReturn>("absent_results_Android") {
			@Override
			public void success(NetworkReturn result, Response response) {
				super.success(result, response);
				setView();
				if (result.result != 1) {
					Toast.makeText(mContext, result.msg, Toast.LENGTH_SHORT).show();
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
					mAdapter = new AllUnSignedAdapter(mContext, mListGroupMemberInfos);
					mExListUnSigned.setAdapter(mAdapter);
				} else {
					mAdapter.notifyDataSetChanged();
				}

				// ExlAllUnSignedAdapter adapter = new
				// ExlAllUnSignedAdapter(SignAllResultActivity.this,
				// groupMemberInfoList.mGroupMemberSignInfosList);
				// mExListUnSigned.setAdapter(adapter);
			}

			@Override
			public void failure(RetrofitError error) {
				super.failure(error);
				setView();
				Toast.makeText(mContext, "获取信息失败！", Toast.LENGTH_SHORT).show();
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

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
}
