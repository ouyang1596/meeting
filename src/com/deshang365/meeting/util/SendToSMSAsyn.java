package com.deshang365.meeting.util;

import retrofit.RetrofitError;
import retrofit.client.Response;

import com.deshang365.meeting.network.NetworkReturn;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.network.OnResponse;

/**
 * 短信验证请求
 * */
public class SendToSMSAsyn {
	private onPostExecuteListener mOnPostExecuteListener;

	private void sengToSMS(String mobile) {
		NewNetwork.sendToSMS(mobile, new OnResponse<NetworkReturn>("send_xsms_Android") {
			@Override
			public void success(NetworkReturn result, Response arg1) {
				super.success(result, arg1);
				if (mOnPostExecuteListener != null) {
					mOnPostExecuteListener.OnPostExecute(result);
				}
			}

			@Override
			public void failure(RetrofitError error) {
				super.failure(error);
				if (mOnPostExecuteListener != null) {
					mOnPostExecuteListener.OnPostExecute(null);
				}
			}
		});
	}

	// @Override
	// protected UserInfo doInBackground(String... params) {
	// return Network.sendToSMS(params[0]);
	// }
	//
	// @Override
	// protected void onPostExecute(UserInfo result) {
	// super.onPostExecute(result);
	// if (mOnPostExecuteListener != null) {
	// mOnPostExecuteListener.OnPostExecute(result);
	// }
	// }

	public interface onPostExecuteListener {
		public void OnPostExecute(NetworkReturn result);
	}

	public void setOnPostExecuteListener(onPostExecuteListener onPostExecuteListener) {
		mOnPostExecuteListener = onPostExecuteListener;
	}
}
