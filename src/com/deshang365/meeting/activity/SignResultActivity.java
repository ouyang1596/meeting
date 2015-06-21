package com.deshang365.meeting.activity;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonNode;

import retrofit.RetrofitError;
import retrofit.client.Response;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deshang365.meeting.R;
import com.deshang365.meeting.adapter.ExlSignListAdapter2;
import com.deshang365.meeting.adapter.ExlSignListAdapter2.OnCheckedListener;
import com.deshang365.meeting.baselib.MeetingApp;
import com.deshang365.meeting.model.GroupMemberInfo;
import com.deshang365.meeting.network.NetworkReturn;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.network.OnResponse;
import com.deshang365.meeting.view.SignedView;
import com.tencent.stat.StatService;

public class SignResultActivity extends BaseActivity {
	private TextView mTvTopical, mTvSignMemberState;
	private Button mBtnEnsure;
	private LinearLayout mLlBack;
	private AlertDialog mExportDialog;
	private RadioGroup mRgSignList;
	private String mMeetingid;
	private SignedView mRelAllSigned;
	private int mState;// 1 请假 2缺席（默认） 3补签 4迟到
	private String mGroupid;
	private AlertDialog mSignDialog;
	private int mUid;
	private View mView;
	private AlertDialog mDialog;
	private RelativeLayout mRelAbsent;
	private ExpandableListView mExlistSIgnList;
	private ExlSignListAdapter2 mAdapter;
	private TextView mTvTopAlertExport;
	private View mChangeView;
	private String mChangeParams;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_result);
		initView();
	}

	@SuppressLint("NewApi")
	private void initView() {
		setExportDialog();
		mTvTopical = (TextView) findViewById(R.id.tv_top_alert_text);
		mTvTopical.setText("签到结果");
		mLlBack = (LinearLayout) findViewById(R.id.ll_top_alert_back);
		mLlBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mRelAllSigned = (SignedView) findViewById(R.id.rel_all_signed);
		mRgSignList = (RadioGroup) findViewById(R.id.rg_show_signlist);
		mMeetingid = getIntent().getStringExtra("meetingid");
		mGroupid = getIntent().getStringExtra("groupid");
		mRelAbsent = (RelativeLayout) findViewById(R.id.rel_sign_absent);
		mExlistSIgnList = (ExpandableListView) findViewById(R.id.exl_sign_list);
		mExlistSIgnList.setGroupIndicator(null);
		mTvTopAlertExport = (TextView) findViewById(R.id.txtv_what_need);
		mTvTopAlertExport.setText("导出签到结果");
		mTvTopAlertExport.setVisibility(View.VISIBLE);
		mBtnEnsure = (Button) findViewById(R.id.btn_export);
		mBtnEnsure.setEnabled(false);
		mBtnEnsure.setBackgroundResource(R.drawable.invalid);
		mBtnEnsure.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mChangeParams = mAdapter.getparams().toString();
				if (mChangeParams.isEmpty()) {
					Toast.makeText(mContext, "您没有执行任何操作！", Toast.LENGTH_SHORT).show();
					return;
				}
				if (mAdapter != null) {
					changeState();
				}
			}
		});
		initAdapter();
		showWaitingDialog();
		getSignList(mGroupid, mMeetingid);
		mTvTopAlertExport.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mExportDialog.show();
			}
		});
		mRgSignList.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (mAdapter == null || mGroupMemberInfoSignList == null) {
					return;
				}
				if (checkedId == R.id.rb_show_un_signed) {
					if (mGroupMemberInfoSignList.get(1).size() == 0) {
						mExlistSIgnList.setVisibility(View.GONE);
						mRelAllSigned.setVisibility(View.VISIBLE);
						return;
					}
					mExlistSIgnList.setVisibility(View.VISIBLE);
					mRelAllSigned.setVisibility(View.GONE);
					mAdapter.setShow(1);
				} else if (checkedId == R.id.rb_show_signed) {
					if (mGroupMemberInfoSignList.get(0).size() == 0) {
						mExlistSIgnList.setVisibility(View.GONE);
						mRelAllSigned.setVisibility(View.GONE);
						return;
					}
					mExlistSIgnList.setVisibility(View.VISIBLE);
					mRelAllSigned.setVisibility(View.VISIBLE);
					mAdapter.setShow(0);
				}
			}
		});
		mTvSignMemberState = (TextView) mRelAllSigned.findViewById(R.id.txtv_sign_member_state);
		mTvSignMemberState.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mGroupMemberInfoSignList != null && mGroupMemberInfoSignList.size() >= 3 && mGroupMemberInfoSignList.get(2).size() > 0) {
					Intent intent = new Intent(mContext, LoginSignedActivity.class);
					intent.putExtra("groupid", mGroupid);
					intent.putExtra("meetingid", mMeetingid);
					intent.putExtra("answer", mGroupMemberInfoSignList.get(2).get(0).signcode);
					startActivity(intent);
				}

			}
		});
	}

	private void changeState() {
		initDialog();
		if (mChangeView == null) {
			mChangeView = View.inflate(mContext, R.layout.exit_dialog, null);
		}
		Button btnEnsure = (Button) mChangeView.findViewById(R.id.btn_exit);
		TextView tvChangeState = (TextView) mChangeView.findViewById(R.id.txtv_exit);
		tvChangeState.setText("您确定修改成员状态？");
		tvChangeState.setVisibility(View.VISIBLE);
		btnEnsure.setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				mDialog.cancel();
				showWaitingDialog();
				String changeState = "[" + mChangeParams + "]";
				changeState(changeState);
			}
		});
		Button btnCancel = (Button) mChangeView.findViewById(R.id.btn_cancel);
		btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mDialog.cancel();
			}
		});
		mDialog.show();
		mDialog.getWindow().setContentView(mChangeView);
	}

	private void initAdapter() {
		mAdapter = new ExlSignListAdapter2(mContext);
		mAdapter.setOnCheckedListener(new OnCheckedListener() {

			@Override
			public void onChecked(int mapSize) {
				if (mapSize != 0) {
					mBtnEnsure.setEnabled(true);
					mBtnEnsure.setBackgroundResource(R.drawable.btn_blue_bg_radius_selector);
				} else {
					mBtnEnsure.setEnabled(false);
					mBtnEnsure.setBackgroundResource(R.drawable.invalid);
				}
			}

		});
		mExlistSIgnList.setAdapter(mAdapter);
	}

	private void changeState(String datas) {
		NewNetwork.changeState(datas, new OnResponse<NetworkReturn>("change_status_Android") {
			@Override
			public void success(NetworkReturn result, Response arg1) {
				super.success(result, arg1);
				hideWaitingDialog();
				if (result.result != 1) {
					Toast.makeText(mContext, result.msg, Toast.LENGTH_SHORT).show();
					return;
				}
				showWaitingDialog();
				getSignList(mGroupid, mMeetingid);

			}

			@Override
			public void failure(RetrofitError error) {
				super.failure(error);
				hideWaitingDialog();
				Toast.makeText(mContext, "修改失败", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void setExportDialog() {
		final View mView = View.inflate(getApplication(), R.layout.dialog_email_item, null);
		final EditText eTvEmail = (EditText) mView.findViewById(R.id.etv_email);
		eTvEmail.setText(MeetingApp.userInfo.email);
		eTvEmail.setSelection(eTvEmail.length());
		AlertDialog.Builder builder = new AlertDialog.Builder(SignResultActivity.this);
		builder.setTitle("请输入要导出的邮箱");
		builder.setView(mView).setPositiveButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				StatService.trackCustomEvent(mContext, "Output1", "OK");
				String email = eTvEmail.getText().toString();
				if (email.length() <= 0) {
					Toast.makeText(SignResultActivity.this, "邮箱地址不能为空！", 0).show();
					return;
				}
				showWaitingDialog();
				exportSingleResult(mMeetingid, email);
				// new ExportSignleResultAsyn().execute(mMeetingid,
				// eTvEmail.getText().toString());
			}
		}).setNegativeButton("取消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				StatService.trackCustomEvent(mContext, "OutputtomailCancel", "OK");
			}
		});
		mExportDialog = builder.create();

	}

	private void initDialog() {
		if (mDialog == null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			mDialog = builder.create();
			mDialog.setCanceledOnTouchOutside(true);
		}
	}

	public void getSignList(String groupid, String meetingid) {
		NewNetwork.getSignList(groupid, meetingid, new OnResponse<NetworkReturn>("meetinglist_Android") {
			@Override
			public void success(NetworkReturn result, Response response) {
				super.success(result, response);
				hideWaitingDialog();
				if (result.result != 1) {
					Toast.makeText(mContext, result.msg, Toast.LENGTH_SHORT).show();
					return;
				}
				ArrayList<List<GroupMemberInfo>> groupMemberInfoLists = new ArrayList<List<GroupMemberInfo>>();
				JsonNode data = result.data;
				JsonNode joiners = data.get("joiners");
				List<GroupMemberInfo> joinersGroupInfoList = jsonData(joiners);
				groupMemberInfoLists.add(joinersGroupInfoList);

				JsonNode absenters = data.get("absenters");
				List<GroupMemberInfo> absentersGroupInfoList = jsonData(absenters);
				groupMemberInfoLists.add(absentersGroupInfoList);

				GroupMemberInfo groupInfo = new GroupMemberInfo();
				groupInfo.signcode = data.get("sign_code").getValueAsText();

				mGroupMemberInfoSignList = groupMemberInfoLists;
				if (groupMemberInfoLists.get(1).size() <= 0) {
					mBtnEnsure.setEnabled(false);
					mBtnEnsure.setBackgroundResource(R.drawable.invalid);
					mExlistSIgnList.setVisibility(View.GONE);
				} else {
					mExlistSIgnList.setVisibility(View.VISIBLE);
				}
				if (mAdapter != null) {
					mAdapter.clearMapTab();
					mAdapter.setGroupMemberInfoList(groupMemberInfoLists, mMeetingid);
					mAdapter.notifyDataSetChanged();
				}
				int groupCount = mAdapter.getGroupCount();
				for (int i = 0; i < groupCount; i++) {
					mExlistSIgnList.expandGroup(i);
				}
			}

			private List<GroupMemberInfo> jsonData(JsonNode absenters) {
				List<GroupMemberInfo> absentersGroupInfoList = new ArrayList<GroupMemberInfo>();
				for (int j = 0; j < absenters.size(); j++) {
					JsonNode jsonItem = absenters.get(j);
					GroupMemberInfo absentersGroupInfo = new GroupMemberInfo();
					if (jsonItem.has("createtime")) {
						absentersGroupInfo.createtime = jsonItem.get("createtime").getValueAsText();
					}
					if (jsonItem.has("showname")) {
						absentersGroupInfo.showname = jsonItem.get("showname").getValueAsText();
					}
					absentersGroupInfo.uid = jsonItem.get("uid").getValueAsInt();
					if (jsonItem.has("state")) {
						absentersGroupInfo.state = jsonItem.get("state").getValueAsInt();
					}
					absentersGroupInfoList.add(absentersGroupInfo);
				}
				return absentersGroupInfoList;
			}

			@Override
			public void failure(RetrofitError error) {
				super.failure(error);
				hideWaitingDialog();
				Toast.makeText(mContext, "获取签到列表失败", Toast.LENGTH_SHORT).show();
			}

		});

	}

	private List<List<GroupMemberInfo>> mGroupMemberInfoSignList;

	private void exportSingleResult(String meeting_id, String email) {
		NewNetwork.exportSingleResult(meeting_id, email, new OnResponse<NetworkReturn>("export_single_meeting_results_Android") {
			@Override
			public void success(NetworkReturn result, Response arg1) {
				super.success(result, arg1);
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

	// class ExportSignleResultAsyn extends AsyncTask<String, Void,
	// NetworkReturnBase> {
	// @Override
	// protected void onPreExecute() {
	// super.onPreExecute();
	// showWaitingDialog();
	// }
	//
	// @Override
	// protected NetworkReturnBase doInBackground(String... params) {
	// return Network.exportSingleResult(params[0], params[1]);
	// }
	//
	// @Override
	// protected void onPostExecute(NetworkReturnBase result) {
	// super.onPostExecute(result);
	// hideWaitingDialog();
	// if (result == null) {
	// Toast.makeText(getApplication(), "导出失败！", Toast.LENGTH_SHORT).show();
	// return;
	// }
	// Toast.makeText(getApplication(), result.message,
	// Toast.LENGTH_SHORT).show();
	// }
	// }

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
}
