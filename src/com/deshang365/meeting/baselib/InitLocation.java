package com.deshang365.meeting.baselib;

import android.content.Context;

import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;

public class InitLocation implements TencentLocationListener {
	private TencentLocationManager mLocationManager;
	private Context mContext;
	private double lat = 0, lng = 0;
	private OnLocationListener listener;

	public InitLocation(Context context) {
		mContext = context;
		initLocation();
	}

	private void initLocation() {
		mLocationManager = TencentLocationManager.getInstance(mContext);
		TencentLocationRequest request = TencentLocationRequest.create();
		// 设置定位周期(位置监听器回调周期), 单位为 ms (毫秒).
		request.setInterval(30 * 1000);
		mLocationManager.requestLocationUpdates(request, this);

	}

	@Override
	public void onLocationChanged(TencentLocation location, int arg1, String arg2) {
		if (listener != null) {
			listener.setLocation(location.getLatitude(), location.getLongitude());
		}
	}

	@Override
	public void onStatusUpdate(String arg0, int arg1, String arg2) {

	}

	public interface OnLocationListener {
		public void setLocation(double lat, double lng);
	}

	public void setOnLocationListener(OnLocationListener listener) {
		this.listener = listener;

	};

	/**
	 * 移除位置监听器并停止定位
	 * */
	public void removeLocation() {
		mLocationManager.removeUpdates(this);
	}
}
