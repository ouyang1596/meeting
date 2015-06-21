package com.deshang365.meeting.model;

import static com.deshang365.meeting.network.Config.HOST;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.UUID;

import android.util.Log;

import com.deshang365.meeting.baselib.Encrypt;
import com.deshang365.meeting.baselib.MeetingApp;
import com.tencent.stat.StatAppMonitor;
import com.tencent.stat.StatService;

public class Network {
	public static String mLoginCookieString = null;

	private static final String API_UPLOADIMAGE = "/api/avatar_des2/avatar/";

	public static String getmLoginCookieString() {
		return mLoginCookieString;
	}

	public static void setmLoginCookieString(String cookies) {
		mLoginCookieString = cookies;
	}

	private static final String TAG = "uploadFile";
	private static final int TIME_OUT = 15 * 1000; // 超时时间
	private static final String CHARSET = "utf-8"; // 设置编码

	/**
	 * 上传文件到服务器
	 * 
	 * @param file
	 *            需要上传的文件
	 * @return 返回响应的内容
	 */
	public static String uploadFile(File file) {
		StatAppMonitor monitor = new StatAppMonitor("avatar_Android");
		long startTime = System.currentTimeMillis();
		int res = 0;
		String result = null;
		String BOUNDARY = UUID.randomUUID().toString();
		String PREFIX = "--", LINE_END = "\r\n";
		String CONTENT_TYPE = "multipart/form-data";
		try {
			URL url = new URL(HOST + API_UPLOADIMAGE);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(TIME_OUT);
			conn.setConnectTimeout(TIME_OUT);
			conn.setDoInput(true); // 允许输入流
			conn.setDoOutput(true); // 允许输出流
			conn.setUseCaches(false); // 不允许使用缓存
			conn.setRequestMethod("POST"); // 请求方式
			conn.setRequestProperty("Charset", CHARSET); // 设置编码
			conn.setRequestProperty("Cookie", mLoginCookieString);
			conn.setRequestProperty("connection", "keep-alive");
			conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
			if (file != null) {
				/**
				 * 当文件不为空时执行上传
				 */
				DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
				StringBuffer sb = new StringBuffer();
				sb.append(PREFIX);
				sb.append(BOUNDARY);
				sb.append(LINE_END);
				/**
				 * 这里重点注意： name里面的值为服务器端对应的value 只有这个value 才可以得到对应的文件
				 * filename是文件的名字，包含后缀名
				 */
				sb.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"" + LINE_END);
				sb.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINE_END);
				sb.append(LINE_END);
				dos.write(sb.toString().getBytes());
				InputStream is = new FileInputStream(file);
				byte[] bytes = new byte[1024 * 10];
				int len = 0;
				while ((len = is.read(bytes)) != -1) {
					dos.write(bytes, 0, len);
				}
				is.close();
				dos.write(LINE_END.getBytes());
				byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
				dos.write(end_data);
				dos.flush();
				/**
				 * 获取响应码 200=成功 当响应成功，获取响应的流
				 */
				res = conn.getResponseCode();
				Log.e(TAG, "response code:" + res);
				long difftime = System.currentTimeMillis() - startTime;
				monitor.setMillisecondsConsume(difftime);
				if (res == 200) {
					Log.e(TAG, "request success");
					InputStream input = conn.getInputStream();
					result = convertStreamToString(input);
					Log.i("bm", "result : " + result);
				} else {
					monitor.setReturnCode(StatAppMonitor.FAILURE_RESULT_TYPE);
					Log.i("bm", "request error");
				}
			}
		} catch (MalformedURLException e) {
			monitor.setReturnCode(StatAppMonitor.LOGIC_FAILURE_RESULT_TYPE);
			return null;
		} catch (IOException e) {
			monitor.setReturnCode(StatAppMonitor.LOGIC_FAILURE_RESULT_TYPE);
			return null;
		}
		monitor.setReturnCode(StatAppMonitor.SUCCESS_RESULT_TYPE);
		StatService.reportAppMonitorStat(MeetingApp.mContext, monitor);
		return result;
	}

	private static String urlEncypt(String url) {
		int start = url.indexOf("?p=") + 3;
		String param = url.substring(start);
		String paramEncrypt = null;
		try {
			paramEncrypt = URLEncoder.encode(Encrypt.encrypt(param, Constants.KEY_NETWORK_OUT));
		} catch (Exception e) {
			return null;
		}
		return url.substring(0, start) + paramEncrypt;
	}

	private static String convertStreamToString(InputStream is) {
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"), 8 * 1024);
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			sb.delete(0, sb.length());
		}
		return sb.toString();
	}
}
