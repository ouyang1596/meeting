package com.deshang365.meeting.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class AbsentDetailListView extends ListView {

	public AbsentDetailListView(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public AbsentDetailListView(Context context) {
		super(context);

	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}
}
