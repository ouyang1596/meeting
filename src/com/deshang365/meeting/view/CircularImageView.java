package com.deshang365.meeting.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.deshang365.meeting.baselib.MeetingApp;

public class CircularImageView extends BaseCircleImage {
	public CircularImageView(Context paramContext) {
		super(paramContext);
	}

	public CircularImageView(Context paramContext, AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
	}

	public CircularImageView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
		super(paramContext, paramAttributeSet, paramInt);
	}

	// now
	public Bitmap createMask() {
		int i = getWidth();
		int j = getHeight();
		Bitmap.Config localConfig = Bitmap.Config.ARGB_8888;// 位图位数越高代表其可以存储的颜色信息越多，图像也就越逼真，占用内存更多。
		Bitmap localBitmap;
		// 判断是否有这个对象，如果有就直接从缓存中拿去
		if (MeetingApp.mCacheImageMap.get(i) == null) {
			localBitmap = Bitmap.createBitmap(i, j, localConfig);
			MeetingApp.mCacheImageMap.put(i, localBitmap);
		} else {
			localBitmap = MeetingApp.mCacheImageMap.get(i);
		}
		Canvas localCanvas = new Canvas(localBitmap);
		Paint localPaint = new Paint(1);
		localPaint.setColor(-16777216);
		float f1 = getWidth();
		float f2 = getHeight();
		RectF localRectF = new RectF(0.0F, 0.0F, f1, f2);
		localCanvas.drawOval(localRectF, localPaint);
		return localBitmap;
	}
	// before
	// public Bitmap createMask() {
	// int i = getWidth();
	// int j = getHeight();
	// Bitmap.Config localConfig = Bitmap.Config.ARGB_8888;//
	// 位图位数越高代表其可以存储的颜色信息越多，图像也就越逼真，占用内存更多。
	// Bitmap localBitmap = Bitmap.createBitmap(i, j, localConfig);
	// Canvas localCanvas = new Canvas(localBitmap);
	// Paint localPaint = new Paint(1);
	// localPaint.setColor(-16777216);
	// float f1 = getWidth();
	// float f2 = getHeight();
	// RectF localRectF = new RectF(0.0F, 0.0F, f1, f2);
	// localCanvas.drawOval(localRectF, localPaint);
	// return localBitmap;
	// }
}
