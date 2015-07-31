package com.deshang365.meeting.baselib;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.deshang365.meeting.model.Constants;

public class BluetoothManager {
	Context mContext;
	BluetoothAdapter mBluetoothAdapter;
	BluetoothReceiver mBluetoothReceiver;
	Handler mScanHandler;
	Handler mHandlerToclearBluetoothData;
	Timer mScanTimer;
	Timer mTimerToClearBluetoothData;
	String mOldName;
	public Map<String, String> mUnUploadBluetoothMap;
	public static final int REQUEST_DISCOVERABLE = 10001;
	static BluetoothManager instance;

	BluetoothManager(Context context) {
		mContext = context;
		instance = this;
	}

	public static void init(Context context) {
		if (instance == null) {
			instance = new BluetoothManager(context);
		}
	}

	public static void unInit(Context context) {
		if (instance != null) {
			instance.resetBluetooth();
			instance = null;
		}
	}

	public static BluetoothManager getInstance() {
		if (instance == null) {
			instance = new BluetoothManager(MeetingApp.mContext);
		}
		return instance;
	}

	public String getMacAddress() {
		if (mBluetoothAdapter == null) {
			return "";
		}
		return mBluetoothAdapter.getAddress();
	}

	public void resetBluetooth() {
		if (mUnUploadBluetoothMap != null) {
			mContext.unregisterReceiver(mBluetoothReceiver);
			if (mBluetoothAdapter != null) {
				mBluetoothAdapter.setName(mOldName);
			}
			closeDiscoverable();
			sendToStopReceive();
		}
	}

	private void sendToStopReceive() {
		Intent receiveIntent = new Intent();
		receiveIntent.setAction("stopreceive");
		receiveIntent.putExtra("receivestate", 1);// 0创建者停止签到1应用推出
		mContext.sendBroadcast(receiveIntent);
	}

	public void openDiscoverableAsyn(Activity activity) {
		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
		activity.startActivityForResult(intent, REQUEST_DISCOVERABLE);
	}

	public boolean isDiscoverable() {
		if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()
				&& mBluetoothAdapter.getScanMode() == mBluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			return true;
		}
		return false;
	}

	public boolean openDiscoverable(int timeout) {
		try {
			Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", int.class);
			setDiscoverableTimeout.setAccessible(true);
			Method setScanMode = BluetoothAdapter.class.getMethod("setScanMode", int.class, int.class);
			setScanMode.setAccessible(true);

			setDiscoverableTimeout.invoke(mBluetoothAdapter, timeout);
			setScanMode.invoke(mBluetoothAdapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, timeout);
			return true;
		} catch (Exception e) {
			// Intent intent = new
			// Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			// intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
			// startActivityForResult(intent, 10001);
			return false;
		}
	}

	public void closeDiscoverable() {
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		try {
			Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", int.class);
			setDiscoverableTimeout.setAccessible(true);
			Method setScanMode = BluetoothAdapter.class.getMethod("setScanMode", int.class, int.class);
			setScanMode.setAccessible(true);
			setDiscoverableTimeout.invoke(adapter, 1);
			setScanMode.invoke(adapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE, 1);
		} catch (Exception e) {
			// mBluetoothAdapter.disable();
		}
	}

	public void startBluetooth() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (!mBluetoothAdapter.isEnabled()) {
			mBluetoothAdapter.enable();
		}
		if (mUnUploadBluetoothMap == null) {
			mUnUploadBluetoothMap = new HashMap<String, String>();
			mOldName = mBluetoothAdapter.getName();
		}

		startTimer();
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		mBluetoothReceiver = new BluetoothReceiver();
		mContext.registerReceiver(mBluetoothReceiver, filter);
		try {
			String encryuid = Encrypt.encrypt("" + MeetingApp.userInfo.uid, Constants.KEY_BLUETOOTH);
			if (encryuid != null) {
				mBluetoothAdapter.setName(encryuid);
			}
		} catch (Exception e) {
		}
		startTimerToClearBluetoothData();
	}

	private class BluetoothReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				String btaddr = device.getAddress();
				String btname = device.getName();
				Log.i("bm", "btaddr====" + btaddr);
				Log.i("bm", "btname====" + btname);
				try {
					btname = Encrypt.decrypt(btname, Constants.KEY_BLUETOOTH);
					Log.i("bm", "bm====" + btname);
				} catch (Exception e) {
					btname = "";
				}
				mUnUploadBluetoothMap.put(btaddr, btname);
			}
		}
	}

	/**
	 * 定时发送请求
	 * */
	private void startTimer() {
		mScanHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				mBluetoothAdapter.startDiscovery();// 12秒后自动停止搜索
			}
		};
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				mScanHandler.sendMessage(mScanHandler.obtainMessage());
			}
		};

		if (mScanTimer == null) {
			mScanTimer = new Timer();
			mScanTimer.schedule(task, 1, 10000);
		}
	}

	/**
	 * 定时删除蓝牙数据
	 * */
	private void startTimerToClearBluetoothData() {
		mHandlerToclearBluetoothData = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (mUnUploadBluetoothMap != null && mUnUploadBluetoothMap.size() > 0) {
					mUnUploadBluetoothMap.clear();
				}
			}
		};
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				mHandlerToclearBluetoothData.sendMessage(mScanHandler.obtainMessage());
			}
		};

		if (mTimerToClearBluetoothData == null) {
			mTimerToClearBluetoothData = new Timer();
			mTimerToClearBluetoothData.schedule(task, 5 * 60 * 1000, 5 * 60 * 1000);
		}
	}

}
