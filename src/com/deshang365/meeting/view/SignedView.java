package com.deshang365.meeting.view;

import com.deshang365.meeting.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

public class SignedView extends RelativeLayout {

	public SignedView(Context context) {
		super(context);

	}

	public SignedView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.signed_view, this);
	}
}
