package com.deshang365.meeting.activity;

import java.io.File;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

import com.deshang365.meeting.R;
import com.deshang365.meeting.baselib.ImageHandle;
import com.deshang365.meeting.baselib.MeetingApp;
import com.deshang365.meeting.model.Constants;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.view.CircularImageView;

public class CompleteGroupActivity extends ImageloaderBaseActivity {
	private Button mBtnBackSRHistory, mBtnShare, mBtnToCreateSign;
	private TextView mTvTopical, mTvGroupName, mTvGroupCode, mTvComplete;
	private LinearLayout mLlBack;
	private CircularImageView mImgvGroupHead;
	private int mUid;
	private String mHxgroupid;
	private String mGroupname;
	private String idcard;
	private String mShowname;
	private String mGroupId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_complete_create);
		initView();
	}

	private void initView() {
		mGroupId = getIntent().getStringExtra("groupid");
		mShowname = getIntent().getStringExtra("showname");
		mGroupname = getIntent().getStringExtra("groupname");
		idcard = getIntent().getStringExtra("idcard");
		mHxgroupid = getIntent().getStringExtra("hxgroupid");
		final int rescode = getIntent().getIntExtra("rescode", -1);
		mImgvGroupHead = (CircularImageView) findViewById(R.id.imgv_group_head);
		mBtnShare = (Button) findViewById(R.id.btn_share);
		mBtnShare.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showShare();
			}
		});
		mLlBack = (LinearLayout) findViewById(R.id.ll_top_alert_back);
		mLlBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mTvComplete = (TextView) findViewById(R.id.txtv_created_or_joined);
		mTvTopical = (TextView) findViewById(R.id.tv_top_alert_text);
		mTvGroupName = (TextView) findViewById(R.id.txtv_group_name);
		mTvGroupCode = (TextView) findViewById(R.id.txtv_group_code);
		mBtnBackSRHistory = (Button) findViewById(R.id.btn_back);
		if (rescode == 1) {
			mTvTopical.setText("创建群组");
			mTvGroupName.setText(mGroupname);
			mTvGroupCode.setText(idcard);
			mTvComplete.setText("恭喜您,成功创建群组");
			Bitmap loacalBitmap = ImageHandle.getLoacalBitmap(Constants.AVATAR_PATH + MeetingApp.userInfo.uid);
			if (loacalBitmap != null) {
				mImgvGroupHead.setImageBitmap(loacalBitmap);
			}
		} else if (rescode == 2) {
			mTvTopical.setText("加入群组");
			mTvGroupName.setText(mGroupname);
			mTvGroupCode.setText(idcard);
			mTvComplete.setText("恭喜您,成功加入群组");
			mUid = getIntent().getIntExtra("uid", -1);
			setImageView();
		}

		mBtnBackSRHistory.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(CompleteGroupActivity.this, MainActivity.class));
			}
		});

		mBtnToCreateSign = (Button) findViewById(R.id.btn_talk_page);
		mBtnToCreateSign.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (rescode == 1) {
					Intent intent = new Intent(mContext, CreateSignActivity.class);
					intent.putExtra("mtype", 0);// 0是创建者 1是参与者
					intent.putExtra("groupname", mGroupname);
					intent.putExtra("groupcode", idcard);
					intent.putExtra("groupid", mGroupId);
					intent.putExtra("hxgroupid", mHxgroupid);
					intent.putExtra("showname", mShowname);
					startActivity(intent);
					finish();
				} else if (rescode == 2) {
					Intent intent = new Intent(mContext, SignHistoryActivity.class);
					intent.putExtra("mtype", 1);
					intent.putExtra("groupname", mGroupname);
					intent.putExtra("groupcode", idcard);
					intent.putExtra("groupid", mGroupId);
					intent.putExtra("hxgroupid", mHxgroupid);
					intent.putExtra("showname", mShowname);
					intent.putExtra("signstate", "1");
					intent.putExtra("meetingid", -1);
					startActivity(intent);
					finish();
				}
				// Intent intent = new Intent(mContext,
				// TalkTogetherActivity.class);
				// intent.putExtra("mobcode", mHxgroupid);
				// intent.putExtra("groupName", mGroupname);
				// startActivity(intent);
				// finish();

			}
		});
	}

	/** 分享 */
	private void showShare() {
		ShareSDK.initSDK(this);
		OnekeyShare oks = new OnekeyShare();
		// 关闭sso授权
		oks.disableSSOWhenAuthorize();

		// 分享时Notification的图标和文字 2.5.9以后的版本不调用此方法
		// oks.setNotification(R.drawable.ic_launcher,
		// getString(R.string.app_name));
		// title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
		oks.setTitle(getString(R.string.share));
		// titleUrl是标题的网络链接，仅在人人网和QQ空间使用
		oks.setTitleUrl("http://sharesdk.cn");
		// text是分享文本，所有平台都需要这个字段
		oks.setText("我是分享文本");
		// imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
		oks.setImagePath("/sdcard/test.jpg");// 确保SDcard下面存在此张图片
		// url仅在微信（包括好友和朋友圈）中使用
		oks.setUrl("http://sharesdk.cn");
		// comment是我对这条分享的评论，仅在人人网和QQ空间使用
		oks.setComment("我是测试评论文本");
		// site是分享此内容的网站名称，仅在QQ空间使用
		oks.setSite(getString(R.string.app_name));
		// siteUrl是分享此内容的网站地址，仅在QQ空间使用
		oks.setSiteUrl("http://sharesdk.cn");
		// 启动分享GUI
		oks.show(this);
	}

	private void setImageView() {
		File file = new File(Constants.AVATAR_PATH, "" + mUid);
		mImageLoader.displayImage(NewNetwork.getAvatarUrl(mUid), mImgvGroupHead, mOptions);
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
