package com.deshang365.meeting.activity;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonNode;

import retrofit.RetrofitError;
import retrofit.client.Response;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.alibaba.fastjson.JSON;
import com.deshang365.meeting.R;
import com.deshang365.meeting.adapter.MyRecSignedAdapter;
import com.deshang365.meeting.adapter.MyRecUnSignedAdapter;
import com.deshang365.meeting.baselib.MeetingApp;
import com.deshang365.meeting.model.MeetingRecords;
import com.deshang365.meeting.model.MeetingRecordsItem;
import com.deshang365.meeting.network.NetworkReturn;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.network.OnResponse;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class MySignRecordActivity extends BaseActivity {
	private RadioGroup mRgSignList;
	private PullToRefreshListView mLvSigned, mLvUnSigned;
	private LinearLayout mLlBack;
	private List<MeetingRecordsItem> mRecUnSignedList;
	private List<MeetingRecordsItem> mRecSignedList;
	private RelativeLayout mRelPro;
	private TextView mTvTopical;
	private int mRec_type;// 0未完成签到 1已完成签到
	private int mPageUnSigned, mPageSigned;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_sign_record);
		initView();

	}

	@SuppressWarnings("unchecked")
	private void initView() {
		mRelPro = (RelativeLayout) findViewById(R.id.rel_progressbar);
		mRelPro.setVisibility(View.VISIBLE);
		mRecUnSignedList = new ArrayList<MeetingRecordsItem>();
		mRecSignedList = new ArrayList<MeetingRecordsItem>();
		mTvTopical = (TextView) findViewById(R.id.tv_top_alert_text);
		mTvTopical.setText("我的签到记录");
		mLlBack = (LinearLayout) findViewById(R.id.ll_top_alert_back);
		mLlBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mLvSigned = (PullToRefreshListView) findViewById(R.id.plv_signed);
		mLvSigned.setMode(Mode.BOTH);
		mLvSigned.setVisibility(View.GONE);
		mLvUnSigned = (PullToRefreshListView) findViewById(R.id.plv_un_signed);
		mLvUnSigned.setMode(Mode.BOTH);
		mLvUnSigned.setVisibility(View.VISIBLE);
		mRgSignList = (RadioGroup) findViewById(R.id.rg_show_signlist);
		mRgSignList.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.rb_show_signed) {
					mLvSigned.setVisibility(View.VISIBLE);
					mLvUnSigned.setVisibility(View.GONE);
					if (mRecSignedList.size() <= 0) {
						initSignedRecord();
					}
				} else if (checkedId == R.id.rb_show_un_signed) {
					mLvSigned.setVisibility(View.GONE);
					mLvUnSigned.setVisibility(View.VISIBLE);
					if (mRecUnSignedList.size() <= 0) {
						initUnSignedRecord();
					}
				}

			}
		});
		mLvSigned.setOnRefreshListener(new OnRefreshListener2() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase refreshView) {
				initSignedRecord();
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase refreshView) {
				mPageSigned += 1;
				myRecSigned(MeetingApp.mVersionName, mPageSigned);
			}
		});
		mLvUnSigned.setOnRefreshListener(new OnRefreshListener2() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase refreshView) {
				initUnSignedRecord();
			}

			@Override
			public void onPullUpToRefresh(@SuppressWarnings("rawtypes") PullToRefreshBase refreshView) {
				mPageUnSigned += 1;
				myRecUnSigned(MeetingApp.mVersionName, mPageUnSigned);
			}
		});
		initUnSignedRecord();
		initSignedRecord();

	}

	private void initSignedRecord() {
		mRec_type = 1;
		mPageUnSigned = 1;
		mRecSignedList.clear();
		myRecSigned(MeetingApp.mVersionName, mPageUnSigned);
	}

	private void initUnSignedRecord() {
		mRec_type = 0;
		mPageUnSigned = 1;
		mRecUnSignedList.clear();
		myRecUnSigned(MeetingApp.mVersionName, mPageUnSigned);
	}

	private MyRecUnSignedAdapter mUnSignedAdapter;
	private MyRecSignedAdapter mSignedAdapter;

	private void myRecUnSigned(String app_version, int page) {
		NewNetwork.myRecUnSigned(app_version, page, new OnResponse<NetworkReturn>("my_signrec_Android") {
			@Override
			public void success(NetworkReturn result, Response arg1) {
				super.success(result, arg1);
				hidePro();
				completeUnSignedRefresh();
				if (result.result != 1) {
					Toast.makeText(mContext, result.msg, Toast.LENGTH_SHORT).show();
					return;
				}
				JsonNode object = result.data;
				MeetingRecords meetingRecords = JSON.parseObject(object.toString(), MeetingRecords.class);
				mRecUnSignedList.addAll(meetingRecords.getMeeting_records());
				if (mUnSignedAdapter == null) {
					mUnSignedAdapter = new MyRecUnSignedAdapter(mContext, mRecUnSignedList);
					mLvUnSigned.setAdapter(mUnSignedAdapter);
				} else {
					mUnSignedAdapter.notifyDataSetChanged();
				}
			}

			@Override
			public void failure(RetrofitError error) {
				super.failure(error);
				hidePro();
				completeUnSignedRefresh();
				Toast.makeText(mContext, "请求失败 ", Toast.LENGTH_SHORT).show();
			}

			private void completeUnSignedRefresh() {
				if (mLvUnSigned.isRefreshing()) {
					mLvUnSigned.onRefreshComplete();
				}
			}
		});

	}

	private void myRecSigned(String app_version, int page) {
		NewNetwork.myRecSigned(app_version, page, new OnResponse<NetworkReturn>("my_signrec_Android") {
			@Override
			public void success(NetworkReturn result, Response arg1) {
				super.success(result, arg1);
				hidePro();
				completeSignedRefresh();
				if (result.result != 1) {
					Toast.makeText(mContext, result.msg, Toast.LENGTH_SHORT).show();
					return;
				}
				JsonNode object = result.data;
				MeetingRecords meetingRecords = JSON.parseObject(object.toString(), MeetingRecords.class);
				mRecSignedList.addAll(meetingRecords.getMeeting_records());
				if (mSignedAdapter == null) {
					mSignedAdapter = new MyRecSignedAdapter(mContext, mRecSignedList);
					mLvSigned.setAdapter(mSignedAdapter);
				} else {
					mSignedAdapter.notifyDataSetChanged();
				}
			}

			@Override
			public void failure(RetrofitError error) {
				super.failure(error);
				hidePro();
				completeSignedRefresh();
				Toast.makeText(mContext, "请求失败 ", Toast.LENGTH_SHORT).show();
			}

			private void completeSignedRefresh() {
				if (mLvSigned.isRefreshing()) {
					mLvSigned.onRefreshComplete();
				}
			}
		});
	}

	private void hidePro() {
		if (mRelPro.isShown()) {
			mRelPro.setVisibility(View.GONE);
		}
	}
}
