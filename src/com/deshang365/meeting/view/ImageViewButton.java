package com.deshang365.meeting.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.deshang365.meeting.R;

public class ImageViewButton extends RelativeLayout {
    public CircularImageView mImgv;
	private TextView mTv;

	public ImageViewButton(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public ImageViewButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.imgv_txtv_combine, this);
		mImgv = (CircularImageView) findViewById(R.id.imgv_signing_group);
		mTv = (TextView) findViewById(R.id.txtv_signing_group);
	}

	/**
	 * 设置图片资源
	 */
	public void setImageResource(int resId) {
		mImgv.setImageResource(resId);
	}

	/**
	 * 设置图片资源
	 */
	public void setImageBitmap(Bitmap bitmap) {
		mImgv.setImageBitmap(bitmap);
	}

	/**
	 * 设置显示的文字
	 */
	public void setTextViewText(String text) {
		mTv.setText(text);
	}

}
