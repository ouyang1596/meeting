package com.deshang365.meeting.service;

import java.io.File;

import com.deshang365.meeting.model.Constants;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class DownloadListenService extends Service {
	private long mDownloadId;
	private BroadcastReceiver mCompleteReceiver;
	private DownloadManager mDownloadManager;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mCompleteReceiver != null) {
			unregisterReceiver(mCompleteReceiver);
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		try {
			String url = intent.getStringExtra("url");
			downloadAndInstall(url);
		} catch (Exception e) {
			Log.e("downloadTag", "param error");
		}
	}

	private void downloadAndInstall(String url) {
		// 调用系统下载；
		mDownloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
		Uri uri = Uri.parse(url);
		File folder = new File(Constants.DOWNLOAD_PATH);
		if (!(folder.exists() && folder.isDirectory())) {
			folder.mkdirs();
		}
		String fileName;
		if (url.endsWith(".apk")) {
			String[] strings = url.split("/");
			fileName = strings[strings.length - 1];
		} else {
			fileName = "meeting.apk";
		}
		// request.setDestinationInExternalFilesDir(mContext,
		// Constants.DOWNLOAD_PATH, fileName);
		Uri localPath = Uri.fromFile(new File(folder, fileName));
		Request request = new Request(uri);
		request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
		request.setDestinationUri(localPath);
		request.setVisibleInDownloadsUi(true);
		mDownloadId = mDownloadManager.enqueue(request);
		mCompleteReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// get complete download id
				String action = intent.getAction();
				if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
					long completeDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
					// to do here
					if (completeDownloadId == mDownloadId) {
						Query query = new Query();
						query.setFilterById(mDownloadId);
						Cursor cursor = mDownloadManager.query(query);
						if (cursor.moveToFirst()) {
							String path = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
							if (path == null) {
								Toast.makeText(context, "更新失败，请稍后重试", Toast.LENGTH_SHORT).show();
								return;
							}
							path = path.substring(path.indexOf(Environment.getExternalStorageDirectory().toString(), 0));
							Intent installIntent = new Intent(Intent.ACTION_VIEW);
							installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							installIntent.setDataAndType(Uri.fromFile(new File(path)), "application/vnd.android.package-archive");
							startActivity(installIntent);
							stopSelf();
						}
					}
				}
			}
		};
		registerReceiver(mCompleteReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
	}
}
