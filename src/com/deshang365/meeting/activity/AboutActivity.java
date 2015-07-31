package com.deshang365.meeting.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.deshang365.meeting.R;
import com.deshang365.meeting.adapter.AboutAdapter;
import com.deshang365.meeting.baselib.MeetingApp;

public class AboutActivity extends BaseActivity {
	private ListView mLvAbout;
	private final String URLQUESTION = "http://meeting.deshang365.com/help.html?type=1";
	private final String URLINTRODUCTION = "http://meeting.deshang365.com/help.html?type=2";
	private final String URLWOLAIYE = "http://www.wlyeah.com/";
	private TextView mTvVersion;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		initView();
	}

	private void initView() {
		mTvVersion = (TextView) findViewById(R.id.txtv_version);
		mTvVersion.setText("V" + MeetingApp.mVersionName);
		mLvAbout = (ListView) findViewById(R.id.lv_about);
		TextView title = (TextView) findViewById(R.id.tv_top_alert_text);
		title.setText("关于我们");
		View backView = findViewById(R.id.ll_top_alert_back);
		backView.setVisibility(View.VISIBLE);
		backView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		AboutAdapter adapter = new AboutAdapter(mContext);
		mLvAbout.setAdapter(adapter);
		mLvAbout.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position == 0) {
					Intent intent = new Intent(mContext, WebActivity.class);
					intent.putExtra("topical", "功能介绍");
					intent.putExtra("url", URLINTRODUCTION);
					startActivity(intent);
				} else if (position == 1) {
					Intent intent = new Intent(mContext, WebActivity.class);
					intent.putExtra("url", URLQUESTION);
					intent.putExtra("topical", "常见问题");
					startActivity(intent);
				} else if (position == 2) {
					String mAddress = "market://details?id=" + getPackageName();
					Intent marketIntent = new Intent("android.intent.action.VIEW");
					marketIntent.setData(Uri.parse(mAddress));
					startActivity(marketIntent);
				} else if (position == 3) {
					startActivity(new Intent(mContext, AppQrCodeActivity.class));
				} else if (position == 4) {
					startActivity(new Intent(mContext, FeedBackActivity.class));
				} else if (position == 5) {
					Intent intent = new Intent(mContext, WebActivity.class);
					intent.putExtra("topical", "我来也");
					intent.putExtra("url", URLWOLAIYE);
					startActivity(intent);
				}
			}
		});
	}
}
