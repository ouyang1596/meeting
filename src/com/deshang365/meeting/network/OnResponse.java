package com.deshang365.meeting.network;

import java.util.Properties;

import retrofit.Callback;
import retrofit.RetrofitError;

import com.deshang365.meeting.baselib.MeetingApp;
import com.tencent.stat.StatAppMonitor;
import com.tencent.stat.StatService;

public abstract class OnResponse<T> implements Callback<T> {
	private StatAppMonitor mMonitor;
	private long mStartTime;
	Properties mProp;
	private String mApiName;

	public OnResponse(String apiName) {
		mMonitor = new StatAppMonitor(apiName);
		mStartTime = System.currentTimeMillis();
		StatService.trackCustomEvent(MeetingApp.mContext, apiName, "OK ");
		
		mApiName = apiName;
		StatService.trackCustomBeginEvent(MeetingApp.mContext, apiName, "OK ");
	}

	@Override
	public void failure(RetrofitError error) {
		long difftime = System.currentTimeMillis() - mStartTime;
		mMonitor.setMillisecondsConsume(difftime);
		mMonitor.setReturnCode(StatAppMonitor.FAILURE_RESULT_TYPE);
		StatService.reportAppMonitorStat(MeetingApp.mContext, mMonitor);
	    StatService.trackCustomEndEvent(MeetingApp.mContext, mApiName, "OK ");
	    StatService.reportError(MeetingApp.mContext, error.toString());
	}

	public void success(T result, retrofit.client.Response arg1) {
		long difftime = System.currentTimeMillis() - mStartTime;
		mMonitor.setMillisecondsConsume(difftime);
		mMonitor.setReturnCode(StatAppMonitor.SUCCESS_RESULT_TYPE);
		StatService.reportAppMonitorStat(MeetingApp.mContext, mMonitor);
		StatService.trackCustomEndEvent(MeetingApp.mContext, mApiName, "OK ");
	}
}
