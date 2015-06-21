package com.deshang365.meeting.adapter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;

import com.deshang365.meeting.R;
import com.deshang365.meeting.network.NewNetwork;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public abstract class ExlImageLoaderAdapterBase extends BaseExpandableListAdapter {
    protected ImageLoader mImageLoader = ImageLoader.getInstance();
    protected DisplayImageOptions mOptions;
    protected ImageLoadingListener mAnimateFirstListener = new AnimateFirstDisplayListener();

    public void removeCacheImage(int uid) {
        mImageLoader.getDiskCache().remove(NewNetwork.getAvatarUrl(uid));
        mImageLoader.getMemoryCache().remove(NewNetwork.getAvatarUrl(uid));
    }
    
    public ExlImageLoaderAdapterBase(Context context) {
        super();
        mOptions = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.default_head_portrait)
                .showImageForEmptyUri(R.drawable.default_head_portrait)
                .showImageOnFail(R.drawable.default_head_portrait).cacheInMemory(true).cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565) // 设置图片的解码类型
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).build();
        mImageLoader.init(config);
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
}
