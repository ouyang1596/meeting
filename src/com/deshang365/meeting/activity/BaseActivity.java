package com.deshang365.meeting.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.deshang365.meeting.R;
import com.deshang365.meeting.model.Constants;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.stat.StatService;

public class BaseActivity extends Activity {
	protected Context mContext;
	protected ProgressDialog mWaitingDialog;
	private AlertDialog mDialog;
	private AnimationDrawable mAnimationDrawable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		mContext = this;
	}

	@Override
	protected void onResume() {
		super.onResume();
		StatService.onResume(this);
		XGPushClickedResult result = XGPushManager.onActivityStarted(this);
		if (null != result) {
			String customContent = result.getCustomContent();
			if (customContent != null && customContent.length() != 0) {
				try {
					JSONObject json = new JSONObject(customContent);
					if (json.getString("type").equals("1")) {
						String url = json.getString("data");
						Intent intent = new Intent();
						intent.putExtra(Constants.KEY_WEB_URL, url);
						intent.setClass(mContext, WebActivity.class);
						mContext.startActivity(intent);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void showWaitingDialog() {
		initDialog();
		View loadingView = View.inflate(this, R.layout.alert_loading, null);
		ImageView imgv = (ImageView) loadingView.findViewById(R.id.imgv);
		mAnimationDrawable = (AnimationDrawable) imgv.getDrawable();
		mAnimationDrawable.start();
		mDialog.show();
		mDialog.getWindow().setContentView(loadingView);
	}

	private void initDialog() {
		if (mDialog == null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			mDialog = builder.create();
			mDialog.setCancelable(false);
		}
	}

	public void hideWaitingDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
			mDialog = null;
			if (mAnimationDrawable != null) {
				mAnimationDrawable.stop();
				mAnimationDrawable = null;
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPause(this);
		XGPushManager.onActivityStoped(this);
	}
}
