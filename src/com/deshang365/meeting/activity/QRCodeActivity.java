package com.deshang365.meeting.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import android.annotation.SuppressLint;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

import com.deshang365.meeting.R;
import com.deshang365.meeting.baselib.MeetingApp;
import com.deshang365.meeting.model.Constants;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.util.MeetingUtils;
import com.deshang365.meeting.view.CircularImageView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.tencent.stat.StatService;

public class QRCodeActivity extends ImageloaderBaseActivity {
	private ImageView mImgvQRCode, mShare;
	private LinearLayout mLlBack;
	private TextView mTvTopical, mTvGroupName;
	private CircularImageView mImgvGroupHead;
	private int mUid;
	private PopupWindow mPop;
	private View mViewLine;
	private View mViewShadow;
	private String mGroupcode;
	private String mGroupid;
	private String mQrcodeImgPath;
	private final int QR_WIDTH = 450;
	private final int QR_HEIGHT = 450;
	private Bitmap mQrcodeBitmap;
	// private static final String URL_SHARE =
	// "http://www.wlyeah.com/share.html?p=%%7B%%22groupcode%%22%%3A%%22%s%%22%%7D";
	private static final String URL_SHARE = "http://www.wlyeah.com/share.html?version=1.4.3.96&clientType=iOS&systemTime=1432347513.43314&token=000c4b03-4e26-449c-b681-82f318c285a3&code=%s&uid=%d";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_qrcode);
		initView();
	}

	private void initView() {
		mViewShadow = findViewById(R.id.view_shadow);
		mViewShadow.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mViewShadow.setVisibility(View.GONE);
			}
		});
		mGroupcode = getIntent().getStringExtra("groupcode");
		mGroupid = getIntent().getStringExtra("groupid");
		String groupname = getIntent().getStringExtra("groupname");
		mUid = getIntent().getIntExtra("uid", -1);
		mViewLine = findViewById(R.id.view_line);
		mImgvGroupHead = (CircularImageView) findViewById(R.id.imgv_group_head);
		mImageLoader.displayImage(NewNetwork.getAvatarUrl(mUid), mImgvGroupHead, mOptions);
		mTvGroupName = (TextView) findViewById(R.id.txtv_group_name);
		mTvGroupName.setText(groupname);
		mTvTopical = (TextView) findViewById(R.id.tv_top_alert_text);
		mTvTopical.setText("群二维码");
		mLlBack = (LinearLayout) findViewById(R.id.ll_top_alert_back);
		mLlBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mImgvQRCode = (ImageView) findViewById(R.id.imgv_QR_code);
		createQRImage("meeting:{\"ctype\":1,\"data\":\"" + mGroupid + "\"}");
		share();
	}

	private View mView;

	private void share() {
		mView = View.inflate(mContext, R.layout.groups_popup_item, null);
		TextView share = (TextView) mView.findViewById(R.id.groups_tv_creatGroups);
		share.setText("分享二维码");
		share.setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				StatService.trackCustomEvent(mContext, "Sharecode", "OK");
				mPop.dismiss();
				mViewShadow.setVisibility(View.GONE);
				String qrcodeImgPath = saveBitmap();
				if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
					new ShareAsync().execute();
				} else {
					new ShareAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				}

			}

		});
		TextView saveImage = (TextView) mView.findViewById(R.id.groups_tv_joinGroups);
		saveImage.setText("保存到手机");
		saveImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				StatService.trackCustomEvent(mContext, "Savecode", "OK");
				mPop.dismiss();
				mViewShadow.setVisibility(View.GONE);
				String picUrlString = MediaStore.Images.Media.insertImage(getContentResolver(), mQrcodeBitmap, "title", "meeting");
				if (picUrlString != null) {
					Toast.makeText(mContext, "保存成功！", Toast.LENGTH_SHORT).show();
				}
			}
		});
		mShare = (ImageView) findViewById(R.id.tv_top_alert_groups);
		mShare.setImageResource(R.drawable.share_save);
		mShare.setVisibility(View.VISIBLE);
		mShare.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mPop == null) {
					mPop = new PopupWindow(mView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					mPop.setBackgroundDrawable(new ColorDrawable(0xffffff));
					mPop.setOutsideTouchable(true);
					mPop.showAsDropDown(mViewLine, 0, 0);
					mViewShadow.setVisibility(View.VISIBLE);
					return;
				}
				mPop.showAsDropDown(mViewLine, 10, 0);
				mViewShadow.setVisibility(View.VISIBLE);
			}
		});
	}

	private void createQRImage(String url) {
		try {
			// 判断URL合法性
			if (url == null || "".equals(url) || url.length() < 1) {
				return;
			}
			Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
			hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
			// 图像数据转换，使用了矩阵转换
			BitMatrix bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
			int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
			// 下面这里按照二维码的算法，逐个生成二维码的图片，
			// 两个for循环是图片横列扫描的结果
			for (int y = 0; y < QR_HEIGHT; y++) {
				for (int x = 0; x < QR_WIDTH; x++) {
					if (bitMatrix.get(x, y)) {
						pixels[y * QR_WIDTH + x] = 0xff000000;
					} else {
						pixels[y * QR_WIDTH + x] = 0xffffffff;
					}
				}
			}
			// 生成二维码图片的格式，使用ARGB_8888
			mQrcodeBitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT, Bitmap.Config.ARGB_8888);
			mQrcodeBitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);
			// 显示到一个ImageView上面
			mImgvQRCode.setImageBitmap(mQrcodeBitmap);
		} catch (WriterException e) {
			e.printStackTrace();
		}
	}

	private String saveBitmap() {
		mQrcodeImgPath = Constants.QRCODE_PATH + mGroupid + ".png";
		File file = new File(Constants.QRCODE_PATH);
		if (!file.exists()) {
			file.mkdirs();
		}
		return MeetingUtils.saveBitmap(mQrcodeImgPath, mQrcodeBitmap);
	}

	/** 分享 */
	private void showShare(String idcard, String imagePath) {
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
		// String urlPathString =
		// "http://www.wlyeah.com/share.html?p=%%7B%%22groupcode%%22%%3A%%22%d%%22%%7D";
		String urlFormat = String.format(URL_SHARE, idcard, MeetingApp.userInfo.uid);
		oks.setTitleUrl(urlFormat);
		// oks.setTitleUrl("http://www.wlyeah.com");
		// text是分享文本，所有平台都需要这个字段
		oks.setText("亲，我正在使用“我来也”，30秒完成一个班的点名签到，特方便，特赞！用群组码" + idcard + "赶快加入！你也可以扫描二维码哟！");
		// imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
		oks.setImagePath(imagePath);// 确保SDcard下面存在此张图片
		// url仅在微信（包括好友和朋友圈）中使用
		oks.setUrl(urlFormat);
		// 启动分享GUI
		oks.show(this);
	}

	/**
	 * 将资源文件写入储存卡
	 * */
	public String resourceSave() {
		String logoPathString = Constants.ICON_PATH + "logo.png";
		Resources resources = getResources();
		AssetFileDescriptor openRawResource = resources.openRawResourceFd(R.drawable.logo2);
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			fis = openRawResource.createInputStream();
			byte[] buf = new byte[1024];
			fos = new FileOutputStream(new File(logoPathString));
			int len = -1;
			while ((len = fis.read(buf)) != -1) {
				fos.write(buf, 0, len);
			}
			if (fis != null) {
				fis.close();
			}
			if (fos != null) {
				fos.close();
			}
			return logoPathString;
		} catch (IOException e) {
			return null;
		}
	}

	class ShareAsync extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			return resourceSave();
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result == null) {
				Toast.makeText(mContext, "没有找到图片储存路径", Toast.LENGTH_SHORT).show();
				return;
			}
			showShare(mGroupcode, result);
		}
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
