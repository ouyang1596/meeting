package com.deshang365.meeting.view;

import java.io.File;

import retrofit.RetrofitError;
import retrofit.client.Response;
import android.R.integer;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deshang365.meeting.R;
import com.deshang365.meeting.activity.AboutActivity;
import com.deshang365.meeting.activity.ChangeDataActivity;
import com.deshang365.meeting.activity.MySignRecordActivity;
import com.deshang365.meeting.activity.SetActivity;
import com.deshang365.meeting.activity.EditPasswordActivity;
import com.deshang365.meeting.activity.LoginActivity;
import com.deshang365.meeting.activity.MainActivity;
import com.deshang365.meeting.baselib.MeetingApp;
import com.deshang365.meeting.model.Constants;
import com.deshang365.meeting.model.Network;
import com.deshang365.meeting.model.VersionInfo;
import com.deshang365.meeting.network.NewNetwork;
import com.deshang365.meeting.network.OnResponse;
import com.deshang365.meeting.service.DownloadListenService;
import com.deshang365.meeting.util.MeetingUtils;
import com.deshang365.util.ProfileHelper;
import com.easemob.chat.EMChatManager;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;
import com.tencent.stat.StatService;

public class MainTabUserInfo extends MainTabViewBase {
	private Context mContext;
	private View mView;
	private ImageView mImgvHead;
	private ImageView mImgvBg;
	private MainActivity mMainActivity;
	private LinearLayout mRelEditPassword, mRelCheckForUpdate, mRelAboutSign, mRelSet, mRelChangeData, mLlSignRecord;
	private Button mBtnLogout;
	private TextView mTvPhonenumber, mTvGroupCount;
	private PackageInfo mPackageInfo;
	private final int REQUESTCODE_TAKEPHOTOS = 1;
	private final int REQUESTCODE_ALBUM = 2;
	protected ImageLoader mImageLoader = ImageLoader.getInstance();
	protected DisplayImageOptions mOptions;

	public MainTabUserInfo(Context context) {
		super(context);
		mContext = context;
		mMainActivity = (MainActivity) mContext;
		mView = LayoutInflater.from(context).inflate(R.layout.main_tab_user_info, this, true);
		init();
	}

	public MainTabUserInfo(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mMainActivity = (MainActivity) mContext;
		mView = LayoutInflater.from(context).inflate(R.layout.main_tab_user_info, this, true);
	}

	private String mCurTime;
	private String mImageTakephotoPathString;

