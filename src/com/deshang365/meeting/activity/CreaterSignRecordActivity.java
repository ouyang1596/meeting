package com.deshang365.meeting.activity;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonNode;

import retrofit.RetrofitError;
import retrofit.client.Response;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deshang365.meeting.R;
import com.deshang365.meeting.adapter.SignHistoryAdapter;
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

public class CreaterSignRecordActivity extends BaseActivity {
	private LinearLayout mLlBack;
	private TextView mTvTopical;
	private RelativeLayout mRelProBar;
	private AlertDialog mExportDialog;
	private RadioGroup mRgTime;
	private Button mBtnExport;
	private ImageView mImgvSignAllResult;
	private PullToRefreshListView mLvGroupSignRecord;
	private String mGroupname;
	private String mGroupid;
	private int mPageCount;
	private List<GroupMemberInfo> mListGroupMemberInfos;
	private boolean mIsExpanded;
	private int date_type = 2;// 时间段 1全部 2一周内 3一个月内 4半年内

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_sign_record);
		initView();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void initView() {
		mListGroupMemberInfos = new ArrayList<GroupMemberInfo>();
		mGroupname = getIntent().getStringExtra("groupname");
		mGroupid = getIntent().getStringExtra("groupid");
		mLlBack = (LinearLayout) findViewById(R.id.ll_top_alert_back);
		mLlBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mRelProBar = (RelativeLayout) findViewById(R.id.rel_progressbar);
		mTvTopical = (TextView) findViewById(R.id.tv_top_alert_text);
		if (mGroupname != null) {
			mTvTopical.setText(mGroupname);
		}
		mLvGroupSignRecord = (PullToRefreshListView) findViewById(R.id.plv_sign_history);
		mLvGroupSignRecord.setOnRefreshListener(new OnRefreshListener2() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase refreshView) {
				initPerSignrecord();
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase refreshView) {
				if (MeetingApp.mVersionCode != null) {
					mPageCount += 1;
					getGroupSignRecord(mGroupid, MeetingApp.mVersionCode, "" + mPageCount, date_type);
				}
			}
		});
		mLvGroupSignRecord.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				position = position - 1;
				StatService.trackCustomEvent(mContext, "onceresult", "OK");
				Intent intent = new Intent(mContext, SignSingleResultActivity.class);
				intent.putExtra("groupid", mGroupid);
				intent.putExtra("meetingid", mAdapter.getItem(position).meetingid);
				startActivity(intent);
			}
		});
		mRgTime = (RadioGroup) findViewById(R.id.rg_time);
		mRgTime.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				showWaitingDialog();
				switch (checkedId) {
				case R.id.rb_all:
					date_type = 1;
					mBtnExport.setText("导出全部记录");
					break;
				case R.id.rb_week:
					date_type = 2;
					mBtnExport.setText("导出一周记录");
					break;
				case R.id.rb_month:
					date_type = 3;
					mBtnExport.setText("导出一个月记录");
					break;
				case R.id.rb_half_year:
					date_type = 4;
					mBtnExport.setText("导出半年记录");
					break;
				}
				initPerSignrecord();
			}
		});
		mLvGroupSignRecord.setMode(Mode.DISABLED);
		mRelProBar.setVisibility(View.VISIBLE);
		initPerSignrecord();
		mImgvSignAllResult = (ImageView) findViewById(R.id.imgv_what_need);
		mImgvSignAllResult.setImageResource(R.drawable.btn_sign_all_result_selector);
		mImgvSignAllResult.setVisibility(View.VISIBLE);
		mImgvSignAllResult.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, SignAllResultActivity.class);
				intent.putExtra("groupid", mGroupid);
				startActivity(intent);
			}
		});
		mBtnExport = (Button) findViewById(R.id.btn_export);
		mBtnExport.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				initExportDialog();
				showExportDialog();
			}
		});
	}

	private OnClickListener signDetailListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int position = (Integer) v.getTag();
			StatService.trackCustomEvent(mContext, "onceresult", "OK");
			Intent intent = new Intent(mContext, SignSingleResultActivity.class);
			intent.putExtra("groupid", mGroupid);
			intent.putExtra("meetingid", mAdapter.getItem(position).meetingid);
			startActivity(intent);
		}
	};
	private SignHistoryAdapter mAdapter;

	private void getGroupSignRecord(String groupid, String appVersion, String pageCount, int date_type) {
		NewNetwork.getGroupSignRecord(groupid, appVersion, pageCount, date_type, new OnResponse<NetworkReturn>(
				"historymeetinglist_group_page_Android") {
			@Override
			public void success(NetworkReturn result, Response arg1) {
				super.success(result, arg1);
				hideWaitingDialog();
				setView();
				if (result.result == 1) {
					GroupMemberInfoList groupMemberInfoList = new GroupMemberInfoList();
					groupMemberInfoList.mGroupMemberInfosList = new ArrayList<GroupMemberInfo>();
					JsonNode data = result.data;
					// 该组发起的签到总次数
					int meetingCount = data.get("meeting_count").getValueAsInt();
					// 当前的签到状态 0正在进行 1结束
					int meetingState = data.get("meetingstate").getValueAsInt();
					JsonNode meetingList = data.get("meetinglist");
					for (int i = 0; i < meetingList.size(); i++) {
						JsonNode item = meetingList.get(i);
						GroupMemberInfo groupInfo = new GroupMemberInfo();
						groupInfo.createtime = item.get("createtime").getValueAsText();
						groupInfo.meetingid = item.get("id").getValueAsText();
						groupMemberInfoList.mGroupMemberInfosList.add(groupInfo);
					}
					mListGroupMemberInfos.addAll(groupMemberInfoList.mGroupMemberInfosList);
					if (mAdapter == null) {
						mAdapter = new SignHistoryAdapter(getApplication(), mListGroupMemberInfos);
						mLvGroupSignRecord.setAdapter(mAdapter);
					} else {
						mAdapter.notifyDataSetChanged();
					}
				} else {
					Toast.makeText(mContext, result.msg, Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void failure(RetrofitError arg0) {
				super.failure(arg0);
				hideWaitingDialog();
				setView();
				Toast.makeText(mContext, "获得签到记录失败", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void exportMeetingRecBytime(String groupid, int date_type, String email) {
		NewNetwork.exportMeetingRecBytime(groupid, date_type, email, new OnResponse<NetworkReturn>("export_meetingrec_bytime_Android") {
			@Override
			public void success(NetworkReturn result, Response arg1) {
				super.success(result, arg1);
				hideWaitingDialog();
				Toast.makeText(mContext, result.msg, Toast.LENGTH_SHORT).show();
			}

			@Override
			public void failure(RetrofitError error) {
				super.failure(error);
				hideWaitingDialog();
				Toast.makeText(mContext, "导出失败", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void setView() {
		if (!mLvGroupSignRecord.isPullToRefreshEnabled()) {
			mLvGroupSignRecord.setMode(Mode.BOTH);
		}
		if (mLvGroupSignRecord.isRefreshing()) {
			mLvGroupSignRecord.onRefreshComplete();
		}
		if (mRelProBar.isShown()) {
			mRelProBar.setVisibility(View.GONE);
		}
	}

	private void initPerSignrecord() {
		mListGroupMemberInfos.clear();
		if (MeetingApp.mVersionCode != null) {
			mPageCount = 1;
			getGroupSignRecord(mGroupid, MeetingApp.mVersionCode, "" + mPageCount, date_type);
		} else {
			hideWaitingDialog();
			setView();
		}
	}

	private void initExportDialog() {
		if (mExportDialog == null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			mExportDialog = builder.create();
			mExportDialog.setCanceledOnTouchOutside(true);
		}
	}

	private View mExportView;

	private void showExportDialog() {
		if (mExportView == null) {
			mExportView = View.inflate(mContext, R.layout.export_dialog, null);

			final EditText eTvEmail = (EditText) mExportView.findViewById(R.id.etv_email);
			eTvEmail.setText(MeetingApp.userInfo.email);
			Button btnEnsure = (Button) mExportView.findViewById(R.id.btn_ensure);
			btnEnsure.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					StatService.trackCustomEvent(mContext, "Outputtomail", "OK");
					String email = eTvEmail.getText().toString();
					if (email.length() <= 0) {
						Toast.makeText(mContext, "邮箱地址不能为空！", 0).show();
						return;
					}
					mExportDialog.cancel();
					showWaitingDialog();
					exportMeetingRecBytime(mGroupid, date_type, email);
					// exportSingleResult(mMeetingid, email);
				}
			});
			Button btnCancel = (Button) mExportView.findViewById(R.id.btn_cancel);
			btnCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mExportDialog.cancel();
					StatService.trackCustomEvent(mContext, "OutputtomailCancel", "OK");
				}
			});
			// 为了能显示输入法
			mExportDialog.setView(new EditText(mContext));
		}
		mExportDialog.show();
		mExportDialog.getWindow().setContentView(mExportView);
	}

}
