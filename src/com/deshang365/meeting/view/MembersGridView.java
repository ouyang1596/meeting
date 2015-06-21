package com.deshang365.meeting.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class MembersGridView extends GridView {

	public MembersGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public MembersGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MembersGridView(Context context) {
		super(context);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}
}