	private void init() {
		mOptions = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.default_head_portrait)
				.showImageForEmptyUri(R.drawable.default_head_portrait).showImageOnFail(R.drawable.default_head_portrait)
				.cacheInMemory(true).cacheOnDisk(true).bitmapConfig(Bitmap.Config.RGB_565) // 设置图片的解码类型
				.build();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(mContext).build();
		mImageLoader.init(config);
		mTvGroupCount = (TextView) findViewById(R.id.txtv_group_count);
		mRelChangeData = (LinearLayout) findViewById(R.id.rel_edit_data);
		mRelChangeData.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mContext.startActivity(new Intent(mContext, ChangeDataActivity.class));
			}
		});
		mRelSet = (LinearLayout) findViewById(R.id.rel_edit_email);
		mRelSet.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mMainActivity.startActivity(new Intent(mContext, SetActivity.class));
			}
		});
		mLlSignRecord = (LinearLayout) findViewById(R.id.ll_my_sign_record);
		mLlSignRecord.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mContext.startActivity(new Intent(mContext, MySignRecordActivity.class));
			}
		});
		mRelAboutSign = (LinearLayout) findViewById(R.id.rel_about_sign);
		mRelAboutSign.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				StatService.trackCustomEvent(mContext, "About", "OK");
				Intent intent = new Intent(mContext, AboutActivity.class);
				mContext.startActivity(intent);
			}
		});
		try {
			mPackageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		mImgvBg = (ImageView) findViewById(R.id.imgv_background);
		mRelCheckForUpdate = (LinearLayout) findViewById(R.id.rel_check_for_update);
		mRelCheckForUpdate.setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				StatService.trackCustomEvent(mContext, "update", "OK");
				mMainActivity.showWaitingDialog();
				checkNewVersion();
			}
		});
		mTvPhonenumber = (TextView) findViewById(R.id.txtv_phone_number);
		mTvPhonenumber.setText(MeetingApp.username);
		mRelEditPassword = (LinearLayout) findViewById(R.id.rel_edit_password);
		mRelEditPassword.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				StatService.trackCustomEvent(mContext, "Cpassword", "OK");
				Intent intent = new Intent(mContext, EditPasswordActivity.class);
				intent.putExtra("password", MeetingApp.password);
				intent.putExtra("username", MeetingApp.username);
				mContext.startActivity(intent);
			}
		});
		mImgvHead = (ImageView) findViewById(R.id.imgv_head);
		mImgvHead.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				StatService.trackCustomEvent(mContext, "Cavatar", "OK");
				initDialog();
				View mPicView = View.inflate(mContext, R.layout.picture_dialog, null);
				LinearLayout relTakePhotos = (LinearLayout) mPicView.findViewById(R.id.take_photos);
				relTakePhotos.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						StatService.trackCustomEvent(mContext, "Camera", "OK");
						mDialog.cancel();
						mCurTime = MeetingUtils.getCurTime("yyyyMMddHHmmss");
						Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
						File file = new File(Constants.TAKEPHOTO_PATH);
						// 先创建目录
						if (!file.exists()) {
							file.mkdirs();
						}
						mImageTakephotoPathString = file.getAbsolutePath() + "/" + mCurTime + ".jpg";
						Uri imageUri = Uri.fromFile(new File(mImageTakephotoPathString));
						// 指定照片保存路径（SD卡）
						openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
						mMainActivity.mPhotoPathToRemember = mImageTakephotoPathString;
						mMainActivity.startActivityForResult(openCameraIntent, REQUESTCODE_TAKEPHOTOS);
					}
				});
				LinearLayout relPicture = (LinearLayout) mPicView.findViewById(R.id.picture);
				relPicture.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						StatService.trackCustomEvent(mContext, "Lalbum", "OK");
						mDialog.cancel();
						Intent picIntent = new Intent(Intent.ACTION_GET_CONTENT);
						picIntent.setType("image/*");
						mMainActivity.startActivityForResult(picIntent, REQUESTCODE_ALBUM);
					}
				});
				mDialog.show();
				mDialog.getWindow().setContentView(mPicView);
			}
		});
		mBtnLogout = (Button) findViewById(R.id.btn_logout);
		mBtnLogout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				initDialog();
				View mExitView = View.inflate(mContext, R.layout.exit_dialog, null);
				Button btnExit = (Button) mExitView.findViewById(R.id.btn_exit);
				TextView tvExit = (TextView) mExitView.findViewById(R.id.txtv_exit);
				tvExit.setText("退出登录？");
				tvExit.setVisibility(View.VISIBLE);
				btnExit.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						StatService.trackCustomEvent(mContext, "ConfirmLoginOut", "OK");
						mDialog.cancel();
						ProfileHelper.delete(mContext, Constants.KEY_REMEMBER_NAME);
						ProfileHelper.delete(mContext, Constants.KEY_REMEMBER_PWD);
						MeetingApp.mHxHasLogin = false;
						MeetingApp.userInfo = null;
						Network.setmLoginCookieString(null);
						EMChatManager.getInstance().logout();// 此方法为同步方法
						Intent intent = new Intent(mContext, LoginActivity.class);
						isDelete = 0;
						intent.putExtra("isDelete", 0);
						mContext.startActivity(intent);
						mMainActivity.finish();
					}
				});
				Button btnCancel = (Button) mExitView.findViewById(R.id.btn_cancel);
				btnCancel.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						mDialog.cancel();
						StatService.trackCustomEvent(mContext, "CancelLoginOut", "OK");
					}
				});
				mDialog.show();
				mDialog.getWindow().setContentView(mExitView);
			}
		});
		getHeadImage();
	}

	private void getHeadImage() {
		if (MeetingApp.userInfo == null) {
			return;
		}
		getPhoto();
	}

	public void setGroupCount(int count) {
		if (mTvGroupCount != null) {
			mTvGroupCount.setText(count + " 群组");
		}
	}

	public ImageView getmImgvHead() {
		return mImgvHead;
	}

	public void setmImgvHead(ImageView mImgvHead) {
		this.mImgvHead = mImgvHead;
	}

	public String getImageTakephotoPathString() {
		return mImageTakephotoPathString;
	}

	public void setmImageTakephotoPathString(String mImageTakephotoPathString) {
		this.mImageTakephotoPathString = mImageTakephotoPathString;
	}

	private int isDelete;// 用户名 密码是否删除， 0删除，1没有

	private void checkNewVersion() {
		NewNetwork.getUpdatedVersion(mContext, new OnResponse<VersionInfo>("check_new_version") {
			@Override
			public void success(VersionInfo result, Response arg1) {
				super.success(result, arg1);
				mMainActivity.hideWaitingDialog();
				if (mPackageInfo == null || result.versionCode <= mPackageInfo.versionCode) {
					Toast.makeText(mContext, "已是最新版本", Toast.LENGTH_SHORT).show();
					return;
				}
				LayoutInflater factory = LayoutInflater.from(mContext);
				View mUpdateInfo = factory.inflate(R.layout.update_info, null);
				TextView mUpdatedVersionName = (TextView) mUpdateInfo.findViewById(R.id.updated_version_name);
				mUpdatedVersionName.setText(result.versionName);
				TextView mUpdatedFileSize = (TextView) mUpdateInfo.findViewById(R.id.updated_file_size);
				mUpdatedFileSize.setText(result.fileSize);
				TextView mUpdatedContent = (TextView) mUpdateInfo.findViewById(R.id.updated_content);
				mUpdatedContent.setText(result.content);
				final String downloadUrl = result.downloadURL;
				new AlertDialog.Builder(mContext).setTitle("发现新版本").setView(mUpdateInfo)
						.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// 启动下载服务
								Intent intent = new Intent();
								intent.putExtra("url", downloadUrl);
								intent.setClass(mContext, DownloadListenService.class);
								intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								mContext.startService(intent);
							}
						}).setNegativeButton("以后再说", null).show();
			}

			@Override
			public void failure(RetrofitError arg0) {
				super.failure(arg0);
				mMainActivity.hideWaitingDialog();
			}
		});
	}

	private AlertDialog mDialog;

	private void initDialog() {
		if (mDialog == null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			mDialog = builder.create();
			mDialog.setCanceledOnTouchOutside(true);
		}
	}

	public void getRemoveImageCache(int uid) {
		MemoryCacheUtils.removeFromCache(NewNetwork.getAvatarUrl(uid), mImageLoader.getMemoryCache());
		DiskCacheUtils.removeFromCache(NewNetwork.getAvatarUrl(uid), mImageLoader.getDiskCache());
	}

	public void getPhoto() {
		mImageLoader.displayImage(NewNetwork.getAvatarUrl(MeetingApp.userInfo.uid), mImgvHead, mOptions);
	}
}
