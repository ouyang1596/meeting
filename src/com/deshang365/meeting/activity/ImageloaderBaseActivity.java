package com.deshang365.meeting.activity;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.deshang365.meeting.R;
import com.deshang365.meeting.model.Constants;
import com.deshang365.meeting.network.NewNetwork;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.stat.StatService;

public class ImageloaderBaseActivity extends BaseActivity {

	protected ImageLoader mImageLoader = ImageLoader.getInstance();
	protected DisplayImageOptions mOptions;
	protected ImageLoadingListener mAnimateFirstListener = new AnimateFirstDisplayListener();

	public void removeCacheImage(int uid) {
		mImageLoader.getDiskCache().remove(NewNetwork.getAvatarUrl(uid));
		mImageLoader.getMemoryCache().remove(NewNetwork.getAvatarUrl(uid));
	}

	private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {
		static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				// 是否第一次显示
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					// 图片淡入效果
					FadeInBitmapDisplayer.animate(imageView, 500);
					displayedImages.add(imageUri);
				}
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		mContext = this;
		mOptions = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.default_head_portrait)
				.showImageForEmptyUri(R.drawable.default_head_portrait).showImageOnFail(R.drawable.default_head_portrait)
				.cacheInMemory(true).cacheOnDisk(true).bitmapConfig(Bitmap.Config.RGB_565) // 设置图片的解码类型
				.build();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(mContext).build();
		mImageLoader.init(config);
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

	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPause(this);
		XGPushManager.onActivityStoped(this);
	}
}
