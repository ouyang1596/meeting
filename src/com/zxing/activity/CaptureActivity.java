package com.zxing.activity;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import org.codehaus.jackson.JsonNode;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit.RetrofitError;
import retrofit.client.Response;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deshang365.meeting.R;
import com.deshang365.meeting.activity.BaseActivity;
import com.deshang365.meeting.activity.JoinGroupActivity;
import com.deshang365.meeting.model.GroupMemberInfo;
import com.deshang365.meeting.network.NetworkReturn;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.network.OnResponse;
import com.deshang365.meeting.util.MeetingUtils;
import com.deshang365.meeting.util.RGBLuminanceSource;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.tencent.stat.StatService;
import com.zxing.camera.CameraManager;
import com.zxing.decoding.CaptureActivityHandler;
import com.zxing.decoding.InactivityTimer;
import com.zxing.view.ViewfinderView;

/**
 * Initial the camera
 * 
 * @author Ryan.Tang
 */
public class CaptureActivity extends BaseActivity implements Callback {

	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.10f;
	private boolean vibrate;
	private Button cancelScanButton;
	private TextView mTvTopical, mQrcodePic;
	private LinearLayout mLlBack;
	private int REQUESTCODE_ALBUM = 1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera);
		initView();
	}

	private void initView() {
		mTvTopical = (TextView) findViewById(R.id.tv_top_alert_text);
		mTvTopical.setText("扫一扫");
		mLlBack = (LinearLayout) findViewById(R.id.ll_top_alert_back);
		mLlBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		// ViewUtil.addTopView(getApplicationContext(), this,
		// R.string.scan_card);
		CameraManager.init(getApplication());
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		cancelScanButton = (Button) this.findViewById(R.id.btn_cancel_scan);
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
		mQrcodePic = (TextView) findViewById(R.id.txtv_what_need);
		mQrcodePic.setVisibility(View.VISIBLE);
		mQrcodePic.setText("相册二维码");
		mQrcodePic.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				StatService.trackCustomEvent(mContext, "2code", "OK");
				Intent picIntent = new Intent(Intent.ACTION_GET_CONTENT);
				picIntent.setType("image/*");
				startActivityForResult(picIntent, REQUESTCODE_ALBUM);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = null;

		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		initBeepSound();
		vibrate = true;

		// quit the scan view
		cancelScanButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CaptureActivity.this.finish();
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	/**
	 * Handler scan result
	 * 
	 * @param result
	 * @param barcode
	 */
	public void handleDecode(Result result, Bitmap barcode) {
		inactivityTimer.onActivity();
		playBeepSoundAndVibrate();
		String resultString = result.getText();
		// FIXME
		if (resultString.equals("")) {
			Toast.makeText(CaptureActivity.this, "Scan failed!", Toast.LENGTH_SHORT).show();
		} else {
			// System.out.println("Result:"+resultString);
			Intent resultIntent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putString("result", resultString);
			resultIntent.putExtras(bundle);
			this.setResult(RESULT_OK, resultIntent);
		}
		CaptureActivity.this.finish();
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(this, decodeFormats, characterSet);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();

	}

	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	private static final long VIBRATE_DURATION = 200L;

	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};

	@SuppressLint("NewApi")
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUESTCODE_ALBUM) {
			if (data == null) {
				return;
			}
			Uri photoUri = data.getData();
			String absolutePath = MeetingUtils.getRealFilePath(this, photoUri);
			if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
				new ScanningImageAsyn().execute(absolutePath);
			} else {
				new ScanningImageAsyn().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, absolutePath);
			}
		}
	};

	class ScanningImageAsyn extends AsyncTask<String, Void, Result> {

		@Override
		protected Result doInBackground(String... params) {
			return scanningImage(params[0]);
		}

		@Override
		protected void onPostExecute(Result result) {
			super.onPostExecute(result);
			if (result != null) {
				String resulString = result.toString();
				jsonUtil(resulString);
			} else {
				Toast.makeText(mContext, "扫面失败", Toast.LENGTH_SHORT).show();
			}
		}

	}

	private Bitmap mScanBitmap;

	/**
	 * 扫描相册二维码
	 * */
	private Result scanningImage(String path) {
		if (TextUtils.isEmpty(path)) {
			return null;

		}
		// DecodeHintType 和EncodeHintType
		Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
		hints.put(DecodeHintType.CHARACTER_SET, "utf-8"); // 设置二维码内容的编码
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true; // 先获取原大小
		mScanBitmap = BitmapFactory.decodeFile(path, options);
		options.inJustDecodeBounds = false; // 获取新的大小

		int sampleSize = (int) (options.outHeight / (float) 200);

		if (sampleSize <= 0)
			sampleSize = 1;
		options.inSampleSize = sampleSize;
		mScanBitmap = BitmapFactory.decodeFile(path, options);

		RGBLuminanceSource source = new RGBLuminanceSource(mScanBitmap);
		BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
		QRCodeReader reader = new QRCodeReader();
		try {
			return reader.decode(bitmap1, hints);
		} catch (NotFoundException e) {
		} catch (ChecksumException e) {
		} catch (FormatException e) {
		}

		return null;

	}

	@SuppressLint("NewApi")
	private void jsonUtil(String resulString) {
		try {
			String substring = resulString.substring(8);
			JSONObject object = new JSONObject(substring);
			String groupcode = object.getString("data");
			showWaitingDialog();
			getGroupInfo(groupcode);
		} catch (JSONException e) {
			Toast.makeText(mContext, "扫面失败", Toast.LENGTH_SHORT).show();
			return;
		}
	}

	public void getGroupInfo(final String idcard) {
		NewNetwork.getGroupInfo(idcard, new OnResponse<NetworkReturn>("groupinfo_byidcard_Android") {
			@Override
			public void success(NetworkReturn result, Response response) {
				super.success(result, response);
				hideWaitingDialog();
				if (result.result != 1) {
					Toast.makeText(mContext, result.msg, 0).show();
					return;
				}
				GroupMemberInfo groupInfo = new GroupMemberInfo();
				JsonNode object = result.data;
				groupInfo.name = object.get("name").getValueAsText();
				groupInfo.group_id = object.get("id").getValueAsText();
				groupInfo.hxgroupid = object.get("mob_code").getValueAsText();
				groupInfo.uid = object.get("uid").getValueAsInt();
				Intent intent = new Intent(mContext, JoinGroupActivity.class);
				intent.putExtra("groupid", groupInfo.group_id);
				intent.putExtra("groupname", groupInfo.name);
				intent.putExtra("hxgroupid", groupInfo.hxgroupid);
				intent.putExtra("uid", groupInfo.uid);
				intent.putExtra("groupcode", idcard);
				startActivity(intent);
				finish();
			}

			@Override
			public void failure(RetrofitError error) {
				super.failure(error);
				hideWaitingDialog();
				Toast.makeText(mContext, "获取信息失败", 0).show();
			}
		});
	}
}