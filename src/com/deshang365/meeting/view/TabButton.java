package com.deshang365.meeting.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.deshang365.meeting.R;

public class TabButton extends LinearLayout {

	private ImageView mImageView;
	private TextView mTextView;
	private LinearLayout mTbView;

	private int mImageNormal;
	private int mImageSelected;
	private int mTextColorNormal;
	private int mTextColorSelected;
	private boolean mSelected;

	public TabButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		mSelected = false;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.tab_button, this);

		// 实例化控件对象
		mImageView = (ImageView) findViewById(R.id.tb_imageView);
		mTextView = (TextView) findViewById(R.id.tb_textView);
		mTbView = (LinearLayout) findViewById(R.id.ll_tabButton);

		TypedArray attrsArray = context.obtainStyledAttributes(attrs, R.styleable.TabButton);
		mImageNormal = attrsArray.getResourceId(R.styleable.TabButton_imageNormal, R.drawable.tab_bar_community_normal);
		mImageSelected = attrsArray.getResourceId(R.styleable.TabButton_imageSelected, R.drawable.tab_bar_community_selected);
		mTextColorNormal = attrsArray.getColor(R.styleable.TabButton_textColorNormal, 0xFFC5C5C5);
		mTextColorSelected = attrsArray.getColor(R.styleable.TabButton_textColorSelected, 0xFFFFFFFF);

		mImageView.setImageResource(mImageNormal);
		mTextView.setTextColor(mTextColorNormal);
		mTextView.setText(attrsArray.getString(R.styleable.TabButton_text));
		//mTextView.setTextSize(attrsArray.getDimension(R.styleable.TabButton_textSize, 100));

		attrsArray.recycle();
	}

	public TabButton(Context context) {
		super(context);
		mSelected = false;
	}

	public boolean isSelected() {
		return mSelected;
	}

	public void setSelected(boolean isSelected) {
		if (isSelected) {
			mImageView.setImageResource(mImageSelected);
			mTextView.setTextColor(mTextColorSelected);
			mTbView.setBackgroundResource(R.color.bottom_bg);
		} else {
			mImageView.setImageResource(mImageNormal);
			mTextView.setTextColor(mTextColorNormal);
			mTbView.setBackgroundResource(R.color.bottom_bg);
		}
		mSelected = isSelected;
	}

	/**
	 * 设置图片资源
	 * 
	 * @param resId
	 */
	public void setImageResource(int resId) {
		mImageView.setImageResource(resId);
	}

	/**
	 * 设置要显示的文字
	 * 
	 * @param text
	 */
	public void setText(String text) {
		mTextView.setText(text);
	}

	/**
	 * 设置字体颜色
	 * 
	 * @param color
	 */
	public void setTextColor(int color) {
		mTextView.setTextColor(color);
	}

	/**
	 * 设置字体大小
	 * 
	 * @param size
	 */
	public void setTextSize(float size) {
		mTextView.setTextSize(size);
	}
}